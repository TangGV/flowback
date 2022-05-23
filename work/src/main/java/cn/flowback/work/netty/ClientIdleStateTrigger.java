package cn.flowback.work.netty;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http://www.52im.net/thread-2663-1-1.html
 * @author 唐警威
 **/
public class ClientIdleStateTrigger extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(ClientIdleStateTrigger.class.getName());

    public static final String HEART_BEAT = "hb";

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                ctx.disconnect();
                // 在规定时间内没有收到客户端的上行数据, 主动断开连接
                logger.info("主动关闭连接");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
