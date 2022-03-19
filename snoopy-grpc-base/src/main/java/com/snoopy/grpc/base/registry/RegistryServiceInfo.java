package com.snoopy.grpc.base.registry;


import com.snoopy.grpc.base.constans.GrpcConstants;


/**
 * @author :   kehanjiang
 * @date :   2021/11/29  16:23
 */
public class RegistryServiceInfo {
    private String namespace;
    private String alias;
    private String protocol;
    private String host;
    private int port;
    private int weight;

    public RegistryServiceInfo(String namespace, String alias, String protocol, String host, int port, int weight) {
        this.namespace = namespace;
        this.alias = alias;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.weight = weight;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getServerPortWeightStr() {
        return host + ":" + port+"["+weight+"]";
    }

    public String getPath() {
        StringBuilder stringBuilder = new StringBuilder(GrpcConstants.BASE_PATH);
        stringBuilder.append(GrpcConstants.PATH_SEPARATOR);
        stringBuilder.append(namespace);
        stringBuilder.append(GrpcConstants.PATH_SEPARATOR);
        stringBuilder.append(alias);
        return stringBuilder.toString();
    }


    public String getFullPath() {
        StringBuilder stringBuilder = new StringBuilder(protocol);
        stringBuilder.append("://");
        stringBuilder.append(getServerPortWeightStr());
        stringBuilder.append(GrpcConstants.BASE_PATH);
        stringBuilder.append(GrpcConstants.PATH_SEPARATOR);
        stringBuilder.append(namespace);
        stringBuilder.append(GrpcConstants.PATH_SEPARATOR);
        stringBuilder.append(alias);
        return stringBuilder.toString();
    }

}
