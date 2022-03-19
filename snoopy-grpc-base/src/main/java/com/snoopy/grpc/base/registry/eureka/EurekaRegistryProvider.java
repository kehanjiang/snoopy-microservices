package com.snoopy.grpc.base.registry.eureka;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.registry.IRegistry;
import com.snoopy.grpc.base.registry.IRegistryProvider;
import com.snoopy.grpc.base.registry.nacos.NacosRegistry;

/**
 * @author :   kehanjiang
 * @date :   2021/12/1  15:44
 */
public class EurekaRegistryProvider implements IRegistryProvider {
    private GrpcRegistryProperties grpcRegistryProperties;

    public EurekaRegistryProvider(GrpcRegistryProperties grpcRegistryProperties) {
        this.grpcRegistryProperties = grpcRegistryProperties;
    }

    @Override
    public IRegistry newRegistryInstance() {
        return new NacosRegistry(grpcRegistryProperties);
    }
}
