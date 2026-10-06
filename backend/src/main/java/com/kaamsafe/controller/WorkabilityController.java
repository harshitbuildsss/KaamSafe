package com.kaamsafe.controller;

import com.kaamsafe.dto.WorkabilityRequest;
import com.kaamsafe.dto.WorkabilityResponse;
import com.kaamsafe.service.WorkabilityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workability")
public class WorkabilityController {

    private final WorkabilityService workabilityService;

    public WorkabilityController(WorkabilityService workabilityService) {
        this.workabilityService = workabilityService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<WorkabilityResponse> analyze(
            @Valid @RequestBody WorkabilityRequest request) {
        return ResponseEntity.ok(workabilityService.analyze(request));
    }
}
