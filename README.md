# Weather Data Backend Application

## Overview

The **Weather Data Backend** is a Spring Boot application designed to fetch, process, and analyze weather data from the **OpenWeatherMap API**. This application demonstrates the use of advanced **Spring** features, including **asynchronous processing** and **caching**, to efficiently handle weather data for cities around the world.

Key functionalities of the application:
- **Fetches weather data** from an external API for a specified city.
- **Computes the average temperature** for the last 7 days.
- **Determines the hottest and coldest days** based on the weather data.
- **Caches the results** for each city to reduce API calls.
- Exposes a **RESTful API endpoint** to fetch weather summaries for a city.

## Features

- **External API Integration**: Fetches weather data using the OpenWeatherMap API.
- **Asynchronous Processing**: Weather data is fetched and processed asynchronously to avoid blocking the main thread.
- **Caching**: The weather summary for each city is cached for 30 minutes to optimize performance.
- **Error Handling**: Gracefully handles invalid city names and API failures.
- **Unit Tests**: Comprehensive unit tests for the service and controller layers using JUnit and Mockito.

## Requirements

- **Java**: 17 or higher
- **Maven**: 3.6+
- **Spring Boot**: 3+
- **OpenWeatherMap API Key**: You must sign up at [OpenWeatherMap](https://openweathermap.org/api) to obtain your API key.

## Setup Instructions

### 1. Clone the Repository
Clone the repository to your local machine:

```bash
https://github.com/Vishnuka084/VISiONEXDIGITAL_TASK.git
cd VISiONEXDIGITAL_TASK

