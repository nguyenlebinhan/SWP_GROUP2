package dto;

import java.math.BigDecimal;

public class PayrollAttendanceSummaryDTO {

    private int recordCount;
    private int paidWorkingDays;
    private int paidLeaveDays;
    private int unpaidLeaveDays;
    private int unauthorizedAbsentDays;
    private int lateCount;
    private int lateMinutes;
    private int lateDeductionBlocks;
    private int lateDeductionMinutes;
    private BigDecimal hoursWorked = BigDecimal.ZERO;
    private BigDecimal lateDeduction = BigDecimal.ZERO;
    private BigDecimal unauthorizedAbsentDeduction = BigDecimal.ZERO;

    public int getRecordCount() {
        return recordCount;
    }

    public void incrementRecordCount() {
        this.recordCount++;
    }

    public int getPaidWorkingDays() {
        return paidWorkingDays;
    }

    public void incrementPaidWorkingDays() {
        this.paidWorkingDays++;
    }

    public int getPaidLeaveDays() {
        return paidLeaveDays;
    }

    public void incrementPaidLeaveDays() {
        this.paidLeaveDays++;
    }

    public int getUnpaidLeaveDays() {
        return unpaidLeaveDays;
    }

    public void incrementUnpaidLeaveDays() {
        this.unpaidLeaveDays++;
    }

    public int getUnauthorizedAbsentDays() {
        return unauthorizedAbsentDays;
    }

    public void incrementUnauthorizedAbsentDays() {
        this.unauthorizedAbsentDays++;
    }

    public int getLateCount() {
        return lateCount;
    }

    public void incrementLateCount() {
        this.lateCount++;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public void addLateMinutes(int lateMinutes) {
        this.lateMinutes += lateMinutes;
    }

    public int getLateDeductionBlocks() {
        return lateDeductionBlocks;
    }

    public void addLateDeductionBlocks(int lateDeductionBlocks) {
        this.lateDeductionBlocks += lateDeductionBlocks;
    }

    public int getLateDeductionMinutes() {
        return lateDeductionMinutes;
    }

    public void addLateDeductionMinutes(int lateDeductionMinutes) {
        this.lateDeductionMinutes += lateDeductionMinutes;
    }

    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void addHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = this.hoursWorked.add(hoursWorked == null ? BigDecimal.ZERO : hoursWorked);
    }

    public BigDecimal getLateDeduction() {
        return lateDeduction;
    }

    public void addLateDeduction(BigDecimal lateDeduction) {
        this.lateDeduction = this.lateDeduction.add(lateDeduction == null ? BigDecimal.ZERO : lateDeduction);
    }

    public BigDecimal getUnauthorizedAbsentDeduction() {
        return unauthorizedAbsentDeduction;
    }

    public void addUnauthorizedAbsentDeduction(BigDecimal unauthorizedAbsentDeduction) {
        this.unauthorizedAbsentDeduction = this.unauthorizedAbsentDeduction.add(
                unauthorizedAbsentDeduction == null ? BigDecimal.ZERO : unauthorizedAbsentDeduction);
    }
}

