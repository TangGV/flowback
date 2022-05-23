package cn.flowback.db;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * 反射获取属性性能很差不建议使用,保留方案
 * @author 唐警威
 **/
public class EntityFillingStrategy extends DefaultFillingStrategy {

    @Override
    public String parseInterSql(Object entity, String tableName) {
        try {
            Map<String, Class> typeMap = new HashMap<>(16);
            List<Map<String, Object>> columns = new ArrayList<>(16);
            Field[] declaredFields = entity.getClass().getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                String name = declaredFields[i].getName();
                String getMethodStr = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
                Class<?> type = declaredFields[i].getType();
                Method declaredMethod = entity.getClass().getDeclaredMethod(getMethodStr, null);
                Object invoke = declaredMethod.invoke(entity, null);
                if (invoke == null) {
                    //填参数，不能让字段为空
                    String setMethodStr = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
                    Method setMethod = entity.getClass().getDeclaredMethod(setMethodStr, declaredFields[i].getType());
                    paddingDefaultValue(setMethod, entity, declaredFields[i].getType());
                    invoke = declaredMethod.invoke(entity, null);
                }
                Map<String, Object> column = new HashMap<>();
                column.put(name, invoke);
                typeMap.put(name, type);
                columns.add(column);
            }
            String sql = "insert into " + tableName.toLowerCase() + "(";
            for (Map<String, Object> stringObjectMap : columns) {
                for (String key : stringObjectMap.keySet()) {
                    sql += key + ",";
                }
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += ")";
            sql += " values(";
            for (Map<String, Object> c : columns) {
                sql += "?,";
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += ")";
            return sql;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }


    @Override
    public PreparedStatement fillSqlParameter(PreparedStatement pstmt, String insertSql, Object entity) {
        try {
            //性能很差建议直接使用json对象保存
            JSONObject parse = JSONObject.parseObject(JSONObject.toJSONString(entity));
            //找出字段所在的索引位置，未出现的字段置为空
            int s = insertSql.indexOf("(");
            int e = insertSql.indexOf(")");
            String columnsSubstring = insertSql.substring(s + 1, e);
            String[] columnsNames = columnsSubstring.split(",");
            Map<String, Integer> columnsIndex = new HashMap<>(16);
            for (int i = 0; i < columnsNames.length; i++) {
                columnsIndex.put(columnsNames[i], i + 1);
            }
            for (String key : columnsIndex.keySet()) {
                Integer index = columnsIndex.get(key);
                Object value = parse.get(key);
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
