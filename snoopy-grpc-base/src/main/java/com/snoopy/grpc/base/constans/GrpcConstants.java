package com.snoopy.grpc.base.constans;

import io.grpc.Attributes;

import java.util.Map;

/**
 * @author :   kehanjiang
 * @date :   2021/12/4  9:43
 */
public final class GrpcConstants {
    public static final String BASE_PATH = "/snoopy";
    public static final String PATH_SEPARATOR = "/";

    /**
     * 客户端默认负载均衡策略
     */
    public static final String DEFAULT_LOAD_BALANCING_POLICY_ROUND_ROBIN = "round_robin";

    /**
     * 通信协议 http 、https
     */
    public static final String PROTOCOL_HTTP2_PLAIN = "http";
    public static final String PROTOCOL_HTTP2_SSL = "https";

    /**
     * grpc监听的额外信息（用于存放服务权重列表）
     */
    public static final Attributes.Key<Map<String, Integer>> WEIGHT_LIST_KEY = Attributes.Key.create("weightList");

}
