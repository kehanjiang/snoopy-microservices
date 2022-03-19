package com.snoopy.grpc.client.balance.weight;

import io.grpc.LoadBalancer;
import io.grpc.LoadBalancerProvider;
import io.grpc.NameResolver;

import java.util.Map;

/**
 * @author :   kehanjiang
 * @date :   2021/12/10  15:59
 */
public class WeightRandomLoadBalancerProvider extends LoadBalancerProvider {

    private static final String NO_CONFIG = "no service config";


    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public String getPolicyName() {
        return "weight_random";
    }

    @Override
    public LoadBalancer newLoadBalancer(LoadBalancer.Helper helper) {
        return new WeightRandomLoadBalancer(helper);
    }

    @Override
    public NameResolver.ConfigOrError parseLoadBalancingPolicyConfig(
            Map<String, ?> rawLoadBalancingPolicyConfig) {
        return NameResolver.ConfigOrError.fromConfig(NO_CONFIG);
    }
}
