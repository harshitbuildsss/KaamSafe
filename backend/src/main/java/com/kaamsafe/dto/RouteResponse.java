package com.kaamsafe.dto;

import com.kaamsafe.model.GreenCoverageLevel;
import java.util.List;

public record RouteResponse(
        String routeId,
        double distanceMeters,
        double durationSeconds,
        int score,
        boolean recommended,
        GreenCoverageLevel greenCoverageLevel,
        double totalAscentMeters,
        boolean waterPointNearby,
        List<String> reasons,
        Object geometry
) {}
