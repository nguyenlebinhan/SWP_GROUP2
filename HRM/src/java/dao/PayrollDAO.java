package dao;

import dal.DBContext;
import dto.PayrollPreviewDTO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Payroll;

public class PayrollDAO {

    private static final Logger LOGGER = Logger.getLogger(PayrollDAO.class.getName());
    private final DBContext dbContext;

    public PayrollDAO() {
        this.dbContext = new DBContext();
    }

    // ── Lưu / lấy ─────────────────────────────────────────────────────────────

    public int saveOrUpdatePayroll(Payroll payroll) {
        try (Connection conn = dbContext.getConnection()) {
            Integer existingId = findPayrollId(conn, payroll.getEmployeeId(),
                    payroll.getPeriodStart(), payroll.getPeriodEnd());
            if (existingId != null) {
                payroll.setPayrollId(existingId);
                return updatePayroll(conn, payroll) ? existingId : -1;
            }
            return insertPayroll(conn, payroll);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot save payroll for employeeId: " + payroll.getEmployeeId(), e);
        }
        return -1;
    }

    public Payroll getPayrollById(int payrollId) {
        String SQL = basePayrollSelect() + " WHERE payrollId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, payrollId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPayroll(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll by id: " + payrollId, e);
        }
        return null;
    }

    public PayrollPreviewDTO getPayrollPreviewById(int payrollId) {
        String SQL = "SELECT p.*, e.employeeCode, u.fullName, d.departmentName, pos.positionName, "
                + "ec.salary AS contractSalary "
                + "FROM Payroll p "
                + "JOIN Employees e ON e.employeeId = p.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = p.departmentId "
                + "LEFT JOIN Positions pos ON pos.positionId = p.positionId "
                + "LEFT JOIN Employment_Contracts ec ON ec.contractId = ( "
                + "    SELECT ec2.contractId FROM Employment_Contracts ec2 "
                + "    WHERE ec2.employeeId = p.employeeId "
                + "      AND ec2.status = 1 "
                + "      AND ec2.startDate <= p.periodEnd "
                + "      AND (ec2.endDate IS NULL OR ec2.endDate >= p.periodStart) "
                + "    ORDER BY ec2.contractId DESC LIMIT 1 "
                + ") "
                + "WHERE p.payrollId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, payrollId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PayrollPreviewDTO dto = new PayrollPreviewDTO();
                    dto.setPayroll(mapPayroll(rs));
                    dto.setEmployeeCode(rs.getString("employeeCode"));
                    dto.setFullName(rs.getNString("fullName"));
                    dto.setDepartmentName(rs.getNString("departmentName"));
                    dto.setPositionName(rs.getString("positionName"));
                    dto.setContractSalary(rs.getBigDecimal("contractSalary"));
                    return dto;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll preview by id: " + payrollId, e);
        }
        return null;
    }

    public Payroll getPayrollByEmployeeAndPeriod(int employeeId, Date periodStart, Date periodEnd) {
        String SQL = basePayrollSelect()
                + " WHERE employeeId = ? AND periodStart = ? AND periodEnd = ?"
                + " ORDER BY payrollId DESC LIMIT 1";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, periodStart);
            ps.setDate(3, periodEnd);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPayroll(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll by employee and period: " + employeeId, e);
        }
        return null;
    }

    public List<PayrollPreviewDTO> getPayrollPreviews(Date periodStart, Date periodEnd,
            Integer departmentId, Integer status) {
        List<PayrollPreviewDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, e.employeeCode, u.fullName, d.departmentName, pos.positionName, "
                + "ec.salary AS contractSalary "
                + "FROM Payroll p "
                + "JOIN Employees e ON e.employeeId = p.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = p.departmentId "
                + "LEFT JOIN Positions pos ON pos.positionId = p.positionId "
                + "LEFT JOIN Employment_Contracts ec ON ec.contractId = ( "
                + "    SELECT ec2.contractId FROM Employment_Contracts ec2 "
                + "    WHERE ec2.employeeId = p.employeeId "
                + "      AND ec2.status = 1 "
                + "      AND ec2.startDate <= p.periodEnd "
                + "      AND (ec2.endDate IS NULL OR ec2.endDate >= p.periodStart) "
                + "    ORDER BY ec2.contractId DESC LIMIT 1 "
                + ") "
                + "WHERE p.periodStart = ? AND p.periodEnd = ? ");
        List<Object> params = new ArrayList<>();
        params.add(periodStart);
        params.add(periodEnd);
        if (departmentId != null) {
            sql.append("AND p.departmentId = ? ");
            params.add(departmentId);
        }
        if (status != null) {
            sql.append("AND p.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY d.departmentName, e.employeeCode");
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PayrollPreviewDTO dto = new PayrollPreviewDTO();
                    dto.setPayroll(mapPayroll(rs));
                    dto.setEmployeeCode(rs.getString("employeeCode"));
                    dto.setFullName(rs.getNString("fullName"));
                    dto.setDepartmentName(rs.getNString("departmentName"));
                    dto.setPositionName(rs.getString("positionName"));
                    dto.setContractSalary(rs.getBigDecimal("contractSalary"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll previews", e);
        }
        return list;
    }

    // ── Duyệt tổng (0 → 1) ────────────────────────────────────────────────────

    public int approveAllPendingPayroll(Date periodStart, Date periodEnd, Integer departmentId,
            int approvedByUserId, Integer excludeEmployeeId) {
        StringBuilder sql = new StringBuilder(
                "UPDATE Payroll "
                + "SET status = 1, approvedBy = ?, approvedAt = CURRENT_TIMESTAMP "
                + "WHERE periodStart = ? AND periodEnd = ? AND status = 0 ");
        List<Object> params = new ArrayList<>();
        params.add(approvedByUserId);
        params.add(periodStart);
        params.add(periodEnd);
        if (departmentId != null) {
            sql.append("AND departmentId = ? ");
            params.add(departmentId);
        }
        if (excludeEmployeeId != null) {
            sql.append("AND employeeId != ? ");
            params.add(excludeEmployeeId);
        }
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot bulk approve payroll for period", e);
        }
        return 0;
    }

    public int countPendingApproval(Date periodStart, Date periodEnd, Integer departmentId,
            Integer excludeEmployeeId) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Payroll "
                + "WHERE periodStart = ? AND periodEnd = ? AND status = 0 ");
        List<Object> params = new ArrayList<>();
        params.add(periodStart);
        params.add(periodEnd);
        if (departmentId != null) {
            sql.append("AND departmentId = ? ");
            params.add(departmentId);
        }
        if (excludeEmployeeId != null) {
            sql.append("AND employeeId != ? ");
            params.add(excludeEmployeeId);
        }
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count pending approval payroll", e);
        }
        return 0;
    }

    // ── Generic status update (giữ lại, không gây hại) ────────────────────────

    public boolean updatePayrollStatus(int payrollId, int status) {
        String SQL = "UPDATE Payroll SET status = ? WHERE payrollId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, status);
            ps.setInt(2, payrollId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update payroll status for payrollId: " + payrollId, e);
        }
        return false;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Integer findPayrollId(Connection conn, int employeeId, Date periodStart, Date periodEnd)
            throws SQLException {
        String SQL = "SELECT payrollId FROM Payroll "
                + "WHERE employeeId = ? AND periodStart = ? AND periodEnd = ? "
                + "ORDER BY payrollId DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, periodStart);
            ps.setDate(3, periodEnd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("payrollId") : null;
            }
        }
    }

    private int insertPayroll(Connection conn, Payroll payroll) throws SQLException {
        String SQL = "INSERT INTO Payroll "
                + "(periodStart, periodEnd, employeeId, positionId, departmentId, workingDays, hoursWorked, "
                + "baseSalary, allowance, bonus, overtimePay, penalty, grossSalary, "
                + "insuranceDeduction, personalIncomeTax, netSalary, note, status, "
                + "approvedBy, approvedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            bindPayroll(ps, payroll, false);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return rs.next() ? rs.getInt(1) : -1;
                }
            }
        }
        return -1;
    }

    private boolean updatePayroll(Connection conn, Payroll payroll) throws SQLException {
        String SQL = "UPDATE Payroll SET "
                + "periodStart = ?, periodEnd = ?, employeeId = ?, positionId = ?, departmentId = ?, "
                + "workingDays = ?, hoursWorked = ?, baseSalary = ?, allowance = ?, bonus = ?, "
                + "overtimePay = ?, penalty = ?, grossSalary = ?, insuranceDeduction = ?, personalIncomeTax = ?, "
                + "netSalary = ?, note = ?, status = ?, "
                + "approvedBy = ?, approvedAt = ? "
                + "WHERE payrollId = ?";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            bindPayroll(ps, payroll, true);
            return ps.executeUpdate() > 0;
        }
    }

    private void bindPayroll(PreparedStatement ps, Payroll p, boolean includeIdAtEnd) throws SQLException {
        ps.setDate(1, p.getPeriodStart());
        ps.setDate(2, p.getPeriodEnd());
        ps.setInt(3, p.getEmployeeId());
        ps.setInt(4, p.getPositionId());
        if (p.getDepartmentId() != null) {
            ps.setInt(5, p.getDepartmentId());
        } else {
            ps.setNull(5, Types.INTEGER);
        }
        ps.setInt(6, p.getWorkingDays());
        setBigDecimal(ps, 7, p.getHoursWorked());
        setBigDecimal(ps, 8, p.getBaseSalary());
        setBigDecimal(ps, 9, p.getAllowance());
        setBigDecimal(ps, 10, p.getBonus());
        setBigDecimal(ps, 11, p.getOvertimePay());
        setBigDecimal(ps, 12, p.getPenalty());
        setBigDecimal(ps, 13, p.getGrossSalary());
        setBigDecimal(ps, 14, p.getInsuranceDeduction());
        setBigDecimal(ps, 15, p.getPersonalIncomeTax());
        setBigDecimal(ps, 16, p.getNetSalary());
        ps.setNString(17, p.getNote());
        ps.setInt(18, p.getStatus());
        
        if (p.getApprovedBy() != null) {
            ps.setInt(19, p.getApprovedBy());
        } else {
            ps.setNull(19, Types.INTEGER);
        }
        ps.setTimestamp(20, p.getApprovedAt());
        if (includeIdAtEnd) {
            ps.setInt(21, p.getPayrollId());
        }
    }

    private void setBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws SQLException {
        if (value != null) {
            ps.setBigDecimal(index, value);
        } else {
            ps.setNull(index, Types.DECIMAL);
        }
    }

    private String basePayrollSelect() {
        return "SELECT payrollId, periodStart, periodEnd, employeeId, positionId, departmentId, "
                + "workingDays, hoursWorked, baseSalary, allowance, bonus, overtimePay, penalty, "
                + "grossSalary, insuranceDeduction, personalIncomeTax, netSalary, note, status, "
                + "approvedBy, approvedAt, "
                + "createdAt, updatedAt FROM Payroll";
    }

    private Payroll mapPayroll(ResultSet rs) throws SQLException {
        Payroll p = new Payroll();
        p.setPayrollId(rs.getInt("payrollId"));
        p.setPeriodStart(rs.getDate("periodStart"));
        p.setPeriodEnd(rs.getDate("periodEnd"));
        p.setEmployeeId(rs.getInt("employeeId"));
        p.setPositionId(rs.getInt("positionId"));
        int departmentId = rs.getInt("departmentId");
        p.setDepartmentId(rs.wasNull() ? null : departmentId);
        p.setWorkingDays(rs.getInt("workingDays"));
        p.setHoursWorked(rs.getBigDecimal("hoursWorked"));
        p.setBaseSalary(rs.getBigDecimal("baseSalary"));
        p.setAllowance(rs.getBigDecimal("allowance"));
        p.setBonus(rs.getBigDecimal("bonus"));
        p.setOvertimePay(rs.getBigDecimal("overtimePay"));
        p.setPenalty(rs.getBigDecimal("penalty"));
        p.setGrossSalary(rs.getBigDecimal("grossSalary"));
        p.setInsuranceDeduction(rs.getBigDecimal("insuranceDeduction"));
        p.setPersonalIncomeTax(rs.getBigDecimal("personalIncomeTax"));
        p.setNetSalary(rs.getBigDecimal("netSalary"));
        p.setNote(rs.getNString("note"));
        p.setStatus(rs.getInt("status"));
        int approvedBy = rs.getInt("approvedBy");
        p.setApprovedBy(rs.wasNull() ? null : approvedBy);
        p.setApprovedAt(rs.getTimestamp("approvedAt"));
        p.setCreatedAt(rs.getTimestamp("createdAt"));
        p.setUpdatedAt(rs.getTimestamp("updatedAt"));
        return p;
    }
}
