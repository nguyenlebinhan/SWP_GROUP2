package enums;

/**
 * Business validation errors for Contract operations.
 * Naming convention: describes the business condition, not the UI message.
 * Each enum value maps to a specific validation rule.
 */
public enum ValidationError {
    DUPLICATE_CONTRACT_CODE,
    
    EMPLOYEE_NOT_FOUND,
    
    OVERLAPPING_CONTRACT,
    
    EFFECTIVE_DATE_IN_PAST,
    END_DATE_BEFORE_START_DATE,
    END_DATE_REQUIRED_FOR_TYPE,
    END_DATE_NOT_ALLOWED_FOR_TYPE,
    
    INVALID_CONTRACT_TYPE,
    INVALID_SALARY,
    
    SYSTEM_ERROR,
    UNKNOWN_ERROR,
    
    CONTRACT_NOT_ACTIVE,
    CONTRACT_INDEFINITE,
}
