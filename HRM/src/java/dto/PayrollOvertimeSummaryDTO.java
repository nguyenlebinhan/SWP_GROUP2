package dto;

import java.math.BigDecimal;

public class PayrollOvertimeSummaryDTO {

    private BigDecimal overtimeHours = BigDecimal.ZERO;
    private BigDecimal overtimePay = BigDecimal.ZERO;
    private int overtimeBlocks;

    public BigDecimal getOvertimeHours() {
        return overtimeHours;
    }

    public void addOvertimeHours(BigDecimal overtimeHours) {
        this.overtimeHours = this.overtimeHours.add(overtimeHours == null ? BigDecimal.ZERO : overtimeHours);
    }

    public BigDecimal getOvertimePay() {
        return overtimePay;
    }

    public void addOvertimePay(BigDecimal overtimePay) {
        this.overtimePay = this.overtimePay.add(overtimePay == null ? BigDecimal.ZERO : overtimePay);
    }

    public int getOvertimeBlocks() {
        return overtimeBlocks;
    }

    public void addOvertimeBlocks(int overtimeBlocks) {
        this.overtimeBlocks += Math.max(0, overtimeBlocks);
    }
}
