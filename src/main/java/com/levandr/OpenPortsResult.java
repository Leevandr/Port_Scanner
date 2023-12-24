package com.levandr;


import java.util.List;

public class OpenPortsResult {
    private final String host;
    private final List<Integer> openPorts;

    public OpenPortsResult(String host, List<Integer> openPorts) {
        this.host = host;
        this.openPorts = openPorts;
    }

    public String getHost() {
        return host;
    }

    public List<Integer> getOpenPorts() {
        return openPorts;
    }
}
