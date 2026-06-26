package model;


public class ContractOperationResult {


    public static final String INVALID_STATUS = "INVALID_STATUS";

    public static final String DATE_MISMATCH = "DATE_MISMATCH";

    public static final String SQL_ERROR = "SQL_ERROR";

    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";

    public static final String DATE_OVERLAP = "DATE_OVERLAP";

    private final boolean success;
    private final String errorCode;
    private final String message;
    private final int draftId;

    public ContractOperationResult(boolean success, String errorCode, String message) {
        this(success, errorCode, message, 0);
    }

    public ContractOperationResult(boolean success, String errorCode, String message, int draftId) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = message;
        this.draftId = draftId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getDraftId() {
        return draftId;
    }

    @Override
    public String toString() {
        if (success) {
            return "ContractOperationResult{success=true}";
        }
        return "ContractOperationResult{success=false, errorCode='" + errorCode + "', message='" + message + "'}";
    }
}

