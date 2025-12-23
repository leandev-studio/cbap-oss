package com.cbap.api.service;

import com.cbap.api.service.expression.ExpressionEvaluator;
import com.cbap.persistence.entity.Measure;
import com.cbap.persistence.repository.MeasureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for measure evaluation.
 * 
 * Evaluates measures with parameter resolution, expression evaluation,
 * and aggregate functions (sum, count, exists).
 */
@Service
public class MeasureEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(MeasureEvaluationService.class);

    private final MeasureRepository measureRepository;

    // Request-scoped cache for measure results
    // Key: measureId:version:paramsHash, Value: evaluation result
    private final ThreadLocal<Map<String, Object>> measureCache = ThreadLocal.withInitial(ConcurrentHashMap::new);

    public MeasureEvaluationService(MeasureRepository measureRepository) {
        this.measureRepository = measureRepository;
    }

    /**
     * Evaluate a measure with given parameters.
     * 
     * @param measureIdentifier The measure identifier
     * @param version The measure version (null for latest)
     * @param parameters Parameter values for the measure
     * @return The evaluation result
     */
    @Transactional(readOnly = true)
    public Object evaluateMeasure(String measureIdentifier, Integer version, Map<String, Object> parameters) {
        // Get the measure
        Optional<Measure> measureOpt;
        if (version != null) {
            measureOpt = measureRepository.findByMeasureIdentifierAndVersion(measureIdentifier, version);
        } else {
            measureOpt = measureRepository.findLatestByMeasureIdentifier(measureIdentifier);
        }

        if (measureOpt.isEmpty()) {
            throw new IllegalArgumentException("Measure not found: " + measureIdentifier + 
                    (version != null ? " version " + version : ""));
        }

        Measure measure = measureOpt.get();

        // Check cache
        String cacheKey = buildCacheKey(measureIdentifier, measure.getVersion(), parameters);
        Map<String, Object> cache = measureCache.get();
        if (cache.containsKey(cacheKey)) {
            logger.debug("Cache hit for measure: {} version {}", measureIdentifier, measure.getVersion());
            return cache.get(cacheKey);
        }

        // Resolve parameters with defaults
        Map<String, Object> resolvedParameters = resolveParameters(measure, parameters);

        // Build evaluation context
        Map<String, Object> context = buildEvaluationContext(measure, resolvedParameters);

        // Evaluate the expression
        try {
            Object result = ExpressionEvaluator.evaluate(measure.getExpression(), context);
            
            // Cache the result
            cache.put(cacheKey, result);
            
            logger.debug("Evaluated measure: {} version {} = {}", measureIdentifier, measure.getVersion(), result);
            return result;
        } catch (ExpressionEvaluator.ExpressionEvaluationException e) {
            logger.error("Error evaluating measure: {} version {}", measureIdentifier, measure.getVersion(), e);
            throw new RuntimeException("Measure evaluation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Resolve parameters with defaults from measure definition.
     */
    private Map<String, Object> resolveParameters(Measure measure, Map<String, Object> providedParameters) {
        Map<String, Object> resolved = new HashMap<>();
        
        List<Map<String, Object>> parameterDefs = measure.getParametersJson();
        if (parameterDefs != null) {
            for (Map<String, Object> paramDef : parameterDefs) {
                String paramName = (String) paramDef.get("name");
                Object defaultValue = paramDef.get("default");
                
                if (providedParameters != null && providedParameters.containsKey(paramName)) {
                    resolved.put(paramName, providedParameters.get(paramName));
                } else if (defaultValue != null) {
                    resolved.put(paramName, defaultValue);
                } else {
                    // Parameter is required but not provided
                    throw new IllegalArgumentException("Required parameter missing: " + paramName);
                }
            }
        }
        
        return resolved;
    }

    /**
     * Build evaluation context for measure expression.
     */
    private Map<String, Object> buildEvaluationContext(Measure measure, Map<String, Object> parameters) {
        Map<String, Object> context = new HashMap<>();
        
        // Add parameters to context (prefixed with $ for clarity)
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
            context.put("$" + entry.getKey(), entry.getValue()); // Also available with $ prefix
        }
        
        // Note: Aggregate functions (sum, count, exists) are handled by ExpressionEvaluator
        // when it encounters function calls in the expression
        
        return context;
    }

    // Note: Entity-level aggregate functions (sum/count/exists with entity names and filters)
    // are not yet implemented. For now, measures can use array-level aggregates
    // like sum(lineItems.total) which are supported by ExpressionEvaluator.

    /**
     * Build cache key for measure evaluation.
     */
    private String buildCacheKey(String measureIdentifier, Integer version, Map<String, Object> parameters) {
        StringBuilder key = new StringBuilder();
        key.append(measureIdentifier).append(":").append(version);
        if (parameters != null && !parameters.isEmpty()) {
            // Sort parameters for consistent key generation
            List<String> sortedKeys = new ArrayList<>(parameters.keySet());
            Collections.sort(sortedKeys);
            for (String paramKey : sortedKeys) {
                key.append(":").append(paramKey).append("=").append(parameters.get(paramKey));
            }
        }
        return key.toString();
    }

    /**
     * Clear the request-scoped cache.
     * Should be called at the end of each request.
     */
    public void clearCache() {
        measureCache.remove();
    }
}
