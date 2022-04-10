package com.snoopy.grpc.base.configure;


import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * @author :   kehanjiang
 * @date :   2022/4/10  15:27
 */
public abstract class GrpcBaseProperties implements EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getProperty(String key) {
        return environment.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    public <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }

    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return environment.getProperty(key, targetType, defaultValue);
    }

}
