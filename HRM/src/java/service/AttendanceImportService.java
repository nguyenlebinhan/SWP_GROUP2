/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dal.DBContext;
import dao.AttendanceDAO;
import dao.AttendanceImportRowDAO;
import dao.FormRequestDAO;
import dao.HolidayDAO;
import dao.UploadedFileDAO;
import dto.AttendanceDataDTO;
import dto.AttendanceImportResultDTO;
import enums.AttendanceStatus;
import enums.FileStatus;
import exception.InvalidFormatException;
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
import utils.ExcelAttendanceParser;

public class AttendanceImportService {

    private static final Logger LOGGER = Logger.getLogger(AttendanceImportService.class.getName());

    private static final Time WORK_START = Time.valueOf("08:00:00");

    private final DBContext dbContext;
    private final AttendanceDAO attendanceDAO;
    private final AttendanceImportRowDAO importRowDAO;
    private final UploadedFileDAO uploadedFileDAO;
    private final HolidayDAO holidayDAO;
    private final FormRequestDAO formRequestDAO;
    private final ExcelAttendanceParser parser;

    public AttendanceImportService() {
        this.dbContext = new DBContext();
        this.attendanceDAO = new AttendanceDAO();
        this.importRowDAO = new AttendanceImportRowDAO();
        this.uploadedFileDAO = new UploadedFileDAO();
        this.holidayDAO = new HolidayDAO();
        this.formRequestDAO = new FormRequestDAO();
        this.parser = new ExcelAttendanceParser();
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
                    int rowId = importRowDAO.insertRow(conn, fileId, ad);
                    try {
                        Attendance att = buildAndValidate(conn, ad, departmentId, month, year, fileId);
                        if (attendanceDAO.upsertAttendance(conn, att)) {
                            importRowDAO.markRow(conn, rowId, true, null);
                            imported++;
                        } else {
                            importRowDAO.markRow(conn, rowId, false, "Lưu dữ liệu thất bại (lỗi cơ sở dữ liệu).");
                            result.addError(ad.getRowNumber(), ad.getEmployeeCode(),
                                    "Lưu dữ liệu thất bại (lỗi cơ sở dữ liệu).");
                        }
                    } catch (RowValidationException e) {
                        importRowDAO.markRow(conn, rowId, false, e.getMessage());
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

        BigDecimal hoursWorked;
        if (timeIn != null && timeOut != null) {
            if (timeOut.getTime() - timeIn.getTime() < 0) {
                throw new RowValidationException("timeOut phải sau timeIn.");
            }
            // Trừ giờ nghỉ trưa: làm đủ 08:00-17:00 -> 8 tiếng (không phải 9).
            hoursWorked = utils.WorkHoursCalculator.hoursWorked(timeIn, timeOut);
        } else {
            // vắng / nghỉ / lễ / cuối tuần: không có giờ làm
            hoursWorked = BigDecimal.ZERO;
        }

        int employeeId = attendanceDAO.findEmployeeIdByCode(employeeCode);
        if (employeeId <= 0) {
            throw new RowValidationException("employeeCode không tồn tại: " + employeeCode);
        }

        // Xác định phòng ban lưu cho bản ghi này.
        //  - departmentId > 0: import theo 1 phòng -> nhân viên phải thuộc đúng phòng đó.
        //  - departmentId <= 0: import gộp tất cả phòng trong 1 file -> tự suy phòng thật của nhân viên.
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

        // Suy trạng thái gốc từ giờ vào/ra, rồi áp thứ tự ưu tiên nghiệp vụ.
        AttendanceStatus baseStatus = deriveStatus(timeIn, timeOut);
        AttendanceStatus finalStatus = determineFinalStatus(baseStatus, employeeId, workDate, conn);

        Attendance att = new Attendance();
        att.setAttendanceCode(generateAttendanceCode(employeeCode, workDate));
        att.setEmployeeId(employeeId);
        att.setEmployeeCode(employeeCode);
        att.setFullName(trimToNull(ad.getFullName()));
        att.setDepartmentId(effectiveDeptId);
        att.setDepartmentName(effectiveDeptName);
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

    /**
     * Suy trạng thái cuối cùng cho MỘT bản ghi (dùng cho luồng sửa tay chấm công).
     * Tự mở connection riêng để đọc Holiday / đơn nghỉ; áp đúng thứ tự ưu tiên như khi import.
     */
    public AttendanceStatus resolveStatus(int employeeId, Date workDate, Time timeIn, Time timeOut)
            throws SQLException {
        AttendanceStatus base = deriveStatus(timeIn, timeOut);
        try (Connection conn = dbContext.getConnection()) {
            return determineFinalStatus(base, employeeId, workDate, conn);
        }
    }

    /**
     * Suy trạng thái gốc CHỈ từ giờ vào/ra.
     * Thiếu một trong hai mốc giờ => ABSENT; vào sau 08:00 => LATE; còn lại PRESENT.
     */
    private AttendanceStatus deriveStatus(Time timeIn, Time timeOut) {
        if (timeIn == null || timeOut == null) {
            return AttendanceStatus.ABSENT;
        }
        if (timeIn.after(WORK_START)) {
            return AttendanceStatus.LATE;
        }
        return AttendanceStatus.PRESENT;
    }

    /**
     * Áp thứ tự ưu tiên. Chỉ chuyển đổi khi trạng thái gốc là ABSENT;
     * PRESENT/LATE (đi làm thực tế) luôn được giữ nguyên.
     * Ưu tiên khi ABSENT: HOLIDAY > WEEKEND > LEAVE > ABSENT.
     */
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

    private static class RowValidationException extends Exception {
        RowValidationException(String message) {
            super(message);
        }
    }
}
