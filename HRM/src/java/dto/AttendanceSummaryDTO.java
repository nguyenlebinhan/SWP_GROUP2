package dto;

import java.math.BigDecimal;
import java.math.RoundingMode;


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

    public int getStandardHours() {
        return standardDays * 8;
    }

    public int getWorkedHoursRounded() {
        return workedHours == null ? 0
                : workedHours.setScale(0, RoundingMode.HALF_UP).intValue();
    }


    public String getWorkedVsStandard() {
        return getWorkedHoursRounded() + " / " + getStandardHours();
    }


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
