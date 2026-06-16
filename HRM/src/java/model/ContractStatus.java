package model;

/**
 * Contract lifecycle status states.
 * Complete workflow: DRAFT → PENDING_APPROVAL → PENDING_ACTIVATION → ACTIVE → EXPIRED/TERMINATED/CANCELLED
 */
public enum ContractStatus {
    DRAFT("Nháp"),
    PENDING_APPROVAL("Chờ duyệt"),
    PENDING_ACTIVATION("Chờ hiệu lực"),
    ACTIVE("Đang hiệu lực"),
    EXPIRED("Đã hết hạn"),
    TERMINATED("Đã chấm dứt"),
    CANCELLED("Đã hủy");

    private final String displayName;

    ContractStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this status allows the contract to be edited.
     * DRAFT, PENDING_APPROVAL, and PENDING_ACTIVATION contracts are editable.
     * ACTIVE, EXPIRED, TERMINATED, CANCELLED are immutable.
     * @return true if editable
     */
    public boolean isEditable() {
        return this == DRAFT || this == PENDING_APPROVAL || this == PENDING_ACTIVATION;
    }

    /**
     * Check if this status is "active-like" (Active or Pending Activation).
     * @return true if status is ACTIVE or PENDING_ACTIVATION
     */
    public boolean isActiveLike() {
        return this == ACTIVE || this == PENDING_ACTIVATION;
    }

    /**
     * Check if this status represents a final/terminal state.
     * Final statuses: EXPIRED, TERMINATED, CANCELLED
     * @return true if this is a final status
     */
    public boolean isFinalStatus() {
        return this == EXPIRED || this == TERMINATED || this == CANCELLED;
    }
}

