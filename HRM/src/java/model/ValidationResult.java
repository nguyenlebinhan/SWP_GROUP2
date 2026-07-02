package model;

/**
 * Immutable validation result.
 * Used by Service validation layer — separate from ContractOperationResult.
 * 
 * Architecture rule:
 * - ValidationResult is for validation only (before execution)
 * - ContractOperationResult is for operation result (after execution)
 * - Service orchestrates: validate → execute → return ContractOperationResult
 */
public final class ValidationResult {
    
    private final boolean success;
    private final ValidationError error;
    private final String message;
    
    private ValidationResult(boolean success, ValidationError error, String message) {
        this.success = success;
        this.error = error;
        this.message = message;
    }
    
    public static ValidationResult success() {
        return new ValidationResult(true, null, null);
    }
    
    public static ValidationResult failure(ValidationError error, String message) {
        if (error == null) {
            throw new IllegalArgumentException("ValidationError must not be null for failure result");
        }
        return new ValidationResult(false, error, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public ValidationError getError() {
        return error;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        if (success) {
            return "ValidationResult{success=true}";
        }
        return "ValidationResult{success=false, error=" + error + ", message='" + message + "'}";
    }
}
