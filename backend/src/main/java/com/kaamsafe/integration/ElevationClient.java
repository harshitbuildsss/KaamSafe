package com.kaamsafe.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class ElevationClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ElevationClient(
            @Value("${open-meteo.base-url:https://api.open-meteo.com}") String baseUrl,
            ObjectMapper objectMapper
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    public double calculateTotalAscent(JsonNode geometry) {

        if (geometry == null || geometry.isNull()) {
            return 0;
        }

        JsonNode coordinates = geometry.get("coordinates");

        if (coordinates == null || !coordinates.isArray() || coordinates.isEmpty()) {
            return 0;
        }

        List<double[]> points = samplePoints(coordinates, 12);

        if (points.size() < 2) {
            return 0;
        }

        try {
            StringBuilder latitudes = new StringBuilder();
            StringBuilder longitudes = new StringBuilder();

            for (int i = 0; i < points.size(); i++) {
                if (i > 0) {
                    latitudes.append(',');
                    longitudes.append(',');
                }

                latitudes.append(points.get(i)[1]);
                longitudes.append(points.get(i)[0]);
            }

            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/elevation")
                            .queryParam("latitude", latitudes.toString())
                            .queryParam("longitude", longitudes.toString())
                            .build())
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode elevations = root.get("elevation");

            if (elevations == null || !elevations.isArray()) {
                return 0;
            }

            double totalAscent = 0;

            for (int i = 1; i < elevations.size(); i++) {
                double previous = elevations.get(i - 1).asDouble();
                double current = elevations.get(i).asDouble();
                double gain = current - previous;

                if (gain > 0) {
                    totalAscent += gain;
                }
            }

            return Math.round(totalAscent * 10.0) / 10.0;

        } catch (Exception e) {
            // Elevation is an enhancement. If the upstream service fails,
            // keep the main workability analysis alive with zero ascent.
            return 0;
        }
    }

    private List<double[]> samplePoints(JsonNode coordinates, int maxPoints) {

        List<double[]> points = new ArrayList<>();

        int total = coordinates.size();
        int count = Math.min(total, maxPoints);

        if (count == total) {
            for (JsonNode coordinate : coordinates) {
                addPoint(points, coordinate);
            }
            return points;
        }

        for (int i = 0; i < count; i++) {
            int index = (int) Math.round(
                    i * (total - 1.0) / (count - 1.0)
            );

            addPoint(points, coordinates.get(index));
        }

        return points;
    }

    private void addPoint(List<double[]> points, JsonNode coordinate) {

        if (coordinate == null
                || !coordinate.isArray()
                || coordinate.size() < 2) {
            return;
        }

        double longitude = coordinate.get(0).asDouble();
        double latitude = coordinate.get(1).asDouble();

        points.add(new double[]{longitude, latitude});
    }
}
