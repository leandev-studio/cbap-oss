package com.cbap.api.controller;

import com.cbap.api.service.MeasureEvaluationService;
import com.cbap.api.service.MeasureMetadataService;
import com.cbap.persistence.entity.Measure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for measure endpoints.
 */
@RestController
@RequestMapping("/api/v1")
public class MeasureController {

    private final MeasureMetadataService measureMetadataService;
    private final MeasureEvaluationService measureEvaluationService;

    public MeasureController(
            MeasureMetadataService measureMetadataService,
            MeasureEvaluationService measureEvaluationService) {
        this.measureMetadataService = measureMetadataService;
        this.measureEvaluationService = measureEvaluationService;
    }

    /**
     * Get all measures.
     * GET /api/v1/metadata/measures
     */
    @GetMapping("/metadata/measures")
    public ResponseEntity<Map<String, Object>> getAllMeasures() {
        if (measureMetadataService == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(Map.of("error", "Measures not yet implemented"));
        }

        List<Measure> measures = measureMetadataService.getAllMeasures();
        
        Map<String, Object> response = new HashMap<>();
        response.put("measures", measures);
        response.put("count", measures.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific measure by identifier and optional version.
     * GET /api/v1/metadata/measures/{measureIdentifier}?version=1
     */
    @GetMapping("/metadata/measures/{measureIdentifier}")
    public ResponseEntity<Map<String, Object>> getMeasure(
            @PathVariable String measureIdentifier,
            @RequestParam(required = false) Integer version) {
        
        Optional<Measure> measure = measureMetadataService.getMeasure(measureIdentifier, version);
        
        if (measure.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Measure not found: " + measureIdentifier + 
                            (version != null ? " version " + version : "")));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("measure", measure.get());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create or update a measure (admin only).
     * POST /api/v1/metadata/measures
     */
    @PostMapping("/metadata/measures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createOrUpdateMeasure(
            @RequestBody Measure measure,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        try {
            Measure savedMeasure = measureMetadataService.saveMeasure(measure);
            
            Map<String, Object> response = new HashMap<>();
            response.put("measure", savedMeasure);
            response.put("message", "Measure saved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }

    /**
     * Evaluate a measure.
     * POST /api/v1/measures/{measureIdentifier}/evaluate?version=1
     */
    @PostMapping("/measures/{measureIdentifier}/evaluate")
    public ResponseEntity<Map<String, Object>> evaluateMeasure(
            @PathVariable String measureIdentifier,
            @RequestParam(required = false) Integer version,
            @RequestBody(required = false) Map<String, Object> parameters,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Authentication required"));
        }

        try {
            Object result = measureEvaluationService.evaluateMeasure(
                    measureIdentifier, 
                    version, 
                    parameters != null ? parameters : Map.of());
            
            Map<String, Object> response = new HashMap<>();
            response.put("measureIdentifier", measureIdentifier);
            response.put("version", version);
            response.put("result", result);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Bad Request", "message", e.getMessage()));
        }
    }
}
