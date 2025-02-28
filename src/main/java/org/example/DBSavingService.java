package org.example;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBSavingService {
    private static final Logger logger = LoggerFactory.getLogger(DBSavingService.class);
    private final SessionFactory sessionFactory;

    public DBSavingService() {
        this.sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    public void saveWeatherDataToDatabase(String city, String weatherData) {
        String[] parts = weatherData.split(", ");
        String cityName = parts[0].split(": ")[1];
        double temperature = Double.parseDouble(parts[1].split(": ")[1].replace("°C", "").replace(",", "."));
        String weatherDescription = parts[2].split(": ")[1];
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            WeatherData data = new WeatherData();
            data.setCity(cityName);
            data.setTemperature(temperature);
            data.setDescription(weatherDescription);
            session.save(data);
            transaction.commit();
            logger.info("Данные о погоде сохранены: {} - {}°C, {}", cityName, temperature, weatherDescription);
        } catch (Exception e) {
            logger.error("Ошибка при сохранении данных в БД", e);
        }
    }
}

