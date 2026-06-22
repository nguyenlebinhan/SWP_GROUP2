package enums;

import java.math.BigDecimal;

public enum PerformanceResult {
    EXCELLENT("EXCELLENT", new BigDecimal("0.08")),
    GOOD("GOOD", new BigDecimal("0.04")),
    AVERAGE("AVERAGE", BigDecimal.ZERO),
    POOR("POOR", new BigDecimal("-0.02"));

    private final String code;
    private final BigDecimal bonusRate;

    PerformanceResult(String code, BigDecimal bonusRate) {
        this.code = code;
        this.bonusRate = bonusRate;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getBonusRate() {
        return bonusRate;
    }

    public static PerformanceResult fromCode(String code) {
        if (code == null) {
            return AVERAGE;
        }
        for (PerformanceResult result : values()) {
            if (result.code.equalsIgnoreCase(code.trim())) {
                return result;
            }
        }
        return AVERAGE;
    }

    public String getLabel() {
        switch (this) {
            case EXCELLENT:
                return "Excellent";
            case GOOD:
                return "Good";
            case POOR:
                return "Poor";
            case AVERAGE:
            default:
                return "Average";
        }
    }
}
