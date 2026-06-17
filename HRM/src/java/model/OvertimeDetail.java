package model;

import java.sql.Date;
import java.sql.Time;

public class OvertimeDetail {
    private int formId;
    private Date otDate;
    private Time startTime;
    private Time endTime;
    private int dayType;

    public OvertimeDetail() {
    }

    public OvertimeDetail(int formId, Date otDate, Time startTime, Time endTime, int dayType) {
        this.formId = formId;
        this.otDate = otDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayType = dayType;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
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
}
