package dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import model.Payroll;

public class PayrollPreviewDTO {
    private Payroll payroll;
    private String employeeCode;
    private String fullName;
    private String departmentName;
    private String positionName;
    private BigDecimal contractSalary;
    private BigDecimal dailyRate;
    private BigDecimal hourlyRate;
    private BigDecimal minuteRate;
    private int standardWorkingDays;
    private int paidLeaveDays;
    private int unpaidLeaveDays;
    private int unauthorizedAbsentDays;
    private int lateMinutes;
    private int latePenaltyBlocks;
    private int latePenaltyMinutes;
    private BigDecimal overtimeHours;
    private BigDecimal attendanceBonus;
    private BigDecimal latePenalty;
    private BigDecimal unauthorizedAbsentPenalty;
    private BigDecimal personalAllowance;
    private int dependentCount;
    private BigDecimal dependentAllowance;
    private BigDecimal familyAllowance;
    private BigDecimal taxableIncome;
    private BigDecimal totalDeduction;
    private String generationError;
    private List<PayrollDetailDTO> details = new ArrayList<>();

    public PayrollPreviewDTO() {
    }

    public Payroll getPayroll() {
        return payroll;
    }

    public void setPayroll(Payroll payroll) {
        this.payroll = payroll;
    }

    public int getPayrollId() {
        return payroll != null ? payroll.getPayrollId() : 0;
    }

    public Date getPeriodStart() {
        return payroll != null ? payroll.getPeriodStart() : null;
    }

    public Date getPeriodEnd() {
        return payroll != null ? payroll.getPeriodEnd() : null;
    }

    public int getEmployeeId() {
        return payroll != null ? payroll.getEmployeeId() : 0;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public BigDecimal getContractSalary() {
        return contractSalary;
    }

    public void setContractSalary(BigDecimal contractSalary) {
        this.contractSalary = contractSalary;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getMinuteRate() {
        return minuteRate;
    }

    public void setMinuteRate(BigDecimal minuteRate) {
        this.minuteRate = minuteRate;
    }

    public int getStandardWorkingDays() {
        return standardWorkingDays;
    }

    public void setStandardWorkingDays(int standardWorkingDays) {
        this.standardWorkingDays = standardWorkingDays;
    }

    public int getPaidLeaveDays() {
        return paidLeaveDays;
    }

    public void setPaidLeaveDays(int paidLeaveDays) {
        this.paidLeaveDays = paidLeaveDays;
    }

    public int getUnpaidLeaveDays() {
        return unpaidLeaveDays;
    }

    public void setUnpaidLeaveDays(int unpaidLeaveDays) {
        this.unpaidLeaveDays = unpaidLeaveDays;
    }

    public int getUnauthorizedAbsentDays() {
        return unauthorizedAbsentDays;
    }

    public void setUnauthorizedAbsentDays(int unauthorizedAbsentDays) {
        this.unauthorizedAbsentDays = unauthorizedAbsentDays;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(int lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public int getLatePenaltyBlocks() {
        return latePenaltyBlocks;
    }

    public void setLatePenaltyBlocks(int latePenaltyBlocks) {
        this.latePenaltyBlocks = latePenaltyBlocks;
    }

    public int getLatePenaltyMinutes() {
        return latePenaltyMinutes;
    }

    public void setLatePenaltyMinutes(int latePenaltyMinutes) {
        this.latePenaltyMinutes = latePenaltyMinutes;
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public BigDecimal getAttendanceBonus() {
        return attendanceBonus;
    }

    public void setAttendanceBonus(BigDecimal attendanceBonus) {
        this.attendanceBonus = attendanceBonus;
    }

    public BigDecimal getLatePenalty() {
        return latePenalty;
    }

    public void setLatePenalty(BigDecimal latePenalty) {
        this.latePenalty = latePenalty;
    }

    public BigDecimal getUnauthorizedAbsentPenalty() {
        return unauthorizedAbsentPenalty;
    }

    public void setUnauthorizedAbsentPenalty(BigDecimal unauthorizedAbsentPenalty) {
        this.unauthorizedAbsentPenalty = unauthorizedAbsentPenalty;
    }

    public BigDecimal getPersonalAllowance() {
        return personalAllowance;
    }

    public void setPersonalAllowance(BigDecimal personalAllowance) {
        this.personalAllowance = personalAllowance;
    }

    public int getDependentCount() {
        return dependentCount;
    }

    public void setDependentCount(int dependentCount) {
        this.dependentCount = Math.max(0, dependentCount);
    }

    public BigDecimal getDependentAllowance() {
        return dependentAllowance;
    }

    public void setDependentAllowance(BigDecimal dependentAllowance) {
        this.dependentAllowance = dependentAllowance;
    }

    public BigDecimal getFamilyAllowance() {
        return familyAllowance;
    }

    public void setFamilyAllowance(BigDecimal familyAllowance) {
        this.familyAllowance = familyAllowance;
    }

    public BigDecimal getTaxableIncome() {
        return taxableIncome;
    }

    public void setTaxableIncome(BigDecimal taxableIncome) {
        this.taxableIncome = taxableIncome;
    }

    public BigDecimal getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(BigDecimal totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public String getGenerationError() {
        return generationError;
    }

    public void setGenerationError(String generationError) {
        this.generationError = generationError;
    }

    public boolean isGenerationBlocked() {
        return generationError != null && !generationError.trim().isEmpty();
    }

    public List<PayrollDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<PayrollDetailDTO> details) {
        this.details = details;
    }

    public BigDecimal getGrossSalary() {
        return payroll != null ? payroll.getGrossSalary() : null;
    }

    public BigDecimal getInsuranceDeduction() {
        return payroll != null ? payroll.getInsuranceDeduction() : null;
    }

    public BigDecimal getPersonalIncomeTax() {
        return payroll != null ? payroll.getPersonalIncomeTax() : null;
    }

    public BigDecimal getNetSalary() {
        return payroll != null ? payroll.getNetSalary() : null;
    }
}
