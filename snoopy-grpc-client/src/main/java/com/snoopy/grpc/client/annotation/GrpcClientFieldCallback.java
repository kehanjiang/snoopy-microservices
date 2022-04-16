package com.snoopy.grpc.client.annotation;


import com.snoopy.grpc.base.constans.GrpcConstants;
import com.snoopy.grpc.client.configure.GrpcClientProperties;
import io.grpc.ClientInterceptor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
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

    protected static Set<ClientInterceptor> globalClientInterceptors = new HashSet<>();

    public GrpcClientFieldCallback(GrpcClientProperties grpcClientProperties, ConfigurableListableBeanFactory cbf, Object bean) {
        this.grpcClientProperties = grpcClientProperties;
        this.configurableListableBeanFactory = cbf;
        this.bean = bean;
        Collection<String> beanNames = Arrays.asList(
                configurableListableBeanFactory.getBeanNamesForAnnotation(SnoopyGrpcGlobalClientInterceptor.class)
        );
        for (String beanName : beanNames) {
            ClientInterceptor globalClientInterceptor = (ClientInterceptor) configurableListableBeanFactory.getBean(beanName);
            globalClientInterceptors.add(globalClientInterceptor);
        }
    }

    @Override
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        SnoopyGrpcClient annotation = AnnotationUtils.findAnnotation(field, SnoopyGrpcClient.class);
        List<ClientInterceptor> privateInterceptors = new ArrayList<>();
        for (Class<? extends ClientInterceptor> clazz : annotation.interceptors()) {
            privateInterceptors.add(loadPrivateInterceptorBean(clazz));
        }

        if (annotation.applyGlobalClientInterceptors()) {
            privateInterceptors.addAll(globalClientInterceptors);
        }

        Class<?> clazz = field.getType();
        String beanName = clazz.getName();
        StubType type = annotation.type();
        String namespace = annotation.namespace();
        if (StringUtils.hasText(grpcClientProperties.getNamespace())) {
            namespace = grpcClientProperties.getNamespace();
        }
        namespace = StringUtils.hasText(namespace) ? namespace : GrpcConstants.DEFAULT_NAMESPACE;

        String alias = annotation.alias();

        Object stubBeanInstance = configurableListableBeanFactory.containsBean(beanName)
                ? configurableListableBeanFactory.getBean(beanName)
                : GrpcClientStubBeanManager.getInstance()
                .newStubBean(null, 0, clazz, beanName, type, namespace, alias, privateInterceptors, configurableListableBeanFactory);

        //设置字段可访问
        ReflectionUtils.makeAccessible(field);
        //将指定对象变量上此 Field 对象表示的字段设置为指定的新值
        field.set(bean, stubBeanInstance);

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
