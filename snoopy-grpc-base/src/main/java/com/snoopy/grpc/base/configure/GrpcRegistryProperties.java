package com.snoopy.grpc.base.configure;


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author :   kehanjiang
 * @date :   2021/10/5  10:22
 */
public class GrpcRegistryProperties {

    private String protocol = "direct";

    private String address = "";

    private String username = "";

    private String password = "";

    private Map<String, Object> extra = new HashMap<>();


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

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public Object getExtra(String name) {
        return this.extra.get(name);
    }

    public void setExtra(String name, Object value) {
        if (!StringUtils.isEmpty(name) && value != null && !StringUtils.isEmpty(String.valueOf(value))) {
            this.extra.put(name, value);
        }
    }

    public boolean hasExtra(String name) {
        return getExtra(name) != null && StringUtils.isNotBlank(String.valueOf(getExtra(name)));
    }

}
