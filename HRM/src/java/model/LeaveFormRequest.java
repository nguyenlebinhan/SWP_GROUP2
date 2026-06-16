package model;

import java.sql.Date;

/**
 * Model dành riêng cho Đơn Nghỉ Phép.
 * Kế thừa FormRequest và bổ sung các trường ngày nghỉ.
 */
public class LeaveFormRequest extends FormRequest {

    private Date startDate;
    private Date endDate;
    private Double totalDays;

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Double getTotalDays() { return totalDays; }
    public void setTotalDays(Double totalDays) { this.totalDays = totalDays; }
}
