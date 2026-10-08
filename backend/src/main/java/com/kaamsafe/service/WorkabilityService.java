package com.kaamsafe.service;

import com.kaamsafe.dto.RouteResponse;
import com.kaamsafe.dto.WorkabilityRequest;
import com.kaamsafe.dto.WorkabilityResponse;
import com.kaamsafe.integration.OsmClient;
import com.kaamsafe.integration.ElevationClient;
import com.kaamsafe.integration.OpenMeteoClient;
import com.kaamsafe.integration.OsrmClient;
import com.kaamsafe.model.Condition;
import com.kaamsafe.model.DataSource;
import com.kaamsafe.scoring.ScoreEngine;
import org.springframework.stereotype.Service;
import com.kaamsafe.model.GreenCoverageLevel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkabilityService {

    private final OsrmClient osrmClient;
    private final OpenMeteoClient openMeteoClient;
    private final ScoreEngine scoreEngine;
    private final OsmClient osmClient;
    private final ElevationClient elevationClient;

    public WorkabilityService(
            OsrmClient osrmClient,
            OpenMeteoClient openMeteoClient,
            ScoreEngine scoreEngine,
            OsmClient osmClient,
            ElevationClient elevationClient
    ) {
        this.osrmClient = osrmClient;
        this.openMeteoClient = openMeteoClient;
        this.scoreEngine = scoreEngine;
        this.osmClient = osmClient;
        this.elevationClient = elevationClient;
    }

    public WorkabilityResponse analyze(WorkabilityRequest request) {

        List<OsrmClient.OsrmRoute> osrmRoutes =
                osrmClient.getRoutes(
                        request.origin(),
                        request.destination()
                );

        if (osrmRoutes.isEmpty()) {
            throw new IllegalStateException(
                    "OSRM returned no routes"
            );
        }

        OpenMeteoClient.WeatherData weather =
                openMeteoClient.getCurrentConditions(
                        request.origin()
                );

        List<OpenMeteoClient.HourlyCondition> hourlyConditions =
                openMeteoClient.getHourlyConditions(
                        request.origin()
                );

        int heatRisk =
                calculateHeatRisk(
                        weather.temperatureCelsius()
                );

        int aqiRisk =
                calculateAqiRisk(weather.aqi());

        int rainRisk =
                clamp(weather.rainRiskPercent());

        WorkabilityResponse.EnvironmentSummary environment =
                new WorkabilityResponse.EnvironmentSummary(
                        weather.temperatureCelsius(),
                        weather.aqi(),
                        weather.rainRiskPercent()
                );

        WorkabilityResponse.RouteFactors factors =
                new WorkabilityResponse.RouteFactors(
                        heatRisk,
                        aqiRisk,
                        rainRisk
                );

        double fastestDuration =
                osrmRoutes.stream()
                        .mapToDouble(
                                OsrmClient.OsrmRoute::durationSeconds
                        )
                        .min()
                        .orElse(0);

        double slowestDuration =
                osrmRoutes.stream()
                        .mapToDouble(
                                OsrmClient.OsrmRoute::durationSeconds
                        )
                        .max()
                        .orElse(fastestDuration);

        List<RouteResponse> routes =
                new ArrayList<>();

        int bestScore = -1;
        String recommendedRouteId = null;

        for (int i = 0; i < osrmRoutes.size(); i++) {

            OsrmClient.OsrmRoute route =
                    osrmRoutes.get(i);

            String routeId =
                    "route-" + (i + 1);

            int timeRisk =
                    calculateRelativeRisk(
                            route.durationSeconds(),
                            fastestDuration,
                            slowestDuration
                    );

            /*
             * Analyze the actual OSRM route using OpenStreetMap.
             *
             * This samples points along the route and checks
             * for parks, forests, woodland, drinking water
             * and toilet/rest facilities.
             */
            OsmClient.OsmFeatures osmFeatures =
                    osmClient.analyzeRoute(
                            route.geometry()
                    );

            int greenFeatures =
                    osmFeatures.greenFeatures();

            int waterPoints =
                    osmFeatures.waterPoints();

            /*
             * Analyze route elevation.
             * Total ascent is the sum of positive elevation
             * changes along sampled route points.
             */
            double totalAscentMeters =
                    elevationClient.calculateTotalAscent(
                            route.geometry()
                    );

            int effortRisk =
                    calculateEffortRisk(
                            totalAscentMeters,
                            route.distanceMeters()
                    );

            /*
             * Convert the number of green features into a
             * simple shade/environment benefit.
             *
             * This is an MVP proxy, not literal percentage
             * tree coverage.
             */
            int shadeBenefit =
                    Math.min(
                            100,
                            greenFeatures * 20
                    );

            int score =
                    scoreEngine.score(
                            request.mobility(),
                            heatRisk,
                            aqiRisk,
                            rainRisk,
                            effortRisk,
                            timeRisk,
                            shadeBenefit
                    );

            if (score > bestScore) {
                bestScore = score;
                recommendedRouteId = routeId;
            }

            GreenCoverageLevel greenCoverageLevel =
                    determineGreenCoverageLevel(
                            greenFeatures
                    );

            boolean waterPointNearby =
                    waterPoints > 0;

            List<String> reasons =
                    buildReasons(
                            timeRisk,
                            heatRisk,
                            aqiRisk,
                            rainRisk,
                            greenFeatures,
                            waterPointNearby
                    );

            if (totalAscentMeters >= 50) {
                reasons.add("Higher physical effort from uphill ascent");
            } else if (totalAscentMeters >= 20) {
                reasons.add("Some uphill ascent");
            }

            routes.add(
                    new RouteResponse(
                            routeId,
                            route.distanceMeters(),
                            route.durationSeconds(),
                            score,
                            false,
                            greenCoverageLevel,
                            totalAscentMeters,
                            waterPointNearby,
                            reasons,
                            route.geometry()
                    )
            );
        }

        final String selectedRouteId =
                recommendedRouteId;

        List<RouteResponse> finalRoutes =
                routes.stream()
                        .map(route ->
                                new RouteResponse(
                                        route.routeId(),
                                        route.distanceMeters(),
                                        route.durationSeconds(),
                                        route.score(),
                                        route.routeId()
                                                .equals(selectedRouteId),
                                        route.greenCoverageLevel(),
                                        route.totalAscentMeters(),
                                        route.waterPointNearby(),
                                        route.reasons(),
                                        route.geometry()
                                )
                        )
                        .toList();

        Condition condition =
                conditionFromScore(bestScore);

        WorkabilityResponse.WorkWindow workWindow =
                calculateRecommendedWorkWindow(
                        request,
                        hourlyConditions
                );

        return new WorkabilityResponse(
                DataSource.live,
                bestScore,
                condition,
                workWindow,
                recommendedRouteId,
                environment,
                factors,
                finalRoutes
        );
    }

    private WorkabilityResponse.WorkWindow
    calculateRecommendedWorkWindow(
            WorkabilityRequest request,
            List<OpenMeteoClient.HourlyCondition> hourlyConditions
    ) {

        if (hourlyConditions.isEmpty()) {
            return new WorkabilityResponse.WorkWindow(
                    "00:00",
                    "00:00"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        List<OpenMeteoClient.HourlyCondition> upcoming =
                hourlyConditions.stream()
                        .filter(hour -> {
                            try {
                                LocalDateTime time =
                                        LocalDateTime.parse(
                                                hour.time(),
                                                DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                        );

                                return !time.isBefore(
                                        now.withMinute(0)
                                                .withSecond(0)
                                                .withNano(0)
                                );

                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .limit(8)
                        .toList();

        if (upcoming.isEmpty()) {
            upcoming =
                    hourlyConditions.stream()
                            .limit(8)
                            .toList();
        }

        if (upcoming.size() < 3) {

            OpenMeteoClient.HourlyCondition first =
                    upcoming.get(0);

            OpenMeteoClient.HourlyCondition last =
                    upcoming.get(upcoming.size() - 1);

            return new WorkabilityResponse.WorkWindow(
                    formatHour(first.time()),
                    formatHour(
                            LocalDateTime.parse(
                                    last.time(),
                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            ).plusHours(1).toString()
                    )
            );
        }

        int bestWindowScore = -1;
        int bestStartIndex = 0;

        for (int start = 0;
             start <= upcoming.size() - 3;
             start++) {

            int totalScore = 0;

            for (int i = start; i < start + 3; i++) {

                OpenMeteoClient.HourlyCondition hour =
                        upcoming.get(i);

                int hourlyHeatRisk =
                        calculateHeatRisk(
                                hour.temperatureCelsius()
                        );

                int hourlyAqiRisk =
                        calculateAqiRisk(hour.aqi());

                int hourlyRainRisk =
                        clamp(hour.rainRiskPercent());

                int hourScore =
                        scoreEngine.score(
                                request.mobility(),
                                hourlyHeatRisk,
                                hourlyAqiRisk,
                                hourlyRainRisk,
                                0,
                                0,
                                0
                        );

                totalScore += hourScore;
            }

            int averageScore =
                    totalScore / 3;

            if (averageScore > bestWindowScore) {
                bestWindowScore = averageScore;
                bestStartIndex = start;
            }
        }

        OpenMeteoClient.HourlyCondition start =
                upcoming.get(bestStartIndex);

        LocalDateTime startTime =
                LocalDateTime.parse(
                        start.time(),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );

        LocalDateTime endTime =
                startTime.plusHours(3);

        return new WorkabilityResponse.WorkWindow(
                startTime.format(
                        DateTimeFormatter.ofPattern("HH:mm")
                ),
                endTime.format(
                        DateTimeFormatter.ofPattern("HH:mm")
                )
        );
    }

    private String formatHour(String timestamp) {

        try {
            LocalDateTime dateTime =
                    LocalDateTime.parse(
                            timestamp,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    );

            return dateTime.format(
                    DateTimeFormatter.ofPattern("HH:mm")
            );

        } catch (Exception e) {
            return timestamp;
        }
    }

    private int calculateHeatRisk(
            double temperature
    ) {

        if (temperature <= 25.0) {
            return 0;
        }

        if (temperature >= 40.0) {
            return 100;
        }

        return (int) Math.round(
                ((temperature - 25.0) / 15.0) * 100
        );
    }

    private int calculateAqiRisk(int aqi) {

        if (aqi <= 50) {
            return 0;
        }

        if (aqi <= 100) {
            return 25;
        }

        if (aqi <= 150) {
            return 50;
        }

        if (aqi <= 200) {
            return 75;
        }

        return 100;
    }

    private int calculateEffortRisk(
            double totalAscentMeters,
            double distanceMeters
    ) {

        if (totalAscentMeters <= 0 || distanceMeters <= 0) {
            return 0;
        }

        double ascentPerKilometre =
                totalAscentMeters / (distanceMeters / 1000.0);

        if (ascentPerKilometre <= 2.0) {
            return 0;
        }

        if (ascentPerKilometre >= 20.0) {
            return 100;
        }

        return (int) Math.round(
                ((ascentPerKilometre - 2.0) / 18.0) * 100.0
        );
    }

    private int calculateRelativeRisk(
            double duration,
            double fastest,
            double slowest
    ) {

        if (slowest <= fastest) {
            return 0;
        }

        double risk =
                ((duration - fastest)
                        / (slowest - fastest))
                        * 100.0;

        return (int) Math.round(
                Math.max(
                        0,
                        Math.min(100, risk)
                )
        );
    }

    private List<String> buildReasons(
            int timeRisk,
            int heatRisk,
            int aqiRisk,
            int rainRisk,
            int greenFeatures,
            boolean waterPointNearby
    ) {

        List<String> reasons =
                new ArrayList<>();

        if (timeRisk == 0) {
            reasons.add(
                    "Fastest available route"
            );
        }

        if (heatRisk >= 70) {
            reasons.add(
                    "High heat exposure"
            );

        } else if (heatRisk >= 40) {
            reasons.add(
                    "Moderate heat exposure"
            );
        }

        if (aqiRisk >= 75) {
            reasons.add(
                    "Very poor air quality"
            );

        } else if (aqiRisk >= 50) {
            reasons.add(
                    "Poor air quality"
            );
        }

        if (rainRisk >= 70) {
            reasons.add(
                    "High rain risk"
            );

        } else if (rainRisk >= 40) {
            reasons.add(
                    "Moderate rain risk"
            );
        }

        if (greenFeatures >= 3) {
            reasons.add(
                    "Good green coverage"
            );

        } else if (greenFeatures > 0) {
            reasons.add(
                    "Some green coverage"
            );
        }

        if (waterPointNearby) {
            reasons.add(
                    "Water/rest facility nearby"
            );
        }

        return reasons;
    }

    private GreenCoverageLevel determineGreenCoverageLevel(
            int greenFeatures
    ) {

        if (greenFeatures >= 5) {
            return GreenCoverageLevel.HIGH;
        }

        if (greenFeatures >= 2) {
            return GreenCoverageLevel.MEDIUM;
        }

        return GreenCoverageLevel.LOW;
    }

    private Condition conditionFromScore(
            int score
    ) {

        if (score >= 75) {
            return Condition.GOOD;
        }

        if (score >= 50) {
            return Condition.MODERATE;
        }

        if (score >= 25) {
            return Condition.DIFFICULT;
        }

        return Condition.SEVERE;
    }

    private int clamp(int value) {
        return Math.max(
                0,
                Math.min(100, value)
        );
    }
}