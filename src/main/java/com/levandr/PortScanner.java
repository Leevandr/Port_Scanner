package com.levandr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PortScanner {

    private final ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(PortScanner.class);

    private static final int MIN_PORT_NUMBER = 0;
    private static final int MAX_PORT_NUMBER = 65535;
    private static final int TIMEOUT = 65;

    public PortScanner(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public List<OpenPortsResult> scan(List<String> hosts, List<ProxyInfo> proxies) {
        int totalPorts = (MAX_PORT_NUMBER - MIN_PORT_NUMBER + 1) * hosts.size() * proxies.size();
        logger.info("Running...");
        logger.info("Scanning ports");

        List<OpenPortsResult> openPortsList = Collections.synchronizedList(new ArrayList<>());

        for (String host : hosts) {
            for (ProxyInfo proxy : proxies) {
                CompletableFuture.runAsync(() ->
                        scanPorts(host, proxy, totalPorts, openPortsList), executorService);
            }
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(openPortsList.stream()
                .map(CompletableFuture::completedFuture)
                .toArray(CompletableFuture[]::new));

        allOf.join();
        logger.info("Finish");
        return openPortsList;
    }

    private void scanPorts(String host, ProxyInfo proxyInfo, int totalPorts, List<OpenPortsResult> openPortsList) {
        List<Integer> openPorts = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger progress = new AtomicInteger(0);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo.getHost(), proxyInfo.getPort()));

        try {
            logger.info("Scanning ports for host {} using proxy {}:{}", host, proxyInfo.getHost(), proxyInfo.getPort());

            for (int port = MIN_PORT_NUMBER; port <= MAX_PORT_NUMBER; port++) {
                int finalPort = port;
                CompletableFuture.runAsync(() -> {
                    try (Socket socket = new Socket(proxy)) {
                        socket.connect(new InetSocketAddress(host, finalPort), TIMEOUT);
                        openPorts.add(finalPort);
                    } catch (IOException e) {
                        // Handle exception if needed
                    } finally {
                        int currentProgress = progress.incrementAndGet();
                        printProgressBar(currentProgress, totalPorts);
                    }
                }, executorService).join();
            }
        } catch (Exception e) {
            logger.error("Error while scanning ports", e);
        }

        openPortsList.add(new OpenPortsResult(host, openPorts));
    }

    private static void printProgressBar(int current, int total) {
        int progressBarWidth = 50;
        int progress = (int) ((double) current / total * progressBarWidth);

        StringBuilder progressBar = new StringBuilder("[");
        for (int i = 0; i < progressBarWidth; i++) {
            progressBar.append(i < progress ? "=" : "-");
        }
        progressBar.append("] " + current + "/" + total);

        System.out.print("\r" + progressBar + "   ");
        System.out.flush();
    }
}
