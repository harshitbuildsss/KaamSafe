package com.kaamsafe.integration;

import com.kaamsafe.dto.LocationDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpenMeteoClient {

    private final RestClient weatherClient;
    private final RestClient airQualityClient;
    private final ObjectMapper objectMapper;

    public OpenMeteoClient(
            @Value("${open-meteo.base-url}") String baseUrl,
            ObjectMapper objectMapper
    ) {
        this.weatherClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.airQualityClient = RestClient.builder()
                .baseUrl("https://air-quality-api.open-meteo.com")
                .build();

        this.objectMapper = objectMapper;
    }

    public WeatherData getCurrentConditions(LocationDto location) {

        String weatherResponse = weatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", location.latitude())
                        .queryParam("longitude", location.longitude())
                        .queryParam("current", "temperature_2m,precipitation")
                        .queryParam("hourly", "precipitation_probability")
                        .queryParam("forecast_days", 1)
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(String.class);

        String airResponse = airQualityClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/air-quality")
                        .queryParam("latitude", location.latitude())
                        .queryParam("longitude", location.longitude())
                        .queryParam("current", "european_aqi")
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode weather = objectMapper.readTree(weatherResponse);
            JsonNode air = objectMapper.readTree(airResponse);

            double temperature =
                    weather.path("current")
                            .path("temperature_2m")
                            .asDouble();

            double precipitation =
                    weather.path("current")
                            .path("precipitation")
                            .asDouble();

            int aqi =
                    air.path("current")
                            .path("european_aqi")
                            .asInt();

            int rainRisk = calculateRainRisk(weather);

            return new WeatherData(
                    temperature,
                    aqi,
                    rainRisk,
                    precipitation
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse Open-Meteo response",
                    e
            );
        }
    }

    public List<HourlyCondition> getHourlyConditions(LocationDto location) {

        String weatherResponse = weatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", location.latitude())
                        .queryParam("longitude", location.longitude())
                        .queryParam(
                                "hourly",
                                "temperature_2m,precipitation_probability"
                        )
                        .queryParam("forecast_days", 1)
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(String.class);

        String airResponse = airQualityClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/air-quality")
                        .queryParam("latitude", location.latitude())
                        .queryParam("longitude", location.longitude())
                        .queryParam("hourly", "european_aqi")
                        .queryParam("forecast_days", 1)
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode weather = objectMapper.readTree(weatherResponse);
            JsonNode air = objectMapper.readTree(airResponse);

            JsonNode times =
                    weather.path("hourly").path("time");

            JsonNode temperatures =
                    weather.path("hourly").path("temperature_2m");

            JsonNode rainProbabilities =
                    weather.path("hourly")
                            .path("precipitation_probability");

            JsonNode aqis =
                    air.path("hourly").path("european_aqi");

            List<HourlyCondition> conditions = new ArrayList<>();

            int count = Math.min(
                    times.size(),
                    Math.min(
                            temperatures.size(),
                            Math.min(
                                    rainProbabilities.size(),
                                    aqis.size()
                            )
                    )
            );

            for (int i = 0; i < count; i++) {

                conditions.add(
                        new HourlyCondition(
                                times.get(i).asText(),
                                temperatures.get(i).asDouble(),
                                aqis.get(i).asInt(),
                                rainProbabilities.get(i).asInt()
                        )
                );
            }

            return conditions;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse hourly Open-Meteo response",
                    e
            );
        }
    }

    private int calculateRainRisk(JsonNode weather) {

        JsonNode probabilities =
                weather.path("hourly")
                        .path("precipitation_probability");

        if (!probabilities.isArray() || probabilities.isEmpty()) {
            return 0;
        }

        int maximum = 0;

        for (JsonNode probability : probabilities) {
            maximum = Math.max(
                    maximum,
                    probability.asInt()
            );
        }

        return maximum;
    }

    public record WeatherData(
            double temperatureCelsius,
            int aqi,
            int rainRiskPercent,
            double precipitationMillimeters
    ) {
    }

    public record HourlyCondition(
            String time,
            double temperatureCelsius,
            int aqi,
            int rainRiskPercent
    ) {
    }
}