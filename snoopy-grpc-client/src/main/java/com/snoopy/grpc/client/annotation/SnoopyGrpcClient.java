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
    String namespace();

    /**
     * 别名
     *
     * @return
     */
    String alias();

    /**
     * 客户端拦截器
     *
     * @return
     */
    Class<? extends ClientInterceptor>[] interceptors() default {};
}
