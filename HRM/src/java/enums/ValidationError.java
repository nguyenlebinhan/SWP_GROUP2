package enums;

/**
 * Business validation errors for Contract operations.
 * Naming convention: describes the business condition, not the UI message.
 * Each enum value maps to a specific validation rule.
 */
public enum ValidationError {
    // Contract code
    DUPLICATE_CONTRACT_CODE,
    
    // Employee
    EMPLOYEE_NOT_FOUND,
    
    // Overlap
    OVERLAPPING_CONTRACT,
    
    // Date rules
    EFFECTIVE_DATE_IN_PAST,
    END_DATE_BEFORE_START_DATE,
    END_DATE_REQUIRED_FOR_TYPE,
    END_DATE_NOT_ALLOWED_FOR_TYPE,
    
    // Type validation
    INVALID_CONTRACT_TYPE,
    INVALID_SALARY,
    
    // System
    SYSTEM_ERROR,
    UNKNOWN_ERROR
}
