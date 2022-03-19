package com.snoopy.grpc.server.configure;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.configure.GrpcSecurityProperties;
import com.snoopy.grpc.base.registry.RegistryProviderFactory;
import com.snoopy.grpc.base.utils.SpringBeanUtil;
import com.snoopy.grpc.server.factory.GrpcServerLifecycle;
import com.snoopy.grpc.server.factory.NettyServerFactory;
import com.snoopy.grpc.server.processor.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author :   kehanjiang
 * @date :   2021/11/9  14:21
 */
@Configuration
public class GrpcServerAutoConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "snoopy.grpc.server")
    public GrpcServerProperties grpcServerProperties() {
        return new GrpcServerProperties();
    }

    @Bean
    @ConditionalOnBean(SpringBeanUtil.class)
    public GrpcInterceptProcess grpcInterceptProcess() {
        return new GrpcInterceptProcess(SpringBeanUtil.getApplicationContext());
    }

    @Bean
    @ConditionalOnBean({
            SpringBeanUtil.class,
            RegistryProviderFactory.class,
            GrpcRegistryProperties.class,
            GrpcServerProperties.class
    })
    public GrpcServiceProcess grpcServiceProcess() {
        return new GrpcServiceProcess(SpringBeanUtil.getApplicationContext());
    }

    @Bean
    @ConditionalOnBean({
            GrpcServerProperties.class,
            GrpcSecurityProperties.class
    })
    public GrpcSecurityProcess grpcSecurityProcess(GrpcServerProperties grpcServerProperties, GrpcSecurityProperties grpcSecurityProperties) {
        return new GrpcSecurityProcess(grpcServerProperties, grpcSecurityProperties);
    }

    @Bean
    @ConditionalOnBean(GrpcServerProperties.class)
    public GrpcReflectionProcess grpcReflectionProcess(GrpcServerProperties grpcServerProperties) {
        return new GrpcReflectionProcess(grpcServerProperties);
    }

    @Bean
    @ConditionalOnBean({
            GrpcServerProperties.class,
            GrpcServiceProcess.class,
            GrpcInterceptProcess.class,
            GrpcSecurityProcess.class
    })
    public NettyServerFactory nettyServerFactory(GrpcServerProperties properties, List<IGrpcProcess> processes) {
        return new NettyServerFactory(properties, processes);
    }

    @Bean
    @ConditionalOnBean({NettyServerFactory.class})
    public GrpcServerLifecycle grpcServerLifecycle(NettyServerFactory nettyServerFactory) {
        return new GrpcServerLifecycle(nettyServerFactory);
    }

}
