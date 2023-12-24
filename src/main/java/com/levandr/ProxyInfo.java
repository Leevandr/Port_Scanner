package com.levandr;


public class ProxyInfo {
    private final String host;
    private final int port;

    public ProxyInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
