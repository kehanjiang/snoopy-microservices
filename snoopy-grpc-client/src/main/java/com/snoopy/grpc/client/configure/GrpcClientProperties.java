package com.snoopy.grpc.client.configure;


import com.snoopy.grpc.base.constans.GrpcConstants;

/**
 * @author :   kehanjiang
 * @date :   2021/10/5  10:22
 */
public class GrpcClientProperties {
    private boolean usePlaintext = false;

    private String loadBalancingPolicy = GrpcConstants.DEFAULT_LOAD_BALANCING_POLICY_ROUND_ROBIN;

    public boolean isUsePlaintext() {
        return usePlaintext;
    }

    public void setUsePlaintext(boolean usePlaintext) {
        this.usePlaintext = usePlaintext;
    }

    public String getLoadBalancingPolicy() {
        return loadBalancingPolicy;
    }

    public void setLoadBalancingPolicy(String loadBalancingPolicy) {
        this.loadBalancingPolicy = loadBalancingPolicy;
    }
}
