package com.levandr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int MIN_PORT_NUMBER = 0;
    private static final int THREADS = 50;
    private static final int MAX_PORT_NUMBER = 65535;
    private static final int TIMEOUT = 65;

    public static void main(String[] args) {

        List<String> hosts = new ArrayList<>();

        hosts.add("loliland.ru");
        hosts.add("kmpo.eljur.ru");
        hosts.add("eljur.ru");
        hosts.add("www.docentum.ru");
        hosts.add("docentum.ru");
        hosts.add("gmail.com");

        List<OpenPortsResult> openPortsList = scan(hosts);

        // Вывод результатов
        for (OpenPortsResult result : openPortsList) {
            System.out.println("Host: " + result.getHost() + ", Open Ports: " + result.getOpenPorts());
        }
    }

    private static List<OpenPortsResult> scan(List<String> hosts) {
        System.out.println("Running...");
        System.out.println("Scanning ports");

        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        List<OpenPortsResult> openPortsList = new ArrayList<>();

        for (String host : hosts) {
            List<Integer> openPorts = new ArrayList<>();
            for (int i = MIN_PORT_NUMBER; i <= MAX_PORT_NUMBER; i++) {
                final int port = i;
                executorService.execute(() -> {
                    var inetSocketAddress = new InetSocketAddress(host, port);

                    try (var socket = new Socket()) {
                        socket.connect(inetSocketAddress, TIMEOUT);
                        System.out.printf("Host: %s, port %d is opened\n", host, port);
                        openPorts.add(port);
                    } catch (IOException e) {
                        // Comment the line below if you don't want to print closed ports
                        // System.err.println(host + " " + e.getMessage() + " on " + port + " port");
                    }
                });
            }
            openPortsList.add(new OpenPortsResult(host, openPorts));
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finish");

        return openPortsList;
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
