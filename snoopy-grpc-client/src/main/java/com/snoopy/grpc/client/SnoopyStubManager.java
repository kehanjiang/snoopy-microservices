package com.snoopy.grpc.client;

import com.snoopy.grpc.base.registry.*;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author :   kehanjiang
 * @date :   2021/12/6  9:21
 */
public class SnoopyStubManager implements Closeable {
    private Map<String, ManagedChannel> channelMap;
    private static volatile SnoopyStubManager instance;

    private SnoopyStubManager() {
        channelMap = new HashMap<>();
        ShutDownHookManager.registerShutdownHook(this);
    }

    public static SnoopyStubManager getInstance() {
        if (instance == null) {
            synchronized (SnoopyStubManager.class) {
                if (instance == null) {
                    instance = new SnoopyStubManager();
                }
            }
        }
        return instance;
    }

    public AbstractStub<?> newBlockingStub(ServiceEndpoint serviceEndpoint) throws Exception {
        ManagedChannel channel = getChannel(serviceEndpoint);
        return (AbstractStub<?>) MethodUtils.invokeStaticMethod(serviceEndpoint.getSvcClass(), "newBlockingStub", channel);
    }

    public AbstractStub<?> newFutureStub(ServiceEndpoint serviceEndpoint) throws Exception {
        ManagedChannel channel = getChannel(serviceEndpoint);
        return (AbstractStub<?>) MethodUtils.invokeStaticMethod(serviceEndpoint.getSvcClass(), "newFutureStub", channel);
    }

    public AbstractStub<?> newStub(ServiceEndpoint serviceEndpoint) throws Exception {
        ManagedChannel channel = getChannel(serviceEndpoint);
        return (AbstractStub<?>) MethodUtils.invokeStaticMethod(serviceEndpoint.getSvcClass(), "newStub", channel);
    }

    public ManagedChannel getChannel(ServiceEndpoint serviceEndpoint) {
        String channelId = serviceEndpoint.getChannelId();
        ManagedChannel channel = channelMap.get(channelId);
        if (!isChannelValid(channel)) {
            channel = serviceEndpoint.getChannel();
            channelMap.put(channelId, channel);
        }
        return channel;
    }

    protected static boolean isChannelValid(ManagedChannel channel) {
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }

    @Override
    public void close() throws IOException {
        for (ManagedChannel channel : channelMap.values()) {
            channel.shutdown();
        }
        channelMap.clear();
    }
}
