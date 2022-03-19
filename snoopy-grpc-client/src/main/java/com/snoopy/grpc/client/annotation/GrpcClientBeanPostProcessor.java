package com.snoopy.grpc.client.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author :   kehanjiang
 * @date :   2021/12/5  12:09
 */
public class GrpcClientBeanPostProcessor implements BeanPostProcessor {
    private ConfigurableListableBeanFactory configurableListableBeanFactory;

    public GrpcClientBeanPostProcessor(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(SnoopyGrpcClient.class)) {
                ReflectionUtils.doWithFields(clazz, new GrpcClientFieldCallback(configurableListableBeanFactory, bean));
            }
        }
        return bean;
    }

}
