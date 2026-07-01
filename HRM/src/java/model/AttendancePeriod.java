package model;

import enums.AttendancePeriodStatus;
import java.sql.Timestamp;

/**
 * Một dòng trạng thái chốt bảng chấm công cho (năm, tháng, phòng ban).
 *
 * @author ADMIN
 */
public class AttendancePeriod {

    private int periodStatusId;
    private int periodYear;
    private int periodMonth;
    private int departmentId;
    private int status;                 // xem enums.AttendancePeriodStatus

    private Integer managerConfirmedBy; // userId trưởng phòng
    private Timestamp managerConfirmedAt;
    private Integer submittedToBaBy;    // userId HR
    private Timestamp submittedToBaAt;
    private Integer baApprovedBy;       // userId BusinessAdmin
    private Timestamp baApprovedAt;
    private String note;

    // Trường hiển thị (join thêm), không có cột riêng.
    private String departmentName;

    public AttendancePeriod() {
    }

    public AttendancePeriod(int periodYear, int periodMonth, int departmentId, int status) {
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
        this.departmentId = departmentId;
        this.status = status;
    }

    public int getPeriodStatusId() {
        return periodStatusId;
    }

    public void setPeriodStatusId(int periodStatusId) {
        this.periodStatusId = periodStatusId;
    }

    public int getPeriodYear() {
        return periodYear;
    }

    public void setPeriodYear(int periodYear) {
        this.periodYear = periodYear;
    }

    public int getPeriodMonth() {
        return periodMonth;
    }

    public void setPeriodMonth(int periodMonth) {
        this.periodMonth = periodMonth;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Integer getManagerConfirmedBy() {
        return managerConfirmedBy;
    }

    public void setManagerConfirmedBy(Integer managerConfirmedBy) {
        this.managerConfirmedBy = managerConfirmedBy;
    }

    public Timestamp getManagerConfirmedAt() {
        return managerConfirmedAt;
    }

    public void setManagerConfirmedAt(Timestamp managerConfirmedAt) {
        this.managerConfirmedAt = managerConfirmedAt;
    }

    public Integer getSubmittedToBaBy() {
        return submittedToBaBy;
    }

    public void setSubmittedToBaBy(Integer submittedToBaBy) {
        this.submittedToBaBy = submittedToBaBy;
    }

    public Timestamp getSubmittedToBaAt() {
        return submittedToBaAt;
    }

    public void setSubmittedToBaAt(Timestamp submittedToBaAt) {
        this.submittedToBaAt = submittedToBaAt;
    }

    public Integer getBaApprovedBy() {
        return baApprovedBy;
    }

    public void setBaApprovedBy(Integer baApprovedBy) {
        this.baApprovedBy = baApprovedBy;
    }

    public Timestamp getBaApprovedAt() {
        return baApprovedAt;
    }

    public void setBaApprovedAt(Timestamp baApprovedAt) {
        this.baApprovedAt = baApprovedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public AttendancePeriodStatus getStatusEnum() {
        return AttendancePeriodStatus.fromCode(status);
    }

    public String getStatusLabel() {
        try {
            return getStatusEnum().getLabel();
        } catch (IllegalArgumentException e) {
            return "Không xác định";
        }
    }

    public boolean isLocked() {
        return status == AttendancePeriodStatus.LOCKED.getRelatedNum();
    }
}
