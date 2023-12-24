package com.levandr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    private static final int MIN_PORT_NUMBER = 0;
    private static final int MAX_PORT_NUMBER = 65535;
    private static final int TIMEOUT = 65;

    public static void main(String[] args) {
        List<String> hosts = List.of(
                "loliland.ru",
                "kmpo.eljur.ru",
                "eljur.ru",
                "www.docentum.ru",
                "docentum.ru",
                "gmail.com"
        );

        List<OpenPortsResult> openPortsList = scan(hosts);

        // Вывод результатов
        openPortsList.forEach(result ->
                System.out.println("Host: " + result.getHost() + ", Open Ports: " + result.getOpenPorts()));
    }

    private static List<OpenPortsResult> scan(List<String> hosts) {
        System.out.println("Scanning ports");

        return hosts.parallelStream()
                .map(host -> CompletableFuture.supplyAsync(() -> scanPorts(host)))
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private static OpenPortsResult scanPorts(String host) {
        List<Integer> openPorts = IntStream.rangeClosed(MIN_PORT_NUMBER, MAX_PORT_NUMBER)
                .parallel()  // использование параллельных потоков
                .mapToObj(port -> CompletableFuture.supplyAsync(() -> checkPort(host, port)))
                .map(CompletableFuture::join)
                .filter(port -> port != -1)
                .collect(Collectors.toList());

        return new OpenPortsResult(host, openPorts);
    }

    private static int checkPort(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), TIMEOUT);
            System.out.printf("Host: %s, port %d is opened\n", host, port);
            return port;
        } catch (IOException e) {
            // Comment the line below if you don't want to print closed ports
            // System.err.println(host + " " + e.getMessage() + " on " + port + " port");
            return -1;
        }
    }

    // Класс для хранения результатов открытых портов для каждого хоста
    static class OpenPortsResult {
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
}
