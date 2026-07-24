package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Payroll {
    private int payrollId;
    private Date periodStart;
    private Date periodEnd;
    private int employeeId;
    private int positionId;
    private Integer departmentId;
    private int workingDays;
    private BigDecimal hoursWorked;
    private BigDecimal baseSalary;
    private BigDecimal allowance;
    private BigDecimal bonus;
    private BigDecimal overtimePay;
    private BigDecimal unpaidDeduction;
    private BigDecimal grossSalary;
    private BigDecimal insuranceDeduction;
    private BigDecimal personalIncomeTax;
    private BigDecimal netSalary;
    private BigDecimal insuranceSalaryBase;
    private BigDecimal postInsuranceIncome;
    private BigDecimal taxableIncome;
    private BigDecimal employerContribution;
    private String note;
    private String configSnapshot;
    private int status;//0 va 1
    private Integer approvedBy;
    private Timestamp approvedAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Payroll() {
    }

    public int getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(int payrollId) {
        this.payrollId = payrollId;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public int getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(int workingDays) {
        this.workingDays = workingDays;
    }

    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getAllowance() {
        return allowance;
    }

    public void setAllowance(BigDecimal allowance) {
        this.allowance = allowance;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getOvertimePay() {
        return overtimePay;
    }

    public void setOvertimePay(BigDecimal overtimePay) {
        this.overtimePay = overtimePay;
    }

    public BigDecimal getUnpaidDeduction() {
        return unpaidDeduction;
    }

    public void setUnpaidDeduction(BigDecimal unpaidDeduction) {
        this.unpaidDeduction = unpaidDeduction;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }

    public BigDecimal getInsuranceDeduction() {
        return insuranceDeduction;
    }

    public void setInsuranceDeduction(BigDecimal insuranceDeduction) {
        this.insuranceDeduction = insuranceDeduction;
    }

    public BigDecimal getPersonalIncomeTax() {
        return personalIncomeTax;
    }

    public void setPersonalIncomeTax(BigDecimal personalIncomeTax) {
        this.personalIncomeTax = personalIncomeTax;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public BigDecimal getInsuranceSalaryBase() {
        return insuranceSalaryBase;
    }

    public void setInsuranceSalaryBase(BigDecimal insuranceSalaryBase) {
        this.insuranceSalaryBase = insuranceSalaryBase;
    }

    public BigDecimal getPostInsuranceIncome() {
        return postInsuranceIncome;
    }

    public void setPostInsuranceIncome(BigDecimal postInsuranceIncome) {
        this.postInsuranceIncome = postInsuranceIncome;
    }

    public BigDecimal getTaxableIncome() {
        return taxableIncome;
    }

    public void setTaxableIncome(BigDecimal taxableIncome) {
        this.taxableIncome = taxableIncome;
    }

    public BigDecimal getEmployerContribution() {
        return employerContribution;
    }

    public void setEmployerContribution(BigDecimal employerContribution) {
        this.employerContribution = employerContribution;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getConfigSnapshot() {
        return configSnapshot;
    }

    public void setConfigSnapshot(String configSnapshot) {
        this.configSnapshot = configSnapshot;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Timestamp getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Timestamp approvedAt) {
        this.approvedAt = approvedAt;
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

