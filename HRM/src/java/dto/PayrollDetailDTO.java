package dto;

import java.math.BigDecimal;

public class PayrollDetailDTO {
    public static final String TYPE_EARNING = "EARNING";
    public static final String TYPE_DEDUCTION = "DEDUCTION";
    public static final String TYPE_INFO = "INFO";
    public static final String TYPE_COMPANY_COST = "COMPANY_COST";

    private String code;
    private String name;
    private String type;
    private BigDecimal amount;
    private String note;
    private BigDecimal base;
    private BigDecimal employeeRatePercent;
    private BigDecimal employerRatePercent;
    private BigDecimal totalRatePercent;
    private BigDecimal employerAmount;

    public PayrollDetailDTO() {
    }

    public PayrollDetailDTO(String code, String name, String type, BigDecimal amount, String note) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.amount = amount;
        this.note = note;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isDeduction() {
        return TYPE_DEDUCTION.equals(type);
    }

    public boolean isInfo() {
        return TYPE_INFO.equals(type);
    }

    public boolean isCompanyCost() {
        return TYPE_COMPANY_COST.equals(type);
    }

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getEmployeeRatePercent() {
        return employeeRatePercent;
    }

    public void setEmployeeRatePercent(BigDecimal employeeRatePercent) {
        this.employeeRatePercent = employeeRatePercent;
    }

    public BigDecimal getEmployerRatePercent() {
        return employerRatePercent;
    }

    public void setEmployerRatePercent(BigDecimal employerRatePercent) {
        this.employerRatePercent = employerRatePercent;
    }

    public BigDecimal getTotalRatePercent() {
        return totalRatePercent;
    }

    public void setTotalRatePercent(BigDecimal totalRatePercent) {
        this.totalRatePercent = totalRatePercent;
    }

    public BigDecimal getEmployerAmount() {
        return employerAmount;
    }

    public void setEmployerAmount(BigDecimal employerAmount) {
        this.employerAmount = employerAmount;
    }
}
