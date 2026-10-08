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
public class OsrmClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OsrmClient(
            @Value("${osrm.base-url}") String baseUrl,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.objectMapper = objectMapper;
    }

    public List<OsrmRoute> getRoutes(
            LocationDto origin,
            LocationDto destination
    ) {

        String coordinates =
                origin.longitude() + "," + origin.latitude()
                        + ";"
                        + destination.longitude() + "," + destination.latitude();

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/route/v1/driving/{coordinates}")
                        .queryParam("alternatives", "true")
                        .queryParam("overview", "full")
                        .queryParam("geometries", "geojson")
                        .build(coordinates))
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);

            if (!"Ok".equals(root.path("code").asText())) {
                throw new IllegalStateException(
                        "OSRM returned error: " + root.path("code").asText()
                );
            }

            List<OsrmRoute> routes = new ArrayList<>();

            for (JsonNode route : root.path("routes")) {

                double distanceMeters =
                        route.path("distance").asDouble();

                double durationSeconds =
                        route.path("duration").asDouble();

                JsonNode geometry =
                        route.path("geometry");

                routes.add(
                        new OsrmRoute(
                                distanceMeters,
                                durationSeconds,
                                geometry
                        )
                );
            }

            return routes;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to parse OSRM response",
                    e
            );
        }
    }

    public record OsrmRoute(
            double distanceMeters,
            double durationSeconds,
            JsonNode geometry
    ) {}
}