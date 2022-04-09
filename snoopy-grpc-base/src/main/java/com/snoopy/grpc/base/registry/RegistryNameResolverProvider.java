package com.snoopy.grpc.base.registry;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.configure.GrpcSecurityProperties;
import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import java.net.URI;

/**
 * 服务提供者类
 *
 * @author :   kehanjiang
 * @date :   2021/11/29  21:49
 */
public class RegistryNameResolverProvider extends NameResolverProvider {
    private GrpcRegistryProperties grpcRegistryProperties;

    private GrpcSecurityProperties grpcSecurityProperties;

    private RegistryServiceInfo registryServiceInfo;

    private IRegistry registry;

    public RegistryNameResolverProvider(GrpcRegistryProperties grpcRegistryProperties,
                                        GrpcSecurityProperties grpcSecurityProperties,
                                        RegistryServiceInfo registryServiceInfo,
                                        IRegistry registry) {
        this.grpcRegistryProperties = grpcRegistryProperties;
        this.grpcSecurityProperties = grpcSecurityProperties;
        this.registryServiceInfo = registryServiceInfo;
        this.registry = registry;
    }

    /**
     * 服务是否可用
     *
     * @return
     */
    @Override
    protected boolean isAvailable() {
        return true;
    }

    /**
     * 优先级默认5
     *
     * @return
     */
    @Override
    protected int priority() {
        return 5;
    }

    /**
     * 服务协议
     *
     * @return
     */
    @Override
    public String getDefaultScheme() {
        return grpcRegistryProperties.getProtocol();
    }

    /**
     * 服务发现类
     *
     * @param targetUri
     * @param args
     * @return
     */
    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        return new RegistryNameResolver(grpcSecurityProperties, registryServiceInfo, registry);
    }
}
