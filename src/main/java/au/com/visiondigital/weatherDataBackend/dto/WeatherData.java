package au.com.visiondigital.weatherDataBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;



@Data
@AllArgsConstructor
@ToString
public class WeatherData {

    private String city;
    private double averageTemperature;
    private String hottestDay;
    private String coldestDay;
}

