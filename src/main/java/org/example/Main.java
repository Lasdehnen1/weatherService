package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        Properties config = loadConfig();
        WeatherService weatherService = new WeatherService(
                config.getProperty("api.key"),
                config.getProperty("api.url")
        );

        DBSavingService dbSavingService = new DBSavingService();
        WeatherController weatherController = new WeatherController(weatherService, dbSavingService);
        try {
            weatherController.startServer();
        } catch (IOException e) {
            logger.error("Ошибка запуска сервера: " + e.getMessage());
        }
    }
    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            logger.info("Ошибка загрузки конфигурации", e);
        }
        return properties;
    }
}