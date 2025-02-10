package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        Properties config = loadConfig();
        WeatherService weatherService = new WeatherService(
                config.getProperty("api.key"),
                config.getProperty("api.url")
        );
        DBSavingService dbSavingService = new DBSavingService(
                config.getProperty("db.url"),
                config.getProperty("db.user"),
                config.getProperty("db.password")
        );

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Введите название города (или 'exit' для выхода): ");
            String city = scanner.nextLine();
            if (city.equalsIgnoreCase("exit")) {
                System.out.println("Выход из программы.");
                break;
            }
            try {
                String weatherData = weatherService.getWeatherData(city);
                System.out.println(weatherData);
                dbSavingService.saveWeatherDataToDatabase(city, weatherData);
            } catch (IOException | InterruptedException | SQLException e) {
                logger.error("Ошибка при получении данных о погоде", e);
            }
        }
        scanner.close();
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