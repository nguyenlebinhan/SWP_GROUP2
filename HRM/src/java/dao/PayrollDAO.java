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

    public PayrollPreviewDTO getPayrollPreviewById(int payrollId) {
        String SQL = "SELECT p.*, e.employeeCode, e.unionMember, u.fullName, d.departmentName, pos.positionName, "
                + "ec.salary AS contractSalary "
                + "FROM Payroll p "
                + "JOIN Employees e ON e.employeeId = p.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = p.departmentId "
                + "LEFT JOIN Positions pos ON pos.positionId = p.positionId "
                + "LEFT JOIN Employment_Contracts ec ON ec.contractId = ( "
                + "    SELECT ec2.contractId FROM Employment_Contracts ec2 "
                + "    WHERE ec2.employeeId = p.employeeId "
                + "      AND ec2.status = 'ACTIVE' "
                + "      AND ec2.effectiveDate <= p.periodEnd "
                + "      AND (ec2.endDate IS NULL OR ec2.endDate >= p.periodStart) "
                + "    ORDER BY ec2.contractId DESC LIMIT 1 "
                + ") "
                + "WHERE p.payrollId = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, payrollId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PayrollPreviewDTO dto = new PayrollPreviewDTO();
                    dto.setPayroll(mapPayroll(rs));
                    dto.setEmployeeCode(rs.getString("employeeCode"));
                    dto.setUnionMember(rs.getBoolean("unionMember"));
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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(SQL)) {
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
                "SELECT p.*, e.employeeCode, e.unionMember, u.fullName, d.departmentName, pos.positionName, "
                + "ec.salary AS contractSalary "
                + "FROM Payroll p "
                + "JOIN Employees e ON e.employeeId = p.employeeId "
                + "JOIN Users u ON u.userId = e.userId "
                + "LEFT JOIN Departments d ON d.departmentId = p.departmentId "
                + "LEFT JOIN Positions pos ON pos.positionId = p.positionId "
                + "LEFT JOIN Employment_Contracts ec ON ec.contractId = ( "
                + "    SELECT ec2.contractId FROM Employment_Contracts ec2 "
                + "    WHERE ec2.employeeId = p.employeeId "
                + "      AND ec2.status = 'ACTIVE' "
                + "      AND ec2.effectiveDate <= p.periodEnd "
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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PayrollPreviewDTO dto = new PayrollPreviewDTO();
                    dto.setPayroll(mapPayroll(rs));
                    dto.setEmployeeCode(rs.getString("employeeCode"));
                    dto.setUnionMember(rs.getBoolean("unionMember"));
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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
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
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
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

    // ── Duyệt chốt cuối (1 → 2, Business Admin) ────────────────────────────────
    public int finalizeApprovedPayroll(Date periodStart, Date periodEnd, Integer departmentId,
            Integer approvedByUserId) {
        StringBuilder sql = new StringBuilder(
                "UPDATE Payroll "
                + "SET status = 2, approvedBy = ?, approvedAt = CURRENT_TIMESTAMP "
                + "WHERE periodStart = ? AND periodEnd = ? AND status = 1 ");
        List<Object> params = new ArrayList<>();
        params.add(approvedByUserId);
        params.add(periodStart);
        params.add(periodEnd);
        if (departmentId != null) {
            sql.append("AND departmentId = ? ");
            params.add(departmentId);
        }
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot finalize payroll for period", e);
        }
        return 0;
    }

    public int countAwaitingFinalization(Date periodStart, Date periodEnd, Integer departmentId) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM Payroll "
                + "WHERE periodStart = ? AND periodEnd = ? AND status = 1 ");
        List<Object> params = new ArrayList<>();
        params.add(periodStart);
        params.add(periodEnd);
        if (departmentId != null) {
            sql.append("AND departmentId = ? ");
            params.add(departmentId);
        }
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count payroll awaiting finalization", e);
        }
        return 0;
    }

    public int countFinalizedForPeriod(Date periodStart, Date periodEnd) {
        String sql = "SELECT COUNT(*) FROM Payroll WHERE periodStart = ? AND periodEnd = ? AND status = 2";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, periodStart);
            ps.setDate(2, periodEnd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count finalized payroll for period", e);
        }
        return 0;
    }

    // Hạn chốt tự động: mọi payroll của các kỳ đã quá hạn (periodStart <= cutoff) mà vẫn
    // chưa được chốt (status 0 hoặc 1) sẽ tự động chuyển thành đã chốt (2).
    public int autoFinalizeOverdue(Date cutoffPeriodStart) {
        String sql = "UPDATE Payroll SET status = 2, approvedAt = CURRENT_TIMESTAMP "
                + "WHERE status IN (0, 1) AND periodStart <= ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, cutoffPeriodStart);
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot auto-finalize overdue payroll", e);
        }
        return 0;
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
                + "baseSalary, allowance, bonus, overtimePay, unpaidDeduction, grossSalary, "
                + "insuranceDeduction, personalIncomeTax, netSalary, "
                + "insuranceSalaryBase, postInsuranceIncome, taxableIncome, employerContribution, note, status, "
                + "approvedBy, approvedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                + "overtimePay = ?, unpaidDeduction = ?, grossSalary = ?, insuranceDeduction = ?, personalIncomeTax = ?, "
                + "netSalary = ?, insuranceSalaryBase = ?, postInsuranceIncome = ?, taxableIncome = ?, employerContribution = ?, "
                + "note = ?, status = ?, "
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
        setBigDecimal(ps, 12, p.getUnpaidDeduction());
        setBigDecimal(ps, 13, p.getGrossSalary());
        setBigDecimal(ps, 14, p.getInsuranceDeduction());
        setBigDecimal(ps, 15, p.getPersonalIncomeTax());
        setBigDecimal(ps, 16, p.getNetSalary());
        setBigDecimal(ps, 17, p.getInsuranceSalaryBase());
        setBigDecimal(ps, 18, p.getPostInsuranceIncome());
        setBigDecimal(ps, 19, p.getTaxableIncome());
        setBigDecimal(ps, 20, p.getEmployerContribution());
        ps.setNString(21, p.getNote());
        ps.setInt(22, p.getStatus());

        if (p.getApprovedBy() != null) {
            ps.setInt(23, p.getApprovedBy());
        } else {
            ps.setNull(23, Types.INTEGER);
        }
        ps.setTimestamp(24, p.getApprovedAt());
        if (includeIdAtEnd) {
            ps.setInt(25, p.getPayrollId());
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
                + "workingDays, hoursWorked, baseSalary, allowance, bonus, overtimePay, unpaidDeduction, "
                + "grossSalary, insuranceDeduction, personalIncomeTax, netSalary, "
                + "insuranceSalaryBase, postInsuranceIncome, taxableIncome, employerContribution, note, status, "
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
        p.setUnpaidDeduction(rs.getBigDecimal("unpaidDeduction"));
        p.setGrossSalary(rs.getBigDecimal("grossSalary"));
        p.setInsuranceDeduction(rs.getBigDecimal("insuranceDeduction"));
        p.setPersonalIncomeTax(rs.getBigDecimal("personalIncomeTax"));
        p.setNetSalary(rs.getBigDecimal("netSalary"));
        p.setInsuranceSalaryBase(rs.getBigDecimal("insuranceSalaryBase"));
        p.setPostInsuranceIncome(rs.getBigDecimal("postInsuranceIncome"));
        p.setTaxableIncome(rs.getBigDecimal("taxableIncome"));
        p.setEmployerContribution(rs.getBigDecimal("employerContribution"));
        p.setNote(rs.getNString("note"));
        p.setStatus(rs.getInt("status"));
        int approvedBy = rs.getInt("approvedBy");
        p.setApprovedBy(rs.wasNull() ? null : approvedBy);
        p.setApprovedAt(rs.getTimestamp("approvedAt"));
        p.setCreatedAt(rs.getTimestamp("createdAt"));
        p.setUpdatedAt(rs.getTimestamp("updatedAt"));
        return p;
    }

    public long countPayrollsInMonth(int year, int month) {
        String sql = "SELECT COUNT(*) FROM Payroll "
                + "WHERE YEAR(periodStart) = ? AND MONTH(periodStart) = ?";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count payrolls in month: " + year + "-" + month, e);
        }
        return 0L;
    }

    public long[] getMonthlySalaryCosts(int year) {
        long[] costs = new long[13];
        String sql = "SELECT MONTH(periodStart) AS m, "
                + "SUM(netSalary + insuranceDeduction + personalIncomeTax + employerContribution) AS total "
                + "FROM Payroll "
                + "WHERE YEAR(periodStart) = ? "
                + "GROUP BY MONTH(periodStart)";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int m = rs.getInt("m");
                    if (m >= 1 && m <= 12) {
                        BigDecimal total = rs.getBigDecimal("total");
                        costs[m] = total != null ? total.longValue() : 0L;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot load monthly salary costs for year: " + year, e);
        }
        return costs;
    }

    public List<Integer> getAvailableSalaryYears() {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(periodStart) AS y "
                + "FROM Payroll "
                + "ORDER BY y DESC";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                years.add(rs.getInt("y"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot load available salary years", e);
        }
        // Đảm bảo luôn có ít nhất năm hiện tại để dropdown không rỗng
        int currentYear = java.time.LocalDate.now().getYear();
        if (!years.contains(currentYear)) {
            years.add(0, currentYear);
        }
        return years;
    }

    public java.util.LinkedHashMap<String, Long> getDepartmentSalaryCosts(int year, int month) {
        java.util.LinkedHashMap<String, Long> result = new java.util.LinkedHashMap<>();
        String sql = "SELECT COALESCE(d.departmentName, 'Chưa phân bổ') AS deptName, "
                + "SUM(p.netSalary + p.insuranceDeduction + p.personalIncomeTax + p.employerContribution) AS total "
                + "FROM Payroll p "
                + "LEFT JOIN Departments d ON d.departmentId = p.departmentId "
                + "WHERE YEAR(p.periodStart) = ? AND MONTH(p.periodStart) = ? "
                + "GROUP BY d.departmentName "
                + "ORDER BY total DESC";
        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    result.put(rs.getString("deptName"), total != null ? total.longValue() : 0L);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot load department salary costs for " + month + "/" + year, e);
        }
        return result;
    }
}
