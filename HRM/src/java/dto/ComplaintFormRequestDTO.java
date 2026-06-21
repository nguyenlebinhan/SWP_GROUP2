package dto;

/**
 * DTO dành riêng cho Đơn Khiếu Nại (formTypeCode = "COMPLAINT").
 * Hiện tại không có trường bổ sung ngoài các trường chung trong FormRequestDTO.
 * Class này tồn tại để dễ mở rộng sau này và để phân biệt rõ loại đơn khi cast.
 */
public class ComplaintFormRequestDTO extends FormRequestDTO {
    private java.sql.Date startDate;
    private java.sql.Time startTime;
    private java.sql.Time endTime;

    public java.sql.Date getStartDate() { return startDate; }
    public void setStartDate(java.sql.Date startDate) { this.startDate = startDate; }

    public java.sql.Time getStartTime() { return startTime; }
    public void setStartTime(java.sql.Time startTime) { this.startTime = startTime; }

    public java.sql.Time getEndTime() { return endTime; }
    public void setEndTime(java.sql.Time endTime) { this.endTime = endTime; }
}
