package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.PayrollConfigChangeHistory;

public class PayrollConfigChangeHistoryDAO {

    private static final Logger LOGGER = Logger.getLogger(PayrollConfigChangeHistoryDAO.class.getName());
    private final DBContext dbContext = new DBContext();

    public List<PayrollConfigChangeHistory> getRecentHistory(int limit) {
        List<PayrollConfigChangeHistory> list = new ArrayList<>();
        String sql = "SELECT h.*, requester.fullName AS requestedByName, reviewer.fullName AS reviewedByName "
                + "FROM Payroll_Config_Change_History h "
                + "LEFT JOIN Users requester ON requester.userId = h.requestedBy "
                + "LEFT JOIN Users reviewer ON reviewer.userId = h.reviewedBy "
                + "ORDER BY h.reviewedAt DESC, h.historyId DESC LIMIT ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot list payroll config change history", e);
        }
        return list;
    }

    private PayrollConfigChangeHistory map(ResultSet rs) throws SQLException {
        PayrollConfigChangeHistory h = new PayrollConfigChangeHistory();
        h.setHistoryId(rs.getInt("historyId"));
        int requestId = rs.getInt("requestId");
        h.setRequestId(rs.wasNull() ? null : requestId);
        h.setRequestType(rs.getString("requestType"));
        h.setActionLabel(rs.getNString("actionLabel"));
        h.setTargetKey(rs.getString("targetKey"));
        int targetId = rs.getInt("targetId");
        h.setTargetId(rs.wasNull() ? null : targetId);
        h.setOldValue(rs.getNString("oldValue"));
        h.setNewValue(rs.getNString("newValue"));
        h.setStatus(rs.getInt("status"));
        h.setRequestedBy(rs.getInt("requestedBy"));
        h.setRequestedByName(rs.getNString("requestedByName"));
        h.setRequestedAt(rs.getTimestamp("requestedAt"));
        h.setReviewedBy(rs.getInt("reviewedBy"));
        h.setReviewedByName(rs.getNString("reviewedByName"));
        h.setReviewedAt(rs.getTimestamp("reviewedAt"));
        h.setReviewNote(rs.getNString("reviewNote"));
        return h;
    }
}
