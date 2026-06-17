/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * 
 *
 * @author admin
 */
public class UploadedFile {
    private int fileId;
    private String fileCode;
    private String fileType;        
    private int departmentId;
    private Integer employeeId;
    private String fileUrl;
    private String fileName;
    private int month;
    private int year;            
    private int totalRows;
    private int importedRows;
    private int failedRows;
    private int status;             // 0: Pending, 1: Imported, 2: Failed, 3: Partial
    private String note;

    public UploadedFile() {
    }

    public UploadedFile(int fileId, String fileCode, String fileType, int departmentId, Integer employeeId, String fileUrl, String fileName, int month, int year, int totalRows, int importedRows, int failedRows, int status, String note) {
        this.fileId = fileId;
        this.fileCode = fileCode;
        this.fileType = fileType;
        this.departmentId = departmentId;
        this.employeeId = employeeId;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.month = month;
        this.year = year;
        this.totalRows = totalRows;
        this.importedRows = importedRows;
        this.failedRows = failedRows;
        this.status = status;
        this.note = note;
    }
    
    

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
