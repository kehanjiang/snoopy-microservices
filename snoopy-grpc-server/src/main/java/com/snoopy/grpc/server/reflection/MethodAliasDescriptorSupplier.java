package com.snoopy.grpc.server.reflection;

import com.google.protobuf.Descriptors;
import io.grpc.protobuf.ProtoMethodDescriptorSupplier;
import io.grpc.protobuf.ProtoServiceDescriptorSupplier;

/**
 *
 * @author :   kehanjiang
 * @date :   2021/11/9  16:57
 */
public class MethodAliasDescriptorSupplier implements ProtoMethodDescriptorSupplier {

    private String methodName;
    private ProtoServiceDescriptorSupplier serviceDescriptorSupplier;

    public MethodAliasDescriptorSupplier(String methodName, ProtoServiceDescriptorSupplier serviceDescriptorSupplier){
        this.methodName = methodName;
        this.serviceDescriptorSupplier = serviceDescriptorSupplier;
    }
    @Override
    public Descriptors.MethodDescriptor getMethodDescriptor() {
        return serviceDescriptorSupplier.getServiceDescriptor().findMethodByName(methodName);
    }

    @Override
    public Descriptors.ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptorSupplier.getServiceDescriptor();
    }

    @Override
    public Descriptors.FileDescriptor getFileDescriptor() {
        return serviceDescriptorSupplier.getFileDescriptor();
    }
}
