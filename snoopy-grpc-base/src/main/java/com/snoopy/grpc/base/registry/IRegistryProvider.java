package com.snoopy.grpc.base.registry;

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
    IRegistry newRegistryInstance();

}