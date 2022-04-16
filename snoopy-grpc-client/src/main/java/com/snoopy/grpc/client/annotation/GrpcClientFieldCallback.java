package com.snoopy.grpc.client.annotation;


import com.snoopy.grpc.base.constans.GrpcConstants;
import com.snoopy.grpc.client.SnoopyServiceEndpoint;
import com.snoopy.grpc.client.SnoopyStubManager;
import com.snoopy.grpc.client.configure.GrpcClientProperties;
import io.grpc.ClientInterceptor;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author :   kehanjiang
 * @date :   2021/12/5  13:13
 */
public class GrpcClientFieldCallback implements ReflectionUtils.FieldCallback {
    private GrpcClientProperties grpcClientProperties;
    private ConfigurableListableBeanFactory configurableListableBeanFactory;
    private Object bean;

    public GrpcClientFieldCallback(GrpcClientProperties grpcClientProperties, ConfigurableListableBeanFactory cbf, Object bean) {
        this.grpcClientProperties = grpcClientProperties;
        this.configurableListableBeanFactory = cbf;
        this.bean = bean;
    }

    @Override
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        try {
            Class<?> clazz = field.getType();
            String beanName = clazz.getName();
            if (!configurableListableBeanFactory.containsBean(beanName)) {
                SnoopyGrpcClient annotation = AnnotationUtils.findAnnotation(field, SnoopyGrpcClient.class);
                List<ClientInterceptor> privateInterceptors = new ArrayList<>();
                for (Class<? extends ClientInterceptor> clz : annotation.interceptors()) {
                    privateInterceptors.add(loadPrivateInterceptorBean(clz));
                }
                boolean applyGlobalClientInterceptors = annotation.applyGlobalClientInterceptors();

                StubType type = annotation.type();
                String namespace = annotation.namespace();
                if (StringUtils.hasText(grpcClientProperties.getNamespace())) {
                    namespace = grpcClientProperties.getNamespace();
                }
                namespace = StringUtils.hasText(namespace) ? namespace : GrpcConstants.DEFAULT_NAMESPACE;
                String alias = annotation.alias();

                Class serviceClass = null;

                String serviceClassName = clazz.getCanonicalName()
                        .subSequence(0, clazz.getCanonicalName().length() - clazz.getSimpleName().length() - 1)
                        .toString();
                serviceClass = Class.forName(serviceClassName);

                SnoopyServiceEndpoint serviceEndpoint = new SnoopyServiceEndpoint(
                        namespace,
                        alias,
                        serviceClass,
                        configurableListableBeanFactory,
                        applyGlobalClientInterceptors,
                        privateInterceptors
                );

                AbstractStub<?> newStubClass;
                switch (type) {
                    case ASYNC:
                        newStubClass = SnoopyStubManager.getInstance().newStub(serviceEndpoint);
                        break;
                    case BLOCKING:
                        newStubClass = SnoopyStubManager.getInstance().newBlockingStub(serviceEndpoint);
                        break;
                    case FUTURE:
                        newStubClass = SnoopyStubManager.getInstance().newFutureStub(serviceEndpoint);
                        break;
                    default:
                        throw new RuntimeException("StubTyp is unSupport!");
                }
                //注册bean
                DefaultListableBeanFactory springFactory = (DefaultListableBeanFactory) configurableListableBeanFactory;
                springFactory.registerSingleton(beanName, newStubClass);
            }

            //设置字段可访问
            ReflectionUtils.makeAccessible(field);
            //将指定对象变量上此 Field 对象表示的字段设置为指定的新值
            Object stubBeanInstance = configurableListableBeanFactory.getBean(beanName);
            field.set(bean, stubBeanInstance);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private <T> T loadPrivateInterceptorBean(Class clazz) {
        try {
            if (configurableListableBeanFactory.getBeanNamesForType(clazz).length > 0) {
                return (T) configurableListableBeanFactory.getBean(clazz);
            } else {
                return (T) clazz.newInstance();
            }
        } catch (Exception e) {
            throw new BeanCreationException("Failed to load  private interceptor bean .", e);
        }
    }
}
