package cn.flowback.core.statistic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 统计数据滑动窗口
 *
 * @author 唐警威
 **/
public class LeapArray {

    Logger logger = LoggerFactory.getLogger(LeapArray.class.getName());

    static  ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * 单个窗口时长单位毫秒
     */
    protected int windowLengthInMs;

    /**
     * 取样窗口个数
     */
    protected int sampleCount;

    /**
     * 窗口间隔时长单位毫秒
     */
    protected int intervalInMs;


    Window[] windowsArr;

    public LeapArray(int windowLengthInMs, int sampleCount) {
        this.windowLengthInMs = windowLengthInMs;
        this.sampleCount = sampleCount;
        intervalInMs = windowLengthInMs / sampleCount;
        windowsArr = new Window[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            windowsArr[i] = new Window(System.currentTimeMillis());
        }
    }

    public Window getCurrentWindow() {
        long currentTimeMillis1 = System.currentTimeMillis();
        long startTime = calculateWindowStart(currentTimeMillis1);
        int index = currentIndex(currentTimeMillis1);
        Window window = windowsArr[index];
        if (window == null) {
            window = new Window(startTime);
            windowsArr[index] = window;
            return window;
        } else if (startTime == window.startTime.get()) {
            //开始时间就是当前窗口
            return window;
        } else if (startTime > window.startTime.get()) {
            //窗口是旧地需要重置
            if (reentrantLock.tryLock()) {
                try {
                    restWindow(window);
                    return window;
                }finally {
                    reentrantLock.unlock();
                }
            }else{
                Thread.yield();
            }
        }
        return window;
    }

    /**
     * 根据时间戳获取窗口
     * @return
     */
    public Window getWindowWithStartTime(long startTime) {
        int index = currentIndex(startTime);
        Window window = windowsArr[index];
        return window;
    }


    private void restWindow(Window window){
        long currentTimeMillis1 = System.currentTimeMillis();
        long startTime = calculateWindowStart(currentTimeMillis1);
        if(startTime > window.getStartTime().get() ){
            window.getStartTime().set(startTime);
            window.getQps().set(0);
            window.getSaveCount().set(0);
            window.getReceiveMessageCount().set(0);
            window.getCachedMessageCount().set(0);
            window.getFail().set(0);
            window.getRt().set(0);
            window.getTimeOut().set(0);
        }
    }

    /**
     * 计算当前时间所在的窗口位置
     *
     * @param timeMillis 当前时间戳
     * @return
     */
    private int currentIndex(long timeMillis) {
        //目前这个是每个窗口的大小,单位是毫秒
        long timeId = timeMillis / windowLengthInMs;
        return (int) (timeId % sampleCount);
    }

    /**
     * 计算当前窗口开始时间
     *
     * @param timeMillis 当前时间戳
     * @return
     */
    private long calculateWindowStart(/*@Valid*/ long timeMillis) {
        return timeMillis - timeMillis % windowLengthInMs;
    }


}
