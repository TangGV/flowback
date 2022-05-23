package cn.flowback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author 唐警威
 **/
@ConfigurationProperties(prefix = "flowback")
public class FlowBackProperties {

    /**
     * Clickhouse
     */
    private String clickhouseUrl;

    /**
     * 数据存储平台
     * Clickhouse
     * Mysql
     * Jvm
     */
    private String datasourcePlatform;

    /**
     * 消息队列名
     */
    private String consumeQueue;


    /**
     * 消费线程数
     */
    private Integer consumeThreadCount;

    /**
     * 消费消息平台
     * rabbitmq
     * netty
     */
    private String consumePlatform;

    /**
     * 开启堆外缓存
     */
    private boolean ohcache;

    /**
     * 堆外内存大小,单位字节
     */
    private long ohSize;

    private NettyProperties netty;

    public String getDatasourcePlatform() {
        return datasourcePlatform;
    }

    public void setDatasourcePlatform(String datasourcePlatform) {
        this.datasourcePlatform = datasourcePlatform;
    }


    public String getConsumePlatform() {
        return consumePlatform;
    }

    public void setConsumePlatform(String consumePlatform) {
        this.consumePlatform = consumePlatform;
    }

    public String getClickhouseUrl() {
        return clickhouseUrl;
    }

    public void setClickhouseUrl(String clickhouseUrl) {
        this.clickhouseUrl = clickhouseUrl;
    }


    public NettyProperties getNetty() {
        return netty;
    }

    public void setNetty(NettyProperties netty) {
        this.netty = netty;
    }

    public String getConsumeQueue() {
        return consumeQueue;
    }

    public void setConsumeQueue(String consumeQueue) {
        this.consumeQueue = consumeQueue;
    }

    public Integer getConsumeThreadCount() {
        return consumeThreadCount;
    }

    public void setConsumeThreadCount(Integer consumeThreadCount) {
        this.consumeThreadCount = consumeThreadCount;
    }

    public boolean isOhcache() {
        return ohcache;
    }

    public void setOhcache(boolean ohcache) {
        this.ohcache = ohcache;
    }


    public long getOhSize() {
        return ohSize;
    }

    public void setOhSize(long ohSize) {
        this.ohSize = ohSize;
    }
}


