package com.snoopy.grpc.base.registry;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author :   kehanjiang
 * @date :   2021/12/4  10:31
 */
public class RegistryProviderFactory {

    private static IRegistryProvider currentRegistryProvider;

    public RegistryProviderFactory(GrpcRegistryProperties registryProperties) {
        Map<String, IRegistryProvider> registryProviderMap = new HashMap<>();
        ServiceLoader<IRegistryProvider> serviceLoader = ServiceLoader.load(IRegistryProvider.class);
        Iterator<IRegistryProvider> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            IRegistryProvider registryProvider = iterator.next();
            registryProviderMap.put(registryProvider.registryType(), registryProvider);
        }
        currentRegistryProvider = registryProviderMap.get(registryProperties.getProtocol());
    }

    public IRegistryProvider newRegistryProviderInstance() {
        return currentRegistryProvider;
    }

}
