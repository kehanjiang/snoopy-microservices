package com.snoopy.grpc.base.registry.zookeeper;

/**
 * @author :   kehanjiang
 * @date :   2021/12/1  16:02
 */
public enum ZookeeperNodeType {

    SERVER("server"),
    CLIENT("client"),
    ;
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    ZookeeperNodeType(String value) {
        this.value = value;
    }
}
