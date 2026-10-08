package com.kaamsafe.scoring;

import com.kaamsafe.model.Mobility;
import org.springframework.stereotype.Component;

@Component
public class ScoreEngine {

    // v1 scoring is mobility-based and explainable.
    // All risk inputs are expected to be in the range 0-100.
    public int score(
            Mobility mobility,
            int heatRisk,
            int aqiRisk,
            int rainRisk,
            int effortRisk,
            int timeRisk,
            int shadeBenefit
    ) {

        WorkerWeights weights = WorkerWeights.forMobility(mobility);

        // Keep every factor inside the expected 0-100 range.
        heatRisk = clamp(heatRisk);
        aqiRisk = clamp(aqiRisk);
        rainRisk = clamp(rainRisk);
        effortRisk = clamp(effortRisk);
        timeRisk = clamp(timeRisk);
        shadeBenefit = clamp(shadeBenefit);

        double score =
                100.0
                        - (heatRisk * weights.heat())
                        - (aqiRisk * weights.aqi())
                        - (rainRisk * weights.rain())
                        - (effortRisk * weights.effort())
                        - (timeRisk * weights.time())
                        + (shadeBenefit * weights.shade());

        return (int) Math.round(clamp(score, 0.0, 100.0));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}