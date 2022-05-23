package cn.flowback.common.protocol;


/**
 * 执行测试日志存储任务任务协议
 *
 * @author 唐警威
 **/
public class LogTestWorkProtocol extends BaseProtocol {

    public LogTestWorkProtocol() {
        super(MessageType.TEST_LOG_WORK);
    }

    /***
     * 产生日志记录数
     */
    private int count = 1;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
