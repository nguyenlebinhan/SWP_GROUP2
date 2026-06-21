package dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Một dòng tổng hợp chấm công của một nhân viên trong một tháng.
 * Dùng chung cho màn Attendance Overview, header màn Detail và file Excel export.
 *
 * Quy ước:
 *  - workedHours: tổng SUM(hoursWorked) trong tháng (DECIMAL).
 *  - standardDays: số ngày công chuẩn của tháng (T2..T6, trừ ngày lễ) -> do
 *    AttendanceService tính theo lịch thật và gán vào, dùng chung cho mọi nhân viên.
 *  - Các *Days: số ngày theo từng trạng thái (đếm theo dữ liệu chấm công của nhân viên).
 */
public class AttendanceSummaryDTO {

    private int employeeId;
    private String employeeCode;
    private String fullName;
    private String positionName;
    private String departmentName;

    private BigDecimal workedHours = BigDecimal.ZERO;
    private int standardDays;

    private int presentDays;
    private int lateDays;
    private int leaveDays;
    private int absentDays;
    private int holidayDays;
    private int weekendDays;

    public AttendanceSummaryDTO() {
    }

    /** Giờ công chuẩn = số ngày công chuẩn * 8h. */
    public int getStandardHours() {
        return standardDays * 8;
    }

    /** Giờ làm thực tế làm tròn về số nguyên để hiển thị (vd "152"). */
    public int getWorkedHoursRounded() {
        return workedHours == null ? 0
                : workedHours.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /** "152 / 176" – giờ làm thực tế trên giờ chuẩn. */
    public String getWorkedVsStandard() {
        return getWorkedHoursRounded() + " / " + getStandardHours();
    }

    /**
     * Tỷ lệ chuyên cần (%) = (ngày đúng giờ + đi muộn) / số ngày công chuẩn.
     * Đi muộn vẫn tính là có mặt.
     */
    public int getAttendanceRate() {
        if (standardDays <= 0) {
            return 0;
        }
        return Math.round((presentDays + lateDays) * 100f / standardDays);
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
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

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public BigDecimal getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(BigDecimal workedHours) {
        this.workedHours = workedHours == null ? BigDecimal.ZERO : workedHours;
    }

    public int getStandardDays() {
        return standardDays;
    }

    public void setStandardDays(int standardDays) {
        this.standardDays = standardDays;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(int presentDays) {
        this.presentDays = presentDays;
    }

    public int getLateDays() {
        return lateDays;
    }

    public void setLateDays(int lateDays) {
        this.lateDays = lateDays;
    }

    public int getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(int leaveDays) {
        this.leaveDays = leaveDays;
    }

    public int getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(int absentDays) {
        this.absentDays = absentDays;
    }

    public int getHolidayDays() {
        return holidayDays;
    }

    public void setHolidayDays(int holidayDays) {
        this.holidayDays = holidayDays;
    }

    public int getWeekendDays() {
        return weekendDays;
    }

    public void setWeekendDays(int weekendDays) {
        this.weekendDays = weekendDays;
    }
}
