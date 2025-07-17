package au.com.visiondigital.weatherDataBackend.controller;

import au.com.visiondigital.weatherDataBackend.dto.WeatherData;
import au.com.visiondigital.weatherDataBackend.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/weather")
@CrossOrigin
public class WeatherController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public ResponseEntity<WeatherData> getWeatherSummary(@RequestParam String city) {

        WeatherData summary = weatherService.getWeatherSummary(city);
        return ResponseEntity.ok(summary);

    }
}
