package dao;

import dal.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.PayrollConfigChangeRequest;
import model.PayrollDeductionRule;
import model.PayrollSetting;
import model.PayrollTaxBracket;

public class PayrollConfigChangeRequestDAO {

    private static final Logger LOGGER = Logger.getLogger(PayrollConfigChangeRequestDAO.class.getName());
    private final DBContext dbContext = new DBContext();
    private final PayrollConfigDAO payrollConfigDAO = new PayrollConfigDAO();

    public int createRequest(PayrollConfigChangeRequest request) {
        String sql = "INSERT INTO Payroll_Config_Change_Requests "
                + "(requestType, actionLabel, oldValue, newValue, targetKey, targetId, "
                + "settingKey, settingValue, settingDescription, "
                + "ruleId, ruleCode, ruleName, ruleType, calculationType, rate, employerRate, fixedAmount, "
                + "taxableDeduction, isActive, taxPayload, status, requestedBy) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindRequest(ps, request);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot create payroll config change request", e);
        }
        return -1;
    }

    public List<PayrollConfigChangeRequest> getRequests(Integer status) {
        List<PayrollConfigChangeRequest> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(baseSelect());
        if (status != null) {
            sql.append("WHERE r.status = ? ");
        }
        sql.append("ORDER BY r.requestedAt DESC, r.requestId DESC");
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (status != null) {
                ps.setInt(1, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot list payroll config change requests", e);
        }
        return list;
    }

    public PayrollConfigChangeRequest getById(int requestId) {
        String sql = baseSelect() + "WHERE r.requestId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll config change request", e);
        }
        return null;
    }

    public boolean reject(int requestId, int reviewedBy, String note) {
        return updateReviewStatus(requestId, PayrollConfigChangeRequest.STATUS_REJECTED, reviewedBy, note);
    }

    public boolean approve(int requestId, int reviewedBy, String note) {
        PayrollConfigChangeRequest request = getById(requestId);
        if (request == null || request.getStatus() != PayrollConfigChangeRequest.STATUS_PENDING) {
            return false;
        }
        boolean applied = applyRequest(request);
        if (!applied) {
            return false;
        }
        return updateReviewStatus(requestId, PayrollConfigChangeRequest.STATUS_APPROVED, reviewedBy, note);
    }

    private boolean applyRequest(PayrollConfigChangeRequest request) {
        switch (request.getRequestType()) {
            case PayrollConfigChangeRequest.TYPE_SETTING_SAVE:
                PayrollSetting setting = new PayrollSetting();
                setting.setSettingKey(request.getSettingKey());
                setting.setSettingValue(request.getSettingValue());
                setting.setDescription(request.getSettingDescription());
                return payrollConfigDAO.saveSetting(setting);
            case PayrollConfigChangeRequest.TYPE_DEDUCTION_SAVE:
                PayrollDeductionRule rule = toDeductionRule(request);
                return rule.getRuleId() > 0
                        ? payrollConfigDAO.updateDeductionRule(rule)
                        : payrollConfigDAO.addDeductionRule(rule) > 0;
            case PayrollConfigChangeRequest.TYPE_DEDUCTION_DELETE:
                return request.getTargetId() != null && payrollConfigDAO.deleteDeductionRule(request.getTargetId());
            case PayrollConfigChangeRequest.TYPE_TAX_SAVE:
                return payrollConfigDAO.updateTaxBrackets(parseTaxPayload(request.getTaxPayload()));
            default:
                return false;
        }
    }

    private boolean updateReviewStatus(int requestId, int status, int reviewedBy, String note) {
        PayrollConfigChangeRequest request = getById(requestId);
        if (request == null || request.getStatus() != PayrollConfigChangeRequest.STATUS_PENDING) {
            return false;
        }
        String sql = "UPDATE Payroll_Config_Change_Requests "
                + "SET status = ?, reviewedBy = ?, reviewedAt = CURRENT_TIMESTAMP, reviewNote = ? "
                + "WHERE requestId = ? AND status = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            ps.setInt(1, status);
            ps.setInt(2, reviewedBy);
            ps.setNString(3, note);
            ps.setInt(4, requestId);
            ps.setInt(5, PayrollConfigChangeRequest.STATUS_PENDING);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                insertHistory(conn, request, status, reviewedBy, note);
                conn.commit();
                return true;
            }
            conn.rollback();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update payroll config request review status", e);
        }
        return false;
    }

    private void insertHistory(Connection conn, PayrollConfigChangeRequest request, int status,
            int reviewedBy, String note) throws SQLException {
        String sql = "INSERT INTO Payroll_Config_Change_History "
                + "(requestId, requestType, actionLabel, targetKey, targetId, oldValue, newValue, "
                + "status, requestedBy, requestedAt, reviewedBy, reviewedAt, reviewNote) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, request.getRequestId());
            ps.setString(2, request.getRequestType());
            ps.setNString(3, request.getActionLabel());
            ps.setString(4, request.getTargetKey());
            setInteger(ps, 5, request.getTargetId());
            ps.setNString(6, request.getOldValue());
            ps.setNString(7, request.getNewValue());
            ps.setInt(8, status);
            ps.setInt(9, request.getRequestedBy());
            ps.setTimestamp(10, request.getRequestedAt());
            ps.setInt(11, reviewedBy);
            ps.setNString(12, note);
            ps.executeUpdate();
        }
    }

    public String serializeTaxBrackets(List<PayrollTaxBracket> brackets) {
        StringBuilder sb = new StringBuilder();
        if (brackets == null) {
            return "";
        }
        for (PayrollTaxBracket b : brackets) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(b.getBracketId()).append('|')
                    .append(value(b.getMinIncome())).append('|')
                    .append(value(b.getMaxIncome())).append('|')
                    .append(value(b.getTaxRate()));
        }
        return sb.toString();
    }

    public List<PayrollTaxBracket> parseTaxPayload(String payload) {
        List<PayrollTaxBracket> brackets = new ArrayList<>();
        if (payload == null || payload.trim().isEmpty()) {
            return brackets;
        }
        String[] lines = payload.split("\\r?\\n");
        for (String line : lines) {
            String[] parts = line.split("\\|", -1);
            if (parts.length != 4) {
                continue;
            }
            PayrollTaxBracket b = new PayrollTaxBracket();
            b.setBracketId(parseInt(parts[0]));
            b.setMinIncome(parseDecimal(parts[1]));
            b.setMaxIncome(parseDecimal(parts[2]));
            b.setTaxRate(parseDecimal(parts[3]));
            brackets.add(b);
        }
        return brackets;
    }

    private void bindRequest(PreparedStatement ps, PayrollConfigChangeRequest r) throws SQLException {
        ps.setString(1, r.getRequestType());
        ps.setNString(2, r.getActionLabel());
        ps.setNString(3, r.getOldValue());
        ps.setNString(4, r.getNewValue());
        ps.setString(5, r.getTargetKey());
        setInteger(ps, 6, r.getTargetId());
        ps.setString(7, r.getSettingKey());
        setBigDecimal(ps, 8, r.getSettingValue());
        ps.setNString(9, r.getSettingDescription());
        setInteger(ps, 10, r.getRuleId());
        ps.setString(11, r.getRuleCode());
        ps.setNString(12, r.getRuleName());
        ps.setString(13, r.getRuleType());
        ps.setString(14, r.getCalculationType());
        setBigDecimal(ps, 15, r.getRate());
        setBigDecimal(ps, 16, r.getEmployerRate());
        setBigDecimal(ps, 17, r.getFixedAmount());
        ps.setInt(18, r.isTaxableDeduction() ? 1 : 0);
        ps.setInt(19, r.isActive() ? 1 : 0);
        ps.setString(20, r.getTaxPayload());
        ps.setInt(21, PayrollConfigChangeRequest.STATUS_PENDING);
        ps.setInt(22, r.getRequestedBy());
    }

    private String baseSelect() {
        return "SELECT r.*, requester.fullName AS requestedByName, reviewer.fullName AS reviewedByName "
                + "FROM Payroll_Config_Change_Requests r "
                + "LEFT JOIN Users requester ON requester.userId = r.requestedBy "
                + "LEFT JOIN Users reviewer ON reviewer.userId = r.reviewedBy ";
    }

    private PayrollConfigChangeRequest map(ResultSet rs) throws SQLException {
        PayrollConfigChangeRequest r = new PayrollConfigChangeRequest();
        r.setRequestId(rs.getInt("requestId"));
        r.setRequestType(rs.getString("requestType"));
        r.setActionLabel(rs.getNString("actionLabel"));
        r.setOldValue(rs.getNString("oldValue"));
        r.setNewValue(rs.getNString("newValue"));
        r.setTargetKey(rs.getString("targetKey"));
        int targetId = rs.getInt("targetId");
        r.setTargetId(rs.wasNull() ? null : targetId);
        r.setSettingKey(rs.getString("settingKey"));
        r.setSettingValue(rs.getBigDecimal("settingValue"));
        r.setSettingDescription(rs.getNString("settingDescription"));
        int ruleId = rs.getInt("ruleId");
        r.setRuleId(rs.wasNull() ? null : ruleId);
        r.setRuleCode(rs.getString("ruleCode"));
        r.setRuleName(rs.getNString("ruleName"));
        r.setRuleType(rs.getString("ruleType"));
        r.setCalculationType(rs.getString("calculationType"));
        r.setRate(rs.getBigDecimal("rate"));
        r.setEmployerRate(rs.getBigDecimal("employerRate"));
        r.setFixedAmount(rs.getBigDecimal("fixedAmount"));
        r.setTaxableDeduction(rs.getInt("taxableDeduction") == 1);
        r.setActive(rs.getInt("isActive") == 1);
        r.setTaxPayload(rs.getString("taxPayload"));
        r.setStatus(rs.getInt("status"));
        r.setRequestedBy(rs.getInt("requestedBy"));
        r.setRequestedByName(rs.getNString("requestedByName"));
        r.setRequestedAt(rs.getTimestamp("requestedAt"));
        int reviewedBy = rs.getInt("reviewedBy");
        r.setReviewedBy(rs.wasNull() ? null : reviewedBy);
        r.setReviewedByName(rs.getNString("reviewedByName"));
        r.setReviewedAt(rs.getTimestamp("reviewedAt"));
        r.setReviewNote(rs.getNString("reviewNote"));
        return r;
    }

    private PayrollDeductionRule toDeductionRule(PayrollConfigChangeRequest request) {
        PayrollDeductionRule rule = new PayrollDeductionRule();
        rule.setRuleId(request.getRuleId() == null ? 0 : request.getRuleId());
        rule.setRuleCode(request.getRuleCode());
        rule.setRuleName(request.getRuleName());
        rule.setRuleType(request.getRuleType());
        rule.setCalculationType(request.getCalculationType());
        rule.setRate(request.getRate());
        rule.setEmployerRate(request.getEmployerRate());
        rule.setFixedAmount(request.getFixedAmount());
        rule.setTaxableDeduction(request.isTaxableDeduction());
        rule.setActive(request.isActive());
        return rule;
    }

    private void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void setBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DECIMAL);
        } else {
            ps.setBigDecimal(index, value);
        }
    }

    private String value(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private int parseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal parseDecimal(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
