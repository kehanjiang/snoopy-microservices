package com.snoopy.grpc.base.registry.zookeeper;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.registry.IRegistry;
import com.snoopy.grpc.base.registry.IRegistryProvider;
import org.I0Itec.zkclient.ZkClient;

/**
 * @author :   kehanjiang
 * @date :   2021/12/1  15:44
 */
public class ZookeeperRegistryProvider implements IRegistryProvider {
    private static final int sessionTimeout = 60000;
    private static final int connectionTimeout = 60000;

    private GrpcRegistryProperties grpcRegistryProperties;

    public ZookeeperRegistryProvider(GrpcRegistryProperties grpcRegistryProperties) {
        this.grpcRegistryProperties = grpcRegistryProperties;
    }

    @Override
    public IRegistry newRegistryInstance() {
        ZkClient zkClient = new ZkClient(grpcRegistryProperties.getAddress(), sessionTimeout, connectionTimeout);
        return new ZookeeperRegistry(zkClient, grpcRegistryProperties);
    }
}
