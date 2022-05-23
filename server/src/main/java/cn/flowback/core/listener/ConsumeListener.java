package cn.flowback.core.listener;

import cn.flowback.config.FlowBackProperties;
import cn.flowback.core.asynch.AsynchPoll;
import cn.flowback.core.cache.OffHeapCache;
import cn.flowback.core.data.DataService;
import cn.flowback.core.statistic.LeapArray;
import cn.flowback.core.statistic.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息监听类创建消费线程,处理消息缓存,数据落库
 * @author Tang
 */
public final class ConsumeListener implements IMessageListener {

    Logger logger = LoggerFactory.getLogger(ConsumeListener.class);

    /**
     * 统计每秒处理消息窗口
     */
    private static  LeapArray statisticsWindow = new LeapArray(1000, 60);

    /**
     * 存储接收到的信息待消费线程去消费
     * long timeId = System.currentTimeMillis() / 100;
     * int index = (int) (timeId % consumerThread);
     */
    private List<BlockingQueue> arrayBlockingQueues;

    private FlowBackProperties flowBackProperties;

    private DataService dataService;

    /**
     * 默认堆内队列大小
     */
    private static Integer DEFAULT_ON_HEAP_QUEUE_SIZE = 20000;

    /**
     * 当前队列索引,每次加入缓存都是按照顺序循环，目的让消息平衡地加入缓冲
     */
    private static AtomicLong MESSAGE_INDEX = new AtomicLong(0);


    /**
     * 索引计数器平衡的将数据加入队列中避免堆积消息不均衡
     */
    private  volatile AtomicInteger INDEX = new AtomicInteger(0);


    public ConsumeListener(FlowBackProperties flowBackProperties, DataService dataService) {
        this.flowBackProperties = flowBackProperties;
        this.dataService = dataService;
        this.arrayBlockingQueues = new ArrayList<>(flowBackProperties.getConsumeThreadCount());
        for (int i = 0; i < flowBackProperties.getConsumeThreadCount(); i++) {
            arrayBlockingQueues.add(new LinkedBlockingQueue());
        }
        startWork();
    }

    @Override
    public void doMyMessage(byte [] zstdSource) {
        try {
            if(zstdSource.length < 4){
                return;
            }
            Window currentWindow = statisticsWindow.getCurrentWindow();
            currentWindow.getReceiveMessageCount().incrementAndGet();
            Queue queue = selectQueue();
            int size = queue.size();
            if(size <= DEFAULT_ON_HEAP_QUEUE_SIZE){
                //分配到堆内
                queue.offer(zstdSource);
            }else{
                long aLong = getMessageIndex();
                //分配到堆外空间
                boolean put = OffHeapCache.cache().put(aLong + "", zstdSource);
                if(put  == false){
                    //堆外分配不下了,放回堆内
                    queue.offer(zstdSource);
                }else{
                    //添加到消费队列
                    queue.offer(aLong);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Queue selectQueue() {
        int i = INDEX.updateAndGet(x->{
            if(x == arrayBlockingQueues.size()-1){
                return 0;
            }else{
                x = x+1;
                return x;
            }
        });
        BlockingQueue blockingQueue = arrayBlockingQueues.get(i);
        return blockingQueue;
    }


    /**
     * 开启入库线程
     */
    private void startWork() {

        for (int i = 0; i < arrayBlockingQueues.size(); i++) {
            BlockingQueue queue = arrayBlockingQueues.get(i);
            AsynchPoll.scheduleAtFixedRateWork(() -> {
                    try {
                            dataService.saveBatch(queue,statisticsWindow);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }, 0, 1, TimeUnit.SECONDS);
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        AsynchPoll.scheduleAtFixedRateWork(()->{
            try {
                //获取的是1秒前的窗口
                long st = System.currentTimeMillis() - 1000;
                Window window = statisticsWindow.getWindowWithStartTime(st);
                AtomicLong startTime = window.getStartTime();
                String format1 = format.format(new Date(startTime.get()));
                //获取到的数据是4秒前的了直接显示0
                if((System.currentTimeMillis() - window.getStartTime().get()) > 4000){
                    logger.info(format.format(new Date())+" 每秒接收消息:"+0+"  每秒入库:"+0+"  当前缓冲消息:"+0);
                }else{
                    logger.info(format1+" 每秒接收消息:"+window.getReceiveMessageCount().get()+"  每秒入库:"+window.getSaveCount().get()+" 当前缓冲消息:"+window.getCachedMessageCount().get());
                }
                }catch (Exception e){
                e.printStackTrace();
            }
        },0,1,TimeUnit.SECONDS);
    }


    private  long getMessageIndex(){
        return MESSAGE_INDEX.updateAndGet(x -> {
            if (x == Integer.MAX_VALUE) {
                return 0;
            }
            return x + 1;
        });
    }

}
