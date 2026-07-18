package service;

import dao.AttendanceDAO;
import dao.DepartmentDAO;
import dao.OvertimeDAO;
import dto.AttendanceDetailDTO;
import dto.AttendanceReportDTO;
import dto.AttendanceSummaryDTO;
import dto.PayrollAttendanceSummaryDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;
import model.Department;

/**
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
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
        return workingDaysBetween(LocalDate.of(year, month, 1),
                LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth()));
    }

    public int workingDaysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return workingDaysBetween(startDate.toLocalDate(), endDate.toLocalDate());
    }

    private int workingDaysBetween(LocalDate start, LocalDate end) {
        LocalDate cursor = start;
        int count = 0;
        while (!cursor.isAfter(end)) {
            DayOfWeek dow = cursor.getDayOfWeek();
            boolean weekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
            if (!weekend) {
                count++;
            }
            cursor = cursor.plusDays(1);
        }
        return count;
    }

    public PayrollAttendanceSummaryDTO getPayrollSummary(Connection conn, int employeeId, int year, int month,
            Date fromDate, Date toDate,
            BigDecimal dailyRate, BigDecimal minuteRate, LocalTime standardStartTime, int lateDeductionBlockMinutes)
            throws SQLException {
        PayrollAttendanceSummaryDTO summary = new PayrollAttendanceSummaryDTO();
        String sql = "SELECT attendanceStatus, COALESCE(hoursWorked, 0) AS hoursWorked, timeIn, workDate "
                + "FROM Attendance "
                + "WHERE employeeId = ? AND YEAR(workDate) = ? AND MONTH(workDate) = ? "
                + "AND workDate BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            ps.setDate(4, fromDate);
            ps.setDate(5, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    summary.incrementRecordCount();

                    Date workDate = rs.getDate("workDate");
                    if (workDate == null || isWeekend(workDate)) {
                        continue;
                    }

                    int status = rs.getInt("attendanceStatus");
                    summary.addHoursWorked(rs.getBigDecimal("hoursWorked"));

                    if (status == 0) {
                        summary.incrementPaidWorkingDays();
                    } else if (status == 1) {
                        summary.incrementPaidWorkingDays();
                        summary.incrementLateCount();

                        LocalTime timeIn = rs.getTime("timeIn") == null
                                ? standardStartTime
                                : rs.getTime("timeIn").toLocalTime();
                        int lateMinutes = Math.max(0,
                                (int) java.time.Duration.between(standardStartTime, timeIn).toMinutes());
                        int lateDeductionMinutes = roundUpToBlock(lateMinutes, lateDeductionBlockMinutes);

                        summary.addLateMinutes(lateMinutes);
                        summary.addLateDeductionMinutes(lateDeductionMinutes);
                        summary.addLateDeductionBlocks(lateDeductionMinutes / lateDeductionBlockMinutes);
                        summary.addLateDeduction(moneyOrZero(minuteRate).multiply(new BigDecimal(lateDeductionMinutes)));
                    } else if (status == 4) {
                        summary.incrementPaidLeaveDays();
                        summary.incrementPaidWorkingDays();
                    } else if (status == 2 || status == 3) {
                        summary.incrementUnauthorizedAbsentDays();
                    }
                }
            }
        }
        return summary;
    }

    private boolean isWeekend(Date workDate) {
        DayOfWeek dow = workDate.toLocalDate().getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    private int roundUpToBlock(int minutes, int blockMinutes) {
        if (minutes <= 0) {
            return 0;
        }
        if (blockMinutes <= 0) {
            return minutes;
        }
        return ((minutes + blockMinutes - 1) / blockMinutes) * blockMinutes;
    }

    private BigDecimal moneyOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value;
    }

}

