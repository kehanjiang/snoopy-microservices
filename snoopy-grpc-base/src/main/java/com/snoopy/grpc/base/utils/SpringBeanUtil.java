package com.snoopy.grpc.base.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * 获取spring容器管理的bean
 *
 * @author :   kehanjiang
 * @date :   2021/11/8  10:42
 */
public class SpringBeanUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static <T> T getBean(String bean) {
        return (T) applicationContext.getBean(bean);
    }

    public static <T> T getBean(Class classType) {
        return (T) applicationContext.getBean(classType);
    }

    public static <T> T getBean(String name, Class<T> requiredType) {
        return (T) applicationContext.getBean(name, requiredType);
    }

    /**
     * 动态注入单例bean实例
     *
     * @param beanName        bean名称
     * @param singletonObject 单例bean实例
     * @return 注入实例
     */
    public static Object registerSingletonBean(String beanName, Object singletonObject) {

        //将applicationContext转换为ConfigurableApplicationContext
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;

        //获取BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getAutowireCapableBeanFactory();

        //动态注册bean.
        defaultListableBeanFactory.registerSingleton(beanName, singletonObject);

        //获取动态注册的bean.
        return configurableApplicationContext.getBean(beanName);
    }

    /**
     * 动态注入bean
     *
     * @param requiredType 注入类
     * @param beanName     bean名称
     * @return
     */
    public static Object registerBean(Class<?> requiredType, String beanName) {

        //将applicationContext转换为ConfigurableApplicationContext
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;

        //获取BeanFactory
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getAutowireCapableBeanFactory();

        //创建bean信息.
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(requiredType);

        //动态注册bean.
        defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());

        //获取动态注册的bean.
        return configurableApplicationContext.getBean(requiredType);
    }

}
