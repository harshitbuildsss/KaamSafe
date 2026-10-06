package com.kaamsafe.controller;

import com.kaamsafe.dto.GeocodeResponse;
import com.kaamsafe.service.GeocodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geocode")
public class GeocodeController {

    private final GeocodeService geocodeService;

    public GeocodeController(GeocodeService geocodeService) {
        this.geocodeService = geocodeService;
    }

    @GetMapping
    public GeocodeResponse geocode(@RequestParam String query) {
        return geocodeService.geocode(query);
    }
}
