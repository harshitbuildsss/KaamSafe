package com.kaamsafe.service;

import com.kaamsafe.dto.GeocodeResponse;
import org.springframework.stereotype.Service;

@Service
public class GeocodeService {

    public GeocodeResponse geocode(String query) {
        // TODO Day 1: proxy Nominatim and add a short cache.
        throw new UnsupportedOperationException("Geocoding not implemented yet");
    }
}
