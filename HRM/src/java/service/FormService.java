/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AttendanceDAO;
import dao.DepartmentDAO;
import dao.DependentDAO;
import dao.EmployeeDAO;
import dao.EmploymentContractDAO;
import dao.FormRequestDAO;
import dao.LeaveBalanceDAO;
import dao.RoleDAO;
import dto.EmployeeDetailDTO;
import enums.FormErrorCode;
import enums.FormTypeCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import model.ComplaintFormRequest;
import model.Department;
import model.DependentFormRequest;
import model.EmploymentContract;
import model.FormOperationalResult;
import model.LeaveBalance;
import model.LeaveFormRequest;
import model.Role;
import model.TransferFormRequest;

/**
 *
 * @author admin
 */
public class FormService {

    private final FormRequestDAO formRequestDAO;
    private final DepartmentDAO departmentDAO;
    private final LeaveBalanceDAO leaveBalanceDAO;
    private final RoleDAO roleDAO;
    private final AttendanceDAO attendanceDAO;
    private final EmployeeDAO employeeDAO;
    private final DependentDAO dependentDAO;
    private final AttendanceClosingService attendanceClosingService;
    private final EmploymentContractDAO employmentContractDAO;

    public FormService() {
        this.formRequestDAO = new FormRequestDAO();
        this.departmentDAO = new DepartmentDAO();
        this.leaveBalanceDAO = new LeaveBalanceDAO();
        this.roleDAO = new RoleDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.employeeDAO = new EmployeeDAO();
        this.dependentDAO = new DependentDAO();
        this.attendanceClosingService = new AttendanceClosingService();
        this.employmentContractDAO = new EmploymentContractDAO();
    }

    public int calculateEntitledLeaveDays(int employeeId) {
        return calculateEntitledLeaveDays(employeeId, java.time.LocalDate.now().getYear());
    }

    public int calculateEntitledLeaveDays(int employeeId, int targetYear) {
        EmploymentContract activeContract = employmentContractDAO.getActiveOrPendingContract(employeeId);
        if (activeContract == null || activeContract.getStatus() != enums.ContractStatus.ACTIVE) {
            return 0;
        }
        LocalDate earliestStartDate = null;
        try {
            List<EmploymentContract> contracts = employmentContractDAO.getAllContractsByEmployeeId(employeeId);
            for (EmploymentContract c : contracts) {
                if (c.getEffectiveDate() != null) {
                    LocalDate eff = c.getEffectiveDate().toLocalDate();
                    if (earliestStartDate == null || eff.isBefore(earliestStartDate)) {
                        earliestStartDate = eff;
                    }
                } else if (c.getSignedDate() != null) {
                    LocalDate sig = c.getSignedDate().toLocalDate();
                    if (earliestStartDate == null || sig.isBefore(earliestStartDate)) {
                        earliestStartDate = sig;
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            java.util.logging.Logger.getLogger(FormService.class.getName()).log(java.util.logging.Level.SEVERE, "Error retrieving contracts for leave calculation", e);
        }

        if (earliestStartDate == null) {
            return 12;
        }

        int startYear = earliestStartDate.getYear();
        if (targetYear < startYear) {
            return 0;
        }

        // Năm đầu tiên vào làm việc (Ví dụ: vào tháng 7 -> 12 - 7 = 5 ngày)
        if (targetYear == startYear) {
            int startMonth = earliestStartDate.getMonthValue();
            return (startMonth == 1) ? 12 : Math.max(0, 12 - startMonth);
        }

        // Những năm còn lại (tính 12 ngày chuẩn + thưởng thâm niên 5 năm + 1 ngày)
        int baseDays = 12;
        LocalDate referenceDate = (targetYear == java.time.LocalDate.now().getYear()) ? java.time.LocalDate.now() : java.time.LocalDate.of(targetYear, 12, 31);
        long yearsOfService = java.time.temporal.ChronoUnit.YEARS.between(earliestStartDate, referenceDate);
        if (yearsOfService > 0) {
            int seniorityBonus = (int) (yearsOfService / 5);
            baseDays += seniorityBonus;
        }
        return baseDays;
    }

    public LeaveBalance getOrInitializeLeaveBalance(int employeeId, int year) {
        LeaveBalance lb = leaveBalanceDAO.getLeaveBalance(employeeId, year);
        int entitledDays = calculateEntitledLeaveDays(employeeId, year);
        if (lb == null) {
            lb = new LeaveBalance(0, employeeId, year, entitledDays, 0);
            leaveBalanceDAO.createLeaveBalance(lb);
        } else {
            if (lb.getTotalAllowed() != entitledDays) {
                lb.setTotalAllowed(entitledDays);
                leaveBalanceDAO.updateTotalAllowed(employeeId, year, entitledDays);
            }
        }
        return lb;
    }

    public FormOperationalResult submitLeaveForm(int employeeId, int formTypeId, String reason,
            Date startDate, Date endDate, String attachmentUrl, String attachmentName) {
        EmployeeDetailDTO emp = employeeDAO.getEmployeeById(employeeId);
        if (emp == null) {
            return new FormOperationalResult(false, FormErrorCode.EMPLOYEE_NOT_FOUND.name(), "Nhân viên không tồn tại.");
        }
        LeaveBalance balance = getOrInitializeLeaveBalance(employeeId, LocalDate.now().getYear());
        if (balance == null || balance.getTotalAllowed() <= 0) {
            return new FormOperationalResult(false, FormErrorCode.NO_LEAVE_BALANCE.name(), "Bạn chưa có hợp đồng lao động hiệu lực hoặc không đủ điều kiện có ngày nghỉ phép năm.");
        }
        int allowedDays = balance.getRemainingDays();
        if (startDate.toLocalDate().isBefore(LocalDate.now())) {
            return new FormOperationalResult(false, FormErrorCode.START_DATE_IN_PAST.name(), "Ngày bắt đầu không được trong quá khứ");
        }
        if (endDate.before(startDate)) {
            return new FormOperationalResult(false, FormErrorCode.END_DATE_BEFORE_START_DATE.name(), "Ngày kết thúc không được trước ngày bắt đầu");
        }

        int totalDays = countDays(startDate.toLocalDate(), endDate.toLocalDate());
        if (totalDays == 0) {
            return new FormOperationalResult(false, FormErrorCode.WEEKEND_ONLY.name(), "Không cần nộp đơn, toàn bộ ngày chọn đều là cuối tuần");
        }
        if (totalDays > allowedDays) {
            return new FormOperationalResult(false, FormErrorCode.INSUFFICIENT_LEAVE_BALANCE.name(), "Số ngày nghỉ mong muốn vượt quá số ngày phép còn lại (" + allowedDays + " ngày)");
        }
        boolean exist = formRequestDAO.hasOverlappingLeave(employeeId, startDate, endDate);
        if (exist) {
            return new FormOperationalResult(false, FormErrorCode.OVERLAPPING_LEAVE.name(), "Đã có đơn xin nghỉ trong khoảng thời gian này");
        }
        LeaveFormRequest fr = new LeaveFormRequest();
        fr.setFormCode(FormTypeCode.LEAVE.name() + employeeId + System.currentTimeMillis());
        fr.setEmployeeId(employeeId);
        fr.setFormTypeId(formTypeId);
        fr.setStartDate(startDate);
        fr.setEndDate(endDate);
        fr.setTotalDays(totalDays);
        fr.setReason(reason);
        fr.setAttachmentUrl(attachmentUrl);
        fr.setAttachmentName(attachmentName);

        int id = formRequestDAO.addFormRequest(fr);
        if (id <= 0) {
            return new FormOperationalResult(false, FormErrorCode.DATABASE_ERROR.name(), "Gửi đơn nghỉ phép thất bại");
        }
        return new FormOperationalResult(true, null, "Đã gửi đơn nghỉ phép thành công.");
    }

    public FormOperationalResult submitComplaintForm(int employeeId, int formTypeId, String reason,
            Date startDate, Time startTime, Time endTime,
            String attachmentUrl, String attachmentName) {
        EmployeeDetailDTO emp = employeeDAO.getEmployeeById(employeeId);
        if (emp == null) {
            return new FormOperationalResult(false, FormErrorCode.EMPLOYEE_NOT_FOUND.name(), "Nhân viên không tồn tại.");
        }
        int[] latestPeriod = attendanceDAO.getLatestAttendanceMonthYear();
        if (latestPeriod == null) {
            return new FormOperationalResult(false, FormErrorCode.NO_ATTENDANCE_RECORD.name(),
                    "Không có dữ liệu chấm công đã nhập thành công để tạo khiếu nại.");
        }
        LocalDate complaintDate = startDate.toLocalDate();
        if (complaintDate.getMonthValue() != latestPeriod[0] || complaintDate.getYear() != latestPeriod[1]) {
            return new FormOperationalResult(false, FormErrorCode.COMPLAINT_PERIOD_MISMATCH.name(), String.format("Chỉ được khiếu nại cho tháng %02d/%d", latestPeriod[0], latestPeriod[1]));
        }
        enums.AttendancePeriodStatus periodStatus = attendanceClosingService.getEffectiveStatus(latestPeriod[1], latestPeriod[0], emp.getDepartmentId());
        if (periodStatus == enums.AttendancePeriodStatus.MANAGER_CONFIRMED
                || periodStatus == enums.AttendancePeriodStatus.WAITING_HR_FINAL_CHECK
                || periodStatus == enums.AttendancePeriodStatus.LOCKED) {
            return new FormOperationalResult(false, FormErrorCode.ATTENDANCE_PERIOD_CLOSED.name(),
                    "Kỳ chấm công của phòng ban đã được Trưởng phòng hoặc HR chốt, không thể nộp thêm đơn khiếu nại.");
        }

        LocalDate deadline = LocalDate.of(latestPeriod[1], latestPeriod[0], 1).plusMonths(1).withDayOfMonth(5);
        if (LocalDate.now().isAfter(deadline)) {
            return new FormOperationalResult(false, FormErrorCode.DEADLINE_EXCEEDED.name(),
                    "Đã quá thời hạn nộp đơn khiếu nại (hạn cuối là ngày 05/" + String.format("%02d/%d", deadline.getMonthValue(), deadline.getYear()) + ").");
        }
        DayOfWeek day = startDate.toLocalDate().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return new FormOperationalResult(false, FormErrorCode.WEEKEND_ONLY.name(), "Công ty không làm việc vào ngày cuối tuần");
        }
        if (endTime.before(startTime)) {
            return new FormOperationalResult(false, FormErrorCode.END_TIME_BEFORE_START_TIME.name(), "Giờ kết thúc phải lớn hơn giờ bắt đầu");
        }

        boolean exist = formRequestDAO.hasPendingComplaintForDate(employeeId, startDate);
        if (exist) {
            return new FormOperationalResult(false, FormErrorCode.OVERLAPPING_COMPLAINT.name(), "Đã có đơn xin khiếu nại vào ngày " + startDate);
        }
        ComplaintFormRequest fr = new ComplaintFormRequest();
        fr.setFormCode(FormTypeCode.COMPLAINT.name() + "-" + employeeId + "-" + System.currentTimeMillis());
        fr.setEmployeeId(employeeId);
        fr.setFormTypeId(formTypeId);
        fr.setStartDate(startDate);
        fr.setStartTime(startTime);
        fr.setEndTime(endTime);
        fr.setReason(reason);
        fr.setAttachmentUrl(attachmentUrl);
        fr.setAttachmentName(attachmentName);

        int id = formRequestDAO.addFormRequest(fr);
        if (id <= 0) {
            return new FormOperationalResult(false, FormErrorCode.DATABASE_ERROR.name(), "Gửi đơn khiếu nại thất bại");
        }
        return new FormOperationalResult(true, null, "Gửi đơn khiếu nại thành công");
    }

    public FormOperationalResult submitTransferRequest(int employeeId, int formTypeId, String reason,
            int targetDepartmentId, int targetRoleId, String attachmentUrl, String attachmentName) {
        EmployeeDetailDTO emp = employeeDAO.getEmployeeById(employeeId);
        if (emp == null) {
            return new FormOperationalResult(false, FormErrorCode.EMPLOYEE_NOT_FOUND.name(), "Nhân viên không tồn tại");
        }

        Department dept = departmentDAO.getDepartmentById(emp.getDepartmentId());
        Role targetRole = roleDAO.getRoleById(targetRoleId);
        boolean isTargetManager = targetRole != null && targetRole.getRoleName() != null && targetRole.getRoleName().toLowerCase().contains("manager");
        if (emp.getDepartmentId() > 0 && dept != null) {
            if (dept.getDepartmentId() == targetDepartmentId) {
                return new FormOperationalResult(false, FormErrorCode.SAME_DEPARTMENT.name(), "Đã ở phòng ban này");
            }
            boolean isCurrentManager = emp.getRoleName() != null && emp.getRoleName().toLowerCase().contains("manager");
            if (isCurrentManager != isTargetManager) {
                return new FormOperationalResult(false, FormErrorCode.INVALID_TRANSFER_LEVEL.name(), "Chỉ được chuyển phòng ban ngang cấp bậc (VD: Employee sang Employee).");
            }
        }

        Department targetDept = departmentDAO.getDepartmentById(targetDepartmentId);
//        if (targetDept != null && targetDept.getManagerId() != null && isTargetManager) {

        if (isTargetManager && targetDept != null) {
            boolean hasManager = targetDept.getManagerId() != null;
            if (!hasManager) {
                for (dto.EmployeeDetailDTO e : employeeDAO.getEmployeesByDepartmentId(targetDepartmentId)) {
                    if (e.getStatus() == 1 && e.getRoleName() != null && e.getRoleName().toLowerCase().contains("manager")) {
                        hasManager = true;
                        break;
                    }
                }
            }
            if (hasManager) {
                return new FormOperationalResult(false, FormErrorCode.DEPARTMENT_MANAGER_EXIST.name(), "Phòng ban đích đã có trưởng phòng");
            }
        }
        if (!departmentDAO.isRoleAllowedForDepartment(targetDepartmentId, targetRoleId)) {
            return new FormOperationalResult(false, FormErrorCode.ROLE_NOT_ALLOWED_FOR_DEPARTMENT.name(), "Vị trí không có trong phòng ban này");
        }
        boolean exist = formRequestDAO.hasPendingTransfer(employeeId);
        if (exist) {
            return new FormOperationalResult(false, FormErrorCode.OVERLAPPING_TRANSFER.name(), "Đã có đơn thuyên chuyển phòng ban");
        }
        TransferFormRequest fr = new TransferFormRequest();
        fr.setFormCode(FormTypeCode.TRANSFER.name()+ "-" + employeeId + "-" + System.currentTimeMillis());
        fr.setEmployeeId(employeeId);
        fr.setFormTypeId(formTypeId);
        fr.setTargetDepartmentId(targetDepartmentId);
        fr.setTargetRoleId(targetRoleId);
        fr.setAttachmentUrl(attachmentUrl);
        fr.setAttachmentName(attachmentName);

        int id = formRequestDAO.addFormRequest(fr);
        if (id <= 0) {
            return new FormOperationalResult(false, FormErrorCode.DATABASE_ERROR.name(), "Gửi đơn thuyên chuyển phòng ban thất bại");
        }
        return new FormOperationalResult(true, null, "Gửi đơn thuyên chuyển phòng ban thành công");
    }

    public FormOperationalResult submitPromotionForm(int employeeId, int formTypeId, String reason,
            int targetRoleId, String attachmentUrl, String attachmentName) {
        EmployeeDetailDTO emp = employeeDAO.getEmployeeById(employeeId);
        if (emp == null) {
            return new FormOperationalResult(false, FormErrorCode.EMPLOYEE_NOT_FOUND.name(), "Nhân viên không tồn tại.");
        }

        Department dept = departmentDAO.getDepartmentById(emp.getDepartmentId());
        Role targetRole = roleDAO.getRoleById(targetRoleId);
        if (dept.getManagerId() != null && targetRole.getRoleName().contains("Manager")) {
            return new FormOperationalResult(false, FormErrorCode.DEPARTMENT_MANAGER_EXIST.name(), "Phòng ban này đã có trưởng phòng");
        }
        int deptId = emp.getDepartmentId();
        if (!departmentDAO.isRoleAllowedForDepartment(deptId, targetRoleId)) {
            return new FormOperationalResult(false, FormErrorCode.ROLE_NOT_ALLOWED_FOR_DEPARTMENT.name(), "Vị trí không có trong phòng ban này");
        }
        TransferFormRequest fr = new TransferFormRequest();
        fr.setFormCode(FormTypeCode.PROMOTION_DEMOTION.name() + "-" + employeeId + "-" + System.currentTimeMillis());
        fr.setEmployeeId(employeeId);
        fr.setFormTypeId(formTypeId);
        fr.setReason(reason);
        fr.setTargetRoleId(targetRoleId);
        fr.setAttachmentUrl(attachmentUrl);
        fr.setAttachmentName(attachmentName);

        int id = formRequestDAO.addFormRequest(fr);
        if (id <= 0) {
            return new FormOperationalResult(false, FormErrorCode.DATABASE_ERROR.name(), "Gửi đơn thăng/giáng chức thất bại.");
        }
        return new FormOperationalResult(true, null, "Gửi đơn thăng/giáng chức thành công.");
    }

    public FormOperationalResult submitDependentForm(int employeeId, int formTypeId, String reason,
            String fullName, String relationship, Date dateOfBirth, String taxCode,
            String attachmentUrl, String attachmentName) {
        EmployeeDetailDTO emp = employeeDAO.getEmployeeById(employeeId);
        if (emp == null) {
            return new FormOperationalResult(false, "NOT_FOUND", "Nhân viên không tồn tại.");
        }
        if (dependentDAO.isDuplicateDependent(employeeId, fullName, dateOfBirth, taxCode)) {
            return new FormOperationalResult(false, FormErrorCode.DUPLICATE_DEPENDENT.name(), "Người phụ thuộc này hoặc mã số thuế/CCCD đã có trong danh sách được giảm trừ hợp lệ.");
        }
        if (formRequestDAO.hasPendingDuplicateDependent(employeeId, fullName, dateOfBirth, taxCode)) {
            return new FormOperationalResult(false, FormErrorCode.DUPLICATE_DEPENDENT.name(), "Người phụ thuộc này hoặc mã số thuế/CCCD đang có đơn chờ duyệt trong hệ thống.");
        }
        DependentFormRequest fr = new DependentFormRequest();
        fr.setFormCode(FormTypeCode.DEPENDENT.name() + "-" + employeeId + "-" + System.currentTimeMillis());
        fr.setEmployeeId(employeeId);
        fr.setFormTypeId(formTypeId);
        fr.setDependentName(fullName);
        fr.setDependentRelationship(relationship);
        fr.setDependentDob(dateOfBirth);
        fr.setDependentTaxCode(taxCode);
        fr.setReason(reason);
        fr.setAttachmentUrl(attachmentUrl);
        fr.setAttachmentName(attachmentName);
        
        int id = formRequestDAO.addFormRequest(fr);
        if (id <= 0) {
            return new FormOperationalResult(false,
                    FormErrorCode.DATABASE_ERROR.name(), "Gửi đơn đăng kí người phụ thuộc thất bại.");
        }
        return new FormOperationalResult(true, null, "Gửi đơn đăng kí người phụ thuộc thành công.");
    }

    private int countDays(LocalDate startDate, LocalDate endDate) {
        int count = 0;
        LocalDate i = startDate;
        while (!i.isAfter(endDate)) {
            DayOfWeek day = i.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            i = i.plusDays(1);
        }
        return count;
    }
}
