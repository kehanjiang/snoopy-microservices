package com.snoopy.grpc.client.annotation;


import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @author :   kehanjiang
 * @date :   2021/10/4  10:58
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface SnoopyGrpcGlobalClientInterceptor {
}
