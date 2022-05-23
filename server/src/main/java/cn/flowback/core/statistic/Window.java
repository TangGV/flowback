package cn.flowback.core.statistic;


import java.util.concurrent.atomic.AtomicLong;

/**
 * 统计窗口
 *
 * @author 唐警威
 **/
public class Window {

    /**
     * 开始时间
     */
    AtomicLong startTime;

    /**
     * 响应时间
     */

    AtomicLong rt = new AtomicLong();

    /**
     * qps
     */
    AtomicLong qps = new AtomicLong();

    /**
     * 统计接收消息次数
     */
    AtomicLong receiveMessageCount = new AtomicLong();

    /**
     * 统计落库次数
     */
    AtomicLong saveCount = new AtomicLong();


    /**
     * 缓存消息计数
     */
    AtomicLong cachedMessageCount = new AtomicLong();

    /**
     * 失败
     */
    AtomicLong fail = new AtomicLong();

    /**
     * 超时
     */
    AtomicLong timeOut = new AtomicLong();


    public Window(long startTime) {
        this.startTime = new AtomicLong(startTime);
    }


    public AtomicLong getReceiveMessageCount() {
        return receiveMessageCount;
    }

    public void setReceiveMessageCount(AtomicLong receiveMessageCount) {
        this.receiveMessageCount = receiveMessageCount;
    }

    public AtomicLong getSaveCount() {
        return saveCount;
    }

    public void setSaveCount(AtomicLong saveCount) {
        this.saveCount = saveCount;
    }

    public AtomicLong getStartTime() {
        return startTime;
    }

    public void setStartTime(AtomicLong startTime) {
        this.startTime = startTime;
    }

    public AtomicLong getRt() {
        return rt;
    }

    public void setRt(AtomicLong rt) {
        this.rt = rt;
    }

    public AtomicLong getQps() {
        return qps;
    }

    public void setQps(AtomicLong qps) {
        this.qps = qps;
    }


    public AtomicLong getFail() {
        return fail;
    }

    public void setFail(AtomicLong fail) {
        this.fail = fail;
    }

    public AtomicLong getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(AtomicLong timeOut) {
        this.timeOut = timeOut;
    }


    public AtomicLong getCachedMessageCount() {
        return cachedMessageCount;
    }

    public void setCachedMessageCount(AtomicLong cachedMessageCount) {
        this.cachedMessageCount = cachedMessageCount;
    }
}
