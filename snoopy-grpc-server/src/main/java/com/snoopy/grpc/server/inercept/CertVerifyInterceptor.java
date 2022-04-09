package com.snoopy.grpc.server.inercept;


import com.snoopy.grpc.base.configure.GrpcSecurityProperties;
import com.snoopy.grpc.base.utils.LoggerBaseUtil;
import com.snoopy.grpc.server.annotation.SnoopyGrpcGlobalServerInterceptor;
import com.snoopy.grpc.server.configure.GrpcServerProperties;
import io.grpc.*;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.FileInputStream;
import java.security.cert.*;
import java.util.Optional;

/**
 * 校验吊销证书列表
 *
 * @author :   kehanjiang
 * @date :   2021/11/25  8:58
 */
@SnoopyGrpcGlobalServerInterceptor
public class CertVerifyInterceptor implements ServerInterceptor {
    private final static String SNOOPY_CLIENT_VERIFIED = "snoopy_client_verified";

    @javax.annotation.Resource
    private GrpcSecurityProperties grpcSecurityProperties;

    @javax.annotation.Resource
    private GrpcServerProperties grpcServerProperties;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Status errorStatus = null;
        if (!grpcServerProperties.isUsePlaintext()) {
            SSLSession sslSession = call.getAttributes().get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
            if (sslSession == null) {
                errorStatus = Status.UNAUTHENTICATED.withDescription("Not ssl/tls session");
            } else {
                Boolean sessionVerified = Optional.ofNullable((Boolean) sslSession.getValue(SNOOPY_CLIENT_VERIFIED)).orElse(false);
                File crlFile = grpcSecurityProperties.getCa().getCrlFile();
                if (crlFile != null && crlFile.exists() && !sessionVerified) {
                    try {
                        Certificate[] certificates = sslSession.getPeerCertificates();
                        X509Certificate certificate = (X509Certificate) certificates[0];
                        try {
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");
                            X509CRL x509CRL = (X509CRL) cf.generateCRL(new FileInputStream(crlFile));
                            X509CRLEntry revokedCertificate = x509CRL.getRevokedCertificate(certificate);
                            if (revokedCertificate != null) {
                                errorStatus = Status.PERMISSION_DENIED.withDescription("Permission denied");
                            } else {
                                sslSession.putValue(SNOOPY_CLIENT_VERIFIED, true);
                            }
                        } catch (Exception e) {
                            errorStatus = Status.UNKNOWN.withDescription(e.getMessage());
                            LoggerBaseUtil.error(this, e.getMessage(), e);
                        }
                    } catch (SSLPeerUnverifiedException e) {
                        errorStatus = Status.UNAUTHENTICATED.withDescription("The client certificate is invalid");
                    }
                }
            }
        }

        if (errorStatus != null) {
            call.close(errorStatus, headers);
            return new ServerCall.Listener<ReqT>() {
            };
        } else {
            return next.startCall(call, headers);
        }
    }

}
