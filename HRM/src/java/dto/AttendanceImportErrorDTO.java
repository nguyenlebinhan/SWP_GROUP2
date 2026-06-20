/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

/**
 * 
 *
 * @author admin
 */
public class AttendanceImportErrorDTO {
    private int rowNumber;          
    private String employeeCode;    
    private String message;

    public AttendanceImportErrorDTO() {
    }

    public AttendanceImportErrorDTO(int rowNumber, String employeeCode, String message) {
        this.rowNumber = rowNumber;
        this.employeeCode = employeeCode;
        this.message = message;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
