package com.levandr;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        // Пример использования
        List<String> hosts = Arrays.asList("loliland.ru");
        List<ProxyInfo> proxies = ProxyReader.readProxiesFromFile("C:\\Users\\levandr\\Port_SCAN\\src\\main\\java\\com\\levandr\\proxy_list.txt");

        // Создание ExecutorService
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        PortScanner portScanner = new PortScanner(executorService);

        // Запуск сканирования портов
        List<OpenPortsResult> openPortsList = portScanner.scan(hosts, proxies);

        // Обработка результатов
        for (OpenPortsResult result : openPortsList) {
            System.out.println("Host: " + result.getHost());
            System.out.println("Open Ports: " + result.getOpenPorts());
            System.out.println();
        }

        // Завершение работы ExecutorService
        executorService.shutdown();
    }
}
