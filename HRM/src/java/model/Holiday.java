package model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Ngày nghỉ lễ do HR cấu hình động. Một bản ghi phủ khoảng [startDate, endDate].
 *
 * @author admin
 */
public class Holiday {

    private int holidayId;
    private String holidayName;
    private Date startDate;
    private Date endDate;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Holiday() {
    }

    public int getHolidayId() {
        return holidayId;
    }

    public void setHolidayId(int holidayId) {
        this.holidayId = holidayId;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
