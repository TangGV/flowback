package cn.flowback.config;

import cn.flowback.core.data.DataService;
import cn.flowback.core.listener.*;
import cn.flowback.core.listener.netty.NettyServer;
import cn.flowback.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.ConsumerTagStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.UUID;

/**
 * 消息配置
 *
 * @author 唐警威
 **/
@Configuration
public class MessageListenerConfig {

    Logger log = LoggerFactory.getLogger(MessageListenerConfig.class.getName());

    @Autowired
    private FlowBackProperties flowBackProperties;

    @Autowired
    private DataService dataService;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    IMessageListener messageListener;

    @Bean
    public IMessageListener messageListener() {
        log.info("=================初始化MessageListener================");
        IMessageListener abstractMessageListener = new ConsumeListener(flowBackProperties, dataService);
        messageListener = abstractMessageListener;
        return abstractMessageListener;
    }


    @Bean
    @ConditionalOnProperty(prefix = "flowback", name = "consumePlatform", havingValue = "rabbitmq")
    public SimpleMessageListenerContainer rabbitMqConsume() {
        log.info("=================使用rabbitmq配置=================");
        /*创建交换机，队列绑定 amqpAdmin.declareBinding()*/
        //获取简单消息监听容器自定义扩展功能
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        String consumeQueue = flowBackProperties.getConsumeQueue();
        if (StringUtils.isNotEmpty(consumeQueue)) {
            for (String queueName : consumeQueue.split(",")) {
                container.addQueueNames(queueName);
            }
        } else {
            log.warn("未配置消息队列名称");
        }
        //设置当前的消费者数量
        container.setConcurrentConsumers(5);
        container.setMaxConcurrentConsumers(5);
        //设置是否重回队列
        container.setDefaultRequeueRejected(false);
        //设置自动签收
        container.setAcknowledgeMode(AcknowledgeMode.NONE);
        //设置监听外露
        container.setExposeListenerChannel(true);
        //设置消费端标签策略
        container.setConsumerTagStrategy(new ConsumerTagStrategy() {
            @Override
            public String createConsumerTag(String queue) {
                String qname = queue + "_" + UUID.randomUUID().toString();
                return qname;
            }
        });
        //设置消息监听
        container.setMessageListener(new RabbitMessageListener(messageListener));
        return container;
    }



    @Bean
    @ConditionalOnProperty(prefix = "flowback", name = "consumePlatform", havingValue = "netty")
    public void nettyConsume() {
        Integer port = flowBackProperties.getNetty().getPort();
        String host = flowBackProperties.getNetty().getHost();
        log.info("=================启动nettyServer=================host:" + host + " port:" + port);
        NettyServer nettyServer = new NettyServer(flowBackProperties);
        nettyServer.start(host, port);
    }






}
