package service;

import dao.AttendanceDAO;
import dao.DepartmentDAO;
import dao.HolidayDAO;
import dao.OvertimeDAO;
import dto.AttendanceDetailDTO;
import dto.AttendanceReportDTO;
import dto.AttendanceSummaryDTO;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import model.Department;
import model.Holiday;

/**
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final HolidayDAO holidayDAO = new HolidayDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final OvertimeDAO overtimeDAO = new OvertimeDAO();


    public List<AttendanceSummaryDTO> getMonthlySummaries(Integer departmentId, int month, int year) {
        List<AttendanceSummaryDTO> rows = attendanceDAO.getMonthlySummary(departmentId, month, year);
        int standardDays = standardWorkingDays(month, year);
        for (AttendanceSummaryDTO s : rows) {
            s.setStandardDays(standardDays);
            s.setOtDays(overtimeDAO.getApprovedOTDaysInMonth(s.getEmployeeId(), month, year).size());
        }
        return rows;
    }

    public AttendanceDetailDTO getEmployeeDetail(int employeeId, Integer departmentId, int month, int year) {
        AttendanceSummaryDTO summary = null;
        for (AttendanceSummaryDTO s : getMonthlySummaries(departmentId, month, year)) {
            if (s.getEmployeeId() == employeeId) {
                summary = s;
                break;
            }
        }
        if (summary == null) {
            return null;
        }
        return new AttendanceDetailDTO(summary, attendanceDAO.getDailyAttendance(employeeId, month, year));
    }


    public AttendanceReportDTO getReport(Integer departmentId, int month, int year) {
        String scope = "Toàn công ty";
        if (departmentId != null) {
            Department d = departmentDAO.getDepartmentById(departmentId);
            scope = (d != null && d.getDepartmentName() != null)
                    ? "Phòng " + d.getDepartmentName() : "Phòng #" + departmentId;
        }
        return new AttendanceReportDTO(scope, month, year, getMonthlySummaries(departmentId, month, year));
    }


    public int standardWorkingDays(int month, int year) {
        List<Holiday> holidays = holidayDAO.getAllHolidays();
        LocalDate cursor = LocalDate.of(year, month, 1);
        LocalDate end = cursor.withDayOfMonth(cursor.lengthOfMonth());
        int count = 0;
        while (!cursor.isAfter(end)) {
            DayOfWeek dow = cursor.getDayOfWeek();
            boolean weekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
            if (!weekend && !isHoliday(cursor, holidays)) {
                count++;
            }
            cursor = cursor.plusDays(1);
        }
        return count;
    }

    private boolean isHoliday(LocalDate date, List<Holiday> holidays) {
        for (Holiday h : holidays) {
            if (!h.isActive() || h.getStartDate() == null || h.getEndDate() == null) {
                continue;
            }
            LocalDate start = h.getStartDate().toLocalDate();
            LocalDate finish = h.getEndDate().toLocalDate();
            if (!date.isBefore(start) && !date.isAfter(finish)) {
                return true;
            }
        }
        return false;
    }
}
