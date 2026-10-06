package com.kaamsafe.service;

import com.kaamsafe.dto.WorkabilityRequest;
import com.kaamsafe.dto.WorkabilityResponse;
import org.springframework.stereotype.Service;

@Service
public class WorkabilityService {

    public WorkabilityResponse analyze(WorkabilityRequest request) {
        // TODO Day 1/2: OSRM -> weather/AQI -> OSM -> elevation -> scoring.
        // Keep all business meaning in the backend. Frontend only renders this response.
        throw new UnsupportedOperationException("Workability engine not implemented yet");
    }
}
