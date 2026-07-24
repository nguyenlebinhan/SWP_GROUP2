package model;

import enums.ContractType;
import enums.ContractStatus;
import java.math.BigDecimal;
import java.sql.Date;

public class EmploymentContract {

    private int contractId;
    private String contractCode;
    private int employeeId;
    private ContractType contractType;
    private Date signedDate;
    private Date effectiveDate;
    private Date endDate;
    private Date actualEndDate;
    private BigDecimal salary;
    private ContractStatus status;
    private String note;
    private Integer previousContractId;
    private String terminationReason;
    private String rejectionReason;
    private String employeeFullName;
    private String employeeCode;
    private String departmentName;
    private String positionName;
    private String contractFilePath;
    private String contractFileName;
    private Date uploadedAt;
    private Integer uploadedBy;
    private Integer durationValue;
    private String durationUnit;
    private int createdBy;
    private String createdByName;
    private Date createdAt;
    private Date updatedAt;

    public EmploymentContract() {
    }

    public EmploymentContract(String contractCode, int employeeId, ContractType contractType,
            Date signedDate, Date effectiveDate, Date endDate,
            BigDecimal salary, ContractStatus status, String note,
            Integer previousContractId, String terminationReason, int createdBy) {
        this.contractCode = contractCode;
        this.employeeId = employeeId;
        this.contractType = contractType;
        this.signedDate = signedDate;
        this.effectiveDate = effectiveDate;
        this.endDate = endDate;
        this.salary = salary;
        this.status = status;
        this.note = note;
        this.previousContractId = previousContractId;
        this.terminationReason = terminationReason;
        this.createdBy = createdBy;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public Date getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(Date signedDate) {
        this.signedDate = signedDate;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Date actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getPreviousContractId() {
        return previousContractId;
    }

    public void setPreviousContractId(Integer previousContractId) {
        this.previousContractId = previousContractId;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getEmployeeFullName() {
        return employeeFullName;
    }

    public void setEmployeeFullName(String employeeFullName) {
        this.employeeFullName = employeeFullName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getContractFilePath() {
        return contractFilePath;
    }

    public void setContractFilePath(String contractFilePath) {
        this.contractFilePath = contractFilePath;
    }

    public String getContractFileName() {
        return contractFileName;
    }

    public void setContractFileName(String contractFileName) {
        this.contractFileName = contractFileName;
    }

    public Date getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Integer getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Integer uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Integer getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(Integer durationValue) {
        this.durationValue = durationValue;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Helper: Determine if this contract is indefinite.
     *
     * @return true if contractType is INDEFINITE
     */
    public boolean isIndefinite() {
        return this.contractType == ContractType.INDEFINITE;
    }

    public Date getActualContractEndDate() {
        return (actualEndDate != null) ? actualEndDate : endDate;
    }

    public Integer getDurationMonths() {
        if (effectiveDate == null || endDate == null) {
            return null;
        }

        java.util.Calendar start = java.util.Calendar.getInstance();
        start.setTime(effectiveDate);

        java.util.Calendar end = java.util.Calendar.getInstance();
        end.setTime(endDate);

        int months = (end.get(java.util.Calendar.YEAR) - start.get(java.util.Calendar.YEAR)) * 12;
        months += end.get(java.util.Calendar.MONTH) - start.get(java.util.Calendar.MONTH);

        if (end.get(java.util.Calendar.DAY_OF_MONTH) >= start.get(java.util.Calendar.DAY_OF_MONTH)) {
            months++;
        }

        return Math.max(0, months);
    }

    public void validate() {
        if (contractType == null) {
            throw new IllegalArgumentException("Contract type cannot be null.");
        }
        if (isIndefinite() && endDate != null) {
            throw new IllegalArgumentException("Indefinite contracts must not have an end date.");
        }
        if (!isIndefinite() && endDate == null) {
            throw new IllegalArgumentException("Fixed-term/Probation/Internship contracts must have an end date.");
        }
    }

    @Override
    public String toString() {
        return "EmploymentContract{"
                + "contractId=" + contractId
                + ", contractCode='" + contractCode + '\''
                + ", employeeId=" + employeeId
                + ", contractType=" + contractType
                + ", effectiveDate=" + effectiveDate
                + ", endDate=" + endDate
                + ", status=" + status
                + '}';
    }

    public String getStatusLabel() {
        if (status == null) {
            return "UNKNOWN";
        }
        return status.getDisplayName();
    }

    public boolean isTerminalStatus() {
        return status != null && status.isFinalStatus();
    }
}
