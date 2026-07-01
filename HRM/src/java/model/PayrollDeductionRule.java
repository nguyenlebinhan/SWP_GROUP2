package model;

import java.math.BigDecimal;

public class PayrollDeductionRule {

    public static final String CALC_PERCENT = "PERCENT";
    public static final String CALC_FIXED = "FIXED";
    public static final String BASE_CONTRACT_SALARY = "CONTRACT_SALARY";
    public static final String BASE_GROSS_SALARY = "GROSS_SALARY";
    public static final String BASE_TAXABLE_INCOME = "TAXABLE_INCOME";

    private int ruleId;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private String calculationType;
    private String baseType;
    private BigDecimal rate;
    private BigDecimal employerRate;
    private BigDecimal fixedAmount;
    private boolean taxableDeduction;
    private boolean active;

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
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

    public String getBaseType() {
        return baseType;
    }

    public void setBaseType(String baseType) {
        this.baseType = baseType;
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

    public BigDecimal getEmployeeRate() {
        BigDecimal total = rate == null ? BigDecimal.ZERO : rate;
        BigDecimal employer = employerRate == null ? BigDecimal.ZERO : employerRate;
        BigDecimal employee = total.subtract(employer);
        return employee.signum() < 0 ? BigDecimal.ZERO : employee;
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

}
