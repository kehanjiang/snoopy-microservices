package com.snoopy.grpc.base.registry;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;

/**
 * @author :   kehanjiang
 * @date :   2021/11/29  16:03
 */
public interface IRegistryProvider {
    /**
     * 创建注册中心实例
     *
     * @return
     */
    IRegistry newRegistryInstance(GrpcRegistryProperties grpcRegistryProperties);

    /**
     * 支持的注册中心类型
     *
     * @return
     */
    String registryType();
}