package com.kaamsafe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaamsafe.dto.GeocodeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeocodeService {

    private final ObjectMapper objectMapper;

    private final String nominatimBaseUrl;

    private final HttpClient httpClient;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static final Duration CACHE_DURATION =
            Duration.ofMinutes(10);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(8);

    private static final long MIN_REQUEST_INTERVAL_MS = 1000;

    private long lastRequestTime = 0;

    public GeocodeService(
            ObjectMapper objectMapper,
            @Value("${nominatim.base-url}") String nominatimBaseUrl) {

        this.objectMapper = objectMapper;
        this.nominatimBaseUrl = nominatimBaseUrl;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    public GeocodeResponse geocode(String query) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Search query must not be empty."
            );
        }

        String normalizedQuery =
                query.trim().replaceAll("\\s+", " ");

        String cacheKey =
                normalizedQuery.toLowerCase();

        CacheEntry cached = cache.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            return cached.response();
        }

        try {
            waitForRateLimit();

            String encodedQuery =
                    URLEncoder.encode(
                            normalizedQuery,
                            StandardCharsets.UTF_8
                    );

            String url =
                    nominatimBaseUrl
                            + "/search"
                            + "?q=" + encodedQuery
                            + "&format=json"
                            + "&limit=5"
                            + "&addressdetails=1";

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(REQUEST_TIMEOUT)
                            .header(
                                    "User-Agent",
                                    "KaamSafe/1.0"
                            )
                            .header(
                                    "Accept",
                                    "application/json"
                            )
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "Nominatim returned HTTP "
                                + response.statusCode()
                );
            }

            GeocodeResponse geocodeResponse =
                    parseResponse(response.body());

            cache.put(
                    cacheKey,
                    new CacheEntry(
                            geocodeResponse,
                            System.currentTimeMillis()
                    )
            );

            return geocodeResponse;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Geocoding request was interrupted.",
                    e
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Geocoding service is temporarily unavailable.",
                    e
            );
        }
    }

    private GeocodeResponse parseResponse(
            String responseBody) throws IOException {

        JsonNode root =
                objectMapper.readTree(responseBody);

        List<GeocodeResponse.Result> results =
                new ArrayList<>();

        if (!root.isArray()) {
            return new GeocodeResponse(results);
        }

        for (JsonNode item : root) {

            JsonNode displayName =
                    item.get("display_name");

            JsonNode latitude =
                    item.get("lat");

            JsonNode longitude =
                    item.get("lon");

            if (displayName == null
                    || latitude == null
                    || longitude == null) {
                continue;
            }

            double lat =
                    Double.parseDouble(
                            latitude.asText()
                    );

            double lon =
                    Double.parseDouble(
                            longitude.asText()
                    );

            results.add(
                    new GeocodeResponse.Result(
                            displayName.asText(),
                            lat,
                            lon
                    )
            );
        }

        return new GeocodeResponse(results);
    }

    private synchronized void waitForRateLimit()
            throws InterruptedException {

        long now =
                System.currentTimeMillis();

        long elapsed =
                now - lastRequestTime;

        if (elapsed < MIN_REQUEST_INTERVAL_MS) {

            long waitTime =
                    MIN_REQUEST_INTERVAL_MS - elapsed;

            Thread.sleep(waitTime);
        }

        lastRequestTime =
                System.currentTimeMillis();
    }

    private record CacheEntry(
            GeocodeResponse response,
            long createdAt
    ) {

        boolean isExpired() {

            return System.currentTimeMillis()
                    - createdAt
                    > CACHE_DURATION.toMillis();
        }
    }
}