/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.sql.Timestamp;

/**
 *
 * @author admin
 */
public class AttendancePeriodSummaryDTO {
    private int departmentId;
    private String departmentName;
    private Integer periodId;       
    private Integer status;         
    private String publishedByName;
    private Timestamp publishedAt;
    private int fileCount;
    private int importedRows;       
    private int failedRows;         

    public AttendancePeriodSummaryDTO() {
    }

    public AttendancePeriodSummaryDTO(int departmentId, String departmentName, Integer periodId, Integer status, String publishedByName, Timestamp publishedAt, int fileCount, int importedRows, int failedRows) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.periodId = periodId;
        this.status = status;
        this.publishedByName = publishedByName;
        this.publishedAt = publishedAt;
        this.fileCount = fileCount;
        this.importedRows = importedRows;
        this.failedRows = failedRows;
    }
    
    

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getPeriodId() {
        return periodId;
    }

    public void setPeriodId(Integer periodId) {
        this.periodId = periodId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPublishedByName() {
        return publishedByName;
    }

    public void setPublishedByName(String publishedByName) {
        this.publishedByName = publishedByName;
    }

    public Timestamp getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Timestamp publishedAt) {
        this.publishedAt = publishedAt;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public int getImportedRows() {
        return importedRows;
    }

    public void setImportedRows(int importedRows) {
        this.importedRows = importedRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public void setFailedRows(int failedRows) {
        this.failedRows = failedRows;
    }
}
