package com.snoopy.grpc.client;

import com.snoopy.grpc.base.configure.GrpcRegistryProperties;
import com.snoopy.grpc.base.configure.GrpcSecurityProperties;
import com.snoopy.grpc.base.constans.GrpcConstants;
import com.snoopy.grpc.base.registry.IRegistry;
import com.snoopy.grpc.base.registry.RegistryNameResolverProvider;
import com.snoopy.grpc.base.registry.RegistryProviderFactory;
import com.snoopy.grpc.base.registry.RegistryServiceInfo;
import com.snoopy.grpc.base.utils.NetUtil;
import com.snoopy.grpc.client.annotation.SnoopyGrpcGlobalClientInterceptor;
import com.snoopy.grpc.client.configure.GrpcClientProperties;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.SocketUtils;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * @author :   kehanjiang
 * @date :   2022/4/16  13:30
 */
public class SnoopyServiceEndpoint implements ServiceEndpoint {
    private String host;
    private int port;
    private String namespace;
    private String alias;
    private Class parentGrpcClazz;

    private List<ClientInterceptor> privateInterceptors;
    private GrpcClientProperties grpcClientProperties;
    private GrpcRegistryProperties grpcRegistryProperties;
    private GrpcSecurityProperties grpcSecurityProperties;
    private RegistryProviderFactory registryProviderFactory;

    public SnoopyServiceEndpoint(
            String namespace,
            String alias,
            Class subStubClazz,
            ConfigurableListableBeanFactory configurableListableBeanFactory) throws ClassNotFoundException {
        this(null, -1, namespace, alias, subStubClazz, true, configurableListableBeanFactory, new ArrayList<>());
    }

    public SnoopyServiceEndpoint(
            String namespace,
            String alias,
            Class subStubClazz,
            boolean applyGlobalClientInterceptors,
            ConfigurableListableBeanFactory configurableListableBeanFactory) throws ClassNotFoundException {
        this(null, -1, namespace, alias, subStubClazz, applyGlobalClientInterceptors, configurableListableBeanFactory, new ArrayList<>());
    }

    public SnoopyServiceEndpoint(
            String namespace,
            String alias,
            Class subStubClazz,
            ConfigurableListableBeanFactory configurableListableBeanFactory,
            boolean applyGlobalClientInterceptors,
            List<ClientInterceptor> privateInterceptors) throws ClassNotFoundException {
        this(null, -1, namespace, alias, subStubClazz, applyGlobalClientInterceptors, configurableListableBeanFactory, privateInterceptors);
    }

    public SnoopyServiceEndpoint(
            String host,
            int port,
            String namespace,
            String alias,
            Class subStubClazz,
            boolean applyGlobalClientInterceptors,
            ConfigurableListableBeanFactory configurableListableBeanFactory,
            List<ClientInterceptor> privateInterceptors) throws ClassNotFoundException {
        this.host = host;
        this.port = port;

        this.namespace = namespace;
        requireNonNull(namespace, "namespace must not be null");
        this.alias = alias;
        requireNonNull(alias, "alias must not be null");

        requireNonNull(subStubClazz, "subStubClazz must not be null");
        String parentGrpcClazzName = subStubClazz.getCanonicalName()
                .subSequence(0, subStubClazz.getCanonicalName().length() - subStubClazz.getSimpleName().length() - 1)
                .toString();
        this.parentGrpcClazz = Class.forName(parentGrpcClazzName);

        this.privateInterceptors = Optional.ofNullable(privateInterceptors).orElse(new ArrayList<>());
        if (applyGlobalClientInterceptors) {
            Collection<String> beanNames = Arrays.asList(
                    configurableListableBeanFactory.getBeanNamesForAnnotation(SnoopyGrpcGlobalClientInterceptor.class)
            );
            for (String beanName : beanNames) {
                ClientInterceptor globalClientInterceptor = (ClientInterceptor) configurableListableBeanFactory.getBean(beanName);
                this.privateInterceptors.add(globalClientInterceptor);
            }
        }
        requireNonNull(configurableListableBeanFactory, "configurableListableBeanFactory must not be null");
        this.grpcClientProperties = configurableListableBeanFactory.getBean(GrpcClientProperties.class);
        requireNonNull(grpcClientProperties, "grpcClientProperties must not be null");
        this.grpcRegistryProperties = configurableListableBeanFactory.getBean(GrpcRegistryProperties.class);
        requireNonNull(grpcRegistryProperties, "grpcRegistryProperties must not be null");
        this.grpcSecurityProperties = configurableListableBeanFactory.getBean(GrpcSecurityProperties.class);
        requireNonNull(grpcSecurityProperties, "grpcSecurityProperties must not be null");
        this.registryProviderFactory = configurableListableBeanFactory.getBean(RegistryProviderFactory.class);
        requireNonNull(registryProviderFactory, "registryProviderFactory must not be null");
    }

    @Override
    public Class getSvcClass() {
        return this.parentGrpcClazz;
    }

    @Override
    public String getChannelId() {
        return host + "@" + port + "@" + namespace + "@" + alias + "@" + parentGrpcClazz;
    }

    @Override
    public ManagedChannel getChannel() {
        RegistryServiceInfo registryServiceInfo = new RegistryServiceInfo(namespace, alias,
                grpcClientProperties.isUsePlaintext() ? GrpcConstants.PROTOCOL_HTTP2_PLAIN : GrpcConstants.PROTOCOL_HTTP2_SSL,
                NetUtil.getLocalIpAddress(), SocketUtils.findAvailableTcpPort(10000, 60000));
        registryServiceInfo.addParameter(GrpcConstants.PARAMETER_WEIGHT, String.valueOf(GrpcConstants.DEFAULT_WEIGHT));
        IRegistry registry = registryProviderFactory.newRegistryProviderInstance().newRegistryInstance(grpcRegistryProperties);

        NettyChannelBuilder builder = NetUtil.isIpAddress(host) && port != -1
                ? NettyChannelBuilder.forAddress(host, port).overrideAuthority(grpcSecurityProperties.getAuthority())
                : NettyChannelBuilder.forTarget("")
                .defaultLoadBalancingPolicy(grpcClientProperties.getLoadBalancingPolicy())
                .nameResolverFactory(new RegistryNameResolverProvider(
                        grpcRegistryProperties,
                        grpcSecurityProperties,
                        registryServiceInfo,
                        registry));

        builder.intercept(privateInterceptors);

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

}
