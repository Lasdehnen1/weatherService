package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String apiUrl;

    public WeatherService(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public String getWeatherData(String city) throws IOException, InterruptedException {
        String url = apiUrl + "?key=" + apiKey + "&q=" + city + "&lang=ru";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Ошибка API: " + response.body());
        }

        return parseWeatherData(response.body());
    }
    private String parseWeatherData(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        String cityName = root.get("location").get("name").asText();
        double temperature = root.get("current").get("temp_c").asDouble();
        String weatherDescription = root.get("current").get("condition").get("text").asText();
        return String.format("Город: %s, Температура: %.1f°C, Погода: %s", cityName, temperature, weatherDescription);
    }
}
