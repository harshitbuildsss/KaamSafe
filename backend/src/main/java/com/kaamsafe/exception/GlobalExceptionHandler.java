package com.kaamsafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        new ApiError.ErrorBody(
                                "VALIDATION_ERROR",
                                message
                        )
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest(
            HttpMessageNotReadableException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        new ApiError.ErrorBody(
                                "VALIDATION_ERROR",
                                "Invalid request body or enum value."
                        )
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(
                        new ApiError.ErrorBody(
                                "VALIDATION_ERROR",
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException ex) {

        String message = ex.getMessage();

        if ("OSRM returned no routes".equals(message)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError(
                            new ApiError.ErrorBody(
                                    "NO_ROUTE_FOUND",
                                    message
                            )
                    ));
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError(
                        new ApiError.ErrorBody(
                                "UPSTREAM_UNAVAILABLE",
                                message != null
                                        ? message
                                        : "An upstream service is temporarily unavailable."
                        )
                ));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiError> handleUnsupported(
            UnsupportedOperationException ex) {

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError(
                        new ApiError.ErrorBody(
                                "UPSTREAM_UNAVAILABLE",
                                ex.getMessage()
                        )
                ));
    }
}