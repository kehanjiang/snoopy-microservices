package com.snoopy.grpc.base.registry;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.constans.GrpcConstants;
import com.snoopy.grpc.base.registry.consul.ConsulRegistryProvider;
import com.snoopy.grpc.base.registry.direct.DirectRegistryProvider;
import com.snoopy.grpc.base.registry.etcd.EtcdRegistryProvider;
import com.snoopy.grpc.base.registry.nacos.NacosRegistryProvider;
import com.snoopy.grpc.base.registry.zookeeper.ZookeeperRegistryProvider;

/**
 * @author :   kehanjiang
 * @date :   2021/12/4  10:31
 */
public class RegistryProviderFactory {

    private IRegistryProvider registryProvider;

    public RegistryProviderFactory(GrpcRegistryProperties registryProperties) {
        switch (registryProperties.getProtocol()) {
            case GrpcConstants.REGISTRY_PROTOCOL_DIRECT:
                registryProvider = new DirectRegistryProvider(registryProperties);
                break;
            case GrpcConstants.REGISTRY_PROTOCOL_CONSUL:
                registryProvider = new ConsulRegistryProvider(registryProperties);
                break;
            case GrpcConstants.REGISTRY_PROTOCOL_NACOS:
                registryProvider = new NacosRegistryProvider(registryProperties);
                break;
            case GrpcConstants.REGISTRY_PROTOCOL_ETCD:
                registryProvider = new EtcdRegistryProvider(registryProperties);
                break;
            case GrpcConstants.REGISTRY_PROTOCOL_ZOOKEEPER:
                registryProvider = new ZookeeperRegistryProvider(registryProperties);
                break;
            default:
                throw new RuntimeException(registryProperties.getProtocol() + " protocol is unSupport !");
        }
    }

    public IRegistryProvider newRegistryProviderInstance() {
        return registryProvider;
    }

}
