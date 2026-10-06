package com.kaamsafe.exception;

public record ApiError(ErrorBody error) {
    public record ErrorBody(String code, String message) {}
}
