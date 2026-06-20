package dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Gói dữ liệu để xuất báo cáo chấm công ra Excel.
 *  - scopeTitle: phạm vi báo cáo, vd "Phòng IT" hoặc "Toàn công ty".
 *  - month/year: kỳ báo cáo.
 *  - rows: danh sách tổng hợp theo nhân viên (mỗi nhân viên một dòng).
 */
public class AttendanceReportDTO {

    private String scopeTitle;
    private int month;
    private int year;
    private List<AttendanceSummaryDTO> rows = new ArrayList<>();

    public AttendanceReportDTO() {
    }

    public AttendanceReportDTO(String scopeTitle, int month, int year, List<AttendanceSummaryDTO> rows) {
        this.scopeTitle = scopeTitle;
        this.month = month;
        this.year = year;
        this.rows = (rows != null) ? rows : new ArrayList<>();
    }

    public String getScopeTitle() {
        return scopeTitle;
    }

    public void setScopeTitle(String scopeTitle) {
        this.scopeTitle = scopeTitle;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<AttendanceSummaryDTO> getRows() {
        return rows;
    }

    public void setRows(List<AttendanceSummaryDTO> rows) {
        this.rows = (rows != null) ? rows : new ArrayList<>();
    }
}
