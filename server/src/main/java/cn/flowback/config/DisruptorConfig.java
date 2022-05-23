package cn.flowback.config;

import cn.flowback.core.data.DataService;
import cn.flowback.core.listener.*;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Disruptor 消息缓冲
 * @author T
 */
@Configuration
public class DisruptorConfig {

    @Autowired
    private FlowBackProperties flowBackProperties;


    @Autowired
    private IMessageListener iMessageListener;

    @Autowired
    private DataService dataService;

    @Bean
    public Producer initProducer(){
        EventFactory<Message> eventFactory = new MessageEventFactory();
        ThreadFactory executor = Executors.defaultThreadFactory();
        // RingBuffer 大小，必须是 2 的 N 次方；
        int ringBufferSize = 1024;
        //Disruptor 等待策略会导致cpu占用，根据实际选择使用 https://www.jianshu.com/p/78c85ce10c0c
        Disruptor<Message> disruptor = new Disruptor(eventFactory,
                ringBufferSize, executor, ProducerType.SINGLE,
                new SleepingWaitStrategy());
        MessageListener[] consumers = new MessageListener[1];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new MessageListener(iMessageListener);
        }
        disruptor.handleEventsWithWorkerPool(consumers);
        return new Producer(disruptor.start());
    }


}
