/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

/**
 *
 * @author admin
 */
public class Attendance {
    private int attendanceId;
    private String attendanceCode;
    private int employeeId;
    private Date workDate;
    private Time timeIn;
    private Time timeOut;
    private BigDecimal hoursWorked;
    private int attendanceStatus;   
    private Integer fileId;
    private String employeeCode;
    private String fullName;
    private Integer departmentId;
    private String departmentName;
    private boolean editable;

    public Attendance() {
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getAttendanceCode() {
        return attendanceCode;
    }

    public void setAttendanceCode(String attendanceCode) {
        this.attendanceCode = attendanceCode;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public Time getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(Time timeIn) {
        this.timeIn = timeIn;
    }

    public Time getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Time timeOut) {
        this.timeOut = timeOut;
    }

    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public int getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(int attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
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

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public boolean isAssignedToDepartment() {
        return departmentId != null;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Hiển thị số giờ làm dạng "8h50m". Ưu tiên tính trực tiếp từ giờ vào/giờ ra cho
     * chính xác (không phụ thuộc làm tròn của cột DECIMAL hoursWorked); nếu thiếu giờ
     * vào/ra thì suy từ hoursWorked đã lưu.
     */
    public String getHoursWorkedLabel() {
        long minutes;
        if (timeIn != null && timeOut != null) {
            minutes = (timeOut.getTime() - timeIn.getTime()) / 60000L;
        } else if (hoursWorked != null) {
            minutes = hoursWorked.multiply(BigDecimal.valueOf(60))
                    .setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        } else {
            return "";
        }
        if (minutes < 0) {
            minutes = 0;
        }
        long h = minutes / 60;
        long m = minutes % 60;
        return h + "h" + String.format("%02d", m) + "m";
    }

    public String getStatusLabel() {
        switch (attendanceStatus) {
            case 0: return "Đúng giờ";
            case 1: return "Đi muộn";
            case 2: return "Vắng mặt";
            case 3: return "Không phép";
            default: return "Không xác định";
        }
    }
}
