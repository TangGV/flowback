package cn.flowback.core.listener.netty;

import cn.flowback.config.FlowBackProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty 服务配置
 *
 * @author Tang
 */
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    /**
     * 服务端NIO线程组
     */
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();

    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    private FlowBackProperties flowBackProperties;

    public NettyServer(FlowBackProperties flowBackProperties) {
        this.flowBackProperties = flowBackProperties;
    }


    public ChannelFuture start(String host, int port) {
        ChannelFuture channelFuture = null;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            //WriteBufferWaterMark writeBufferWaterMark = new WriteBufferWaterMark(2024000,4024000);
            //bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark);
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    //.option(ChannelOption.SO_SNDBUF,2*1024*1024)
                    //.option(ChannelOption.SO_RCVBUF,1024*1024*1024)
                    .childHandler(new ServerHandlerInitializer(flowBackProperties));
            // 绑定端口并同步等待
            channelFuture = bootstrap.bind(host, port).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channelFuture;
    }
}