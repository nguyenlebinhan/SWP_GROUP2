package model;

import java.sql.Timestamp;

/**
 * Audit log entry for contract state transitions.
 * Records every status change with actor, timestamp, and reason.
 */
public class ContractAuditLog {

    private int logId;
    private int contractId;
    private String oldStatus;
    private String newStatus;
    private int changedBy;
    private String changedByName; // Joined from Users table
    private Timestamp changeDate;
    private String actionReason;
    private int employeeId; // Joined from Employment_Contracts

    public ContractAuditLog() {}

    // Getters and Setters
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public int getContractId() { return contractId; }
    public void setContractId(int contractId) { this.contractId = contractId; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public int getChangedBy() { return changedBy; }
    public void setChangedBy(int changedBy) { this.changedBy = changedBy; }
    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }
    public Timestamp getChangeDate() { return changeDate; }
    public void setChangeDate(Timestamp changeDate) { this.changeDate = changeDate; }
    public String getActionReason() { return actionReason; }
    public void setActionReason(String actionReason) { this.actionReason = actionReason; }
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
}
