/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dal.DBContext;
import dao.AttendanceDAO;
import dao.FormRequestDAO;
import dao.UploadedFileDAO;
import dto.AttendanceDataDTO;
import dto.AttendanceImportResultDTO;
import dto.EmployeeDetailDTO;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Attendance;
import utils.ExcelAttendanceParser;

public class AttendanceImportService {

    private static final Logger LOGGER = Logger.getLogger(AttendanceImportService.class.getName());

    private static final Time WORK_START = Time.valueOf("08:00:00");

    private static final Time WORK_END = Time.valueOf("17:00:00");

    private static final BigDecimal STANDARD_HOURS = new BigDecimal("8.00");

    private final DBContext dbContext;
    private final AttendanceDAO attendanceDAO;
    private final UploadedFileDAO uploadedFileDAO;
    private final FormRequestDAO formRequestDAO;
    private final dao.OvertimeDAO overtimeDAO;
    private final ExcelAttendanceParser parser;

    public AttendanceImportService() {
        this.dbContext = new DBContext();
        this.attendanceDAO = new AttendanceDAO();
        this.uploadedFileDAO = new UploadedFileDAO();
        this.formRequestDAO = new FormRequestDAO();
        this.overtimeDAO = new dao.OvertimeDAO();
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
                Set<java.sql.Date> importedDates = new HashSet<>();
                for (AttendanceDataDTO ad : attendanceDataDTOs) {
                    try {
                        Attendance att = buildAndValidate(conn, ad, departmentId, month, year, fileId);
                        if (attendanceDAO.upsertAttendance(conn, att)) {
                            imported++;
                            importedDates.add(att.getWorkDate());
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
                
                for (java.sql.Date d : importedDates) {
                    overtimeDAO.cancelUnfulfilledOTForms(conn, d);
                }
                
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
        if (isWeekend(workDate)) {
            throw new RowValidationException("workDate " + workDate
                    + " rơi vào ngày cuối tuần (Thứ Bảy/Chủ nhật), không được phép import.");
        }

        Time timeIn = parseTime(ad.getTimeIn(), "timeIn");
        Time timeOut = parseTime(ad.getTimeOut(), "timeOut");

        EmployeeDetailDTO employee = attendanceDAO.findEmployeeDetailsByCode(conn, employeeCode);
        if (employee == null) {
            throw new RowValidationException("employeeCode không tồn tại: " + employeeCode);
        }
        int employeeId = employee.getEmployeeId();
        employeeCode = employee.getEmployeeCode();

        // Giữ NGUYÊN giờ thực (timeIn/timeOut) để lưu và hiển thị.
        // Mọi tính toán bên dưới (giờ công, trạng thái) dùng bản chuẩn hóa theo block.
        Time calcTimeIn = timeIn;
        Time calcTimeOut = timeOut;

        BigDecimal hoursWorked;
        if (timeIn != null && timeOut != null) {
            if (timeOut.getTime() - timeIn.getTime() < 0) {
                throw new RowValidationException("timeOut phải sau timeIn.");
            }

            boolean hasOT = overtimeDAO.hasApprovedOT(conn, employeeId, workDate);

            Time maxTime = hasOT ? Time.valueOf("19:00:00") : Time.valueOf("17:00:00");

            if (!timeOut.before(maxTime)) {
                boolean otRevived = overtimeDAO.reviveAndCompleteOTForm(conn, employeeId, workDate);
                if (otRevived && !hasOT) {
                    hasOT = true;
                    maxTime = Time.valueOf("19:00:00");
                }
            }

            if (timeIn.after(maxTime)) {
                throw new RowValidationException("Thời gian Check-in tối đa là " + (hasOT ? "19:00" : "17:00") + ".");
            }

            // Chuẩn hóa thời gian theo block 30 phút: giờ vào làm tròn LÊN
            // (đi muộn trong block nào mất trọn block đó), giờ ra làm tròn XUỐNG.
            calcTimeIn = utils.WorkHoursCalculator.ceilToBlock(timeIn);
            calcTimeOut = utils.WorkHoursCalculator.floorToBlock(timeOut);

            if (calcTimeOut.after(maxTime)) {
                calcTimeOut = maxTime;
            }

            hoursWorked = utils.WorkHoursCalculator.hoursWorked(calcTimeIn, calcTimeOut);

            // Không OT thì giờ công không vượt quá 8 tiếng chuẩn.
            if (!hasOT && hoursWorked.compareTo(STANDARD_HOURS) > 0) {
                hoursWorked = STANDARD_HOURS;
            }
        } else {

            hoursWorked = BigDecimal.ZERO;
        }

        if (employee.getDepartmentId() <= 0
                || trimToNull(employee.getDepartmentName()) == null) {
            throw new RowValidationException("Nhân viên " + employeeCode
                    + " chưa được phân công phòng ban, không thể import.");
        }
        if (departmentId > 0 && employee.getDepartmentId() != departmentId) {
            throw new RowValidationException("Nhân viên " + employeeCode
                    + " không thuộc phòng ban đã chọn để import.");
        }

        AttendanceStatus baseStatus = deriveStatus(calcTimeIn, calcTimeOut);
        AttendanceStatus finalStatus = determineFinalStatus(baseStatus, employeeId, workDate, conn);

        Attendance att = new Attendance();
        att.setAttendanceCode(generateAttendanceCode(employeeCode, workDate));
        att.setEmployeeId(employeeId);
        att.setEmployeeCode(employeeCode);
        att.setFullName(employee.getFullName());
        att.setDepartmentId(employee.getDepartmentId());
        att.setDepartmentName(employee.getDepartmentName());
        if (employee.getPositionId() > 0) {
            att.setPositionId(employee.getPositionId());
            att.setPositionName(employee.getPositionName());
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
        // Thiếu cả hai: chưa chấm công ngày đó -> Vắng mặt (có thể được xét lại
        // thành nghỉ phép/cuối tuần ở determineFinalStatus).
        if (timeIn == null && timeOut == null) {
            return AttendanceStatus.ABSENT;
        }
        // Chỉ có một trong hai: quên chấm công vào hoặc ra -> đánh dấu riêng để HR xử lý.
        if (timeIn == null || timeOut == null) {
            return AttendanceStatus.MISSING_CHECK;
        }
        if (timeIn.after(WORK_START)) {
            return AttendanceStatus.LATE;
        }
        return AttendanceStatus.PRESENT;
    }

    private AttendanceStatus determineFinalStatus(AttendanceStatus base, int employeeId,
            Date workDate, Connection conn) throws SQLException {
        if (base == AttendanceStatus.PRESENT || base == AttendanceStatus.LATE
                || base == AttendanceStatus.MISSING_CHECK) {
            return base;
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
