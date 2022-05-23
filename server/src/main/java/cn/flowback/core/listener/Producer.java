package cn.flowback.core.listener;
import com.lmax.disruptor.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 消息生产者
 * @author Tang
 */
public class Producer {


    private Logger logger = LoggerFactory.getLogger(getClass());

    RingBuffer<Message> ringBuffer;

    public Producer(RingBuffer<Message> ringBuffer){
        this.ringBuffer = ringBuffer;
    }

    /**
     * 发送数据
     */
    public void sendData(byte [] bytes) {
        long sequence = ringBuffer.next();
        try {
            Message message = ringBuffer.get(sequence);
            message.setZstdSource(bytes);
        } finally {
            ringBuffer.publish(sequence);
        }
    }


}
