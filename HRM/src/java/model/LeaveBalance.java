package model;



public class LeaveBalance {
    private int balanceId;
    private int employeeId;
    private int year;
    private int totalAllowed;
    private int usedDays;

    public LeaveBalance() {
    }

    public LeaveBalance(int balanceId, int employeeId, int year, int totalAllowed, int usedDays) {
        this.balanceId = balanceId;
        this.employeeId = employeeId;
        this.year = year;
        this.totalAllowed = totalAllowed;
        this.usedDays = usedDays;
    }

    public int getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(int balanceId) {
        this.balanceId = balanceId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getTotalAllowed() {
        return totalAllowed;
    }

    public void setTotalAllowed(int totalAllowed) {
        this.totalAllowed = totalAllowed;
    }

    public int getUsedDays() {
        return usedDays;
    }

    public void setUsedDays(int usedDays) {
        this.usedDays = usedDays;
    }

    public int getRemainingDays() {
        return totalAllowed - usedDays;
    }
}
