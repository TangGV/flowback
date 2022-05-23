package cn.flowback.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 默认填充策略
 *
 * @author 唐警威
 **/
public abstract class DefaultFillingStrategy implements SqlFillingStrategy {


    /**
     * 填充插入参数
     *
     * @param statement
     * @param index     当前内容索引
     * @param value     插入内容
     */
    public void fillingParameter(PreparedStatement statement, int index, Object value) {
        try {
             statement.setObject(index, value);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}
