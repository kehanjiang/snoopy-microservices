package com.snoopy.grpc.base.registry;

import java.util.List;

/**
 * @author :   kehanjiang
 * @date :   2021/12/30  14:24
 */
public interface ISubscribeCallback {
    void handle(List<RegistryServiceInfo> registryServiceInfoList);
}
