package cn.flowback.core.listener;

import com.lmax.disruptor.EventFactory;

/**
 * @author T
 */
public class MessageEventFactory implements EventFactory<Message>
{
    @Override
    public Message newInstance()
    {
        return new Message();
    }
}
