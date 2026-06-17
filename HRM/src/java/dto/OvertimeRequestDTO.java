package dto;

import java.sql.Date;
import java.sql.Time;

public class OvertimeRequestDTO extends FormRequestDTO {
    private Date otDate;
    private Time startTime;
    private Time endTime;
    private int dayType;
    private int totalAssignees;

    public OvertimeRequestDTO() {
        super();
    }

    public Date getOtDate() {
        return otDate;
    }

    public void setOtDate(Date otDate) {
        this.otDate = otDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public int getDayType() {
        return dayType;
    }

    public void setDayType(int dayType) {
        this.dayType = dayType;
    }

    public int getTotalAssignees() {
        return totalAssignees;
    }

    public void setTotalAssignees(int totalAssignees) {
        this.totalAssignees = totalAssignees;
    }

    public String getDayTypeLabel() {
        switch (dayType) {
            case 1: return "Ngày thường";
            case 2: return "Cuối tuần";
            case 3: return "Ngày lễ";
            default: return "Không xác định";
        }
    }
}
