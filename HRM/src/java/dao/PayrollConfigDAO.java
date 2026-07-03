package dao;

import dal.DBContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.PayrollDeductionRule;
import model.PayrollSetting;
import model.PayrollTaxBracket;

public class PayrollConfigDAO {

    private static final Logger LOGGER = Logger.getLogger(PayrollConfigDAO.class.getName());
    private final DBContext dbContext = new DBContext();

    public Map<String, BigDecimal> getSettingsMap() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        String sql = "SELECT settingKey, settingValue FROM Payroll_Settings";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("settingKey"), rs.getBigDecimal("settingValue"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll settings", e);
        }
        return map;
    }

    public List<PayrollSetting> getAllSettings() {
        List<PayrollSetting> list = new ArrayList<>();
        String sql = "SELECT settingKey, settingValue, description FROM Payroll_Settings ORDER BY settingKey";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PayrollSetting s = new PayrollSetting();
                s.setSettingKey(rs.getString("settingKey"));
                s.setSettingValue(rs.getBigDecimal("settingValue"));
                s.setDescription(rs.getNString("description"));
                list.add(s);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll settings", e);
        }
        return list;
    }

    public List<PayrollSetting> getConfigurablePayrollSettings() {
        List<PayrollSetting> list = new ArrayList<>();
        String sql = "SELECT settingKey, settingValue, description FROM Payroll_Settings "
                + "WHERE settingKey NOT IN ('WORK_START', 'WORK_END', 'WORK_START_MINUTES', 'WORK_END_MINUTES', 'WORK_BREAK_MINUTES') "
                + "ORDER BY settingKey";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PayrollSetting s = new PayrollSetting();
                s.setSettingKey(rs.getString("settingKey"));
                s.setSettingValue(rs.getBigDecimal("settingValue"));
                s.setDescription(rs.getNString("description"));
                list.add(s);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get configurable payroll settings", e);
        }
        return list;
    }

    public boolean saveSetting(PayrollSetting setting) {
        String sql = "INSERT INTO Payroll_Settings (settingKey, settingValue, description) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE settingValue = VALUES(settingValue), description = VALUES(description), updatedAt = CURRENT_TIMESTAMP";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, setting.getSettingKey());
            ps.setBigDecimal(2, setting.getSettingValue());
            ps.setNString(3, setting.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot save payroll setting", e);
        }
        return false;
    }

    public boolean deleteSetting(String settingKey) {
        String sql = "DELETE FROM Payroll_Settings WHERE settingKey = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, settingKey);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot delete payroll setting", e);
        }
        return false;
    }

    public List<PayrollDeductionRule> getDeductionRules(boolean activeOnly) {
        List<PayrollDeductionRule> list = new ArrayList<>();
        String sql = "SELECT ruleId, ruleCode, ruleName, ruleType, calculationType, rate, employerRate, fixedAmount, "
                + "taxableDeduction, isActive FROM Payroll_Deduction_Rules "
                + (activeOnly ? "WHERE isActive = 1 " : "")
                + "ORDER BY ruleId";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDeductionRule(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll deduction rules", e);
        }
        return list;
    }

    public PayrollDeductionRule getDeductionRuleById(int ruleId) {
        String sql = "SELECT ruleId, ruleCode, ruleName, ruleType, calculationType, rate, employerRate, fixedAmount, "
                + "taxableDeduction, isActive FROM Payroll_Deduction_Rules WHERE ruleId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ruleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapDeductionRule(rs) : null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll deduction rule", e);
        }
        return null;
    }

    public int addDeductionRule(PayrollDeductionRule rule) {
        String sql = "INSERT INTO Payroll_Deduction_Rules "
                + "(ruleCode, ruleName, ruleType, calculationType, rate, employerRate, fixedAmount, taxableDeduction, isActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindDeductionRule(ps, rule);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot add payroll deduction rule", e);
        }
        return -1;
    }

    public boolean updateDeductionRule(PayrollDeductionRule rule) {
        String sql = "UPDATE Payroll_Deduction_Rules SET ruleCode = ?, ruleName = ?, ruleType = ?, "
                + "calculationType = ?, rate = ?, employerRate = ?, fixedAmount = ?, taxableDeduction = ?, "
                + "isActive = ? WHERE ruleId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            bindDeductionRule(ps, rule);
            ps.setInt(10, rule.getRuleId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update payroll deduction rule", e);
        }
        return false;
    }

    public boolean deleteDeductionRule(int ruleId) {
        String sql = "DELETE FROM Payroll_Deduction_Rules WHERE ruleId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ruleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot delete payroll deduction rule", e);
        }
        return false;
    }

    public List<PayrollTaxBracket> getTaxBrackets(boolean activeOnly) {
        List<PayrollTaxBracket> list = new ArrayList<>();
        String sql = "SELECT bracketId, minIncome, maxIncome, taxRate "
                + "FROM Payroll_Tax_Brackets ORDER BY minIncome, bracketId";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTaxBracket(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll tax brackets", e);
        }
        return list;
    }

    public PayrollTaxBracket getTaxBracketById(int bracketId) {
        String sql = "SELECT bracketId, minIncome, maxIncome, taxRate "
                + "FROM Payroll_Tax_Brackets WHERE bracketId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bracketId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapTaxBracket(rs) : null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get payroll tax bracket", e);
        }
        return null;
    }

    public boolean updateTaxBracket(PayrollTaxBracket bracket) {
        String sql = "UPDATE Payroll_Tax_Brackets SET minIncome = ?, maxIncome = ?, taxRate = ? "
                + "WHERE bracketId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            bindTaxBracket(ps, bracket);
            ps.setInt(4, bracket.getBracketId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update payroll tax bracket", e);
        }
        return false;
    }

    public boolean updateTaxBrackets(List<PayrollTaxBracket> brackets) {
        String sql = "UPDATE Payroll_Tax_Brackets SET minIncome = ?, maxIncome = ?, taxRate = ? "
                + "WHERE bracketId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (PayrollTaxBracket bracket : brackets) {
                bindTaxBracket(ps, bracket);
                ps.setInt(4, bracket.getBracketId());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update payroll tax brackets", e);
        }
        return false;
    }

    private void bindDeductionRule(PreparedStatement ps, PayrollDeductionRule r) throws SQLException {
        ps.setString(1, r.getRuleCode());
        ps.setNString(2, r.getRuleName());
        ps.setString(3, r.getRuleType());
        ps.setString(4, r.getCalculationType());
        ps.setBigDecimal(5, r.getRate());
        ps.setBigDecimal(6, r.getEmployerRate());
        ps.setBigDecimal(7, r.getFixedAmount());
        ps.setInt(8, r.isTaxableDeduction() ? 1 : 0);
        ps.setInt(9, r.isActive() ? 1 : 0);
    }

    private PayrollDeductionRule mapDeductionRule(ResultSet rs) throws SQLException {
        PayrollDeductionRule r = new PayrollDeductionRule();
        r.setRuleId(rs.getInt("ruleId"));
        r.setRuleCode(rs.getString("ruleCode"));
        r.setRuleName(rs.getNString("ruleName"));
        r.setRuleType(rs.getString("ruleType"));
        r.setCalculationType(rs.getString("calculationType"));
        r.setRate(rs.getBigDecimal("rate"));
        r.setEmployerRate(rs.getBigDecimal("employerRate"));
        r.setFixedAmount(rs.getBigDecimal("fixedAmount"));
        r.setTaxableDeduction(rs.getInt("taxableDeduction") == 1);
        r.setActive(rs.getInt("isActive") == 1);
        return r;
    }

    private void bindTaxBracket(PreparedStatement ps, PayrollTaxBracket b) throws SQLException {
        ps.setBigDecimal(1, b.getMinIncome());
        ps.setBigDecimal(2, b.getMaxIncome());
        ps.setBigDecimal(3, b.getTaxRate());
    }

    private PayrollTaxBracket mapTaxBracket(ResultSet rs) throws SQLException {
        PayrollTaxBracket b = new PayrollTaxBracket();
        b.setBracketId(rs.getInt("bracketId"));
        b.setMinIncome(rs.getBigDecimal("minIncome"));
        b.setMaxIncome(rs.getBigDecimal("maxIncome"));
        b.setTaxRate(rs.getBigDecimal("taxRate"));
        return b;
    }
}
