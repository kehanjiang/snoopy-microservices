package com.snoopy.grpc.base.registry.direct;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.registry.IRegistry;
import com.snoopy.grpc.base.registry.IRegistryProvider;

/**
 * @author :   kehanjiang
 * @date :   2021/12/1  15:44
 */
public class DirectRegistryProvider implements IRegistryProvider {
    private GrpcRegistryProperties grpcRegistryProperties;

    public DirectRegistryProvider(GrpcRegistryProperties grpcRegistryProperties) {
        this.grpcRegistryProperties = grpcRegistryProperties;
    }

    @Override
    public IRegistry newRegistryInstance() {
        return new DirectRegistry(grpcRegistryProperties);
    }
}
