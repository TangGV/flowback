package cn.flowback.core.listener.netty;

import cn.flowback.config.FlowBackProperties;
import cn.flowback.core.listener.IMessageListener;
import cn.flowback.core.listener.netty.client.WorkNodeClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author 唐警威
 **/
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private IMessageListener messageListener;

    FlowBackProperties flowBackProperties;

    NettyServerHandler(IMessageListener messageListener, FlowBackProperties flowBackProperties) {
        this.messageListener = messageListener;
        this.flowBackProperties = flowBackProperties;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        byte [] bytes = (byte[]) msg;
        messageListener.doMyMessage(bytes);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("=================建立链接成功=================" + ctx.channel().remoteAddress());
        WorkNodeClient.addClient(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Disconnected with the remote client.");
        logger.info("断开连接-频道不活动{}", ctx.channel().remoteAddress());
        WorkNodeClient.removeClient(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        WorkNodeClient.removeClient(ctx);
        logger.info("连接异常{}", cause.getMessage());
        logger.info("断开连接,{}", ctx.channel().remoteAddress());
        ctx.close();
    }
}
