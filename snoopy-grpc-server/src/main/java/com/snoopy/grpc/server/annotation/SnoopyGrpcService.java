package com.snoopy.grpc.server.annotation;

import io.grpc.ServerInterceptor;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @author :   kehanjiang
 * @date :   2021/10/4  10:58
 * <p>
 * See https://blog.csdn.net/liang100k/article/details/79515910?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-1.no_search_link&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-1.no_search_link
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface SnoopyGrpcService {
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
     * 是否添加服务端全局拦截器
     *
     * @return
     */
    boolean applyGlobalServerInterceptors() default true;

    /**
     * 服务端拦截器
     * The first interceptor will have its {@link ServerInterceptor#interceptCall} called first.
     *
     * @return
     */

    Class<? extends ServerInterceptor>[] interceptors() default {};
}
