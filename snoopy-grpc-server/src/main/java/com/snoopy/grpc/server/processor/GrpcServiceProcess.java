package com.snoopy.grpc.server.processor;

import com.google.common.collect.Lists;
import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.constans.GrpcConstants;
import com.snoopy.grpc.base.registry.IRegistry;
import com.snoopy.grpc.base.registry.RegistryProviderFactory;
import com.snoopy.grpc.base.registry.RegistryServiceInfo;
import com.snoopy.grpc.base.utils.LoggerBaseUtil;
import com.snoopy.grpc.server.annotation.SnoopyGrpcGlobalServerInterceptor;
import com.snoopy.grpc.server.annotation.SnoopyGrpcService;
import com.snoopy.grpc.server.configure.GrpcServerProperties;
import com.snoopy.grpc.server.reflection.SnoopyServiceUtil;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.netty.NettyServerBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 服务端绑定服务
 *
 * @author :   kehanjiang
 * @date :   2021/11/9  16:57
 */
public class GrpcServiceProcess implements IGrpcProcess {
    private ApplicationContext applicationContext;

    private IRegistry registry;

    private GrpcServerProperties grpcServerProperties;

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    protected static Set<RegistryServiceInfo> registryServices = new HashSet<>();

    protected static Set<ServerInterceptor> globalServerInterceptors=new HashSet<>();

    public GrpcServiceProcess(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        RegistryProviderFactory registryProviderFactory = applicationContext.getBean(RegistryProviderFactory.class);
        this.grpcServerProperties = applicationContext.getBean(GrpcServerProperties.class);
        GrpcRegistryProperties grpcRegistryProperties = applicationContext.getBean(GrpcRegistryProperties.class);
        this.registry = registryProviderFactory.newRegistryProviderInstance().newRegistryInstance(grpcRegistryProperties);

        Collection<String> beanNames = Arrays.asList(
                applicationContext.getBeanNamesForAnnotation(SnoopyGrpcGlobalServerInterceptor.class)
        );
        for (String beanName : beanNames) {
            ServerInterceptor globalServerInterceptor = applicationContext.getBean(beanName, ServerInterceptor.class);
            globalServerInterceptors.add(globalServerInterceptor);
        }
    }

    @Override
    public void handle(NettyServerBuilder builder) {
        Collection<String> beanNames = Arrays.asList(
                applicationContext.getBeanNamesForAnnotation(SnoopyGrpcService.class)
        );
        for (String beanName : beanNames) {
            BindableService bindableService = applicationContext.getBean(beanName, BindableService.class);
            ServerServiceDefinition serverServiceDefinition = bindableService.bindService();

            List<ServerInterceptor> privateInterceptors = Lists.newArrayList();
            SnoopyGrpcService snoopyGrpcService = applicationContext.findAnnotationOnBean(beanName, SnoopyGrpcService.class);
            for (Class<? extends ServerInterceptor> interceptorClass : snoopyGrpcService.interceptors()) {
                privateInterceptors.add(loadPrivateInterceptorBean(interceptorClass));
            }
            if(snoopyGrpcService.applyGlobalServerInterceptors()){
                privateInterceptors.addAll(globalServerInterceptors);
            }

            serverServiceDefinition = ServerInterceptors.interceptForward(serverServiceDefinition, privateInterceptors);
            builder.addService(serverServiceDefinition);
            //泛化调用时获取.proto文件setFileContainingSymbol可以使用服务别名alias而不使用<package>.<service>[.<method>]
            if (!StringUtils.isEmpty(snoopyGrpcService.alias())) {
                ServerServiceDefinition  serverServiceDefinitionOfAlias = SnoopyServiceUtil.rename(serverServiceDefinition, snoopyGrpcService.alias());
                builder.addService(serverServiceDefinitionOfAlias);
            }

            RegistryServiceInfo registryServiceInfo = new RegistryServiceInfo(snoopyGrpcService.namespace(),
                    snoopyGrpcService.alias(),
                    grpcServerProperties.isUsePlaintext() ? GrpcConstants.PROTOCOL_HTTP2_PLAIN : GrpcConstants.PROTOCOL_HTTP2_SSL,
                    grpcServerProperties.getAddress(),
                    grpcServerProperties.getPort(),
                    grpcServerProperties.getWeight());
            registryServices.add(registryServiceInfo);
            registry.register(registryServiceInfo);
            LoggerBaseUtil.info(this, "[{}] Registry gRPC service successful: {}", registry.getClass().getSimpleName(), registryServiceInfo.getFullPath());
            atomicInteger.incrementAndGet();
        }

        LoggerBaseUtil.info(this,
                "\n======= \ngRPC service  count: {} \n=======",
                atomicInteger.get());
    }

    public void removeAllRegistryService() {
        registryServices.stream().forEach(registryServiceInfo -> {
            registry.unregister(registryServiceInfo);
            LoggerBaseUtil.info(this, "[{}] unRegistry gRPC service successful: {}", registry.getClass().getSimpleName(), registryServiceInfo.getFullPath());
        });
    }

    public <T> T loadPrivateInterceptorBean(Class clazz) {
        try {
            if (applicationContext.getBeanNamesForType(clazz).length > 0) {
                return (T) applicationContext.getBean(clazz);
            } else {
                return (T) clazz.newInstance();
            }
        } catch (Exception e) {
            throw new BeanCreationException("Failed to load  bean .", e);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }


}
