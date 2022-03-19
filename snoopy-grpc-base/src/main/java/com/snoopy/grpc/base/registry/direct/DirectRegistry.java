package com.snoopy.grpc.base.registry.direct;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.registry.IRegistry;
import com.snoopy.grpc.base.registry.ISubscribeCallback;
import com.snoopy.grpc.base.registry.RegistryServiceInfo;
import com.snoopy.grpc.base.utils.NetUtil;
import org.springframework.util.SocketUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author :   kehanjiang
 * @date :   2021/12/1  15:18
 */
public class DirectRegistry implements IRegistry {

    private String[] registryAddress;

    public DirectRegistry(GrpcRegistryProperties grpcRegistryProperties) {
        String address = grpcRegistryProperties.getAddress();
        address = StringUtils.isEmpty(address) ? (NetUtil.LOOPBACK_ADDRESS + ":"+ SocketUtils.findAvailableTcpPort(10000,60000)+"[5]") : address;
        this.registryAddress = address.split(",");
    }


    @Override
    public void subscribe(RegistryServiceInfo serviceInfo, ISubscribeCallback subscribeCallback) {
        List<RegistryServiceInfo> serviceInfoList = Arrays.stream(registryAddress).map(x -> {
            String[] serverAndPort = x.split(":");
            String server=serverAndPort[0];
            int port=Integer.parseInt(serverAndPort[1].split("\\[")[0]);
            int weight = Integer.parseInt(serverAndPort[1].split("\\[").length>1
                    ?serverAndPort[1].split("\\[")[1].replace("]", "")
                    :"5");
            return new RegistryServiceInfo(serviceInfo.getNamespace(),
                    serviceInfo.getAlias(),
                    serviceInfo.getProtocol(),
                    server,
                    port,
                    weight);
        }).collect(Collectors.toList());
        subscribeCallback.handle(serviceInfoList);
    }

    @Override
    public void unsubscribe(RegistryServiceInfo serviceInfo) {

    }

    @Override
    public void register(RegistryServiceInfo serviceInfo) {

    }

    @Override
    public void unregister(RegistryServiceInfo serviceInfo) {

    }

}
