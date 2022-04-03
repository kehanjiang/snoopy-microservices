package com.snoopy.grpc.base.configure;


/**
 * @author :   kehanjiang
 * @date :   2021/10/5  10:22
 */
public class GrpcRegistryProperties {

    private String protocol = "direct";

    private String address = "";

    private String username = "";

    private String password = "";

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
