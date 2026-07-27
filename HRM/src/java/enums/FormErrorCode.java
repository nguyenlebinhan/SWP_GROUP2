/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package enums;

/**
 *
 * @author admin
 */
public enum FormErrorCode {
    EMPLOYEE_NOT_FOUND,
    FORM_TYPE_NOT_FOUND,

    // Leave
    START_DATE_IN_PAST,
    END_DATE_BEFORE_START_DATE,
    WEEKEND_ONLY,
    NO_LEAVE_BALANCE,
    INSUFFICIENT_LEAVE_BALANCE,
    OVERLAPPING_LEAVE,

    // Complaint
    NO_ATTENDANCE_RECORD,
    COMPLAINT_PERIOD_MISMATCH,
    END_TIME_BEFORE_START_TIME,
    OVERLAPPING_COMPLAINT,
    ATTENDANCE_PERIOD_CLOSED,
    DEADLINE_EXCEEDED,

    // Transfer
    SAME_DEPARTMENT,
    INVALID_TRANSFER_LEVEL,
    OVERLAPPING_TRANSFER,
    DEPARTMENT_MANAGER_EXIST,

    // Promotion
    ROLE_NOT_ALLOWED_FOR_DEPARTMENT,

    // Dependent
    TAX_CODE_ALREADY_USED,
    DUPLICATE_DEPENDENT,

    // File upload
    INVALID_FILE_TYPE,
    FILE_TOO_LARGE,

    // System
    DATABASE_ERROR
}
