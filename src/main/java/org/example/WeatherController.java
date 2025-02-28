package org.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WeatherController {
    private final WeatherService weatherService;
    private final DBSavingService dbSavingService;

    public WeatherController(WeatherService weatherService, DBSavingService dbSavingService) {
        this.weatherService = weatherService;
        this.dbSavingService = dbSavingService;
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/weather", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String city = exchange.getRequestURI().getQuery().split("=")[1];
                try {
                    String weatherData = weatherService.getWeatherData(city);
                    dbSavingService.saveWeatherDataToDatabase(city, weatherData);
                    exchange.sendResponseHeaders(200, weatherData.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(weatherData.getBytes());
                    }
                } catch (Exception e) {
                    String response = "Ошибка: " + e.getMessage();
                    exchange.sendResponseHeaders(500, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Метод не поддерживается
            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Сервер запущен на порту 8080");
    }
}
