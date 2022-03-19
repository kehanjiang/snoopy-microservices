package com.snoopy.grpc.base.registry.eureka;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.registry.IRegistry;
import com.snoopy.grpc.base.registry.ISubscribeCallback;
import com.snoopy.grpc.base.registry.RegistryServiceInfo;

/**
 * @author :   kehanjiang
 * @date :   2021/12/1  15:18
 */
public class EurekaRegistry implements IRegistry {

    public EurekaRegistry(GrpcRegistryProperties grpcRegistryProperties) {
    }


    @Override
    public void subscribe(RegistryServiceInfo serviceInfo, ISubscribeCallback subscribeCallback) {

    }

    @Override
    public void unsubscribe(RegistryServiceInfo serviceInfo) {

    }

    @Override
    public void register(RegistryServiceInfo serviceInfo) {

    }

    @Override
    public void unregister(RegistryServiceInfo serviceInfo) {

    }

}
