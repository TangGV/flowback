package cn.flowback.config;


/**
 * netty 配置
 *
 * @author 唐警威
 **/
public class NettyProperties {

    /**
     * 监听地址
     */
    private String host;

    /**
     * 监听端口
     */
    private Integer port;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}


