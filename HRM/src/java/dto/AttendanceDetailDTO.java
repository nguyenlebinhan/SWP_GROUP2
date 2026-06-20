package dto;

import java.util.ArrayList;
import java.util.List;
import model.Attendance;

/**
 * Dữ liệu cho màn Attendance Detail của một nhân viên trong một tháng.
 *  - summary: dùng cho phần header (mã NV, tên, phòng ban, vị trí + chỉ số tổng hợp).
 *  - dailyRows: danh sách chấm công theo từng ngày (sắp xếp tăng dần theo ngày).
 */
public class AttendanceDetailDTO {

    private AttendanceSummaryDTO summary;
    private List<Attendance> dailyRows = new ArrayList<>();

    public AttendanceDetailDTO() {
    }

    public AttendanceDetailDTO(AttendanceSummaryDTO summary, List<Attendance> dailyRows) {
        this.summary = summary;
        this.dailyRows = (dailyRows != null) ? dailyRows : new ArrayList<>();
    }

    public AttendanceSummaryDTO getSummary() {
        return summary;
    }

    public void setSummary(AttendanceSummaryDTO summary) {
        this.summary = summary;
    }

    public List<Attendance> getDailyRows() {
        return dailyRows;
    }

    public void setDailyRows(List<Attendance> dailyRows) {
        this.dailyRows = (dailyRows != null) ? dailyRows : new ArrayList<>();
    }
}
