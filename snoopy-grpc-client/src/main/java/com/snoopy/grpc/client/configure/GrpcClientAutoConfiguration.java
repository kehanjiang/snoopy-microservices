package com.snoopy.grpc.client.configure;


import com.snoopy.grpc.base.configure.GrpcBaseAutoConfiguration;
import com.snoopy.grpc.client.annotation.GrpcClientBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author :   kehanjiang
 * @date :   2021/11/9  14:21
 */
@Configuration
@AutoConfigureAfter(GrpcBaseAutoConfiguration.class)
public class GrpcClientAutoConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "snoopy.grpc.client")
    public GrpcClientProperties grpcClientProperties() {
        return new GrpcClientProperties();
    }

    @Bean
    @ConditionalOnMissingBean(GrpcClientBeanPostProcessor.class)
    public GrpcClientBeanPostProcessor grpcClientBeanPostProcessor(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        return new GrpcClientBeanPostProcessor(configurableListableBeanFactory);
    }

}
