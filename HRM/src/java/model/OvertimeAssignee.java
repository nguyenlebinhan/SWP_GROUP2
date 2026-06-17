package model;

public class OvertimeAssignee {
    private int formId;
    private int employeeId;

    public OvertimeAssignee() {
    }

    public OvertimeAssignee(int formId, int employeeId) {
        this.formId = formId;
        this.employeeId = employeeId;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }
}
