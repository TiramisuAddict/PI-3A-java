package test;

import service.api.WeatherService;

public class TestWeatherService {
    public static void main(String[] args) {
        WeatherService weatherService = new WeatherService();

        System.out.println("Weather Service Enabled: " + weatherService.isEnabled());

        WeatherService.WeatherData weather = weatherService.getCurrentWeather();

        System.out.println("\n" + weather);

        if (weather.isValid) {
            System.out.println("\n✅ Weather data is VALID");
        } else {
            System.out.println("\n❌ Weather data is INVALID - using defaults");
        }
    }
}