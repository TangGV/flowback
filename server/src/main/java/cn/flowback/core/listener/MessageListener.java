package cn.flowback.core.listener;
import com.lmax.disruptor.WorkHandler;

/***
 * 消息监听器
 * @author Tang
 */
public class MessageListener implements WorkHandler<Message> {

    private IMessageListener messageListener;

    public MessageListener(IMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void onEvent(Message log) {
        byte[] zstdSource = log.getZstdSource();
        if(zstdSource != null){
            messageListener.doMyMessage(log.getZstdSource());
        }
    }

}
