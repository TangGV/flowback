package cn.flowback.core.listener;


import java.util.Queue;

/**
 * 消息消费接口
 *
 * @author 唐警威
 **/
public interface IMessageListener {



    /**
     * 接收到消息
     *
     * @param zstdSource  zstd压缩前的字节
     */
    void doMyMessage(byte [] zstdSource);


    /**
     * 选择存储的消息队列
     * @return
     */
    Queue selectQueue();

}
