package com.kaamsafe.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Component
public class OsmClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OsmClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${overpass.base-url:https://overpass-api.de}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        this.objectMapper = objectMapper;
    }

    /**
     * Sends an Overpass query.
     *
     * If Overpass is temporarily unavailable or times out,
     * return an empty JSON object instead of crashing the
     * entire workability analysis.
     */
    private JsonNode query(String overpassQuery) {

        try {

            String encodedQuery =
                    URLEncoder.encode(
                            overpassQuery,
                            StandardCharsets.UTF_8
                    );

            String response =
                    restClient
                            .post()
                            .uri("/api/interpreter")
                            .contentType(
                                    MediaType.APPLICATION_FORM_URLENCODED
                            )
                            .body("data=" + encodedQuery)
                            .retrieve()
                            .body(String.class);

            if (response == null || response.isBlank()) {
                return objectMapper.createObjectNode();
            }

            return objectMapper.readTree(response);

        } catch (RestClientResponseException ex) {

            System.out.println(
                    "OSM/Overpass unavailable: HTTP "
                            + ex.getStatusCode().value()
                            + ". Continuing without OSM route data."
            );

            return objectMapper.createObjectNode();

        } catch (Exception ex) {

            System.out.println(
                    "OSM/Overpass request failed: "
                            + ex.getMessage()
                            + ". Continuing without OSM route data."
            );

            return objectMapper.createObjectNode();
        }
    }

    /**
     * Kept for the existing OSM test endpoint.
     *
     * Finds green areas, drinking water points and toilets
     * around one location.
     */
    public OsmFeatures findGreenAndWaterFeatures(
            double latitude,
            double longitude,
            int radiusMeters
    ) {

        String overpassQuery =
                "[out:json][timeout:8];"
                        + "("
                        + "way(around:"
                        + radiusMeters
                        + ","
                        + latitude
                        + ","
                        + longitude
                        + ")[leisure=park];"

                        + "way(around:"
                        + radiusMeters
                        + ","
                        + latitude
                        + ","
                        + longitude
                        + ")[landuse=forest];"

                        + "way(around:"
                        + radiusMeters
                        + ","
                        + latitude
                        + ","
                        + longitude
                        + ")[natural=wood];"

                        + "node(around:"
                        + radiusMeters
                        + ","
                        + latitude
                        + ","
                        + longitude
                        + ")[amenity=drinking_water];"

                        + "node(around:"
                        + radiusMeters
                        + ","
                        + latitude
                        + ","
                        + longitude
                        + ")[amenity=toilets];"

                        + ");"
                        + "out center;";

        JsonNode response = query(overpassQuery);

        return parseFeatures(response);
    }

    /**
     * Analyzes an entire OSRM route.
     *
     * IMPORTANT:
     * Instead of making one Overpass request for every sampled
     * point, all sampled points are combined into ONE Overpass
     * query per route.
     *
     * This dramatically reduces the number of external requests.
     */
    public OsmFeatures analyzeRoute(JsonNode geometry) {

        if (geometry == null
                || geometry.isNull()
                || !geometry.has("coordinates")) {

            return new OsmFeatures(0, 0);
        }

        JsonNode coordinates =
                geometry.get("coordinates");

        if (!coordinates.isArray()
                || coordinates.isEmpty()) {

            return new OsmFeatures(0, 0);
        }

        /*
         * Sample at most 3 points:
         *
         * 1. beginning
         * 2. middle
         * 3. end
         *
         * This is intentionally lightweight so the public
         * Overpass API is not hammered during a demo.
         */
        int pointCount = coordinates.size();

        int[] indexes;

        if (pointCount == 1) {

            indexes = new int[]{
                    0
            };

        } else if (pointCount == 2) {

            indexes = new int[]{
                    0,
                    1
            };

        } else {

            indexes = new int[]{
                    0,
                    pointCount / 2,
                    pointCount - 1
            };
        }

        StringBuilder query =
                new StringBuilder(
                        "[out:json][timeout:8];("
                );

        for (int index : indexes) {

            JsonNode point =
                    coordinates.get(index);

            if (point == null
                    || !point.isArray()
                    || point.size() < 2) {

                continue;
            }

            /*
             * GeoJSON coordinates are:
             *
             * [longitude, latitude]
             */
            double longitude =
                    point.get(0).asDouble();

            double latitude =
                    point.get(1).asDouble();

            /*
             * 120m radius gives a reasonable MVP
             * approximation of nearby shade/rest facilities.
             */
            int radiusMeters = 120;

            query.append(
                    "way(around:"
                            + radiusMeters
                            + ","
                            + latitude
                            + ","
                            + longitude
                            + ")[leisure=park];"
            );

            query.append(
                    "way(around:"
                            + radiusMeters
                            + ","
                            + latitude
                            + ","
                            + longitude
                            + ")[landuse=forest];"
            );

            query.append(
                    "way(around:"
                            + radiusMeters
                            + ","
                            + latitude
                            + ","
                            + longitude
                            + ")[natural=wood];"
            );

            query.append(
                    "node(around:"
                            + radiusMeters
                            + ","
                            + latitude
                            + ","
                            + longitude
                            + ")[amenity=drinking_water];"
            );

            query.append(
                    "node(around:"
                            + radiusMeters
                            + ","
                            + latitude
                            + ","
                            + longitude
                            + ")[amenity=toilets];"
            );
        }

        query.append(");out center;");

        JsonNode response =
                query(query.toString());

        return parseFeatures(response);
    }

    /**
     * Parses Overpass response.
     *
     * We deduplicate OSM element IDs because the same park,
     * toilet or water point can appear around multiple sampled
     * route points.
     */
    private OsmFeatures parseFeatures(JsonNode response) {

        if (response == null
                || !response.has("elements")) {

            return new OsmFeatures(0, 0);
        }

        Set<String> greenIds =
                new HashSet<>();

        Set<String> waterIds =
                new HashSet<>();

        JsonNode elements =
                response.get("elements");

        if (!elements.isArray()) {

            return new OsmFeatures(0, 0);
        }

        for (JsonNode element : elements) {

            String type =
                    element.path("type").asText();

            String id =
                    element.path("id").asText();

            if (id.isBlank()) {
                continue;
            }

            JsonNode tags =
                    element.get("tags");

            if (tags == null
                    || tags.isNull()) {

                continue;
            }

            String leisure =
                    tags.path("leisure").asText();

            String landuse =
                    tags.path("landuse").asText();

            String natural =
                    tags.path("natural").asText();

            String amenity =
                    tags.path("amenity").asText();

            /*
             * Green features:
             *
             * - parks
             * - forests
             * - woodland
             */
            if ("way".equals(type)
                    && (
                    "park".equals(leisure)
                            || "forest".equals(landuse)
                            || "wood".equals(natural)
            )) {

                greenIds.add(
                        type + ":" + id
                );
            }

            /*
             * Water/rest features:
             *
             * - drinking water
             * - toilets
             */
            if ("node".equals(type)
                    && (
                    "drinking_water".equals(amenity)
                            || "toilets".equals(amenity)
            )) {

                waterIds.add(
                        type + ":" + id
                );
            }
        }

        return new OsmFeatures(
                greenIds.size(),
                waterIds.size()
        );
    }

    /**
     * Result returned to WorkabilityService.
     */
    public record OsmFeatures(
            int greenFeatures,
            int waterPoints
    ) {
    }
}