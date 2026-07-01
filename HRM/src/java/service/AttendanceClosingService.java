package service;

import dao.AttendancePeriodDAO;
import dao.DepartmentDAO;
import dao.EmployeeDAO;
import dao.RoleDAO;
import dto.EmployeeDetailDTO;
import enums.AttendancePeriodStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.AttendancePeriod;
import model.Department;
import model.User;

/**
 * Điều phối quy trình chốt bảng chấm công theo (tháng, phòng ban):
 *
 * OPEN -> (HR đóng kỳ) -> WAITING_MANAGER -> (trưởng phòng chốt) -> MANAGER_CONFIRMED
 *       -> (HR gửi lên BA) -> SUBMITTED_TO_BA -> (BA chốt cuối) -> LOCKED
 *
 * Khi LOCKED: cho phép tính lương và khoá sửa chấm công vĩnh viễn.
 *
 * Ghi chú: phần gửi email được bổ sung ở bước sau; hiện tại chỉ xử lý trạng thái.
 *
 * @author ADMIN
 */
public class AttendanceClosingService {

    private final AttendancePeriodDAO periodDAO = new AttendancePeriodDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final AuditLogService auditLogService = new AuditLogService();

    /** Ngày cuối cùng được sửa chấm công: mùng 5 của tháng kế tiếp. */
    private static final int EDIT_DEADLINE_DAY = 5;

    // ── Trạng thái ────────────────────────────────────────────────────────

    /**
     * Trạng thái hiệu lực của một phòng trong kỳ. Không có dòng dữ liệu = OPEN.
     */
    public AttendancePeriodStatus getEffectiveStatus(int year, int month, int departmentId) {
        AttendancePeriod row = periodDAO.get(year, month, departmentId);
        if (row == null) {
            return AttendancePeriodStatus.OPEN;
        }
        return AttendancePeriodStatus.fromCode(row.getStatus());
    }

    public List<AttendancePeriod> listPeriod(int year, int month) {
        return periodDAO.listByPeriod(year, month);
    }

    /**
     * Dòng trạng thái chốt của một phòng ban (không bao giờ null; OPEN nếu chưa có dòng).
     */
    public AttendancePeriod getClosingRow(int year, int month, int departmentId) {
        AttendancePeriod row = periodDAO.get(year, month, departmentId);
        if (row == null) {
            row = new AttendancePeriod(year, month, departmentId,
                    AttendancePeriodStatus.OPEN.getRelatedNum());
        }
        if (row.getDepartmentName() == null) {
            row.setDepartmentName(departmentLabel(departmentId));
        }
        return row;
    }

    /**
     * Trạng thái chốt của mọi phòng ban có dữ liệu chấm công trong kỳ.
     * Phòng chưa được đóng kỳ (không có dòng dữ liệu) được coi là OPEN.
     */
    public List<AttendancePeriod> getClosingOverview(int year, int month) {
        List<AttendancePeriod> result = new ArrayList<>();
        for (Integer deptId : periodDAO.getDepartmentIdsWithAttendance(year, month)) {
            AttendancePeriod row = periodDAO.get(year, month, deptId);
            if (row == null) {
                row = new AttendancePeriod(year, month, deptId,
                        AttendancePeriodStatus.OPEN.getRelatedNum());
            }
            if (row.getDepartmentName() == null) {
                row.setDepartmentName(departmentLabel(deptId));
            }
            result.add(row);
        }
        return result;
    }

    // ── Khoá sửa chấm công ────────────────────────────────────────────────

    /**
     * Xác định một bản ghi chấm công có bị khoá sửa hay không.
     *
     * Khoá khi: đã quá mùng 5 tháng kế tiếp (quy tắc cũ), HOẶC kỳ của phòng đã
     * bước vào quy trình chốt (khác OPEN). Khi LOCKED thì khoá vĩnh viễn.
     */
    public boolean isEditLocked(java.sql.Date workDate, Integer departmentId) {
        if (workDate == null) {
            return false;
        }
        LocalDate date = workDate.toLocalDate();
        if (isAfterEditDeadline(date)) {
            return true;
        }
        if (departmentId != null) {
            AttendancePeriodStatus status = getEffectiveStatus(date.getYear(), date.getMonthValue(), departmentId);
            return status != AttendancePeriodStatus.OPEN;
        }
        return false;
    }

    private boolean isAfterEditDeadline(LocalDate workDate) {
        LocalDate deadline = workDate.withDayOfMonth(1).plusMonths(1).withDayOfMonth(EDIT_DEADLINE_DAY);
        return LocalDate.now().isAfter(deadline);
    }

    // ── Bước 1: HR đóng kỳ, gửi cho trưởng các phòng ──────────────────────

    public ClosingResult openPeriodForManagers(int year, int month, User hrUser) {
        if (!isHr(hrUser)) {
            return ClosingResult.fail("Bạn không có quyền đóng kỳ chấm công.");
        }
        List<Integer> deptIds = periodDAO.getDepartmentIdsWithAttendance(year, month);
        if (deptIds.isEmpty()) {
            return ClosingResult.fail("Chưa có dữ liệu chấm công của phòng ban nào trong kỳ này.");
        }
        int opened = 0;
        for (Integer deptId : deptIds) {
            AttendancePeriodStatus current = getEffectiveStatus(year, month, deptId);
            if (current == AttendancePeriodStatus.OPEN) {
                periodDAO.insertIfAbsent(year, month, deptId,
                        AttendancePeriodStatus.WAITING_MANAGER.getRelatedNum());
                opened++;
            }
        }
        if (opened == 0) {
            return ClosingResult.fail("Kỳ chấm công này đã được đóng trước đó.");
        }
        audit(hrUser, "OPEN_ATTENDANCE_PERIOD", periodLabel(year, month),
                "openedDepartments=" + opened);
        return ClosingResult.ok("Đã gửi bảng chấm công cho trưởng " + opened + " phòng ban để chốt.", opened);
    }

    // ── Bước 2: Trưởng phòng chốt phòng mình ──────────────────────────────

    public ClosingResult confirmByManager(int year, int month, int departmentId, User user) {
        if (!isManagerOf(user, departmentId)) {
            return ClosingResult.fail("Bạn không phải trưởng phòng ban này.");
        }
        AttendancePeriodStatus current = getEffectiveStatus(year, month, departmentId);
        if (current == AttendancePeriodStatus.OPEN) {
            return ClosingResult.fail("Kỳ chấm công chưa được HR đóng, chưa thể chốt.");
        }
        if (current != AttendancePeriodStatus.WAITING_MANAGER) {
            return ClosingResult.fail("Bảng chấm công phòng này không ở trạng thái chờ chốt "
                    + "(hiện tại: " + current.getLabel() + ").");
        }
        boolean ok = periodDAO.markManagerConfirmed(year, month, departmentId, user.getUserId());
        if (!ok) {
            return ClosingResult.fail("Không thể chốt bảng chấm công. Vui lòng thử lại.");
        }
        audit(user, "CONFIRM_DEPT_ATTENDANCE", periodLabel(year, month),
                "departmentId=" + departmentId);
        return ClosingResult.ok("Đã chốt bảng chấm công phòng ban.", 1);
    }

    // ── Bước 3: HR gửi toàn bộ lên BA ─────────────────────────────────────

    public ClosingResult submitToBa(int year, int month, User hrUser) {
        if (!isHr(hrUser)) {
            return ClosingResult.fail("Bạn không có quyền gửi bảng chấm công lên BA.");
        }
        List<Integer> deptIds = periodDAO.getDepartmentIdsWithAttendance(year, month);
        if (deptIds.isEmpty()) {
            return ClosingResult.fail("Chưa có dữ liệu chấm công trong kỳ này.");
        }
        List<String> notReady = new ArrayList<>();
        for (Integer deptId : deptIds) {
            AttendancePeriodStatus status = getEffectiveStatus(year, month, deptId);
            if (status != AttendancePeriodStatus.MANAGER_CONFIRMED) {
                notReady.add(departmentLabel(deptId) + " (" + status.getLabel() + ")");
            }
        }
        if (!notReady.isEmpty()) {
            return ClosingResult.fail("Chưa thể gửi lên BA vì các phòng sau chưa chốt: "
                    + String.join(", ", notReady));
        }
        int submitted = periodDAO.markSubmittedToBa(year, month, hrUser.getUserId());
        if (submitted == 0) {
            return ClosingResult.fail("Không có phòng ban nào để gửi lên BA.");
        }
        audit(hrUser, "SUBMIT_ATTENDANCE_TO_BA", periodLabel(year, month),
                "submittedDepartments=" + submitted);
        return ClosingResult.ok("Đã gửi bảng chấm công của " + submitted + " phòng ban lên BA duyệt.", submitted);
    }

    // ── Bước 4: BA chốt cuối cùng ─────────────────────────────────────────

    public ClosingResult approveByBa(int year, int month, User baUser) {
        if (!isBusinessAdmin(baUser)) {
            return ClosingResult.fail("Bạn không có quyền chốt bảng chấm công.");
        }
        List<Integer> deptIds = periodDAO.getDepartmentIdsWithAttendance(year, month);
        if (deptIds.isEmpty()) {
            return ClosingResult.fail("Chưa có dữ liệu chấm công trong kỳ này.");
        }
        List<String> notReady = new ArrayList<>();
        for (Integer deptId : deptIds) {
            AttendancePeriodStatus status = getEffectiveStatus(year, month, deptId);
            if (status != AttendancePeriodStatus.SUBMITTED_TO_BA) {
                notReady.add(departmentLabel(deptId) + " (" + status.getLabel() + ")");
            }
        }
        if (!notReady.isEmpty()) {
            return ClosingResult.fail("Chưa thể chốt vì các phòng sau chưa được HR gửi lên: "
                    + String.join(", ", notReady));
        }
        int locked = periodDAO.markLocked(year, month, baUser.getUserId());
        if (locked == 0) {
            return ClosingResult.fail("Không có phòng ban nào để chốt.");
        }
        audit(baUser, "APPROVE_ATTENDANCE_PERIOD", periodLabel(year, month),
                "lockedDepartments=" + locked);
        return ClosingResult.ok("Đã chốt bảng chấm công kỳ " + periodLabel(year, month)
                + ". Có thể tiến hành tính lương.", locked);
    }

    // ── Cổng tính lương ───────────────────────────────────────────────────

    /**
     * Kỳ đã được BA chốt hoàn toàn: mọi phòng ban có chấm công đều ở trạng thái LOCKED.
     * Dùng để chặn cứng việc tính lương toàn công ty.
     */
    public boolean isPeriodLocked(int year, int month) {
        List<Integer> deptIds = periodDAO.getDepartmentIdsWithAttendance(year, month);
        if (deptIds.isEmpty()) {
            return false;
        }
        for (Integer deptId : deptIds) {
            if (getEffectiveStatus(year, month, deptId) != AttendancePeriodStatus.LOCKED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Một phòng ban cụ thể đã được BA chốt (dùng khi tính lương theo phòng).
     */
    public boolean isDepartmentLocked(int year, int month, int departmentId) {
        return getEffectiveStatus(year, month, departmentId) == AttendancePeriodStatus.LOCKED;
    }

    // ── Phân quyền ────────────────────────────────────────────────────────

    private boolean isHr(User user) {
        if (user == null) {
            return false;
        }
        String role = roleDAO.getRoleByUserId(user.getUserId());
        return role != null && role.contains("HR");
    }

    private boolean isBusinessAdmin(User user) {
        if (user == null) {
            return false;
        }
        String role = roleDAO.getRoleByUserId(user.getUserId());
        return role != null && role.equalsIgnoreCase("BusinessAdmin");
    }

    private boolean isManagerOf(User user, int departmentId) {
        if (user == null) {
            return false;
        }
        EmployeeDetailDTO me = employeeDAO.getEmployeeByUserId(user.getUserId());
        if (me == null) {
            return false;
        }
        // 1) Trưởng phòng đã được gán tường minh trong Departments.managerId.
        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept != null && dept.getManagerId() != null
                && dept.getManagerId() == me.getEmployeeId()) {
            return true;
        }
        // 2) Người có role quản lý (…Manager) và thuộc đúng phòng ban đó
        //    (khớp cách hệ thống xác định trưởng phòng ở cổng manager).
        String role = roleDAO.getRoleByUserId(user.getUserId());
        return role != null && role.contains("Manager") && me.getDepartmentId() == departmentId;
    }

    // ── Tiện ích ──────────────────────────────────────────────────────────

    private String departmentLabel(int departmentId) {
        Department dept = departmentDAO.getDepartmentById(departmentId);
        if (dept != null && dept.getDepartmentName() != null) {
            return dept.getDepartmentName();
        }
        return "Phòng #" + departmentId;
    }

    private String periodLabel(int year, int month) {
        return String.format("%02d/%04d", month, year);
    }

    private void audit(User user, String action, String recordLabel, String newValue) {
        Integer userId = user == null ? null : user.getUserId();
        auditLogService.logAsync(userId, action, "Attendance_Period_Status", null,
                recordLabel, newValue, null, null, "SUCCESS");
    }

    /**
     * Kết quả một thao tác trong quy trình chốt, dùng cho controller hiển thị thông báo.
     */
    public static class ClosingResult {

        private final boolean success;
        private final String message;
        private final int affected;

        private ClosingResult(boolean success, String message, int affected) {
            this.success = success;
            this.message = message;
            this.affected = affected;
        }

        public static ClosingResult ok(String message, int affected) {
            return new ClosingResult(true, message, affected);
        }

        public static ClosingResult fail(String message) {
            return new ClosingResult(false, message, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getAffected() {
            return affected;
        }
    }
}
