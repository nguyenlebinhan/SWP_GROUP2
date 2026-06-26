/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dal.DBContext;
import dao.AttendanceDAO;
import dao.FormRequestDAO;
import dao.HolidayDAO;
import dao.UploadedFileDAO;
import dto.AttendanceDataDTO;
import dto.AttendanceImportResultDTO;
import enums.AttendanceStatus;
import enums.FileStatus;
import exception.InvalidFormatException;
import exception.RowValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Attendance;
import model.Position;
import utils.ExcelAttendanceParser;

public class AttendanceImportService {

    private static final Logger LOGGER = Logger.getLogger(AttendanceImportService.class.getName());

    private static final Time WORK_START = Time.valueOf("08:00:00");

    /** Số giờ làm chuẩn trong một ngày. Khi không có đơn OT được duyệt, giờ công bị giới hạn tối đa ở mức này. */
    private static final BigDecimal STANDARD_HOURS = new BigDecimal("8.00");

    private final DBContext dbContext;
    private final AttendanceDAO attendanceDAO;
    private final UploadedFileDAO uploadedFileDAO;
    private final HolidayDAO holidayDAO;
    private final FormRequestDAO formRequestDAO;
    private final dao.OvertimeDAO overtimeDAO;
    private final ExcelAttendanceParser parser;

    public AttendanceImportService() {
        this.dbContext = new DBContext();
        this.attendanceDAO = new AttendanceDAO();
        this.uploadedFileDAO = new UploadedFileDAO();
        this.holidayDAO = new HolidayDAO();
        this.formRequestDAO = new FormRequestDAO();
        this.overtimeDAO = new dao.OvertimeDAO();
        this.parser = new ExcelAttendanceParser();
    }

    /**
     * Giới hạn giờ công ở mức 8 tiếng chuẩn nếu nhân viên không có đơn OT được duyệt cho ngày đó.
     * Nếu có đơn OT được duyệt thì giữ nguyên giờ công thực tế (cho phép vượt 8 tiếng).
     */
    private BigDecimal capHoursWithoutOT(Connection conn, int employeeId, Date workDate,
            BigDecimal hoursWorked) throws SQLException {
        if (hoursWorked.compareTo(STANDARD_HOURS) > 0
                && !overtimeDAO.hasApprovedOT(conn, employeeId, workDate)) {
            return STANDARD_HOURS;
        }
        return hoursWorked;
    }


    public AttendanceImportResultDTO importAttendance(InputStream in, int departmentId,
            int month, int year, int fileId) {
        AttendanceImportResultDTO result = new AttendanceImportResultDTO();
        result.setFileId(fileId);

        List<AttendanceDataDTO> attendanceDataDTOs;
        try {
            attendanceDataDTOs = parser.parse(in);
        } catch (InvalidFormatException e) {
            result.setStatus(FileStatus.FILE_STATUS_FAILED.getRelatedNum());
            result.setNote(e.getMessage());
            result.addError(1, null, e.getMessage());
            uploadedFileDAO.updateImportResult(fileId, 0, 0, 0, result.getStatus(), result.getNote());
            return result;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot read Excel file", e);
            result.setStatus(FileStatus.FILE_STATUS_FAILED.getRelatedNum());
            result.setNote("Không thể đọc file Excel. File có thể bị hỏng hoặc không đúng định dạng .xlsx.");
            result.addError(1, null, result.getNote());
            uploadedFileDAO.updateImportResult(fileId, 0, 0, 0, result.getStatus(), result.getNote());
            return result;
        }

        result.setTotalRows(attendanceDataDTOs.size());

        try (Connection conn = dbContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int imported = 0;
                for (AttendanceDataDTO ad : attendanceDataDTOs) {
                    try {
                        Attendance att = buildAndValidate(conn, ad, departmentId, month, year, fileId);
                        if (attendanceDAO.upsertAttendance(conn, att)) {
                            imported++;
                        } else {
                            result.addError(ad.getRowNumber(), ad.getEmployeeCode(),
                                    "Lưu dữ liệu thất bại (lỗi cơ sở dữ liệu).");
                        }
                    } catch (RowValidationException e) {
                        result.addError(ad.getRowNumber(), ad.getEmployeeCode(), e.getMessage());
                    }
                }

                result.setImportedRows(imported);
                result.setFailedRows(result.getTotalRows() - imported);
                applyResultStatus(result, imported);
                uploadedFileDAO.updateImportResult(conn, fileId, result.getTotalRows(),
                        result.getImportedRows(), result.getFailedRows(), result.getStatus(), result.getNote());
                conn.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Attendance import transaction failed for fileId: " + fileId, e);
            result.setImportedRows(0);
            result.setFailedRows(result.getTotalRows());
            result.setStatus(FileStatus.FILE_STATUS_FAILED.getRelatedNum());
            result.setNote("Lỗi hệ thống khi import. Toàn bộ thay đổi đã được hoàn tác, dữ liệu cũ không bị mất.");
            result.addError(1, null, result.getNote());
            uploadedFileDAO.updateImportResult(fileId, result.getTotalRows(), 0,
                    result.getTotalRows(), FileStatus.FILE_STATUS_FAILED.getRelatedNum(), result.getNote());
            return result;
        }
    }

    private void applyResultStatus(AttendanceImportResultDTO result, int imported) {
        if (result.getTotalRows() == 0) {
            result.setStatus(FileStatus.FILE_STATUS_FAILED.getRelatedNum());
            result.setNote("File không có dòng dữ liệu nào.");
        } else if (result.getFailedRows() == 0) {
            result.setStatus(FileStatus.FILE_STATUS_IMPORTED.getRelatedNum());
            result.setNote("Import thành công toàn bộ " + imported + " dòng.");
        } else if (imported == 0) {
            result.setStatus(FileStatus.FILE_STATUS_FAILED.getRelatedNum());
            result.setNote("Tất cả " + result.getTotalRows() + " dòng đều lỗi.");
        } else {
            result.setStatus(FileStatus.FILE_STATUS_PARTIAL.getRelatedNum());
            result.setNote("Import thành công " + imported + "/" + result.getTotalRows()
                    + " dòng, " + result.getFailedRows() + " dòng lỗi.");
        }
    }

    private Attendance buildAndValidate(Connection conn, AttendanceDataDTO ad, int departmentId,
            int month, int year, int fileId) throws RowValidationException, SQLException {

        String employeeCode = trimToNull(ad.getEmployeeCode());
        if (employeeCode == null) {
            throw new RowValidationException("Thiếu employeeCode.");
        }

        if (trimToNull(ad.getWorkDate()) == null) {
            throw new RowValidationException("Thiếu workDate.");
        }
        Date workDate;
        try {
            workDate = Date.valueOf(ad.getWorkDate().trim());
        } catch (IllegalArgumentException e) {
            throw new RowValidationException("workDate không hợp lệ (yêu cầu yyyy-MM-dd): " + ad.getWorkDate());
        }
        if (workDate.toLocalDate().getMonthValue() != month
                || workDate.toLocalDate().getYear() != year) {
            throw new RowValidationException("workDate " + ad.getWorkDate().trim()
                    + " không thuộc tháng " + month + "/" + year + " đã chọn.");
        }

        Time timeIn = parseTime(ad.getTimeIn(), "timeIn");
        Time timeOut = parseTime(ad.getTimeOut(), "timeOut");

        int employeeId = attendanceDAO.findEmployeeIdByCode(employeeCode);
        if (employeeId <= 0) {
            throw new RowValidationException("employeeCode không tồn tại: " + employeeCode);
        }

        BigDecimal hoursWorked;
        if (timeIn != null && timeOut != null) {
            if (timeOut.getTime() - timeIn.getTime() < 0) {
                throw new RowValidationException("timeOut phải sau timeIn.");
            }

            hoursWorked = utils.WorkHoursCalculator.hoursWorked(timeIn, timeOut);
            hoursWorked = capHoursWithoutOT(conn, employeeId, workDate, hoursWorked);
        } else {

            hoursWorked = BigDecimal.ZERO;
        }

        int effectiveDeptId;
        String effectiveDeptName;
        if (departmentId > 0) {
            if (!attendanceDAO.employeeBelongsToDepartment(employeeId, departmentId)) {
                throw new RowValidationException("Nhân viên " + employeeCode
                        + " không thuộc phòng ban đã chọn để import.");
            }
            effectiveDeptId = departmentId;
            effectiveDeptName = trimToNull(ad.getDepartmentName());
        } else {
            model.Department dep = attendanceDAO.getEmployeeDepartment(employeeId);
            if (dep == null) {
                throw new RowValidationException("Nhân viên " + employeeCode
                        + " chưa được phân công phòng ban, không thể import.");
            }
            effectiveDeptId = dep.getDepartmentId();
            effectiveDeptName = dep.getDepartmentName();
        }

        Position position = attendanceDAO.getEmployeePosition(employeeId);

        AttendanceStatus baseStatus = deriveStatus(timeIn, timeOut);
        AttendanceStatus finalStatus = determineFinalStatus(baseStatus, employeeId, workDate, conn);

        Attendance att = new Attendance();
        att.setAttendanceCode(generateAttendanceCode(employeeCode, workDate));
        att.setEmployeeId(employeeId);
        att.setEmployeeCode(employeeCode);
        att.setFullName(trimToNull(ad.getFullName()));
        att.setDepartmentId(effectiveDeptId);
        att.setDepartmentName(effectiveDeptName);
        if (position != null) {
            att.setPositionId(position.getPositionId());
            att.setPositionName(position.getPositionName());
        }
        att.setWorkDate(workDate);
        att.setTimeIn(timeIn);
        att.setTimeOut(timeOut);
        att.setHoursWorked(hoursWorked);
        att.setAttendanceStatus(finalStatus.getRelatedNum());
        att.setFileId(fileId);
        return att;
    }

    private Time parseTime(String raw, String fieldName) throws RowValidationException {
        String v = trimToNull(raw);
        if (v == null) {
            return null;
        }
        if (v.matches("\\d{1,2}:\\d{2}")) {
            v = v + ":00";
        }
        try {
            return Time.valueOf(v);
        } catch (IllegalArgumentException e) {
            throw new RowValidationException(fieldName + " không hợp lệ (yêu cầu HH:mm): " + raw);
        }
    }

    public AttendanceStatus resolveStatus(int employeeId, Date workDate, Time timeIn, Time timeOut)
            throws SQLException {
        AttendanceStatus base = deriveStatus(timeIn, timeOut);
        try (Connection conn = dbContext.getConnection()) {
            return determineFinalStatus(base, employeeId, workDate, conn);
        }
    }

    private AttendanceStatus deriveStatus(Time timeIn, Time timeOut) {
        if (timeIn == null || timeOut == null) {
            return AttendanceStatus.ABSENT;
        }
        if (timeIn.after(WORK_START)) {
            return AttendanceStatus.LATE;
        }
        return AttendanceStatus.PRESENT;
    }

    private AttendanceStatus determineFinalStatus(AttendanceStatus base, int employeeId,
            Date workDate, Connection conn) throws SQLException {
        if (base == AttendanceStatus.PRESENT || base == AttendanceStatus.LATE) {
            return base;
        }
        if (holidayDAO.isHoliday(conn, workDate)) {
            return AttendanceStatus.HOLIDAY;
        }
        if (isWeekend(workDate)) {
            return AttendanceStatus.WEEKEND;
        }
        if (formRequestDAO.hasApprovedLeave(conn, employeeId, workDate)) {
            return AttendanceStatus.LEAVE;
        }
        return AttendanceStatus.ABSENT;
    }

     private boolean isWeekend(Date workDate) {
        DayOfWeek day = workDate.toLocalDate().getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private String generateAttendanceCode(String employeeCode, Date workDate) {
        String datePart = workDate.toString().replace("-", "");
        return "ATT-" + employeeCode + "-" + datePart;
    }

    private String trimToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

}
