package model;

public class ContractOperationResult {

    private final boolean success;
    private final String errorCode;
    private final String message;

    public ContractOperationResult(boolean success, String errorCode, String message) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = message;
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

    @Override
    public String toString() {
        if (success) {
            return "ContractOperationResult{success=true}";
        }
        return "ContractOperationResult{success=false, errorCode='" + errorCode + "', message='" + message + "'}";
    }
}
