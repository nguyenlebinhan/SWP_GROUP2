package dto;

import java.sql.Date;

/**
 * DTO dành riêng cho Đơn Nghỉ Phép (formTypeCode = "LEAVE").
 * Kế thừa toàn bộ trường chung từ FormRequestDTO và bổ sung
 * thông tin ngày nghỉ.
 */
public class LeaveFormRequestDTO extends FormRequestDTO {

    private java.sql.Date startDate;
    private java.sql.Date endDate;
    private Integer totalDays;
    private Integer usedDays;

    public java.sql.Date getStartDate() { return startDate; }
    public void setStartDate(java.sql.Date startDate) { this.startDate = startDate; }

    public java.sql.Date getEndDate() { return endDate; }
    public void setEndDate(java.sql.Date endDate) { this.endDate = endDate; }

    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }

    public Integer getUsedDays() { return usedDays; }
    public void setUsedDays(Integer usedDays) { this.usedDays = usedDays; }
}
