/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import enums.AttendancePeriodStatus;
import java.sql.Timestamp;

/**
 *
 * @author admin
 */
public class AttendancePeriod {
    private int periodId;
    private int departmentId;
    private int month;
    private int year;
    private int status;
    private Integer publishedBy;
    private Timestamp publishedAt;

    
    public AttendancePeriod() {
    }

    public AttendancePeriod(int periodId, int departmentId, int month, int year, int status, Integer publishedBy, Timestamp publishedAt) {
        this.periodId = periodId;
        this.departmentId = departmentId;
        this.month = month;
        this.year = year;
        this.status = status;
        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
    }
    
    public int getPeriodId() {
        return periodId;
    }

    public void setPeriodId(int periodId) {
        this.periodId = periodId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Integer getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(Integer publishedBy) {
        this.publishedBy = publishedBy;
    }

    public Timestamp getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Timestamp publishedAt) {
        this.publishedAt = publishedAt;
    }

    public boolean isPublished() {
        return status == AttendancePeriodStatus.STATUS_PUBLIC.getRelatedNum();
    }
}
