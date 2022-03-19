package com.snoopy.grpc.base.registry;


/**
 * @author :   kehanjiang
 * @date :   2021/11/29  16:03
 */
public interface IRegistry {
    /**
     * 注册服务
     *
     * @param serviceInfo
     */
    void register(RegistryServiceInfo serviceInfo);

    /**
     * 注销服务
     *
     * @param serviceInfo
     */
    void unregister(RegistryServiceInfo serviceInfo);


    /**
     * 监听服务列表
     * @param serviceInfo  监听服务
     * @param subscribeCallback   服务地址列表变更回调
     */
    void subscribe(RegistryServiceInfo serviceInfo, ISubscribeCallback subscribeCallback);


    /**
     * 注销监听
     *
     * @param serviceInfo
     */
    void unsubscribe(RegistryServiceInfo serviceInfo);


}
