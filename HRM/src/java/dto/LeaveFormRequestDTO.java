package dto;

import java.sql.Date;

/**
 * DTO dành riêng cho Đơn Nghỉ Phép (formTypeCode = "LEAVE").
 * Kế thừa toàn bộ trường chung từ FormRequestDTO và bổ sung
 * thông tin ngày nghỉ.
 */
public class LeaveFormRequestDTO extends FormRequestDTO {

    private Date startDate;
    private Date endDate;
    private Double totalDays;
    private Double usedDays;

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Double getTotalDays() { return totalDays; }
    public void setTotalDays(Double totalDays) { this.totalDays = totalDays; }

    public Double getUsedDays() { return usedDays; }
    public void setUsedDays(Double usedDays) { this.usedDays = usedDays; }
}
