package enums;

public enum ContractStatus {
    DRAFT("Nháp"),
    PENDING_APPROVAL("Chờ duyệt"),
    PENDING_ACTIVATION("Chờ hiệu lực"),
    ACTIVE("Đang hiệu lực"),
    EXPIRED("Đã hết hạn"),
    TERMINATED("Đã chấm dứt"),
    CANCELLED("Đã hủy"),
    REJECTED("Bị từ chối");

    private final String displayName;

    ContractStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEditable() {
        return this == PENDING_APPROVAL || this == PENDING_ACTIVATION;
    }

    public boolean isActiveLike() {
        return this == ACTIVE || this == PENDING_ACTIVATION;
    }

    public boolean isFinalStatus() {
        return this == EXPIRED || this == TERMINATED || this == CANCELLED || this == REJECTED;
    }
}
