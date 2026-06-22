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
    private Integer positionId;
    private String positionName;
    private boolean editable;

    public Attendance() {
    }

    public Attendance(int attendanceId, String attendanceCode, int employeeId, Date workDate, Time timeIn, Time timeOut, BigDecimal hoursWorked, int attendanceStatus, Integer fileId, String employeeCode, String fullName, Integer departmentId, String departmentName, Integer positionId, String positionName, boolean editable) {
        this.attendanceId = attendanceId;
        this.attendanceCode = attendanceCode;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.hoursWorked = hoursWorked;
        this.attendanceStatus = attendanceStatus;
        this.fileId = fileId;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.positionId = positionId;
        this.positionName = positionName;
        this.editable = editable;
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

    public Integer getPositionId() {
        return positionId;
    }

    public void setPositionId(Integer positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }
    
    
    public String getHoursWorkedLabel() {
        long minutes;
        if (timeIn != null && timeOut != null) {
           
            minutes = utils.WorkHoursCalculator.workedMinutes(timeIn, timeOut);
        } else if (hoursWorked != null) {
            minutes = hoursWorked.multiply(BigDecimal.valueOf(60))
                    .setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        } else {
            return "";
        }
        return utils.WorkHoursCalculator.label(minutes);
    }

    public String getStatusLabel() {
        switch (attendanceStatus) {
            case 0: return "Đúng giờ";
            case 1: return "Đi muộn";
            case 2: return "Vắng mặt";
            case 3: return "Không phép";
            case 4: return "Nghỉ phép";
            case 5: return "Nghỉ lễ";
            case 6: return "Cuối tuần";
            default: return "Không xác định";
        }
    }
}
