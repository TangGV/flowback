package cn.flowback.work.netty;

import cn.flowback.work.config.FlowbackClientProperties;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.logging.Logger;

/**
 * @author 唐警威
 **/
public class TcpClient {

    static Logger log = Logger.getLogger(TcpClient.class.getName());

    Bootstrap bootstrap;

    private String ip;

    private int port;

    /**
     * 将Channel保存起来, 可用于在其他非handler的地方发送数据
     */
    private Channel channel;

    private FlowbackClientProperties flowbackClientProperties;

    public TcpClient(FlowbackClientProperties flowbackClientProperties) {
        this.flowbackClientProperties = flowbackClientProperties;
        log.info("建立netty连接:" + flowbackClientProperties.getServerId() + ":" + flowbackClientProperties.getPort());
        this.ip = flowbackClientProperties.getServerId();
        this.port = flowbackClientProperties.getPort();
        connection();
    }

    private void connection() {
        // bootstrap 可重用, 只需在TcpClient实例化的时候初始化即可.
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        WriteBufferWaterMark writeBufferWaterMark = new WriteBufferWaterMark(1,100*1024*1024);
        bootstrap.group(group)
                .option(ChannelOption.SO_SNDBUF,2*1024*1024)
                .option(ChannelOption.SO_RCVBUF,2*1024*1024)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark)
                .channel(NioSocketChannel.class)
                .handler(new ClientHandlersInitializer(this));
    }

    public void connect() {
        synchronized (bootstrap) {
            ChannelFuture future = bootstrap.connect(ip, port);
            future.addListener(getConnectionListener());
            this.channel = future.channel();
        }
    }

    private ChannelFutureListener getConnectionListener() {
        return new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireChannelInactive();
                }
            }
        };
    }

    public Channel getChannel() {
        return channel;
    }

    public FlowbackClientProperties getFlowbackClientProperties() {
        return flowbackClientProperties;
    }

    public void setFlowbackClientProperties(FlowbackClientProperties flowbackClientProperties) {
        this.flowbackClientProperties = flowbackClientProperties;
    }
}
