package com.snoopy.grpc.server.inercept;

import com.snoopy.grpc.server.annotation.SnoopyGrpcGlobalServerInterceptor;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * @author :   kehanjiang
 * @date :   2021/11/10  10:48
 */
@SnoopyGrpcGlobalServerInterceptor
public class DefaultGlobalServerInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        //全局拦截器
        return next.startCall(call, headers);
    }
}
