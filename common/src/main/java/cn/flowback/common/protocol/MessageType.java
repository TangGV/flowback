package cn.flowback.common.protocol;

/**
 * 消息类型
 *
 * @author Tang
 */
public class MessageType {

    public static final String MESSAGE_TYPE_TAG = "messageType";


    /**
     * 心跳包
     */
    public static final Integer HEART = 0;

    /**
     * 节点消息
     */
    public static final Integer WORK_NODE = 1;

    /**
     * 执行任务
     */
    public static final Integer EXECUTE_WORK = 2;

    /**
     * 执行任务完毕统计结果
     */
    public static final Integer STATISTICS = 3;

    /**
     * 执行测试日志存储任务
     */
    public static final  Integer TEST_LOG_WORK = 4;


}