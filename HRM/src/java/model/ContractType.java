package model;

/**
 * Contract types supported by the HRM system.
 * Duration is determined from EffectiveDate and EndDate.
 * INDEFINITE contracts have EndDate = NULL.
 */
public enum ContractType {
    PROBATION("Probation Contract"),
    INTERNSHIP("Internship Contract"),
    FIXED_TERM("Fixed-term Contract"),
    INDEFINITE("Indefinite Contract");

    private final String displayName;

    ContractType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this contract type requires an end date.
     * INDEFINITE contracts do not have end dates.
     * @return false for INDEFINITE, true for all others
     */
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

