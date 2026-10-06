package com.kaamsafe.scoring;

import com.kaamsafe.model.Mobility;
import org.springframework.stereotype.Component;

@Component
public class ScoreEngine {

    // v1 weights are mobility-based and explainable. Do not move scoring into React.
    public int score(Mobility mobility,
                     int heatRisk,
                     int aqiRisk,
                     int rainRisk,
                     int effortRisk,
                     int timeRisk,
                     int shadeBenefit) {
        // TODO: implement the frozen weight table from docs/API_CONTRACT.md.
        return 0;
    }
}
