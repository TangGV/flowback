package cn.flowback.core.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * 数据库功能扩展
 *
 * @author 唐警威
 **/
public interface DataBaseExtension {


    /**
     * 获取数据库链接对象，该方法随机获取配置中的数据库链接
     * @return
     */
    Connection getBalancedConnection();

    /**
     * 初始化已存在的表名称
     */
    void initExistingTableName();


    /**
     * 检查是否有新字段，如果有则自动新增，确保插入前表结构跟对象一致
     * @param connection
     * @param tableName
     * @param log
     */
    void checkColumnsAndCreateNewColumns(Connection connection, String tableName, Object log);


    /**
     * 自动创建表
     * @param connection 数据库链接对象
     * @param tableName 表名
     * @param columns 字段map，key为字段名,value为字段类型
     */
    void createTable(Connection connection, String tableName, Map<String, Class> columns);


    /**
     * 判断是否存在表
     *
     * @param connection 数据库链接对象
     * @param tableName 表名
     * @return
     */
    boolean existTable(Connection connection, String tableName);


    /**
     * 获取当前存在的表
     * @retru key jdbc url,value 存在的表名称Set
     */
    Map<String, Set<String>> currentTable();

    /**
     * 将对象解析为插入的sql
     *
     * @param obj       保存对象
     * @param tableName 表名
     * @return 解析成的插入语句
     */
    String parseInsertSql(Object obj, String tableName);


    /**
     * 使用sql创建PreparedStatement对象
     * 不同的sql返回同的对象以支持在一个入库队列中存在不同的数据同时插入数据库
     * 使用完毕后调用 closeStatement 清理缓冲对象
     * @param sql  插入sql语句  insert  id ...(?,?....)
     * @throws SQLException
     * @return  PreparedStatement 对象
     */
    PreparedStatement createStatement(Connection collection,String sql) throws SQLException;


    /**
     * 清除statement缓存
     * @param preparedStatement
     * @throws SQLException
     */
    void clearStatementCache(PreparedStatement preparedStatement) throws SQLException;



}
