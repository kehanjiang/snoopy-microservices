package com.snoopy.grpc.server.reflection;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author :   kehanjiang
 * @date :   2021/11/9  16:57
 */
public class ServiceAliasDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier,
        io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAliasDescriptorSupplier.class);

    private String fullSvcAlias;
    private String fullSvcName;
    private Descriptors.FileDescriptor fileDescriptor;
    private Descriptors.FileDescriptor aliasFileDescriptor;

    public ServiceAliasDescriptorSupplier(String fullSvcAlias, String fullSvcName,
                                          Descriptors.FileDescriptor fileDescriptor) {
        this.fullSvcAlias = fullSvcAlias;
        this.fullSvcName = fullSvcName;
        this.fileDescriptor = fileDescriptor;
    }

    @Override
    public Descriptors.ServiceDescriptor getServiceDescriptor() {
        Descriptors.FileDescriptor descriptor = getFileDescriptor();
        if (descriptor != null)
            return descriptor.findServiceByName(ProtoMethodName.extractServiceName(fullSvcAlias));
        return null;
    }

    @Override
    public Descriptors.FileDescriptor getFileDescriptor() {
        try {
            if (aliasFileDescriptor == null) {
                DescriptorProtos
                        .FileDescriptorProto.Builder protoBuilder = DescriptorProtos
                        .FileDescriptorProto.newBuilder()
                        //.mergeFrom(fileDescriptor.toProto())
                        .setName(fullSvcAlias + ".proto");
                int idxPackage = fullSvcAlias.lastIndexOf('.');
                if (idxPackage > 0) {
                    protoBuilder.setPackage(fullSvcAlias.substring(0, idxPackage));
                }
                protoBuilder.addDependency(fileDescriptor.getName());
                Descriptors.ServiceDescriptor svcDec =
                        fileDescriptor.findServiceByName(ProtoMethodName.extractServiceName(fullSvcName));
                if (svcDec != null) {
                    protoBuilder.clearService();
                    protoBuilder.addService(DescriptorProtos.ServiceDescriptorProto.newBuilder()
                            .mergeFrom(svcDec.toProto())
                            .setName(ProtoMethodName.extractServiceName(fullSvcAlias)));
                }
                //Descriptors.FileDescriptor[] dependencies =
                //        fileDescriptor.getDependencies().toArray(new Descriptors.FileDescriptor[fileDescriptor
                // .getDependencies().size()]);
                aliasFileDescriptor = Descriptors.FileDescriptor.buildFrom(protoBuilder.build(),
                        new Descriptors.FileDescriptor[]{fileDescriptor});
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return aliasFileDescriptor;
    }
}
