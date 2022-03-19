package com.snoopy.grpc.base.registry;


import com.snoopy.grpc.base.configure.GrpcSecurityProperties;
import com.snoopy.grpc.base.constans.GrpcConstants;
import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务发现类
 *
 * @author :   kehanjiang
 * @date :   2021/11/29  21:36
 */
public class RegistryNameResolver extends NameResolver implements ISubscribeCallback{

    private GrpcSecurityProperties grpcSecurityProperties;

    private Listener listener;

    private RegistryServiceInfo registryServiceInfo;
    private IRegistry registry;

    public RegistryNameResolver(GrpcSecurityProperties grpcSecurityProperties,
                                RegistryServiceInfo registryServiceInfo,
                                IRegistry registry) {
        this.grpcSecurityProperties = grpcSecurityProperties;
        this.registryServiceInfo = registryServiceInfo;
        this.registry = registry;
    }

    @Override
    public String getServiceAuthority() {
        return StringUtils.isEmpty(grpcSecurityProperties.getAuthority()) ? "" : grpcSecurityProperties.getAuthority();
    }

    @Override
    public void shutdown() {
        registry.unsubscribe(registryServiceInfo);
    }

    @Override
    public void start(Listener listener) {
        this.listener = listener;
        registry.subscribe(registryServiceInfo, this);
    }

    @Override
    public void handle(List<RegistryServiceInfo> registryServiceInfoList) {
        ArrayList<EquivalentAddressGroup> addressGroups = new ArrayList<EquivalentAddressGroup>();
        Attributes.Builder builder = Attributes.newBuilder();
        Map<String, Integer> weightMap = new HashMap<>();
        for (RegistryServiceInfo registryServiceInfo : registryServiceInfoList) {
            List<SocketAddress> socketAddresses = new ArrayList<>();
            socketAddresses.add(new InetSocketAddress(registryServiceInfo.getHost(), registryServiceInfo.getPort()));
            addressGroups.add(new EquivalentAddressGroup(socketAddresses));
            weightMap.put(registryServiceInfo.getHost() + ":" + registryServiceInfo.getPort(), registryServiceInfo.getWeight());
        }
        builder.set(GrpcConstants.WEIGHT_LIST_KEY, weightMap);
        listener.onAddresses(addressGroups, builder.build());
    }
}
