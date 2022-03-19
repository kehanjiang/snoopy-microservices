package com.snoopy.grpc.base.registry.etcd;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.registry.IRegistry;
import com.snoopy.grpc.base.registry.IRegistryProvider;

/**
 * @author :   kehanjiang
 * @date :   2021/12/1  15:44
 */
public class EtcdRegistryProvider implements IRegistryProvider {
    private GrpcRegistryProperties grpcRegistryProperties;

    public EtcdRegistryProvider(GrpcRegistryProperties grpcRegistryProperties) {
        this.grpcRegistryProperties = grpcRegistryProperties;
    }

    @Override
    public IRegistry newRegistryInstance() {
        return new EtcdRegistry(grpcRegistryProperties);
    }
}
