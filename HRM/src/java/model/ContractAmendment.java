/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.sql.Date;

/**
 *
 * @author Admin
 */
public class ContractAmendment {
    private int amendmentId;
    private int contractId;
    private String amendmentCode;
    private String amendmentType;
    private Date effectiveDate;
    
    private Integer oldDepartmentId;
    private Integer newDepartmentId;
    private Integer oldPositionId;
    private Integer newPositionId;
    
    private BigDecimal oldSalary;
    private BigDecimal newSalary;
    
    private String reason;
    private Integer sourceFormId;
    private String status;
    
    private int createdBy;
    private Integer approvedBy;
    private Date createdAt;
    private Date approvedAt;

    public ContractAmendment() {
    }

    public int getAmendmentId() {
        return amendmentId;
    }

    public void setAmendmentId(int amendmentId) {
        this.amendmentId = amendmentId;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public String getAmendmentCode() {
        return amendmentCode;
    }

    public void setAmendmentCode(String amendmentCode) {
        this.amendmentCode = amendmentCode;
    }

    public String getAmendmentType() {
        return amendmentType;
    }

    public void setAmendmentType(String amendmentType) {
        this.amendmentType = amendmentType;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Integer getOldDepartmentId() {
        return oldDepartmentId;
    }

    public void setOldDepartmentId(Integer oldDepartmentId) {
        this.oldDepartmentId = oldDepartmentId;
    }

    public Integer getNewDepartmentId() {
        return newDepartmentId;
    }

    public void setNewDepartmentId(Integer newDepartmentId) {
        this.newDepartmentId = newDepartmentId;
    }

    public Integer getOldPositionId() {
        return oldPositionId;
    }

    public void setOldPositionId(Integer oldPositionId) {
        this.oldPositionId = oldPositionId;
    }

    public Integer getNewPositionId() {
        return newPositionId;
    }

    public void setNewPositionId(Integer newPositionId) {
        this.newPositionId = newPositionId;
    }

    public BigDecimal getOldSalary() {
        return oldSalary;
    }

    public void setOldSalary(BigDecimal oldSalary) {
        this.oldSalary = oldSalary;
    }

    public BigDecimal getNewSalary() {
        return newSalary;
    }

    public void setNewSalary(BigDecimal newSalary) {
        this.newSalary = newSalary;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getSourceFormId() {
        return sourceFormId;
    }

    public void setSourceFormId(Integer sourceFormId) {
        this.sourceFormId = sourceFormId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Date approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    
}
