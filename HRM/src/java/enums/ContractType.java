package enums;

public enum ContractType {
    PROBATION("Thử việc"),
    INTERNSHIP("Thực tập"),
    FIXED_TERM("Có thời hạn"),
    INDEFINITE("Không xác định thời hạn");

    private final String displayName;

    ContractType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasEndDate() {
        return this != INDEFINITE;
    }

    public static ContractType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (ContractType type : values()) {
            if (type.name().equalsIgnoreCase(value) || type.displayName.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
