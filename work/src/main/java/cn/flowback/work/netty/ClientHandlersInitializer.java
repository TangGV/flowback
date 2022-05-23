package cn.flowback.work.netty;

import cn.flowback.common.protocol.codec.ByteDecode;
import cn.flowback.common.protocol.codec.ByteEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;


/**
 * 客户端处理器初始化
 * @author 唐警威
 **/
public class ClientHandlersInitializer extends ChannelInitializer<SocketChannel> {


    private TcpClient tcpClient;

    private ReconnectHandler reconnectHandler;


    public ClientHandlersInitializer(TcpClient tcpClient) {
        this.tcpClient = tcpClient;
        reconnectHandler = new ReconnectHandler(tcpClient);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //流量整形
        //GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(ch.eventLoop().parent(), 1 * 1024 * 1024, 10 * 1024 * 1024);
        //pipeline.addLast(globalTrafficShapingHandler);
        pipeline.addLast(reconnectHandler);
        pipeline.addLast(new ByteDecode());
        pipeline.addLast(new ByteEncoder());
        pipeline.addLast(new Ping());
        pipeline.addLast(new WorkClientHandler(tcpClient.getFlowbackClientProperties()));
    }
}
