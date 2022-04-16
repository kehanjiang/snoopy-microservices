package com.snoopy.grpc.base.configure;


import io.netty.handler.ssl.ClientAuth;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;


/**
 * @author :   kehanjiang
 * @date :   2021/10/5  10:22
 */
public class GrpcSecurityProperties extends GrpcBaseProperties {
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
        private String certFile = null;

        private String crlFile = null;

        public File getCertFile() {
            try {
                return ResourceUtils.getFile(certFile);
            } catch (Exception e) {
                return null;
            }
        }

        public void setCertFile(String certFile) {
            this.certFile = certFile;
        }

        public File getCrlFile() {
            try {
                return ResourceUtils.getFile(crlFile);
            } catch (Exception e) {
                return null;
            }
        }

        public void setCrlFile(String crlFile) {
            this.crlFile = crlFile;
        }
    }

    public static class Client {
        private String certFile = null;

        private String keyFile = null;

        private String keyPassword = null;

        private Boolean enabledOcsp = false;

        public File getCertFile() {
            try {
                return ResourceUtils.getFile(certFile);
            } catch (Exception e) {
                return null;
            }
        }

        public void setCertFile(String certFile) {
            this.certFile = certFile;
        }

        public File getKeyFile() {
            try {
                return ResourceUtils.getFile(keyFile);
            } catch (Exception e) {
                return null;
            }
        }

        public void setKeyFile(String keyFile) {
            this.keyFile = keyFile;
        }

        public String getKeyPassword() {
            return keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }

        public Boolean getEnabledOcsp() {
            return enabledOcsp;
        }

        public void setEnabledOcsp(Boolean enabledOcsp) {
            this.enabledOcsp = enabledOcsp;
        }
    }

    public static class Server {
        private String certFile = null;

        private String keyFile = null;

        private String keyPassword = null;

        private ClientAuth clientAuth = ClientAuth.REQUIRE;

        private Boolean enabledOcsp = false;

        public File getCertFile() {
            try {
                return ResourceUtils.getFile(certFile);
            } catch (Exception e) {
                return null;
            }
        }

        public void setCertFile(String certFile) {
            this.certFile = certFile;
        }

        public File getKeyFile() {
            try {
                return ResourceUtils.getFile(keyFile);
            } catch (Exception e) {
                return null;
            }
        }

        public void setKeyFile(String keyFile) {
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

        public Boolean getEnabledOcsp() {
            return enabledOcsp;
        }

        public void setEnabledOcsp(Boolean enabledOcsp) {
            this.enabledOcsp = enabledOcsp;
        }
    }
}
