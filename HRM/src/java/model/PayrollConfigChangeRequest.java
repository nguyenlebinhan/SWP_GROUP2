package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PayrollConfigChangeRequest {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    public static final String TYPE_SETTING_SAVE = "SETTING_SAVE";
    public static final String TYPE_DEDUCTION_SAVE = "DEDUCTION_SAVE";
    public static final String TYPE_DEDUCTION_DELETE = "DEDUCTION_DELETE";
    public static final String TYPE_TAX_SAVE = "TAX_SAVE";

    private int requestId;
    private String requestType;
    private String actionLabel;
    private String oldValue;
    private String newValue;
    private String targetKey;
    private Integer targetId;
    private String settingKey;
    private BigDecimal settingValue;
    private String settingDescription;
    private Integer ruleId;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private String calculationType;
    private BigDecimal rate;
    private BigDecimal employerRate;
    private BigDecimal fixedAmount;
    private boolean taxableDeduction;
    private boolean active;
    private String taxPayload;
    private int status;
    private int requestedBy;
    private String requestedByName;
    private Timestamp requestedAt;
    private Integer reviewedBy;
    private String reviewedByName;
    private Timestamp reviewedAt;
    private String reviewNote;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public BigDecimal getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(BigDecimal settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingDescription() {
        return settingDescription;
    }

    public void setSettingDescription(String settingDescription) {
        this.settingDescription = settingDescription;
    }

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getEmployerRate() {
        return employerRate;
    }

    public void setEmployerRate(BigDecimal employerRate) {
        this.employerRate = employerRate;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public boolean isTaxableDeduction() {
        return taxableDeduction;
    }

    public void setTaxableDeduction(boolean taxableDeduction) {
        this.taxableDeduction = taxableDeduction;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTaxPayload() {
        return taxPayload;
    }

    public void setTaxPayload(String taxPayload) {
        this.taxPayload = taxPayload;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(int requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public Timestamp getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Timestamp requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Integer reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }

    public Timestamp getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Timestamp reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public String getStatusLabel() {
        if (status == STATUS_PENDING) {
            return "Pending";
        }
        if (status == STATUS_APPROVED) {
            return "Approved";
        }
        if (status == STATUS_REJECTED) {
            return "Rejected";
        }
        return "Unknown";
    }
}
