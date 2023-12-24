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
        int totalPorts = (MAX_PORT_NUMBER - MIN_PORT_NUMBER + 1) * hosts.size();
        System.out.println("Running...");
        System.out.println("Scanning ports");

        List<OpenPortsResult> openPortsList = new ArrayList<>();

        try {
            openPortsList = hosts.parallelStream()
                    .flatMap(host -> proxies.stream()
                            .map(proxy -> CompletableFuture.supplyAsync(() -> scanPorts(host, proxy, totalPorts), executorService)))
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        } finally {
            shutdownThreadPool(executorService);
            System.out.println("Finish");
        }

        return openPortsList;
    }

    private OpenPortsResult scanPorts(String host, ProxyInfo proxyInfo, int totalPorts) {
        List<Integer> openPorts = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger progress = new AtomicInteger(0);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo.getHost(), proxyInfo.getPort()));

        try {
            System.out.println("Scanning ports for host " + host + " using proxy " + proxyInfo.getHost() + ":" + proxyInfo.getPort());

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int port = MIN_PORT_NUMBER; port <= MAX_PORT_NUMBER; port++) {
                int finalPort = port;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try (Socket socket = new Socket(proxy)) {
                        socket.connect(new InetSocketAddress(host, finalPort), TIMEOUT);
                        openPorts.add(finalPort);
                    } catch (IOException e) {
                        // Handle exception if needed
                    } finally {
                        int currentProgress = progress.incrementAndGet();
                        printProgressBar(currentProgress, totalPorts);
                    }
                }, executorService);

                futures.add(future);
            }

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.join();

            System.out.println(); // Move to the next line after the progress bar
        } catch (Exception e) {
            // Handle exception if needed
        }

        return new OpenPortsResult(host, openPorts);
    }

    private void printProgressBar(int current, int total) {
        int progressBarWidth = 50;
        int progress = (int) ((double) current / total * progressBarWidth);

        StringBuilder progressBar = new StringBuilder("[");
        for (int i = 0; i < progressBarWidth; i++) {
            progressBar.append(i < progress ? "=" : "-");
        }
        progressBar.append("]");

        System.out.print("\r" + progressBar + " " + current + "/" + total);
    }

    private void shutdownThreadPool(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
                logger.error("ExecutorService did not terminate in the specified time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
