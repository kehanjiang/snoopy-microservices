package com.snoopy.grpc.server.factory;

import com.google.common.net.InetAddresses;
import com.snoopy.grpc.base.utils.LoggerBaseUtil;
import com.snoopy.grpc.server.configure.GrpcServerProperties;
import com.snoopy.grpc.server.processor.GrpcServiceProcess;
import com.snoopy.grpc.server.processor.IGrpcProcess;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :   kehanjiang
 * @date :   2021/10/4  10:07
 */
public class NettyServerFactory {
    private String address;
    private int port;
    private Map<String, IGrpcProcess> grpcProcessMap = new HashMap<>();

    public NettyServerFactory(GrpcServerProperties grpcServerProperties, List<IGrpcProcess> processes) {
        this.address = grpcServerProperties.getAddress();
        this.port = grpcServerProperties.getPort();
        if (null != processes) {
            processes.sort(new Comparator<IGrpcProcess>() {
                @Override
                public int compare(IGrpcProcess o1, IGrpcProcess o2) {
                    return o1.getOrder() - o2.getOrder();
                }
            });
            processes.stream().forEach(process -> grpcProcessMap.put(process.getClass().getSimpleName(), process));
        }
    }

    public Server createServer() {
        NettyServerBuilder builder = NettyServerBuilder.forAddress(new InetSocketAddress(InetAddresses.forString(address), port));

        for (IGrpcProcess process : grpcProcessMap.values()) {
            process.handle(builder);
        }

        LoggerBaseUtil.info(this, "gRPC Server started, listening on address: {}, port:{}", address, port);
        return builder.build();
    }

    public void removeAllRegistryServices() {
        GrpcServiceProcess grpcServiceProcess = (GrpcServiceProcess) grpcProcessMap.get(GrpcServiceProcess.class.getSimpleName());
        grpcServiceProcess.removeAllRegistryService();
    }

}
