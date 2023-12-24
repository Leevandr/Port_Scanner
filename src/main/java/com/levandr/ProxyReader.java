package com.levandr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProxyReader {

    public static List<ProxyInfo> readProxiesFromFile(String filePath) {
        List<ProxyInfo> proxies = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Path.of(filePath));
            proxies = lines.stream()
                    .map(line -> parseProxyInfo(line, filePath))
                    .filter(Objects::nonNull)
                    .filter(ProxyReader::isProxyWorking)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading proxy file " + filePath + ": " + e.getMessage());
        }

        return proxies;
    }

    private static ProxyInfo parseProxyInfo(String line, String filePath) {
        String[] parts = line.trim().split(":");
        if (parts.length == 2) {
            String host = parts[0];
            int port;
            try {
                port = Integer.parseInt(parts[1]);
                if (isValidPort(port)) {
                    ProxyInfo proxyInfo = new ProxyInfo(host, port);
                    if (isProxyWorking(proxyInfo)) {
                        return proxyInfo;
                    } else {
                        System.err.println("Proxy is not working: " + line + " in file " + filePath);
                    }
                } else {
                    System.err.println("Invalid proxy port: " + parts[1] + " in file " + filePath);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid proxy port format: " + parts[1] + " in file " + filePath);
            }
        } else {
            System.err.println("Invalid proxy format: " + line + " in file " + filePath);
        }
        return null;
    }

    private static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }

    private static boolean isProxyWorking(ProxyInfo proxyInfo) {
        try (Socket socket = new Socket(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo.getHost(), proxyInfo.getPort())))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
