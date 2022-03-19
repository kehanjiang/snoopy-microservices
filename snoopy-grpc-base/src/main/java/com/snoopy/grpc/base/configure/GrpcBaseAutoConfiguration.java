package com.snoopy.grpc.base.configure;

import com.snoopy.grpc.base.registry.RegistryProviderFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author :   kehanjiang
 * @date :   2021/11/9  14:21
 */
@Configuration
public class GrpcBaseAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "snoopy.grpc.security")
    public GrpcSecurityProperties grpcSecurityProperties() {
        return new GrpcSecurityProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "snoopy.grpc.registry")
    public GrpcRegistryProperties grpcRegistryProperties() {
        return new GrpcRegistryProperties();
    }

    @Bean
    @ConditionalOnBean(GrpcRegistryProperties.class)
    public RegistryProviderFactory registryProviderFactory(GrpcRegistryProperties grpcRegistryProperties) {
        return new RegistryProviderFactory(grpcRegistryProperties);
    }

}
