package com.snoopy.grpc.client.annotation;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.configure.GrpcSecurityProperties;
import com.snoopy.grpc.base.constans.GrpcConstants;
import com.snoopy.grpc.base.registry.*;
import com.snoopy.grpc.base.utils.NetUtil;
import com.snoopy.grpc.client.balance.weight.WeightRandomLoadBalancerProvider;
import com.snoopy.grpc.client.configure.GrpcClientProperties;
import io.grpc.ClientInterceptor;
import io.grpc.LoadBalancerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.AbstractStub;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.SocketUtils;

import javax.net.ssl.SSLException;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author :   kehanjiang
 * @date :   2021/12/6  9:21
 */
public class GrpcClientStubBeanManager implements Closeable {
    private List<ClientInterceptor> privateInterceptors;
    private ConfigurableListableBeanFactory configurableListableBeanFactory;
    private Map<String, ManagedChannel> channelMap;
    private static volatile GrpcClientStubBeanManager instance;

    private GrpcClientStubBeanManager() {
        channelMap = new HashMap<>();
        ShutDownHookManager.registerShutdownHook(this);
    }

    public static GrpcClientStubBeanManager getInstance() {
        if (instance == null) {
            synchronized (GrpcClientStubBeanManager.class) {
                if (instance == null) {
                    instance = new GrpcClientStubBeanManager();
                }
            }
        }
        return instance;
    }

    public Object newStubBean(
            String host,
            int port,
            Class<?> stubClass,
            String beanName,
            StubType type,
            String namespace,
            String alias,
            List<ClientInterceptor> privateInterceptors,
            ConfigurableListableBeanFactory configurableListableBeanFactory) {
        this.privateInterceptors = privateInterceptors;
        this.configurableListableBeanFactory = configurableListableBeanFactory;
        DefaultListableBeanFactory springFactory = (DefaultListableBeanFactory) configurableListableBeanFactory;
        try {
            //取得内部类
            String serviceClassName = stubClass.getCanonicalName()
                    .subSequence(0, stubClass.getCanonicalName().length() - stubClass.getSimpleName().length() - 1)
                    .toString();

            Class serviceClass = Class.forName(serviceClassName);
            String channelKey = host + "@" + port + "@" + namespace + "@" + alias;
            ManagedChannel channel = channelMap.get(channelKey);
            if (channel == null) {
                channel = createManagedChannel(host, port, namespace, alias);
                channelMap.put(channelKey, channel);
            }
            AbstractStub<?> newStubClass = null;
            switch (type) {
                case ASYNC:
                    newStubClass = (AbstractStub<?>) MethodUtils.invokeStaticMethod(serviceClass, "newStub", channel);
                    break;
                case BLOCKING:
                    newStubClass = (AbstractStub<?>) MethodUtils.invokeStaticMethod(serviceClass, "newBlockingStub", channel);
                    break;
                case FUTURE:
                    newStubClass = (AbstractStub<?>) MethodUtils.invokeStaticMethod(serviceClass, "newFutureStub", channel);
                    break;
                default:
                    throw new RuntimeException("StubTyp is unSupport!");
            }
            //注册bean
            springFactory.registerSingleton(beanName, newStubClass);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return springFactory.getBean(beanName);
    }

    /**
     * @param host      ip地址
     * @param port      ip端口
     * @param namespace 服务命名空间
     * @param alias     服务别名
     * @return
     * @throws Exception
     */
    private ManagedChannel createManagedChannel(String host, int port, String namespace, String alias) throws Exception {
        GrpcClientProperties grpcClientProperties = configurableListableBeanFactory.getBean(GrpcClientProperties.class);
        GrpcRegistryProperties grpcRegistryProperties = configurableListableBeanFactory.getBean(GrpcRegistryProperties.class);
        GrpcSecurityProperties grpcSecurityProperties = configurableListableBeanFactory.getBean(GrpcSecurityProperties.class);
        RegistryServiceInfo registryServiceInfo = new RegistryServiceInfo(namespace, alias,
                grpcClientProperties.isUsePlaintext() ? GrpcConstants.PROTOCOL_HTTP2_PLAIN : GrpcConstants.PROTOCOL_HTTP2_SSL,
                NetUtil.getLocalIpAddress(),
                SocketUtils.findAvailableTcpPort(10000, 60000));
        registryServiceInfo.addParameter(GrpcConstants.PARAMETER_WEIGHT, String.valueOf(GrpcConstants.DEFAULT_WEIGHT));
        IRegistry registry = configurableListableBeanFactory.getBean(RegistryProviderFactory.class).
                newRegistryProviderInstance().newRegistryInstance(grpcRegistryProperties);

        NettyChannelBuilder builder = NetUtil.isIpAddress(host)
                ? NettyChannelBuilder.forAddress(host, port).overrideAuthority(grpcSecurityProperties.getAuthority())
                : NettyChannelBuilder.forTarget("")
                .defaultLoadBalancingPolicy(grpcClientProperties.getLoadBalancingPolicy())
                .nameResolverFactory(new RegistryNameResolverProvider(
                        grpcRegistryProperties,
                        grpcSecurityProperties,
                        registryServiceInfo,
                        registry));

        if (privateInterceptors.size() > 0) {
            builder.intercept(privateInterceptors);
        }

        if (grpcClientProperties.isUsePlaintext()) {
            builder.usePlaintext();
        } else {
            SslContextBuilder sslContextBuilder;
            GrpcSecurityProperties.Client client = grpcSecurityProperties.getClient();
            requireNonNull(client, "security client not configured");
            File certFile = client.getCertFile();
            requireNonNull(certFile, "certFile not configured");
            File keyFile = client.getKeyFile();
            requireNonNull(keyFile, "keyFile not configured");
            try (InputStream certFileStream = new FileInputStream(certFile);
                 InputStream keyFileStream = new FileInputStream(keyFile)) {
                sslContextBuilder = GrpcSslContexts.forClient().keyManager(certFileStream, keyFileStream, client.getKeyPassword());
            } catch (IOException | RuntimeException e) {
                throw new IllegalArgumentException("Failed to create SSLContext (PK/Cert)", e);
            }

            File trustCertCollection = grpcSecurityProperties.getCa().getCertFile();
            if (trustCertCollection != null) {
                try (InputStream trustCertCollectionStream = new FileInputStream(trustCertCollection)) {
                    sslContextBuilder.trustManager(trustCertCollectionStream);
                } catch (IOException | RuntimeException e) {
                    throw new IllegalArgumentException("Failed to create SSLContext (TrustStore)", e);
                }
            }

            sslContextBuilder.enableOcsp(client.getEnabledOcsp());

            if (grpcSecurityProperties.getCiphers() != null && !grpcSecurityProperties.getCiphers().isEmpty()) {
                sslContextBuilder.ciphers(grpcSecurityProperties.getCiphers());
            }

            if (grpcSecurityProperties.getProtocols() != null && grpcSecurityProperties.getProtocols().length > 0) {
                sslContextBuilder.protocols(grpcSecurityProperties.getProtocols());
            }

            try {
                builder.sslContext(sslContextBuilder.build());
            } catch (SSLException e) {
                throw new IllegalStateException("Failed to create ssl context for grpc client", e);
            }
        }
        return builder.build();
    }

    @Override
    public void close() throws IOException {
        for (ManagedChannel channel : channelMap.values()) {
            channel.shutdown();
        }
        channelMap.clear();
    }
}
