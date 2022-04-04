package com.snoopy.grpc.base.registry;


import com.snoopy.grpc.base.constans.GrpcConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author :   kehanjiang
 * @date :   2021/11/29  16:23
 */
public class RegistryServiceInfo {
    private String namespace;
    private String alias;
    private String protocol;
    private String host;
    private int port;
    private Map<String, String> parameters = new HashMap<>();

    public RegistryServiceInfo() {
    }

    public RegistryServiceInfo(String namespace, String alias, String protocol, String host, int port) {
        this.namespace = namespace;
        this.alias = alias;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public RegistryServiceInfo(String namespace, String alias, String protocol, String host, int port, Map<String, String> parameters) {
        this.namespace = namespace;
        this.alias = alias;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.parameters = parameters;
    }

    public RegistryServiceInfo(String url) {
        if (StringUtils.isBlank(url)) {
            throw new RuntimeException("url is null");
        }
        Map<String, String> parameters = new HashMap<String, String>();
        URI uri = URI.create(url);
        if (StringUtils.isNotEmpty(uri.getRawQuery())) {
            parameters.putAll(parseQueryParams(uri.getRawQuery()));
        }
        String[] path = uri.getRawPath().split(GrpcConstants.PATH_SEPARATOR);

        this.namespace = path[2];
        this.alias = path[3];
        this.protocol = uri.getScheme();
        this.host = uri.getHost();
        this.port = uri.getPort();
        this.parameters = parameters;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void addParameter(String name, String value) {
        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(value)) {
            this.parameters.put(name, value);
        }
    }

    public void addParameters(Map<String, String> params) {
        if (params != null) {
            this.parameters.putAll(params);
        }
    }

    public void addParameterIfAbsent(String name, String value) {
        if (hasParameter(name)) {
            return;
        }
        parameters.put(name, value);
    }

    public String getParameter(String name) {
        return (String) this.parameters.get(name);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public boolean hasParameter(String key) {
        return StringUtils.isNotBlank(getParameter(key));
    }

    public String getHostAndPort() {
        return host + ":" + port;
    }

    public String getPath() {
        StringBuilder stringBuilder = new StringBuilder(GrpcConstants.BASE_PATH);
        stringBuilder.append(GrpcConstants.PATH_SEPARATOR);
        stringBuilder.append(namespace);
        stringBuilder.append(GrpcConstants.PATH_SEPARATOR);
        stringBuilder.append(alias);
        return stringBuilder.toString();
    }

    public String getFullPath() {
        StringBuilder stringBuilder = new StringBuilder(protocol);
        stringBuilder.append(GrpcConstants.PROTOCOL_SEPARATOR);
        stringBuilder.append(getHostAndPort());
        stringBuilder.append(getPath());
        return stringBuilder.toString();
    }

    public String generateData() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getFullPath());
        if (!this.getParameters().isEmpty()) {
            builder.append("?");
            Iterator it = this.getParameters().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry) it.next();
                String name = entry.getKey();
                String value =  entry.getValue();
                builder.append(urlEncode(name));
                if (value != null) {
                    builder.append('=');
                    builder.append(urlEncode(value));
                }
                if (it.hasNext()) {
                    builder.append('&');
                }
            }
        }
        return builder.toString();
    }

    private Map<String, String> parseQueryParams(String rawRefer) {
        String[] pairs = GrpcConstants.QUERY_PARAM_PATTERN.split(rawRefer);
        Map<String, String> result = new HashMap<String, String>(pairs.length);
        for (String pair : pairs) {
            int idx = pair.indexOf(GrpcConstants.EQUAL_SIGN_SEPERATOR);
            if (idx == -1) {
                result.put(urlDecode(pair), "");
            } else {
                String name = urlDecode(pair.substring(0, idx));
                String value = urlDecode(pair.substring(idx + 1));
                result.put(name, value);
            }
        }
        return result;
    }

    private String urlEncode(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        } else {
            try {
                return URLEncoder.encode(value, GrpcConstants.DEFAULT_CHARACTER);
            } catch (UnsupportedEncodingException var2) {
                throw new RuntimeException(var2.getMessage(), var2);
            }
        }
    }

    private String urlDecode(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        try {
            return URLDecoder.decode(value, GrpcConstants.DEFAULT_CHARACTER);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
