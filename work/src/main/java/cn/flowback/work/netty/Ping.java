package cn.flowback.work.netty;

/**
 * TODO
 *
 * @author 唐警威
 **/

import cn.flowback.common.protocol.HeartProtocol;
import cn.flowback.common.utils.ProtocolUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * https://www.jianshu.com/p/1a28e48edd92
 * <p>客户端连接到服务器端后，会循环执行一个任务：随机等待几秒，然后ping一下Server端，即发送一个心跳包。</p>
 *
 * @author Tang
 */
public class Ping extends ChannelInboundHandlerAdapter {

    static Logger logger = LoggerFactory.getLogger(Ping.class.getName());

    private Random random = new Random();

    private int baseRandom = 15;

    private Channel channel;

    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channel = ctx.channel();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            ping(ctx);
        }, 0, 3, TimeUnit.SECONDS);
    }

    private void ping(ChannelHandlerContext ctx) {
        if (channel.isActive()) {
            HeartProtocol heartProtocol = new HeartProtocol();
            ProtocolUtils.sendMsg(ctx,heartProtocol);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 当Channel已经断开的情况下, 仍然发送数据, 会抛异常, 该方法会被调用.
        cause.printStackTrace();
        ctx.close();
    }
}