package com.snoopy.grpc.base.registry.direct;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.registry.IRegistry;
import com.snoopy.grpc.base.registry.IRegistryProvider;

/**
 * @author :   kehanjiang
 * @date :   2021/12/1  15:44
 */
public class DirectRegistryProvider implements IRegistryProvider {
    public static final String REGISTRY_PROTOCOL_DIRECT = "direct";

    @Override
    public IRegistry newRegistryInstance(GrpcRegistryProperties grpcRegistryProperties) {
        return new DirectRegistry(grpcRegistryProperties);
    }

    @Override
    public String registryType() {
        return REGISTRY_PROTOCOL_DIRECT;
    }
}
