package cn.flowback.core.listener.netty.client;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 任务节点客户端
 *
 * @author 唐警威
 **/
public class WorkNodeClient {

    static Logger logger = Logger.getLogger(WorkNodeClient.class.getName());

    private static Set<ChannelHandlerContext> client = new HashSet<>();


    private static Map<ChannelHandlerContext, Object> clientMsg = new HashMap<>();


    public static Set<ChannelHandlerContext> clients() {
        return client;
    }


    public static void addClient(ChannelHandlerContext ctx) {
        client.add(ctx);
        logger.info("增加任务节点" + ctx.channel().remoteAddress().toString()+" 当前节点数:"+client.size());
    }


    public static void removeClient(ChannelHandlerContext ctx) {
        logger.info("移除任务节点" + ctx.channel().remoteAddress().toString());
        client.remove(ctx);
    }

}
