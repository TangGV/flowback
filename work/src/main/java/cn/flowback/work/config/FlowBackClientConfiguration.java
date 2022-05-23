package cn.flowback.work.config;

import cn.flowback.work.netty.TcpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Logger;


/**
 * 工作节点自动配置类
 *
 * @author Tang
 */
@Configuration
@EnableConfigurationProperties(FlowbackClientProperties.class)
public class FlowBackClientConfiguration implements ApplicationContextAware {

    static Logger log = Logger.getLogger(FlowBackClientConfiguration.class.getName());


    FlowBackClientConfiguration() {
        log.info("执行FlowBackClientConfiguration初始化");
    }

    ApplicationContext applicationContext;

    @Autowired
    private FlowbackClientProperties flowbackClientProperties;


    @Bean
    public TcpClient nettyStart() throws InterruptedException {
        log.info("=================使用netty作为传输工具=================");
        TcpClient tcpClient = new TcpClient(flowbackClientProperties);
        tcpClient.connect();
        //todo 延迟一下防止netty初始化完导致后续的空指针
        Thread.sleep(100);
        return tcpClient;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext.getParent();
    }

}
