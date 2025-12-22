package com.cbap.api.service.expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Simple expression evaluator for CEL-v0 expressions.
 * 
 * This is a basic implementation for OSS. For production use, consider using
 * a more robust expression engine like MVEL or SpEL, but this provides
 * a self-contained solution that meets CEL-v0 requirements.
 */
public class ExpressionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionEvaluator.class);

    /**
     * Evaluate a CEL-v0 expression in the given context.
     * 
     * @param expression The expression to evaluate
     * @param context The evaluation context (property values, etc.)
     * @return The evaluation result (Boolean, Number, String, or null)
     * @throws ExpressionEvaluationException If evaluation fails
     */
    public static Object evaluate(String expression, Map<String, Object> context) throws ExpressionEvaluationException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new ExpressionEvaluationException("Expression cannot be null or empty");
        }

        try {
            // For now, use a simple evaluator
            // This is a placeholder - in production, use a proper expression engine
            // For OSS, we'll implement basic evaluation
            
            // Remove whitespace
            String trimmed = expression.trim();
            
            // Handle boolean literals
            if ("true".equals(trimmed)) {
                return true;
            }
            if ("false".equals(trimmed)) {
                return false;
            }
            if ("null".equals(trimmed)) {
                return null;
            }
            
            // Handle property access
            if (context != null && context.containsKey(trimmed)) {
                return context.get(trimmed);
            }
            
            // For now, delegate to a more sophisticated evaluator
            // This is a simplified version - full implementation would parse the expression
            return evaluateExpression(trimmed, context);
            
        } catch (Exception e) {
            logger.error("Error evaluating expression: {}", expression, e);
            throw new ExpressionEvaluationException("Failed to evaluate expression: " + e.getMessage(), e);
        }
    }

    /**
     * Evaluate a boolean expression (for validations).
     * 
     * @param expression The expression to evaluate
     * @param context The evaluation context
     * @return true if valid, false if invalid, null if error
     */
    public static Boolean evaluateBoolean(String expression, Map<String, Object> context) {
        try {
            Object result = evaluate(expression, context);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            if (result == null) {
                return false; // null is treated as validation failure
            }
            // Convert to boolean
            if (result instanceof Number) {
                return ((Number) result).doubleValue() != 0.0;
            }
            if (result instanceof String) {
                return !((String) result).isEmpty();
            }
            return false;
        } catch (ExpressionEvaluationException e) {
            logger.warn("Expression evaluation error: {}", e.getMessage());
            return false; // Errors are treated as validation failure
        }
    }

    /**
     * Simple expression evaluator.
     * This is a basic implementation - for production, use a proper parser.
     */
    private static Object evaluateExpression(String expression, Map<String, Object> context) throws ExpressionEvaluationException {
        // This is a placeholder for a full expression parser
        // For now, we'll handle simple cases
        
        // Handle comparison operators
        if (expression.contains("==")) {
            String[] parts = expression.split("==", 2);
            Object left = evaluateSimpleValue(parts[0].trim(), context);
            Object right = evaluateSimpleValue(parts[1].trim(), context);
            return Objects.equals(left, right);
        }
        if (expression.contains("!=")) {
            String[] parts = expression.split("!=", 2);
            Object left = evaluateSimpleValue(parts[0].trim(), context);
            Object right = evaluateSimpleValue(parts[1].trim(), context);
            return !Objects.equals(left, right);
        }
        if (expression.contains("<=")) {
            String[] parts = expression.split("<=", 2);
            Double left = toNumber(evaluateSimpleValue(parts[0].trim(), context));
            Double right = toNumber(evaluateSimpleValue(parts[1].trim(), context));
            return left != null && right != null && left <= right;
        }
        if (expression.contains(">=")) {
            String[] parts = expression.split(">=", 2);
            Double left = toNumber(evaluateSimpleValue(parts[0].trim(), context));
            Double right = toNumber(evaluateSimpleValue(parts[1].trim(), context));
            return left != null && right != null && left >= right;
        }
        if (expression.contains("<") && !expression.contains("<=")) {
            String[] parts = expression.split("<", 2);
            Double left = toNumber(evaluateSimpleValue(parts[0].trim(), context));
            Double right = toNumber(evaluateSimpleValue(parts[1].trim(), context));
            return left != null && right != null && left < right;
        }
        if (expression.contains(">") && !expression.contains(">=")) {
            String[] parts = expression.split(">", 2);
            Double left = toNumber(evaluateSimpleValue(parts[0].trim(), context));
            Double right = toNumber(evaluateSimpleValue(parts[1].trim(), context));
            return left != null && right != null && left > right;
        }
        
        // Handle logical operators (simple cases)
        if (expression.contains("&&")) {
            String[] parts = expression.split("&&");
            for (String part : parts) {
                Object partResult = evaluateExpression(part.trim(), context);
                if (partResult instanceof Boolean && !(Boolean) partResult) {
                    return false;
                }
            }
            return true;
        }
        if (expression.contains("||")) {
            String[] parts = expression.split("\\|\\|");
            for (String part : parts) {
                Object partResult = evaluateExpression(part.trim(), context);
                if (partResult instanceof Boolean && (Boolean) partResult) {
                    return true;
                }
            }
            return false;
        }
        
        // Handle arithmetic (simple cases)
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            Double sum = 0.0;
            for (String part : parts) {
                Double value = toNumber(evaluateSimpleValue(part.trim(), context));
                if (value == null) {
                    throw new ExpressionEvaluationException("Cannot add non-numeric value");
                }
                sum += value;
            }
            return sum;
        }
        if (expression.contains("-") && !expression.startsWith("-")) {
            String[] parts = expression.split("-", 2);
            Double left = toNumber(evaluateSimpleValue(parts[0].trim(), context));
            Double right = toNumber(evaluateSimpleValue(parts[1].trim(), context));
            if (left == null || right == null) {
                throw new ExpressionEvaluationException("Cannot subtract non-numeric values");
            }
            return left - right;
        }
        if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            Double product = 1.0;
            for (String part : parts) {
                Double value = toNumber(evaluateSimpleValue(part.trim(), context));
                if (value == null) {
                    throw new ExpressionEvaluationException("Cannot multiply non-numeric value");
                }
                product *= value;
            }
            return product;
        }
        if (expression.contains("/")) {
            String[] parts = expression.split("/", 2);
            Double left = toNumber(evaluateSimpleValue(parts[0].trim(), context));
            Double right = toNumber(evaluateSimpleValue(parts[1].trim(), context));
            if (left == null || right == null) {
                throw new ExpressionEvaluationException("Cannot divide non-numeric values");
            }
            if (right == 0.0) {
                throw new ExpressionEvaluationException("Division by zero");
            }
            return left / right;
        }
        
        // Handle function calls (e.g., sum(array.field))
        if (expression.contains("(") && expression.contains(")")) {
            int openParen = expression.indexOf("(");
            int closeParen = expression.lastIndexOf(")");
            if (openParen < closeParen) {
                String functionName = expression.substring(0, openParen).trim();
                String argsStr = expression.substring(openParen + 1, closeParen).trim();
                
                // Handle sum() function for arrays
                if ("sum".equals(functionName)) {
                    return evaluateSumFunction(argsStr, context);
                }
            }
        }
        
        // Handle simple value
        return evaluateSimpleValue(expression, context);
    }

    /**
     * Evaluate sum() function: sum(array.field) or sum(array)
     * Example: sum(lineItems.total) sums the 'total' field of each item in lineItems array
     */
    private static Object evaluateSumFunction(String argsStr, Map<String, Object> context) throws ExpressionEvaluationException {
        if (argsStr.contains(".")) {
            // sum(array.field) - sum a specific field from array items
            String[] parts = argsStr.split("\\.", 2);
            String arrayName = parts[0].trim();
            String fieldName = parts[1].trim();
            
            Object arrayObj = evaluateSimpleValue(arrayName, context);
            if (!(arrayObj instanceof java.util.List)) {
                throw new ExpressionEvaluationException("sum() expects an array, got: " + arrayObj);
            }
            
            @SuppressWarnings("unchecked")
            java.util.List<Object> array = (java.util.List<Object>) arrayObj;
            double sum = 0.0;
            
            for (Object item : array) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) item;
                    Object fieldValue = itemMap.get(fieldName);
                    Double numValue = toNumber(fieldValue);
                    if (numValue != null) {
                        sum += numValue;
                    }
                }
            }
            
            return sum;
        } else {
            // sum(array) - sum array values directly
            Object arrayObj = evaluateSimpleValue(argsStr, context);
            if (!(arrayObj instanceof java.util.List)) {
                throw new ExpressionEvaluationException("sum() expects an array, got: " + arrayObj);
            }
            
            @SuppressWarnings("unchecked")
            java.util.List<Object> array = (java.util.List<Object>) arrayObj;
            double sum = 0.0;
            
            for (Object item : array) {
                Double numValue = toNumber(item);
                if (numValue != null) {
                    sum += numValue;
                }
            }
            
            return sum;
        }
    }

    private static Object evaluateSimpleValue(String value, Map<String, Object> context) {
        // Remove quotes from strings
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        
        // Try as number
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            // Not a number
        }
        
        // Try as boolean
        if ("true".equals(value)) {
            return true;
        }
        if ("false".equals(value)) {
            return false;
        }
        if ("null".equals(value)) {
            return null;
        }
        
        // Try as context variable
        if (context != null && context.containsKey(value)) {
            return context.get(value);
        }
        
        // Return as string
        return value;
    }

    private static Double toNumber(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Exception thrown when expression evaluation fails.
     */
    public static class ExpressionEvaluationException extends Exception {
        public ExpressionEvaluationException(String message) {
            super(message);
        }

        public ExpressionEvaluationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
