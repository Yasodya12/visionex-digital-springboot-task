package au.com.visiondigital.weatherDataBackend.service;

import au.com.visiondigital.weatherDataBackend.dto.WeatherData;


public interface WeatherService {
    WeatherData getWeatherSummary(String city);
}
