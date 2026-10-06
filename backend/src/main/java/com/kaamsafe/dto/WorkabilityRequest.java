package com.kaamsafe.dto;

import com.kaamsafe.model.Mobility;
import com.kaamsafe.model.WorkerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record WorkabilityRequest(
        @NotNull WorkerType workerType,
        @NotNull Mobility mobility,
        @NotNull @Valid LocationDto origin,
        @NotNull @Valid LocationDto destination
) {}
