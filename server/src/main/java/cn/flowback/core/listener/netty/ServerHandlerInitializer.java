package cn.flowback.core.listener.netty;

import cn.flowback.common.protocol.codec.ByteDecode;
import cn.flowback.common.protocol.codec.ByteEncoder;
import cn.flowback.config.FlowBackProperties;
import cn.flowback.core.listener.IMessageListener;
import cn.flowback.utils.BeanUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * nettyServer处理器初始化
 * @author 唐警威
 **/
@ChannelHandler.Sharable
public class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {


    @Autowired
    private IMessageListener messageListener;

    private FlowBackProperties flowBackProperties;

    public ServerHandlerInitializer(FlowBackProperties flowBackProperties) {
        this.flowBackProperties = flowBackProperties;
    }

    ServerHandlerInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel ch)  {
        if (messageListener == null) {
            //因为配置的启动配置类netty启动优先级比IMessageListener前，第一次注入肯定为空故在此重新判断获取
            messageListener = BeanUtils.getBean(IMessageListener.class);
        }
        ChannelPipeline pipeline = ch.pipeline();
        //心跳包处理
        pipeline.addLast("idleStateHandler", new IdleStateHandler(200, 0, 0));
        pipeline.addLast("idleStateTrigger", new ServerIdleStateTrigger());
        //编解码方式
        pipeline.addLast(new ByteDecode());
        pipeline.addLast(new ByteEncoder());
        pipeline.addLast(new NettyServerHandler(messageListener, flowBackProperties));
    }

}
