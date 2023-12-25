package com.levandr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Пример использования
            List<String> hosts = Arrays.asList("loliland.ru","openai.com","javarush.com");
            List<ProxyInfo> proxies = ProxyReader.readProxiesFromFile("C:\\Users\\levandr\\Port_SCAN\\src\\main\\java\\com\\levandr\\proxy_list.txt");

            // Создание ExecutorService
            try (ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
                PortScanner portScanner = new PortScanner(executorService);

                // Запуск сканирования портов
                List<OpenPortsResult> openPortsList = portScanner.scan(hosts, proxies);

                // Обработка результатов
                for (OpenPortsResult result : openPortsList) {
                    System.out.println("Host: " + result.getHost());
                    System.out.println("Open Ports: " + result.getOpenPorts());
                    System.out.println();
                }

                logger.info("Сканирование завершено успешно.");
            } catch (Exception e) {
                logger.error("Ошибка при работе программы", e);
            }
        } catch (Exception e) {
            logger.error("Ошибка при чтении списка прокси", e);
        }
    }
}
