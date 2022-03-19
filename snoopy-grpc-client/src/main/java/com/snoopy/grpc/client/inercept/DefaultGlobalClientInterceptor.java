package com.snoopy.grpc.client.inercept;

import com.snoopy.grpc.client.annotation.SnoopyGrpcGlobalClientInterceptor;
import io.grpc.*;

/**
 * @author :   kehanjiang
 * @date :   2021/11/10  10:48
 */
@SnoopyGrpcGlobalClientInterceptor
public class DefaultGlobalClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return next.newCall(method,callOptions);
    }
}
