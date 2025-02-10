package org.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBSavingService {
    private static final Logger logger = LoggerFactory.getLogger(DBSavingService.class);

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public DBSavingService(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public void saveWeatherDataToDatabase(String city, String weatherData) throws SQLException {
        String[] parts = weatherData.split(", ");
        String cityName = parts[0].split(": ")[1];

        String tempString = parts[1].split(": ")[1].replace("°C", "").replace(",", ".");
        double temperature = Double.parseDouble(tempString);
        String weatherDescription = parts[2].split(": ")[1];

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String sql = "INSERT INTO weather_data (city, temperature, weather_description) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, cityName);
                statement.setDouble(2, temperature);
                statement.setString(3, weatherDescription);
                statement.executeUpdate();
                logger.info("Данные о погоде сохранены: {} - {}°C, {}", cityName, temperature, weatherDescription);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при сохранении данных в БД", e);
        }
    }
}
