package com.kaamsafe.dto;

import java.util.List;

public record GeocodeResponse(List<Result> results) {
    public record Result(String label, double latitude, double longitude) {}
}
