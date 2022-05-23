package cn.flowback.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FlowBackProperties.class)
@ConditionalOnProperty(prefix = "flowback", value = "enabled", matchIfMissing = true)
public class FlowBackConfiguration {

    @Autowired
    private FlowBackProperties flowBackProperties;

}
