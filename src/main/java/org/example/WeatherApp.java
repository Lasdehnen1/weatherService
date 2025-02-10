package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class WeatherApp {
    
    private static final String API_KEY = "dc4bfbfaf980411080c142444250302";
    private static final String API_URL = "http://api.weatherapi.com/v1/current.json";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/test_db";
    private static final String DB_USER = "myUser";
    private static final String DB_PASSWORD = "trust";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Введите название города (или 'exit' для выхода): ");
            String city = scanner.nextLine();
            if (city.equalsIgnoreCase("exit")) {
                System.out.println("Выход из программы.");
                break;
            }
            try {
                String weatherData = getWeatherData(city);
                System.out.println(weatherData);
                saveWeatherDataToDatabase(city, weatherData);
            } catch (IOException | InterruptedException |SQLException e) {
                System.out.println("Ошибка при получении данных о погоде: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static String getWeatherData(String city) throws IOException, InterruptedException {
        String url = API_URL + "?key=" + API_KEY + "&q=" + city + "&lang=ru";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Ошибка API: " + response.body());
        }
        return parseWeatherData(response.body());
    }

    private static String parseWeatherData(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        String cityName = root.get("location").get("name").asText();
        double temperature = root.get("current").get("temp_c").asDouble();
        String weatherDescription = root.get("current").get("condition").get("text").asText();
        return String.format("Город: %s, Температура: %.1f°C, Погода: %s", cityName, temperature, weatherDescription);
    }
    private static void saveWeatherDataToDatabase(String city, String weatherData) throws SQLException {
        String[] parts = weatherData.split(", ");
        String cityName = parts[0].split(": ")[1];

        String tempString = parts[1].split(": ")[1].replace("°C", "").replace(",", ".");
        double temperature = Double.parseDouble(tempString);
        String weatherDescription = parts[2].split(": ")[1];

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO weather_data (city, temperature, weather_description) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, cityName);
                statement.setDouble(2, temperature);
                statement.setString(3, weatherDescription);
                statement.executeUpdate();

                System.out.println("Данные сохранены");
            }
        }
    }
}