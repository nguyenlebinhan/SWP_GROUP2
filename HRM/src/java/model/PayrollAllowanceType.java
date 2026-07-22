package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PayrollAllowanceType {

    private int allowanceId;
    private String allowanceCode;
    private String allowanceName;
    private BigDecimal amount;
    private boolean insuranceApplicable;
    private boolean active;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public int getAllowanceId() {
        return allowanceId;
    }

    public void setAllowanceId(int allowanceId) {
        this.allowanceId = allowanceId;
    }

    public String getAllowanceCode() {
        return allowanceCode;
    }

    public void setAllowanceCode(String allowanceCode) {
        this.allowanceCode = allowanceCode;
    }

    public String getAllowanceName() {
        return allowanceName;
    }

    public void setAllowanceName(String allowanceName) {
        this.allowanceName = allowanceName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isInsuranceApplicable() {
        return insuranceApplicable;
    }

    public void setInsuranceApplicable(boolean insuranceApplicable) {
        this.insuranceApplicable = insuranceApplicable;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
