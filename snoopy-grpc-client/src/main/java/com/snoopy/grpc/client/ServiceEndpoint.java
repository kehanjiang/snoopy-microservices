package com.snoopy.grpc.client;

import io.grpc.ManagedChannel;

/**
 * @author :   kehanjiang
 * @date :   2022/4/16  13:24
 */
public interface ServiceEndpoint<T> {
    Class<T> getSvcClass();

    String getChannelId();

    ManagedChannel getChannel() ;
}
