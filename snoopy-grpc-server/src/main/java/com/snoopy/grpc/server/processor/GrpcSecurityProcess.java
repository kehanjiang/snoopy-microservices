package com.snoopy.grpc.server.processor;

import com.snoopy.grpc.base.configure.GrpcSecurityProperties;
import com.snoopy.grpc.server.configure.GrpcServerProperties;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * 服务端连接加密设置
 *
 * @author :   kehanjiang
 * @date :   2021/11/9  17:00
 */
public class GrpcSecurityProcess implements IGrpcProcess {

    private GrpcServerProperties grpcServerProperties;

    private GrpcSecurityProperties grpcSecurityProperties;

    public GrpcSecurityProcess(GrpcServerProperties grpcServerProperties, GrpcSecurityProperties grpcSecurityProperties) {
        this.grpcServerProperties = grpcServerProperties;
        this.grpcSecurityProperties=grpcSecurityProperties;

    }

    @Override
    public void handle(NettyServerBuilder builder) {
        if (!grpcServerProperties.isUsePlaintext()) {
            SslContextBuilder sslContextBuilder;
            GrpcSecurityProperties.Server server = grpcSecurityProperties.getServer();
            requireNonNull(server, "security server not configured");
            Resource certFile = server.getCertFile();
            requireNonNull(certFile, "certFile not configured");
            Resource keyFile = server.getKeyFile();
            requireNonNull(keyFile, "keyFile not configured");
            try (InputStream certFileStream = certFile.getInputStream();
                 InputStream keyFileStream = keyFile.getInputStream()) {
                sslContextBuilder = GrpcSslContexts.forServer(certFileStream, keyFileStream,
                        server.getKeyPassword());
            } catch (IOException | RuntimeException e) {
                throw new IllegalArgumentException("Failed to create SSLContext (PK/Cert)", e);
            }
            if (server.getClientAuth() != ClientAuth.NONE) {
                sslContextBuilder.clientAuth(of(server.getClientAuth()));
                Resource trustCertCollection = grpcSecurityProperties.getCa().getCertFile();
                if (trustCertCollection != null) {
                    try (InputStream trustCertCollectionStream = trustCertCollection.getInputStream()) {
                        sslContextBuilder.trustManager(trustCertCollectionStream);
                    } catch (IOException | RuntimeException e) {
                        throw new IllegalArgumentException("Failed to create SSLContext (TrustStore)", e);
                    }
                }
            }

            if (grpcSecurityProperties.getCiphers() != null && !grpcSecurityProperties.getCiphers().isEmpty()) {
                sslContextBuilder.ciphers(grpcSecurityProperties.getCiphers());
            }

            if (grpcSecurityProperties.getProtocols() != null && grpcSecurityProperties.getProtocols().length > 0) {
                sslContextBuilder.protocols(grpcSecurityProperties.getProtocols());
            }

            try {
                builder.sslContext(sslContextBuilder.build());
            } catch (SSLException e) {
                throw new IllegalStateException("Failed to create ssl context for grpc server", e);
            }
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }

    /**
     * Converts the given client auth option to netty's client auth.
     *
     * @param clientAuth The client auth option to convert.
     * @return The converted client auth option.
     */
    protected static io.netty.handler.ssl.ClientAuth of(final ClientAuth clientAuth) {
        switch (clientAuth) {
            case NONE:
                return io.netty.handler.ssl.ClientAuth.NONE;
            case OPTIONAL:
                return io.netty.handler.ssl.ClientAuth.OPTIONAL;
            case REQUIRE:
                return io.netty.handler.ssl.ClientAuth.REQUIRE;
            default:
                throw new IllegalArgumentException("Unsupported ClientAuth: " + clientAuth);
        }
    }
}
