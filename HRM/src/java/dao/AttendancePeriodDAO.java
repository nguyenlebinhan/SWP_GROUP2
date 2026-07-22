package dao;

import dal.DBContext;
import enums.AttendancePeriodStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.AttendancePeriod;

/**
 * 
 *
 * @author ADMIN
 */
public class AttendancePeriodDAO {

    private static final Logger LOGGER = Logger.getLogger(AttendancePeriodDAO.class.getName());
    private final DBContext dbContext = new DBContext();

    private static final String BASE_SELECT =
            "SELECT aps.periodStatusId, aps.periodYear, aps.periodMonth, aps.departmentId, aps.status, "
            + "aps.managerConfirmedBy, aps.managerConfirmedAt, aps.submittedToBaBy, aps.submittedToBaAt, "
            + "aps.baApprovedBy, aps.baApprovedAt, aps.note, d.departmentName "
            + "FROM Attendance_Period_Status aps "
            + "LEFT JOIN Departments d ON d.departmentId = aps.departmentId ";

    public AttendancePeriod get(int year, int month, int departmentId) {
        String sql = BASE_SELECT
                + "WHERE aps.periodYear = ? AND aps.periodMonth = ? AND aps.departmentId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.setInt(3, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get attendance period status", e);
        }
        return null;
    }

    public List<AttendancePeriod> listByPeriod(int year, int month) {
        List<AttendancePeriod> list = new ArrayList<>();
        String sql = BASE_SELECT
                + "WHERE aps.periodYear = ? AND aps.periodMonth = ? "
                + "ORDER BY d.departmentName";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot list attendance period statuses", e);
        }
        return list;
    }

    /**
     * Tạo dòng trạng thái nếu chưa có; trả về id dòng (mới hoặc đang tồn tại).
     */
    public int insertIfAbsent(int year, int month, int departmentId, int status) {
        AttendancePeriod existing = get(year, month, departmentId);
        if (existing != null) {
            return existing.getPeriodStatusId();
        }
        String sql = "INSERT INTO Attendance_Period_Status "
                + "(periodYear, periodMonth, departmentId, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            ps.setInt(3, departmentId);
            ps.setInt(4, status);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot insert attendance period status", e);
        }
        return -1;
    }


    public boolean markManagerConfirmed(int year, int month, int departmentId, int managerUserId) {
        String sql = "UPDATE Attendance_Period_Status "
                + "SET status = ?, managerConfirmedBy = ?, managerConfirmedAt = CURRENT_TIMESTAMP "
                + "WHERE periodYear = ? AND periodMonth = ? AND departmentId = ? AND status = ?";
        return runStatusUpdate(sql, ps -> {
            ps.setInt(1, AttendancePeriodStatus.MANAGER_CONFIRMED.getRelatedNum());
            ps.setInt(2, managerUserId);
            ps.setInt(3, year);
            ps.setInt(4, month);
            ps.setInt(5, departmentId);
            ps.setInt(6, AttendancePeriodStatus.WAITING_MANAGER.getRelatedNum());
        });
    }


    public int markSubmittedToBa(int year, int month, int hrUserId) {
        String sql = "UPDATE Attendance_Period_Status "
                + "SET status = ?, submittedToBaBy = ?, submittedToBaAt = CURRENT_TIMESTAMP "
                + "WHERE periodYear = ? AND periodMonth = ? AND status = ?";
        return runBulkUpdate(sql, ps -> {
            ps.setInt(1, AttendancePeriodStatus.SUBMITTED_TO_BA.getRelatedNum());
            ps.setInt(2, hrUserId);
            ps.setInt(3, year);
            ps.setInt(4, month);
            ps.setInt(5, AttendancePeriodStatus.MANAGER_CONFIRMED.getRelatedNum());
        });
    }


    public int markLocked(int year, int month, int baUserId) {
        String sql = "UPDATE Attendance_Period_Status "
                + "SET status = ?, baApprovedBy = ?, baApprovedAt = CURRENT_TIMESTAMP "
                + "WHERE periodYear = ? AND periodMonth = ? AND status = ?";
        return runBulkUpdate(sql, ps -> {
            ps.setInt(1, AttendancePeriodStatus.LOCKED.getRelatedNum());
            ps.setInt(2, baUserId);
            ps.setInt(3, year);
            ps.setInt(4, month);
            ps.setInt(5, AttendancePeriodStatus.SUBMITTED_TO_BA.getRelatedNum());
        });
    }


    public List<Integer> getDepartmentIdsWithAttendance(int year, int month) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT departmentId FROM Attendance "
                + "WHERE YEAR(workDate) = ? AND MONTH(workDate) = ? AND departmentId IS NOT NULL "
                + "ORDER BY departmentId";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("departmentId"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get departments with attendance", e);
        }
        return list;
    }

    private AttendancePeriod map(ResultSet rs) throws SQLException {
        AttendancePeriod p = new AttendancePeriod();
        p.setPeriodStatusId(rs.getInt("periodStatusId"));
        p.setPeriodYear(rs.getInt("periodYear"));
        p.setPeriodMonth(rs.getInt("periodMonth"));
        p.setDepartmentId(rs.getInt("departmentId"));
        p.setStatus(rs.getInt("status"));
        int mgr = rs.getInt("managerConfirmedBy");
        p.setManagerConfirmedBy(rs.wasNull() ? null : mgr);
        p.setManagerConfirmedAt(rs.getTimestamp("managerConfirmedAt"));
        int hr = rs.getInt("submittedToBaBy");
        p.setSubmittedToBaBy(rs.wasNull() ? null : hr);
        p.setSubmittedToBaAt(rs.getTimestamp("submittedToBaAt"));
        int ba = rs.getInt("baApprovedBy");
        p.setBaApprovedBy(rs.wasNull() ? null : ba);
        p.setBaApprovedAt(rs.getTimestamp("baApprovedAt"));
        p.setNote(rs.getNString("note"));
        p.setDepartmentName(rs.getNString("departmentName"));
        return p;
    }


    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private boolean runStatusUpdate(String sql, StatementBinder binder) {
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update attendance period status", e);
            return false;
        }
    }

    private int runBulkUpdate(String sql, StatementBinder binder) {
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot bulk update attendance period status", e);
            return 0;
        }
    }
}
