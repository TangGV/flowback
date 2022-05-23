package cn.flowback.common.utils;

import cn.flowback.common.protocol.WorkNodeProtocol;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * 协议工具
 *
 * @author 唐警威
 **/
public class ProtocolUtils {

    static Logger logger = Logger.getLogger(ProtocolUtils.class.getName());

    static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    static  AtomicInteger sendMessageCount = new AtomicInteger(0);

    static  AtomicInteger sendFaultMessageCount = new AtomicInteger(0);

    static {
        executorService.scheduleAtFixedRate(()->{
            logger.info("Send message:"+sendMessageCount.get() +" fault: "+sendFaultMessageCount.get());
        },1, 5, TimeUnit.SECONDS);
    }

    public static void sendMsg(ChannelHandlerContext ctx, Object msg) {
        if (ctx.channel().isWritable()) {
            ChannelFuture channelFuture = ctx.writeAndFlush(msg);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future){
                    sendMessageCount.incrementAndGet();
                }
            });
        }else{
            sendFaultMessageCount.incrementAndGet();
        }
    }

    public static void sendZstdMsg(ChannelHandlerContext ctx, String msg) {
        if (ctx.channel().isWritable()) {
            byte[] bytes = msg.getBytes();
            byte[] tmp = ZstdUtils.compress(bytes);
            ChannelFuture channelFuture = ctx.writeAndFlush(tmp);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future){
                    sendMessageCount.incrementAndGet();
                }
            });
        }else{
            sendFaultMessageCount.incrementAndGet();
        }
    }

    /**
     * 注册任务节点
     * @param ctx
     */
    public static void registerWorkNode(ChannelHandlerContext ctx) {
        if (ctx.channel().isWritable()) {
            WorkNodeProtocol workNodeProtocol = new WorkNodeProtocol();
            workNodeProtocol.setCores(Runtime.getRuntime().availableProcessors() + "");
            try {
                InetAddress adder = InetAddress.getLocalHost();
                workNodeProtocol.setIp(adder.getHostAddress().toLowerCase());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            ProtocolUtils.sendMsg(ctx,workNodeProtocol);
        }
    }
}
