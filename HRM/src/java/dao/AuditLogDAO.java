/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.AuditLog;

/**
 *
 * @author admin
 */
public class AuditLogDAO {

    private static final Logger LOGGER = Logger.getLogger(AuditLogDAO.class.getName());
    private final DBContext dbContext;

    public AuditLogDAO() {
        this.dbContext = new DBContext();
    }

    public int insertAuditLog(AuditLog log) {
        String SQL = "INSERT INTO Audit_Logs "
                + "(userId, action, tableName, recordId, oldValue, newValue, ipAddress, userAgent, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null) {
                ps.setInt(1, log.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, log.getAction());
            ps.setString(3, log.getTableName());
            if (log.getRecordId() != null) {
                ps.setInt(4, log.getRecordId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setNString(5, log.getOldValue());
            ps.setNString(6, log.getNewValue());
            ps.setString(7, log.getIpAddress());
            ps.setNString(8, log.getUserAgent());
            ps.setString(9, log.getStatus() == null ? "SUCCESS" : log.getStatus());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot insert audit log (action=" + log.getAction() + ")", e);
        }
        return -1;
    }

    public List<AuditLog> getRecentLogs(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String SQL = "SELECT logId, userId, action, tableName, recordId, oldValue, newValue, "
                + "ipAddress, userAgent, status, createdAt FROM Audit_Logs ORDER BY logId DESC LIMIT ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setLogId(rs.getInt("logId"));
                    int uid = rs.getInt("userId");
                    log.setUserId(rs.wasNull() ? null : uid);
                    log.setAction(rs.getString("action"));
                    log.setTableName(rs.getString("tableName"));
                    int rid = rs.getInt("recordId");
                    log.setRecordId(rs.wasNull() ? null : rid);
                    log.setOldValue(rs.getNString("oldValue"));
                    log.setNewValue(rs.getNString("newValue"));
                    log.setIpAddress(rs.getString("ipAddress"));
                    log.setUserAgent(rs.getNString("userAgent"));
                    log.setStatus(rs.getString("status"));
                    log.setCreatedAt(rs.getTimestamp("createdAt"));
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve recent audit logs", e);
        }
        return list;
    }
}
