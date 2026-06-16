/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;

/**
 *
 * @author admin
 */
public class FormRequest {
    private int formId;
    private String formCode;
    private String formName;
    private int employeeId;
    private int formTypeId;
    private String reason;
    private int status;             //0: Pending, 1: Approved, 2: Rejected
    private Integer approverId;
    private String approverNote;
    private Timestamp approvedAt;
    private String attachmentUrl;
    private String attachmentName;

    public FormRequest() {
    }

    public FormRequest(int formId, String formCode, String formName, int employeeId, int formTypeId, String reason, int status, Integer approverId, String approverNote, Timestamp approvedAt, String attachmentUrl, String attachmentName) {
        this.formId = formId;
        this.formCode = formCode;
        this.formName = formName;
        this.employeeId = employeeId;
        this.formTypeId = formTypeId;
        this.reason = reason;
        this.status = status;
        this.approverId = approverId;
        this.approverNote = approverNote;
        this.approvedAt = approvedAt;
        this.attachmentUrl = attachmentUrl;
        this.attachmentName = attachmentName;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Integer getApproverId() {
        return approverId;
    }

    public void setApproverId(Integer approverId) {
        this.approverId = approverId;
    }

    public String getApproverNote() {
        return approverNote;
    }

    public void setApproverNote(String approverNote) {
        this.approverNote = approverNote;
    }

    public Timestamp getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Timestamp approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }


    public String getStatusLabel(){
        switch(status) {
            case 0: return "Chờ duyệt";
            case 1: return "Đồng ý";
            case 2: return "Từ chối";
            case 3: return "Đã hủy";
            default: return "Không xác định";
        }
    
    }
}
