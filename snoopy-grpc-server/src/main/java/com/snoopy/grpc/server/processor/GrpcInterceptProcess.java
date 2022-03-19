package com.snoopy.grpc.server.processor;

import com.snoopy.grpc.server.annotation.SnoopyGrpcGlobalServerInterceptor;
import io.grpc.ServerInterceptor;
import io.grpc.netty.NettyServerBuilder;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collection;


/**
 * 设置服务端全局拦截器
 *
 * @author :   kehanjiang
 * @date :   2021/11/10  11:47
 */
public class GrpcInterceptProcess implements IGrpcProcess {
    private ApplicationContext applicationContext;

    public GrpcInterceptProcess(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(NettyServerBuilder builder) {
        Collection<String> beanNames = Arrays.asList(
                applicationContext.getBeanNamesForAnnotation(SnoopyGrpcGlobalServerInterceptor.class)
        );
        for (String beanName : beanNames) {
            ServerInterceptor globalServerInterceptor = applicationContext.getBean(beanName, ServerInterceptor.class);
            builder.intercept(globalServerInterceptor);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
