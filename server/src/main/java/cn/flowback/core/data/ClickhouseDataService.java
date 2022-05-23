package cn.flowback.core.data;

import cn.flowback.common.utils.ZstdUtils;
import cn.flowback.config.FlowBackProperties;
import cn.flowback.common.config.CommonConst;
import cn.flowback.core.cache.OffHeapCache;
import cn.flowback.core.statistic.LeapArray;
import cn.flowback.core.statistic.Window;
import cn.flowback.db.*;
import cn.flowback.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.caffinitas.ohc.OHCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * clickhouse 方式
 *
 * @author 唐警威
 **/
@Component
@ConditionalOnProperty(value = "flowback.datasourcePlatform", havingValue = "clickhouse")
public class ClickhouseDataService implements DataService {

    Logger logger = LoggerFactory.getLogger(ClickhouseDataService.class.getName());


    private  static  Object lock = new Object();

    @Autowired
    private FlowBackProperties flowBackProperties;

    @Autowired
    private DataBaseExtensionImpl dataBaseExtension;

    @Override
    public void saveBatch(BlockingQueue<Object> logs, LeapArray leapArray) throws SQLException {
        Connection connection = null;
        Set<PreparedStatement> preparedStatements = null;
        try {
            preparedStatements = new HashSet<>();
            int total = logs.size();
            int executeSize = total > 100000?100000:total ;
            ArrayList<Object> objects = new ArrayList<>();
            logs.drainTo(objects, executeSize);
            //选择一个数据库链接对象,当前批次操作都基于此连接
            connection = dataBaseExtension.getBalancedConnection();
            for (int i = 0; i < objects.size(); i++) {
                Object o = objects.get(i);
                byte[] array ;
                if(o instanceof  byte []){
                    array = (byte[]) o;
                }else{
                    OHCache<String, byte[]> cache = OffHeapCache.cache();
                    String l = o.toString();
                    array = cache.get(l);
                    cache.remove(l);
                }
                if(array == null){
                    continue;
                }
                String decompress = ZstdUtils.decompress(array);
                JSONObject log = JSONObject.parseObject(decompress);
                String tableName = log.getString(CommonConst.PARSE_TABLE_TAG);
                if(StringUtils.isEmpty(tableName)){
                    continue;
                }
                //解析插入表名如果不存在表则会自动创建表
                tableName = parseTableNameAndAutoCreateTable(connection, log);
                //如果有新字段自动增加字段
                dataBaseExtension.checkColumnsAndCreateNewColumns(connection, tableName, log);
                //解析插入语句
                String sql = dataBaseExtension.parseInsertSql(log, tableName);
                //插入语句预处理
                PreparedStatement preparedStatement = dataBaseExtension.createStatement(connection, sql);
                if (!preparedStatements.contains(preparedStatement)) {
                    //缓存预处理
                    preparedStatements.add(preparedStatement);
                }
                //填充插入参数
                SqlFillingContext.getStrategy(log).fillSqlParameter(preparedStatement, sql, log);
            }
            //执行插入
            executeBatch(leapArray, preparedStatements, total, executeSize);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (preparedStatements!= null){
                for (PreparedStatement preparedStatement : preparedStatements) {
                    preparedStatement.close();
                }
            }
            if (connection!= null && !connection.isClosed()) {
               connection.close();
            }
        }
    }

    /**
     * 执行入库操作
     * @param leapArray
     * @param preparedStatements
     * @param total
     * @param executeSize
     * @throws InterruptedException
     * @throws SQLException
     */
    private void executeBatch(LeapArray leapArray, Set<PreparedStatement> preparedStatements, int total, int executeSize) throws InterruptedException, SQLException {
            for (PreparedStatement preparedStatement : preparedStatements) {
                long s = System.currentTimeMillis();
                int[] ints = new int[0];
                boolean execute = false;
                try {
                    ints = preparedStatement.executeBatch();
                    if(ints.length > 0 ){
                        execute = true;
                    }
                }catch (SQLException sqlException){
                    //失败后重新尝试3次间隔1s
                    for (int i = 0; i < 3; i++) {
                        if(execute) {
                            continue;
                        }
                        try {
                            logger.info("尝试重新入库第"+(i+1)+"次...");
                            ints = preparedStatement.executeBatch();
                            if(ints.length > 0){
                                execute = true;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        TimeUnit.SECONDS.sleep(1);
                    }
                }
                long e = System.currentTimeMillis();
                if(execute == false){
                    logger.error("入库失败请检查数据库是否正常...");
                }else{
                    Window currentWindow = leapArray.getCurrentWindow();
                    currentWindow.getSaveCount().addAndGet(ints.length);
                    int cacheMessage = total - executeSize;
                    currentWindow.getCachedMessageCount().addAndGet(cacheMessage);
                    logger.info("-"+Thread.currentThread().getName()+ " 当前日志入库耗时:" + (e - s) + " size:" + ints.length + " 剩余缓冲消息:" + cacheMessage);
                }
                dataBaseExtension.clearStatementCache(preparedStatement);
            }
    }



    /**
     * 解析表名自动创建表
     * 约定消息必须是json格式 table 标记为表名其它为字段名称
     * Automatically create table by parsing table name
     *
     * @param log
     * @return
     */
    private String parseTableNameAndAutoCreateTable(Connection connection, Object log) {
        JSONObject jsonObject = (JSONObject) log;
        String tableName = jsonObject.getString(CommonConst.PARSE_TABLE_TAG);
        if (jsonObject.containsKey(CommonConst.TABLE_ID) == false) {
            jsonObject.put(CommonConst.TABLE_ID, UUID.randomUUID().toString());
        }
        if (!dataBaseExtension.existTable(connection, tableName)) {
            if (!dataBaseExtension.existTable(connection, tableName)) {
                Map<String, Class> columns = new HashMap<>(16);
                for (String c : jsonObject.keySet()) {
                    columns.put(c, jsonObject.get(c).getClass());
                }
                tableName = tableName.trim();
                dataBaseExtension.createTable(connection, tableName, columns);
                return tableName;
            }
        }
        return tableName;
    }



}
