package cn.flowback.db;

import cn.flowback.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;

import java.sql.PreparedStatement;
import java.util.*;

/**
 * JSON对象 sql填充策略
 *
 * @author 唐警威
 **/
public class JsonPaddingStrategy extends DefaultFillingStrategy {

    /**
     * 通常入库对象相对稳定，如果每次都解析插入语句将会比较耗时
     * 所以用缓存一些会比较好
     * key tableName + 字段个数(通常结构相对稳定，适用只增长不减少，正常来说也不会出现减少的情况)
     */
    private static  Map<String,String> insertSqlCache = new HashMap<>();

    @Override
    public String parseInterSql(Object entity, String tableName) {
        try {
            JSONObject jsonObject = (JSONObject) entity;
            Set<String> keys = jsonObject.keySet();
            String cacheName = tableName + keys.size();
            String interSql = insertSqlCache.get(cacheName);
            if(StringUtils.isNotEmpty(interSql)){
                return interSql;
            }
            String sql = "insert into " + tableName + "(";
            for (String columnName : keys) {
                sql += columnName + ",";
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += ")";
            sql += " values(";
            for (String k : jsonObject.keySet()) {
                sql += "?,";
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += ")";
            insertSqlCache.put(cacheName,sql);
            return sql;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     *  根据insert语句缓存的字段位置信息
     */
    static Map<String,Map<String, Integer>> cacheColumnsIndex = new HashMap();

    /**
     * 缓存出现过的插入语句
     */
    static Map<String,String> cacheInsertSql = new HashMap();

    @Override
    public PreparedStatement fillSqlParameter(PreparedStatement pstmt, String insertSql, Object entity) {
        try {
            JSONObject columnsValue = (JSONObject) entity;
            //找出字段所在的索引位置，未出现的字段置为空
            String columnsSubstring;
            if (!cacheInsertSql.containsKey(insertSql)) {
                 columnsSubstring = insertSql.substring(insertSql.indexOf("(") + 1, insertSql.indexOf(")"));
                cacheInsertSql.put(insertSql,columnsSubstring);
            }else{
                 columnsSubstring = cacheInsertSql.get(insertSql);
            }
            Map<String, Integer> columnsIndex = cacheColumnsIndex.get(columnsSubstring);
            if(columnsIndex == null){
                String[] columnsNames = columnsSubstring.split(",");
                columnsIndex = new HashMap<>(16);
                for (int i = 0; i < columnsNames.length; i++) {
                    columnsIndex.put(columnsNames[i], i + 1);
                }
                cacheColumnsIndex.put(columnsSubstring,columnsIndex);
            }
            for (String key : columnsIndex.keySet()) {
                Integer index = columnsIndex.get(key);
                Object value = columnsValue.get(key);
                fillingParameter(pstmt, index, value);
            }
            pstmt.addBatch();
            return pstmt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pstmt;
    }



}
