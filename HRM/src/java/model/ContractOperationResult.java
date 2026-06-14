package model;

/**
 * Standard lightweight operation outcome wrapper for Employment Contract business operations.
 * Uses String-based error codes for flexibility across different flow contexts
 * (HR Manual Flow, Scheduler Automated Flow).
 * 
 * Error Code Constants:
 * - INVALID_STATUS: Action attempted on an unapproved state.
 * - DATE_MISMATCH: Timeline validation constraints failed.
 * - SQL_ERROR: Database layer exceptions.
 * - SYSTEM_ERROR: Unexpected runtime anomalies.
 */
public class ContractOperationResult {

    // =========================================================================
    // Error Code Constants
    // =========================================================================

    /** Action attempted on an unapproved state. */
    public static final String INVALID_STATUS = "INVALID_STATUS";

    /** Timeline validation constraints failed. */
    public static final String DATE_MISMATCH = "DATE_MISMATCH";

    /** Database layer exceptions. */
    public static final String SQL_ERROR = "SQL_ERROR";

    /** Unexpected runtime anomalies. */
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";

    // =========================================================================
    // Fields
    // =========================================================================

    private final boolean success;
    private final String errorCode;
    private final String message;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Construct a new operation result.
     *
     * @param success   true if the operation completed successfully
     * @param errorCode a constant identifying the error category (null on success)
     * @param message   a human-readable description of the outcome
     */
    public ContractOperationResult(boolean success, String errorCode, String message) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = message;
    }

    // =========================================================================
    // Getters
    // =========================================================================

    /**
     * @return true if the operation completed successfully
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return the error code constant, or null if the operation succeeded
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @return a human-readable message describing the outcome
     */
    public String getMessage() {
        return message;
    }

    // =========================================================================
    // Object Overrides
    // =========================================================================

    @Override
    public String toString() {
        if (success) {
            return "ContractOperationResult{success=true}";
        }
        return "ContractOperationResult{success=false, errorCode='" + errorCode + "', message='" + message + "'}";
    }
}
