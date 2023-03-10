package com.snoopy.grpc.base.configure;


import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author :   kehanjiang
 * @date :   2021/10/5  10:22
 */
public class GrpcRegistryProperties extends GrpcBaseProperties{

    private String protocol = "direct";

    private String address = "";

    private String username = "";

    private String password = "";

    private Map<String, String> extra = new HashMap<>();


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

    public Map<String, String> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }

    public String getExtra(String name) {
        return this.extra.get(name);
    }

    public void setExtra(String name, String value) {
        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(value)) {
            this.extra.put(name, value);
        }
    }

    public boolean hasExtra(String name) {
        return StringUtils.hasText(getExtra(name));
    }

    public String getExtra(String name, String defaultValue) {
        String value = this.getExtra(name);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    public Boolean getBooleanExtra(String name, boolean defaultValue) {
        String value = extra.get(name);
        return StringUtils.hasText(value) ? Boolean.parseBoolean(value) : defaultValue;
    }

    public Integer getIntExtra(String name, int defaultValue) {
        String value = extra.get(name);
        return StringUtils.hasText(value) ? Integer.parseInt(value) : defaultValue;
    }

    public Long getLongExtra(String name, long defaultValue) {
        String value = extra.get(name);
        return StringUtils.hasText(value) ? Long.parseLong(value) : defaultValue;
    }

    public File getFileExtra(String name) {
        try {
            return ResourceUtils.getFile(name);
        } catch (Exception e) {
            return null;
        }
    }

}
