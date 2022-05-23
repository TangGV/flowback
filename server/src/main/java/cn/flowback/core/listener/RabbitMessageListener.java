package cn.flowback.core.listener;

import cn.flowback.common.utils.ZstdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

/**
 * rabbitmq消息消费
 *
 * @author 唐警威
 **/
public class RabbitMessageListener  implements MessageListener {

    IMessageListener messageListener;

    Logger logger = LoggerFactory.getLogger(RabbitMessageListener.class.getName());

    public RabbitMessageListener(IMessageListener messageListener ){
        this.messageListener = messageListener;
    }

    @Override
    public void onMessage(Message message) {
        try {
            String msg = new String(message.getBody(), "utf-8");
            String consumerQueue = message.getMessageProperties().getConsumerQueue();
            logger.debug(consumerQueue + "=================rabbitmq消费消息=================" + msg);
            byte[] compress = ZstdUtils.compress(msg.getBytes());
            messageListener.doMyMessage(compress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
