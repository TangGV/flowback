package cn.flowback.core.data;

import cn.flowback.config.FlowBackProperties;
import cn.flowback.db.JsonPaddingStrategy;
import cn.flowback.db.EntityFillingStrategy;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.clickhouse.BalancedClickhouseDataSource;
import ru.yandex.clickhouse.ClickHouseConnection;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tang
 */
@Component
public class DataBaseExtensionImpl implements DataBaseExtension {

    Logger logger = LoggerFactory.getLogger(DataBaseExtensionImpl.class.getName());

    public static volatile byte[] CREATE_TABLE_LOCK = new byte[1];

    public static volatile byte[] CREATE_COLUMNS_LOCK = new byte[1];


    public static final String DATABASE = "default";

    /**
     * 表名映射字段
     *
     * @key jdbc url
     * @value map.key 表名,map.value 存在的字段
     */
    private static final Map<String, Map<String, Set<String>>> tableColumnMap = new HashMap();

    /**
     * 存储已经存在的表名
     *
     * @key jdbc url
     * @value 存在的表名称Set
     */
    private static final Map<String, Set<String>> tableNameMap = new HashMap();

    /**
     * 存储数据库连接对象
     */
    private static final ConcurrentHashMap<String, PreparedStatement> preparedStatementMap = new ConcurrentHashMap<>();

    /**
     * 存储数据库连接对象 映射 存储数据库连接对象 缓存名称
     */
    private static final ConcurrentHashMap<PreparedStatement, String> preparedStatementMapCacheName = new ConcurrentHashMap<>();

    private BalancedClickhouseDataSource balancedClickhouseDataSource;

    @Autowired
    private FlowBackProperties flowBackProperties;


    @PostConstruct
    public void init() {
        balancedClickhouseDataSource  = new BalancedClickhouseDataSource(flowBackProperties.getClickhouseUrl());
        this.initExistingTableName();
    }


    @Override
    public void createTable(Connection connection, String tableName, Map<String, Class> columns) {
        try {
            if (!existTable(connection, tableName)) {
                synchronized (CREATE_TABLE_LOCK) {
                    if (!existTable(connection, tableName)) {
                        Statement statement = connection.createStatement();
                        String c = "";
                        Set<String> columnsCache = new HashSet<>();
                        for (String column : columns.keySet()) {
                            //todo 需要优化多种数据类型
                            Class aClass = columns.get(column);
                            if (aClass == Date.class) {
                                c += column + " DateTime, ";
                            }else if(aClass == Integer.class){
                                c += column + " int, ";
                            }else if(aClass == BigDecimal.class){
                                c += column + " Decimal, ";
                            }else {
                                c += column + " String, ";
                            }
                            columnsCache.add(column);
                        }
                        c = c.substring(0, c.length() - 2);
                        String localTable = "create table " + tableName + " (" + c + ") engine = MergeTree ORDER BY id SETTINGS index_granularity = 8192";
                        statement.executeQuery(localTable);
                        addCacheTableName(getConnectionDbUrl(connection),tableName);
                        //增加字段缓存
                        String connectionDbUrl = getConnectionDbUrl(connection);
                        Map<String, Set<String>> tableMapColumns = tableColumnMap.get(connectionDbUrl);
                        if(tableMapColumns == null){
                            tableMapColumns = new HashMap<>();
                            tableMapColumns.put(connectionDbUrl,new HashSet<>());
                        }
                        if(tableMapColumns.get(tableName) == null){
                            tableMapColumns.put(tableName, new HashSet<>());
                        }
                        tableMapColumns.get(tableName).addAll(columnsCache);
                        logger.info("创建表:" + getConnectionDbUrl(connection) +  "   "+ tableName + "  sql:" + localTable);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String parseInsertSql(Object obj, String tableName) {
        String sql;
        if (obj instanceof JSONObject) {
            sql = new JsonPaddingStrategy().parseInterSql(obj, tableName);
        } else {
            sql = new EntityFillingStrategy().parseInterSql(obj, tableName);
        }
        return sql;
    }


    @Override
    public PreparedStatement createStatement(Connection collection,String sql) {
        //多线程消费的情况下会出现同一个statement对象被不同线程同时操作会出现正在执行被其它执行完毕线程关闭链接对接，此处使用线程name区分，使得每个线程独享statement对象
        String cacheName = Thread.currentThread().getName()+sql;
        try {
            PreparedStatement preparedStatement = preparedStatementMap.get(cacheName);
            if (preparedStatement != null) {
                return preparedStatement;
            } else {
                ClickHouseConnection connection = (ClickHouseConnection)collection;
                preparedStatement = connection.prepareStatement(sql);
                connection.setAutoCommit(false);
                preparedStatementMap.put(cacheName, preparedStatement);
                preparedStatementMapCacheName.put(preparedStatement,cacheName);
            }
            return preparedStatement;
        } catch (SQLException e) {
            e.printStackTrace();
        }
       throw new RuntimeException("创建statement对象失败!请检查数据库链接");
    }

    @Override
    public void clearStatementCache(PreparedStatement preparedStatement) throws SQLException {
        String cacheName = preparedStatementMapCacheName.get(preparedStatement);
        preparedStatementMap.remove(cacheName);
        preparedStatementMapCacheName.remove(preparedStatement);
    }

    @Override
    public Connection getBalancedConnection() {
        try {
            ClickHouseConnection connection = balancedClickhouseDataSource.getConnection();
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("获取数据库链接失败,请检查链接是否正常");
    }


    /**
     * 初始化存在的所有表和表映射的字段
     */
    @Override
    public void initExistingTableName() {
        List<String> allClickhouseUrls = balancedClickhouseDataSource.getAllClickhouseUrls();
        try {
            for (String clickhouseUrl : allClickhouseUrls) {
                Connection connection = DriverManager.getConnection(clickhouseUrl);
                Statement statement = connection.createStatement();
                ResultSet show_tables = statement.executeQuery("show tables");
                Set<String> tableNames = tableNameMap.get(clickhouseUrl);
                if (tableNames == null) {
                    tableNames = new HashSet<>();
                    tableNameMap.put(clickhouseUrl, tableNames);
                }
                //缓存表的字段 key 表名称，value  字段set
                Map<String, Set<String>> tableColumnMapCache = new HashMap<>();
                Set<String> columnSet = new HashSet<>();
                while (show_tables.next()) {
                    String tableName = show_tables.getString("name");
                    tableNames.add(tableName);

                    try {
                        ResultSet columnsResult = statement.executeQuery("select distinct name from system.columns where database='" + DATABASE + "' and table='" + tableName + "';");
                        while (columnsResult.next()) {
                            String column = columnsResult.getString("name");
                            columnSet.add(column);
                        }
                    } catch (SQLException sqlException) {
                        sqlException.printStackTrace();
                    }
                    tableColumnMapCache.put(tableName, columnSet);
                }
                tableColumnMap.put(clickhouseUrl, tableColumnMapCache);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void checkColumnsAndCreateNewColumns(Connection connection, String tableName, Object log) {
        try {
            String url = getConnectionDbUrl(connection);
            Set<String> columns = tableColumnMap.get(url).get(tableName);
            if (columns == null) {
                return;
            }
            JSONObject jsonObject;
            if (log instanceof JSONObject) {
                jsonObject = (JSONObject) log;
            } else {
                jsonObject = JSONObject.parseObject(JSONObject.toJSONString(log));
            }
            Set<String> keySet = new HashSet<>();
            keySet.addAll(jsonObject.keySet());
            keySet.removeAll(columns);
            //新增字段
            if (keySet.size() > 0) {
                synchronized (CREATE_COLUMNS_LOCK) {
                    Set<String> columnsTmp = tableColumnMap.get(url).get(tableName);
                    if (keySet.size() > 0) {
                        keySet.stream().forEach(column -> {
                            if (columnsTmp.contains(column)) {
                                return;
                            }
                            Statement statement;
                            try {
                                statement = connection.createStatement();
                                //todo 默认是字符串类型，后续可扩展
                                String sql = "alter table " + tableName + " add column " + column + " String default '';";
                                logger.info("执行新增字段语句:" +url+"   sql:"+ sql);
                                statement.executeQuery(sql);
                                columns.add(column);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                        keySet.clear();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getConnectionDbUrl(Connection connection) {
        try {
            String url = connection.getMetaData().getURL();
            return url;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }


    private void addCacheTableName(String ckUrl, String tableName) {
        Set<String> tableNameSet = tableNameMap.get(ckUrl);
        tableNameSet.add(tableName);
    }


    @Override
    public boolean existTable(Connection connection, String tableName) {
        Set<String> tableNames = tableNameMap.get(getConnectionDbUrl(connection));
        return tableNames.contains(tableName);
    }


    @Override
    public Map currentTable() {
        return tableNameMap;
    }


}
