package model;

public class FormOperationalResult {

    private final boolean success;
    private final String errorCode;
    private final String message;

    public FormOperationalResult(boolean success, String errorCode, String message) {
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
