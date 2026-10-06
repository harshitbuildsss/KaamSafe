package com.kaamsafe.scoring;

import com.kaamsafe.model.Mobility;

public record WorkerWeights(
        double heat,
        double aqi,
        double rain,
        double effort,
        double time,
        double shade
) {
    public static WorkerWeights forMobility(Mobility mobility) {
        return switch (mobility) {
            case BICYCLE -> new WorkerWeights(0.25, 0.15, 0.20, 0.25, 0.10, 0.05);
            case MOTORCYCLE -> new WorkerWeights(0.15, 0.25, 0.25, 0.05, 0.25, 0.05);
            case HANDCART -> new WorkerWeights(0.25, 0.15, 0.20, 0.25, 0.10, 0.05);
            case WALKING -> new WorkerWeights(0.30, 0.20, 0.20, 0.20, 0.05, 0.05);
            case E_RICKSHAW -> new WorkerWeights(0.15, 0.25, 0.25, 0.05, 0.25, 0.05);
        };
    }
}
