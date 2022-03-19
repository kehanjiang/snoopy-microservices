package com.snoopy.grpc.server.reflection;

import com.google.protobuf.Descriptors;
import io.grpc.MethodDescriptor;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.protobuf.ProtoFileDescriptorSupplier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :   kehanjiang
 * @date :   2022/1/16  17:02
 */
public class SnoopyServiceUtil {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ServerServiceDefinition rename(ServerServiceDefinition _serviceDefinition,
                                                 String serviceAlias) {
        String serviceName = _serviceDefinition.getServiceDescriptor().getName();
        int index = serviceAlias.lastIndexOf('.');
        if (index == -1) {
            //--别名不规范直接抛出异常
            throw new IllegalArgumentException("Service alias(" + serviceAlias
                    + ") is illegal,Please use packageName.serviceName format");
        }
        ServiceAliasDescriptorSupplier aliasDescriptorSupplier = null;
        ServiceDescriptor _svcDescriptor = _serviceDefinition.getServiceDescriptor();
        if (_svcDescriptor.getSchemaDescriptor() instanceof ProtoFileDescriptorSupplier) {
            Descriptors.FileDescriptor fileDescriptor =
                    ((ProtoFileDescriptorSupplier) _svcDescriptor.getSchemaDescriptor())
                            .getFileDescriptor();
            aliasDescriptorSupplier = new ServiceAliasDescriptorSupplier(serviceAlias,
                    serviceName, fileDescriptor);
        }
        ServiceDescriptor.Builder svcDesBuilder = ServiceDescriptor.newBuilder(serviceAlias);
        List<ServerMethodDefinition> methodDefinitions = new ArrayList<>();
        for (ServerMethodDefinition _serverMethod : _serviceDefinition.getMethods()) {
            MethodDescriptor _methodDescriptor = _serverMethod.getMethodDescriptor();
            ProtoMethodName protoMethodName =
                    ProtoMethodName.parseFullGrpcMethodName(_methodDescriptor.getFullMethodName());
            String fullMethodName = MethodDescriptor.generateFullMethodName(serviceAlias,
                    protoMethodName.getMethodName());
            MethodDescriptor methodDescriptor = MethodDescriptor.newBuilder(_methodDescriptor.getRequestMarshaller(),
                    _methodDescriptor.getResponseMarshaller())
                    .setFullMethodName(fullMethodName)
                    .setIdempotent(_methodDescriptor.isIdempotent())
                    .setSafe(_methodDescriptor.isSafe())
                    .setSampledToLocalTracing(_methodDescriptor.isSampledToLocalTracing())
                    .setType(_methodDescriptor.getType())
                    .setSchemaDescriptor(new MethodAliasDescriptorSupplier(protoMethodName.getMethodName(),
                            aliasDescriptorSupplier))
                    .build();
            svcDesBuilder.addMethod(methodDescriptor);

            methodDefinitions.add(ServerMethodDefinition.create(methodDescriptor, _serverMethod.getServerCallHandler()));
        }

        svcDesBuilder.setSchemaDescriptor(aliasDescriptorSupplier);

        ServerServiceDefinition.Builder svcDefBuilder = ServerServiceDefinition.builder(svcDesBuilder.build());
        methodDefinitions.forEach(svcDefBuilder::addMethod);

        return svcDefBuilder.build();
    }
}
