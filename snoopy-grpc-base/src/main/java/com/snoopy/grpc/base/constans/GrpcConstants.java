package com.snoopy.grpc.base.constans;

import io.grpc.Attributes;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author :   kehanjiang
 * @date :   2021/12/4  9:43
 */
public final class GrpcConstants {
    public static final String BASE_PATH = "/snoopy";
    public static final String PATH_SEPARATOR = "/";
    public static final String PROTOCOL_SEPARATOR = "://";

    /**
     * 默认命名空间
     */
    public static final String DEFAULT_NAMESPACE = "Default";

    /**
     * 服务默认权重
     */
    public static final int DEFAULT_WEIGHT = 5;

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

    /**
     * 分隔符
     */
    public static final Pattern QUERY_PARAM_PATTERN = Pattern.compile("\\s*[&]+\\s*");
    public static final String EQUAL_SIGN_SEPERATOR = "=";

    /**
     * 编码格式
     */
    public static final String DEFAULT_CHARACTER = "utf-8";

    /**
     * RegistryServiceInfo 参数
     */
    public static final String PARAMETER_WEIGHT = "weight";
    public static final String PARAMETER_TAG = "tag";
}
