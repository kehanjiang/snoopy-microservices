package com.snoopy.grpc.server.processor;


import com.snoopy.grpc.server.configure.GrpcServerProperties;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;


/**
 * 添加反射服务
 *
 * @author :   kehanjiang
 * @date :   2021/11/9  16:57
 */
public class GrpcReflectionProcess implements IGrpcProcess {

    private GrpcServerProperties grpcServerProperties;

    public GrpcReflectionProcess(GrpcServerProperties grpcServerProperties) {
        this.grpcServerProperties = grpcServerProperties;
    }

    @Override
    public void handle(NettyServerBuilder builder) {
        if (grpcServerProperties.isUseProtoReflection()) {
            builder.addService(ProtoReflectionService.newInstance());
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }


}
