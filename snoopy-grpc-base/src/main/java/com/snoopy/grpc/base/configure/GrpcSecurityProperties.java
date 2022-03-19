package com.snoopy.grpc.base.configure;


import io.netty.handler.ssl.ClientAuth;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;


/**
 * @author :   kehanjiang
 * @date :   2021/10/5  10:22
 */
public class GrpcSecurityProperties {
    private String authority = null;

    private Ca ca = null;

    private Server server = null;

    private Client client = null;

    private List<String> ciphers = null;

    public void setCiphers(String ciphers) {
        this.ciphers = Arrays.asList(ciphers.split("[,]"));
    }

    private String[] protocols = null;

    public void setProtocols(String protocols) {
        this.protocols = protocols.split("[,]");
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Ca getCa() {
        return ca;
    }

    public void setCa(Ca ca) {
        this.ca = ca;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<String> getCiphers() {
        return ciphers;
    }

    public String[] getProtocols() {
        return protocols;
    }

    public static class Ca {
        private Resource certFile = null;

        private Resource crlFile = null;

        public Resource getCertFile() {
            return certFile;
        }

        public void setCertFile(Resource certFile) {
            this.certFile = certFile;
        }

        public Resource getCrlFile() {
            return crlFile;
        }

        public void setCrlFile(Resource crlFile) {
            this.crlFile = crlFile;
        }
    }

    public static class Client {
        private Resource certFile = null;

        private Resource keyFile = null;

        private String keyPassword = null;

        public Resource getCertFile() {
            return certFile;
        }

        public void setCertFile(Resource certFile) {
            this.certFile = certFile;
        }

        public Resource getKeyFile() {
            return keyFile;
        }

        public void setKeyFile(Resource keyFile) {
            this.keyFile = keyFile;
        }

        public String getKeyPassword() {
            return keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }
    }

    public static class Server {
        private Resource certFile = null;

        private Resource keyFile = null;

        private String keyPassword = null;

        private ClientAuth clientAuth = ClientAuth.REQUIRE;

        public Resource getCertFile() {
            return certFile;
        }

        public void setCertFile(Resource certFile) {
            this.certFile = certFile;
        }

        public Resource getKeyFile() {
            return keyFile;
        }

        public void setKeyFile(Resource keyFile) {
            this.keyFile = keyFile;
        }

        public String getKeyPassword() {
            return keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }

        public ClientAuth getClientAuth() {
            return clientAuth;
        }

        public void setClientAuth(ClientAuth clientAuth) {
            this.clientAuth = clientAuth;
        }
    }
}
