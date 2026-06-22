package model;

import java.sql.Date;
import java.sql.Time;

/**
 * Model dành riêng cho Đơn Khiếu Nại.
 */
public class ComplaintFormRequest extends FormRequest {
    private Date startDate;
    private Time startTime;
    private Time endTime;

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }
}
