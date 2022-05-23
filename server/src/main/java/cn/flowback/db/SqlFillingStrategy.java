package cn.flowback.db;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.Date;

/**
 * sql填充策略
 *
 * @author 唐警威
 **/
public interface SqlFillingStrategy {


    /**
     * 根据对象解析出插入语句sql
     *
     * @param entity
     * @param tableName
     * @return
     */
    String parseInterSql(Object entity, String tableName);


    /**
     * 填充sql插入参数
     * @param preparedStatement
     * @param insertSql
     * @param entity
     * @return
     */
    PreparedStatement fillSqlParameter(PreparedStatement preparedStatement, String insertSql, Object entity);



    /**
     * 填充默认参数
     *
     * @param setMethod
     * @param logObj
     * @param type
     */
    default void paddingDefaultValue(Method setMethod, Object logObj, Type type) {
        try {
            Object p = null;
            if (Integer.class == type) {
                p = -1;
            }
            if (String.class == type) {
                p = "";
            }
            if (Date.class == type) {
                p = new Date();
            }
            if (Double.class == type) {
                p = -1;
            }
            if (BigDecimal.class == type) {
                p = BigDecimal.ZERO;
            }
            setMethod.invoke(logObj, p);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
