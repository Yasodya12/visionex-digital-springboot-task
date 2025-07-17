package au.com.visiondigital.weatherDataBackend.service.impl;

import au.com.visiondigital.weatherDataBackend.dto.WeatherData;
import au.com.visiondigital.weatherDataBackend.exeption.CityNotFoundException;
import au.com.visiondigital.weatherDataBackend.exeption.ExternalApiException;
import au.com.visiondigital.weatherDataBackend.service.Impl.WeatherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private static final String SAMPLE_JSON_RESPONSE = """
            {
                "city": {
                    "name": "London"
                },
                "list": [
                    {
                        "main": {
                            "temp": 288.15
                        },
                        "dt_txt": "2024-01-15 12:00:00"
                    },
                    {
                        "main": {
                            "temp": 293.15
                        },
                        "dt_txt": "2024-01-16 12:00:00"
                    },
                    {
                        "main": {
                            "temp": 283.15
                        },
                        "dt_txt": "2024-01-17 12:00:00"
                    }
                ]
            }
            """;

    @BeforeEach
    void setUp() {
        // Setup the WebClient mock chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getWeatherSummary_ValidCity_ReturnsWeatherData() {
        // Arrange
        String city = "London";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(SAMPLE_JSON_RESPONSE));

        // Act
        WeatherData result = weatherService.getWeatherSummary(city);

        // Assert
        assertNotNull(result);
        assertEquals("London", result.getCity());
        assertEquals(15.0, result.getAverageTemperature(), 0.1); // (15 + 20 + 10) / 3
        assertEquals("2024-01-16", result.getHottestDay()); // 20°C
        assertEquals("2024-01-17", result.getColdestDay()); // 10°C

        // Verify WebClient interactions
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(String.class);
    }

    @Test
    void getWeatherSummary_NullApiResponse_ThrowsCityNotFoundException() {
        // Arrange
        String city = "InvalidCity";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.justOrEmpty(null));

        // Act & Assert
        CityNotFoundException exception = assertThrows(CityNotFoundException.class,
                () -> weatherService.getWeatherSummary(city));

        assertEquals("City not found: InvalidCity", exception.getMessage());
    }

    @Test
    void getWeatherSummary_WebClientException_ThrowsExternalApiException() {
        // Arrange
        String city = "London";
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));

        // Act & Assert
        ExternalApiException exception = assertThrows(ExternalApiException.class,
                () -> weatherService.getWeatherSummary(city));

        assertTrue(exception.getMessage().contains("Error fetching data for city: London"));
        assertNotNull(exception.getCause());
    }

    @Test
    void getWeatherSummary_EmptyWeatherList_ReturnsDefault() {
        // Arrange
        String city = "London";
        String emptyListResponse = """
                {
                    "city": {
                        "name": "London"
                    },
                    "list": []
                }
                """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(emptyListResponse));

        // Act
        WeatherData result = weatherService.getWeatherSummary(city);

        // Assert
        assertNotNull(result);
        assertEquals("London", result.getCity());
        // Add checks for w
    }

    @Test
    void getWeatherSummary_SingleWeatherEntry_ReturnsCorrectData() {
        // Arrange
        String city = "Paris";
        String singleEntryResponse = """
            {
                "city": {
                    "name": "Paris"
                },
                "list": [
                    {
                        "main": {
                            "temp": 290.15
                        },
                        "dt_txt": "2024-01-15 12:00:00"
                    }
                ]
            }
            """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(singleEntryResponse));

        // Act
        WeatherData result = weatherService.getWeatherSummary(city);

        // Assert
        assertNotNull(result);
        assertEquals("Paris", result.getCity());
        assertEquals(17.0, result.getAverageTemperature(), 0.1);
        assertEquals("2024-01-15", result.getHottestDay());
        assertEquals("2024-01-15", result.getColdestDay());
    }

    @Test
    void getWeatherSummary_MalformedJson_ThrowsException() {
        // Arrange
        String city = "London";
        String malformedJson = "{ invalid json }";

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(malformedJson));

        // Act & Assert
        assertThrows(org.json.JSONException.class,
                () -> weatherService.getWeatherSummary(city));
    }

    @Test
    void getWeatherSummary_VerifyUriBuilding() {
        // Arrange
        String city = "New York";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(SAMPLE_JSON_RESPONSE));

        // Act
        weatherService.getWeatherSummary(city);

        // Assert - Verify that URI building function is called
        verify(requestHeadersUriSpec).uri(any(Function.class));

        // You can also test the URI building logic separately if needed
        // by extracting it to a separate method or using argument captors
    }

    @Test
    void getWeatherSummary_MultipleEntriesSameDay_HandlesCorrectly() {
        // Arrange
        String city = "Tokyo";
        String sameDayResponse = """
            {
                "city": {
                    "name": "Tokyo"
                },
                "list": [
                    {
                        "main": {
                            "temp": 285.15
                        },
                        "dt_txt": "2024-01-15 12:00:00"
                    },
                    {
                        "main": {
                            "temp": 295.15
                        },
                        "dt_txt": "2024-01-15 15:00:00"
                    }
                ]
            }
            """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(sameDayResponse));

        // Act
        WeatherData result = weatherService.getWeatherSummary(city);

        // Assert
        assertNotNull(result);
        assertEquals("Tokyo", result.getCity());
        assertEquals(17.0, result.getAverageTemperature(), 0.1); // (12 + 22) / 2
        assertEquals("2024-01-15", result.getHottestDay());
        assertEquals("2024-01-15", result.getColdestDay());
    }

    @Test
    void getWeatherSummary_ExtremeTemperatures_HandlesCorrectly() {
        // Arrange
        String city = "Antarctica";
        String extremeTempsResponse = """
            {
                "city": {
                    "name": "Antarctica"
                },
                "list": [
                    {
                        "main": {
                            "temp": 200.15
                        },
                        "dt_txt": "2024-01-15 12:00:00"
                    },
                    {
                        "main": {
                            "temp": 350.15
                        },
                        "dt_txt": "2024-01-16 12:00:00"
                    }
                ]
            }
            """;

        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(extremeTempsResponse));

        // Act
        WeatherData result = weatherService.getWeatherSummary(city);

        // Assert
        assertNotNull(result);
        assertEquals("Antarctica", result.getCity());
        assertEquals(1.5, result.getAverageTemperature(), 0.1); // (-73 + 77) / 2
        assertEquals("2024-01-16", result.getHottestDay());
        assertEquals("2024-01-15", result.getColdestDay());
    }
}