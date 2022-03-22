package com.snoopy.grpc.client.annotation;

import io.grpc.ClientInterceptor;

import java.lang.annotation.*;

/**
 * @author :   kehanjiang
 * @date :   2021/10/4  10:58
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Documented
public @interface SnoopyGrpcClient {
    /**
     * stub类型  {@link StubType}
     *
     * @return
     */
    StubType type() default StubType.ASYNC;

    /**
     * 命名空间
     *
     * @return
     */
    String namespace() default "Default";

    /**
     * 别名
     *
     * @return
     */
    String alias();

    /**
     * 是否添加客户端全局拦截器
     *
     * @return
     */
    boolean applyGlobalClientInterceptors() default true;

    /**
     * 客户端拦截器
     *
     * @return
     */
    Class<? extends ClientInterceptor>[] interceptors() default {};
}
