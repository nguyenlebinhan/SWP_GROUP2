/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AttendanceDAO;
import dto.AttendanceDataDTO;
import dto.AttendanceImportResultDTO;
import exception.InvalidFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
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

    // Trạng thái Uploaded_Files
    public static final int FILE_STATUS_PENDING = 0;
    public static final int FILE_STATUS_IMPORTED = 1;
    public static final int FILE_STATUS_FAILED = 2;
    public static final int FILE_STATUS_PARTIAL = 3;

    private static final Map<String, Integer> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("PRESENT", 0);
        STATUS_MAP.put("LATE", 1);
        STATUS_MAP.put("ABSENT", 2);
        STATUS_MAP.put("UNEXCUSED", 3);
    }

    private final AttendanceDAO attendanceDAO;
    private final ExcelAttendanceParser parser;

    public AttendanceImportService() {
        this.attendanceDAO = new AttendanceDAO();
        this.parser = new ExcelAttendanceParser();
    }


    public AttendanceImportResultDTO importAttendance(InputStream in, Integer departmentId, int fileId) {
        AttendanceImportResultDTO result = new AttendanceImportResultDTO();
        result.setFileId(fileId);

        List<AttendanceDataDTO> attendanceDataDTOs;
        try {
            attendanceDataDTOs = parser.parse(in);
        } catch (InvalidFormatException e) {
            result.setStatus(FILE_STATUS_FAILED);
            result.setNote(e.getMessage());
            result.addError(1, null, e.getMessage());
            return result;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot read Excel file", e);
            result.setStatus(FILE_STATUS_FAILED);
            result.setNote("Không thể đọc file Excel. File có thể bị hỏng hoặc không đúng định dạng .xlsx.");
            result.addError(1, null, result.getNote());
            return result;
        }

        result.setTotalRows(attendanceDataDTOs.size());
        int imported = 0;

        for (AttendanceDataDTO ad : attendanceDataDTOs) {
            try {
                Attendance att = buildAndValidate(ad, departmentId, fileId);
                if (attendanceDAO.upsertAttendance(att)) {
                    imported++;
                } else {
                    result.addError(ad.getRowNumber(), ad.getEmployeeCode(),
                            "Lưu dữ liệu thất bại (lỗi cơ sở dữ liệu).");
                }
            } catch (RowValidationException e) {
                result.addError(ad.getRowNumber(), ad.getEmployeeCode(), e.getMessage());
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Unexpected error importing row " + ad.getRowNumber(), e);
                result.addError(ad.getRowNumber(), ad.getEmployeeCode(),
                        "Lỗi không xác định: " + e.getMessage());
            }
        }

        result.setImportedRows(imported);
        result.setFailedRows(result.getTotalRows() - imported);

        if (result.getTotalRows() == 0) {
            result.setStatus(FILE_STATUS_FAILED);
            result.setNote("File không có dòng dữ liệu nào.");
        } else if (result.getFailedRows() == 0) {
            result.setStatus(FILE_STATUS_IMPORTED);
            result.setNote("Import thành công toàn bộ " + imported + " dòng.");
        } else if (imported == 0) {
            result.setStatus(FILE_STATUS_FAILED);
            result.setNote("Tất cả " + result.getTotalRows() + " dòng đều lỗi.");
        } else {
            result.setStatus(FILE_STATUS_PARTIAL);
            result.setNote("Import thành công " + imported + "/" + result.getTotalRows()
                    + " dòng, " + result.getFailedRows() + " dòng lỗi.");
        }
        return result;
    }

    private Attendance buildAndValidate(AttendanceDataDTO ad, Integer departmentId, int fileId)
            throws RowValidationException {

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

        // timeIn/timeOut: optional khi ABSENT/UNEXCUSED
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
            hoursWorked = null; // không đủ dữ liệu để tính giờ
        }

        // Map nhân viên theo employeeCode
        int employeeId = attendanceDAO.findEmployeeIdByCode(employeeCode);
        if (employeeId <= 0) {
            throw new RowValidationException("employeeCode không tồn tại: " + employeeCode);
        }
        if (departmentId != null && !attendanceDAO.employeeBelongsToDepartment(employeeId, departmentId)) {
            throw new RowValidationException("Nhân viên " + employeeCode
                    + " không thuộc phòng ban đã chọn để import.");
        }

        Attendance att = new Attendance();
        att.setAttendanceCode(generateAttendanceCode(employeeCode, workDate));
        att.setEmployeeId(employeeId);
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

    /** Sinh attendanceCode dạng ATT-EMP001-20260601. */
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

    /** Lỗi validate ở mức một dòng — không làm hỏng toàn bộ quá trình import. */
    private static class RowValidationException extends Exception {
        RowValidationException(String message) {
            super(message);
        }
    }
}
