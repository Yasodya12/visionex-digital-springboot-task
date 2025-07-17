package au.com.visiondigital.weatherDataBackend.controller;


import au.com.visiondigital.weatherDataBackend.dto.WeatherData;
import au.com.visiondigital.weatherDataBackend.exeption.CityNotFoundException;
import au.com.visiondigital.weatherDataBackend.exeption.ExternalApiException;
import au.com.visiondigital.weatherDataBackend.service.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
public class WeatherControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Autowired
    private ObjectMapper objectMapper;

    private WeatherData sampleWeatherData;

    @BeforeEach
    void setUp() {
        sampleWeatherData = new WeatherData(
                "London",
                15.5,
                "2024-01-16",
                "2024-01-14"
        );
    }

    @Test
    void getWeatherSummary_ValidCity_ReturnsWeatherData() throws Exception {
        // Arrange
        String city = "London";
        when(weatherService.getWeatherSummary(city)).thenReturn(sampleWeatherData);

        // Act & Assert
        mockMvc.perform(get("/api/weather/summary/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName").value("London"))
                .andExpect(jsonPath("$.averageTemperature").value(15.5))
                .andExpect(jsonPath("$.hottestDay").value("2024-01-16"))
                .andExpect(jsonPath("$.coldestDay").value("2024-01-14"));

        verify(weatherService, times(1)).getWeatherSummary(city);
    }

    @Test
    void getWeatherSummary_CityNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        String city = "InvalidCity";
        when(weatherService.getWeatherSummary(city))
                .thenThrow(new CityNotFoundException(city));

        // Act & Assert
        mockMvc.perform(get("/api/weather/summary/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(weatherService, times(1)).getWeatherSummary(city);
    }

    @Test
    void getWeatherSummary_ExternalApiException_ReturnsServiceUnavailable() throws Exception {
        // Arrange
        String city = "London";
        when(weatherService.getWeatherSummary(city))
                .thenThrow(new ExternalApiException("API Error", new RuntimeException()));

        // Act & Assert
        mockMvc.perform(get("/api/weather/summary/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());

        verify(weatherService, times(1)).getWeatherSummary(city);
    }

    @Test
    void getWeatherSummary_EmptyCity_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/summary/ ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Spring treats empty path variable as not found

        verify(weatherService, never()).getWeatherSummary(anyString());
    }

    @Test
    void getWeatherSummary_CityWithSpecialCharacters_HandlesCorrectly() throws Exception {
        // Arrange
        String city = "São Paulo";
        WeatherData weatherData = new WeatherData(city, 25.0, "2024-01-15", "2024-01-13");
        when(weatherService.getWeatherSummary(city)).thenReturn(weatherData);

        // Act & Assert
        mockMvc.perform(get("/api/weather/summary/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cityName").value("São Paulo"))
                .andExpect(jsonPath("$.averageTemperature").value(25.0));

        verify(weatherService, times(1)).getWeatherSummary(city);
    }

    @Test
    void getWeatherSummary_CityWithSpaces_HandlesCorrectly() throws Exception {
        // Arrange
        String city = "New York";
        WeatherData weatherData = new WeatherData(city, 18.0, "2024-01-15", "2024-01-13");
        when(weatherService.getWeatherSummary(city)).thenReturn(weatherData);

        // Act & Assert
        mockMvc.perform(get("/api/weather/summary/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cityName").value("New York"));

        verify(weatherService, times(1)).getWeatherSummary(city);
    }

    @Test
    void getWeatherSummary_UnexpectedException_ReturnsInternalServerError() throws Exception {
        // Arrange
        String city = "London";
        when(weatherService.getWeatherSummary(city))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/api/weather/summary/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(weatherService, times(1)).getWeatherSummary(city);
    }

    @Test
    void healthCheck_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/weather/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Weather service is running"));

        // Verify that service is not called for health check
        verify(weatherService, never()).getWeatherSummary(anyString());
    }

    @Test
    void getWeatherSummary_NegativeTemperature_HandlesCorrectly() throws Exception {
        // Arrange
        String city = "Moscow";
        WeatherData weatherData = new WeatherData(city, -10.5, "2024-01-15", "2024-01-13");
        when(weatherService.getWeatherSummary(city)).thenReturn(weatherData);

        // Act & Assert
        mockMvc.perform(get("/api/weather/summary/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cityName").value("Moscow"))
                .andExpect(jsonPath("$.averageTemperature").value(-10.5));

        verify(weatherService, times(1)).getWeatherSummary(city);
    }

    @Test
    void getWeatherSummary_CaseSensitivity_PassesCorrectly() throws Exception {
        // Arrange
        String city = "LONDON";
        when(weatherService.getWeatherSummary(city)).thenReturn(sampleWeatherData);

        // Act & Assert
        mockMvc.perform(get("/api/weather/summary/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(weatherService, times(1)).getWeatherSummary("LONDON");
    }
}
