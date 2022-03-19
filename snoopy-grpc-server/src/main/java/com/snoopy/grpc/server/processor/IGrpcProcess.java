package com.snoopy.grpc.server.processor;

import io.grpc.netty.NettyServerBuilder;

/**
 * @author :   kehanjiang
 * @date :   2021/11/9  16:46
 */
public interface IGrpcProcess {

    void handle(NettyServerBuilder builder);

    int getOrder();
}
