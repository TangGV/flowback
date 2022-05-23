package cn.flowback.core.data;

import cn.flowback.core.statistic.LeapArray;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * 数据操作service
 * @author 唐警威
 **/
public interface DataService {



    /**
     * 批量保存
     * @param logs 待存储消息
     * @param leapArray 统计窗口
     * @throws SQLException
     */
    void saveBatch(BlockingQueue<Object> logs, LeapArray leapArray) throws SQLException;
}
