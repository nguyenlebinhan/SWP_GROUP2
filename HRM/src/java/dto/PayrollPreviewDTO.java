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
    private int lateDeductionBlocks;
    private int lateDeductionMinutes;
    private int lateDeductionBlockMinutes;
    private BigDecimal overtimeHours;
    private int overtimeBlocks;
    private int overtimeBlockMinutes;
    private BigDecimal overtimeBlockAmount;
    private BigDecimal overtimeWorkdayMultiplier;
    private BigDecimal attendanceBonus;
    private BigDecimal lateDeduction;
    private BigDecimal lateDeductionBlockAmount;
    private BigDecimal unauthorizedAbsentDeduction;
    private BigDecimal personalAllowance;
    private int dependentCount;
    private boolean unionMember;
    private boolean insuranceCalculated = true;
    private int insuranceNotWorkedDaysThreshold = 14;
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

    public int getNotWorkedDays() {
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

    public int getLateDeductionBlocks() {
        return lateDeductionBlocks;
    }

    public void setLateDeductionBlocks(int lateDeductionBlocks) {
        this.lateDeductionBlocks = lateDeductionBlocks;
    }

    public int getLateDeductionMinutes() {
        return lateDeductionMinutes;
    }

    public void setLateDeductionMinutes(int lateDeductionMinutes) {
        this.lateDeductionMinutes = lateDeductionMinutes;
    }

    public int getLateDeductionBlockMinutes() {
        return lateDeductionBlockMinutes;
    }

    public void setLateDeductionBlockMinutes(int lateDeductionBlockMinutes) {
        this.lateDeductionBlockMinutes = Math.max(0, lateDeductionBlockMinutes);
    }

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public int getOvertimeBlocks() {
        return overtimeBlocks;
    }

    public void setOvertimeBlocks(int overtimeBlocks) {
        this.overtimeBlocks = Math.max(0, overtimeBlocks);
    }

    public int getOvertimeBlockMinutes() {
        return overtimeBlockMinutes;
    }

    public void setOvertimeBlockMinutes(int overtimeBlockMinutes) {
        this.overtimeBlockMinutes = Math.max(0, overtimeBlockMinutes);
    }

    public BigDecimal getOvertimeBlockAmount() {
        return overtimeBlockAmount;
    }

    public void setOvertimeBlockAmount(BigDecimal overtimeBlockAmount) {
        this.overtimeBlockAmount = overtimeBlockAmount;
    }

    public BigDecimal getOvertimeWorkdayMultiplier() {
        return overtimeWorkdayMultiplier;
    }

    public void setOvertimeWorkdayMultiplier(BigDecimal overtimeWorkdayMultiplier) {
        this.overtimeWorkdayMultiplier = overtimeWorkdayMultiplier;
    }

    public BigDecimal getAttendanceBonus() {
        return attendanceBonus;
    }

    public void setAttendanceBonus(BigDecimal attendanceBonus) {
        this.attendanceBonus = attendanceBonus;
    }

    public BigDecimal getLateDeduction() {
        return lateDeduction;
    }

    public void setLateDeduction(BigDecimal lateDeduction) {
        this.lateDeduction = lateDeduction;
    }

    public BigDecimal getLateDeductionBlockAmount() {
        return lateDeductionBlockAmount;
    }

    public void setLateDeductionBlockAmount(BigDecimal lateDeductionBlockAmount) {
        this.lateDeductionBlockAmount = lateDeductionBlockAmount;
    }

    public BigDecimal getUnauthorizedAbsentDeduction() {
        return unauthorizedAbsentDeduction;
    }

    public void setUnauthorizedAbsentDeduction(BigDecimal unauthorizedAbsentDeduction) {
        this.unauthorizedAbsentDeduction = unauthorizedAbsentDeduction;
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

    public boolean isUnionMember() {
        return unionMember;
    }

    public void setUnionMember(boolean unionMember) {
        this.unionMember = unionMember;
    }

    public boolean isInsuranceCalculated() {
        return insuranceCalculated;
    }

    public void setInsuranceCalculated(boolean insuranceCalculated) {
        this.insuranceCalculated = insuranceCalculated;
    }

    public int getInsuranceNotWorkedDaysThreshold() {
        return insuranceNotWorkedDaysThreshold;
    }

    public void setInsuranceNotWorkedDaysThreshold(int insuranceNotWorkedDaysThreshold) {
        this.insuranceNotWorkedDaysThreshold = insuranceNotWorkedDaysThreshold;
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
