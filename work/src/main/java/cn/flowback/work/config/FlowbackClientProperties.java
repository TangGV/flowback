package cn.flowback.work.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 客户端配置类
 * @author Tang
 */
@ConfigurationProperties(prefix = "flowback.client")
public class FlowbackClientProperties {

    private String serverId;

    private int port;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "FlowbackClientProperties{" +
                "serverId='" + serverId + '\'' +
                ", port=" + port +
                '}';
    }
}


