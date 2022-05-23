package cn.flowback.work.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 断线重连
 *
 * @author 唐警威
 **/
@ChannelHandler.Sharable
public class ReconnectHandler extends ChannelInboundHandlerAdapter {

    static Logger log = Logger.getLogger(ReconnectHandler.class.getName());

    private int retries = 0;

    TcpClient tcpClient;

    public ReconnectHandler(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Successfully established a connection to the server.");
        retries = 0;
        ctx.fireChannelActive();
    }

    /**
     * 通道不活跃时尝试重连固定5秒一周期
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(() -> {
            tcpClient.connect();
        }, 5, TimeUnit.SECONDS);
        ctx.fireChannelInactive();
    }
}
