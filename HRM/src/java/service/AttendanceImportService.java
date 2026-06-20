/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dal.DBContext;
import dao.AttendanceDAO;
import dao.AttendanceImportRowDAO;
import dao.UploadedFileDAO;
import dto.AttendanceDataDTO;
import dto.AttendanceImportResultDTO;
import enums.AttendanceStatus;
import enums.FileStatus;
import exception.InvalidFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Attendance;
import utils.ExcelAttendanceParser;

public class AttendanceImportService {

    private static final Logger LOGGER = Logger.getLogger(AttendanceImportService.class.getName());
    private static final Map<String, Integer> STATUS_MAP = new HashMap<>();
    static {
        for (AttendanceStatus s : AttendanceStatus.values()) {
            STATUS_MAP.put(s.name(), s.getRelatedNum());
        }
    }
        private final DBContext dbContext;
    private final AttendanceDAO attendanceDAO;
    private final AttendanceImportRowDAO importRowDAO;
    private final UploadedFileDAO uploadedFileDAO;
    private final ExcelAttendanceParser parser;

    public AttendanceImportService() {
        this.dbContext = new DBContext();
        this.attendanceDAO = new AttendanceDAO();
        this.importRowDAO = new AttendanceImportRowDAO();
        this.uploadedFileDAO = new UploadedFileDAO();
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
                        Attendance att = buildAndValidate(ad, departmentId, month, year, fileId);
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

    private Attendance buildAndValidate(AttendanceDataDTO ad, int departmentId,
            int month, int year, int fileId) throws RowValidationException {

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

        String statusRaw = trimToNull(ad.getAttendanceStatus());
        if (statusRaw == null) {
            throw new RowValidationException("Thiếu attendanceStatus.");
        }
        Integer statusCode = STATUS_MAP.get(statusRaw.toUpperCase(Locale.ROOT));
        if (statusCode == null) {
            throw new RowValidationException("attendanceStatus không hợp lệ: " + statusRaw
                    + " (chỉ chấp nhận PRESENT, LATE, ABSENT, UNEXCUSED).");
        }
        boolean isAbsent = statusCode == 2 || statusCode == 3;
        Time timeIn = parseTime(ad.getTimeIn(), "timeIn");
        Time timeOut = parseTime(ad.getTimeOut(), "timeOut");

        BigDecimal hoursWorked;
        if (isAbsent) {
            hoursWorked = BigDecimal.ZERO;
        } else if (timeIn != null && timeOut != null) {
            long diffMillis = timeOut.getTime() - timeIn.getTime();
            if (diffMillis < 0) {
                throw new RowValidationException("timeOut phải sau timeIn.");
            }
            hoursWorked = new BigDecimal(diffMillis)
                    .divide(new BigDecimal(3600000), 2, RoundingMode.HALF_UP);
        } else {
            hoursWorked = null; 
        }

        int employeeId = attendanceDAO.findEmployeeIdByCode(employeeCode);
        if (employeeId <= 0) {
            throw new RowValidationException("employeeCode không tồn tại: " + employeeCode);
        }
        if (!attendanceDAO.employeeBelongsToDepartment(employeeId, departmentId)) {
            throw new RowValidationException("Nhân viên " + employeeCode
                    + " không thuộc phòng ban đã chọn để import.");
        }

        Attendance att = new Attendance();
        att.setAttendanceCode(generateAttendanceCode(employeeCode, workDate));
        att.setEmployeeId(employeeId);
        att.setEmployeeCode(employeeCode);
        att.setFullName(trimToNull(ad.getFullName()));
        att.setDepartmentId(departmentId);
        att.setDepartmentName(trimToNull(ad.getDepartmentName()));
        att.setWorkDate(workDate);
        att.setTimeIn(timeIn);
        att.setTimeOut(timeOut);
        att.setHoursWorked(hoursWorked);
        att.setAttendanceStatus(statusCode);
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
