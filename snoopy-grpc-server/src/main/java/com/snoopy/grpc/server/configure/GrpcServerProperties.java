package com.snoopy.grpc.server.configure;


import com.snoopy.grpc.base.utils.NetUtil;
import org.springframework.util.SocketUtils;
import org.springframework.util.StringUtils;

/**
 * @author :   kehanjiang
 * @date :   2021/10/5  10:22
 */
public class GrpcServerProperties {
    private String namespace;

    private String address = "";

    private static int port = 0;

    private int weight = 5;

    private boolean usePlaintext = false;

    private boolean useProtoReflection = true;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAddress() {
        return (StringUtils.isEmpty(address)
                || NetUtil.LOCALHOST_ADDRESS.equalsIgnoreCase(address)
                || NetUtil.LOOPBACK_ADDRESS.equals(address)
        ) ? NetUtil.getLocalIpAddress() : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        if (port == 0) {
            port = SocketUtils.findAvailableTcpPort(10000, 60000);
        }
        return port;
    }

    public void setPort(int newPort) {
        port = newPort;
    }

    public boolean isUsePlaintext() {
        return usePlaintext;
    }

    public void setUsePlaintext(boolean usePlaintext) {
        this.usePlaintext = usePlaintext;
    }

    public int getWeight() {
        if (weight < 1 || weight > 10) {
            throw new RuntimeException("权重取值范围区间：整数1-10");
        }
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isUseProtoReflection() {
        return useProtoReflection;
    }

    public void setUseProtoReflection(boolean useProtoReflection) {
        this.useProtoReflection = useProtoReflection;
    }
}
