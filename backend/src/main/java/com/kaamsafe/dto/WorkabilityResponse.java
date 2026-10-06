package com.kaamsafe.dto;

import com.kaamsafe.model.Condition;
import com.kaamsafe.model.DataSource;
import java.util.List;

public record WorkabilityResponse(
        DataSource dataSource,
        int workabilityScore,
        Condition condition,
        WorkWindow recommendedWorkWindow,
        String recommendedRouteId,
        EnvironmentSummary environment,
        RouteFactors factors,
        List<RouteResponse> routes
) {
    public record WorkWindow(String start, String end) {}

    public record EnvironmentSummary(
            double temperatureCelsius,
            int aqi,
            int rainRiskPercent
    ) {}

    public record RouteFactors(
            int heatRisk,
            int aqiRisk,
            int rainRisk
    ) {}
}
