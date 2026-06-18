package dao;

import dal.DBContext;
import model.ApplicationStageLog;
import model.Candidate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CandidateDAO {

    private static final Logger LOGGER = Logger.getLogger(CandidateDAO.class.getName());
    private final DBContext dbContext;

    public CandidateDAO() {
        this.dbContext = new DBContext();
    }

    // ==================== MAPPING ====================

    private Candidate mapCandidate(ResultSet rs) throws SQLException {
        Candidate c = new Candidate();
        c.setCandidateId(rs.getInt("candidateId"));
        c.setCandidateCode(rs.getString("candidateCode"));
        c.setFullName(rs.getNString("fullName"));
        c.setEmail(rs.getString("email"));
        c.setPhoneNumber(rs.getString("phoneNumber"));
        c.setDateOfBirth(rs.getDate("dateOfBirth"));
        c.setGender(rs.getNString("gender"));
        c.setAddress(rs.getNString("address"));
        c.setSkills(rs.getNString("skills"));
        c.setExperience(rs.getNString("experience"));
        c.setCertificates(rs.getNString("certificates"));
        c.setDegree(rs.getNString("degree"));
        c.setCvFileUrl(rs.getString("cvFileUrl"));
        c.setDepartmentId(rs.getInt("departmentId"));
        c.setDepartmentName(rs.getNString("departmentName"));
        c.setPositionId(rs.getInt("positionId"));
        c.setPositionName(rs.getNString("positionName"));
        int fileId = rs.getInt("importFileId");
        c.setImportFileId(rs.wasNull() ? null : fileId);
        c.setStage(rs.getString("stage"));
        c.setCreatedAt(rs.getTimestamp("createdAt"));
        c.setUpdatedAt(rs.getTimestamp("updatedAt"));
        return c;
    }

    private ApplicationStageLog mapLog(ResultSet rs) throws SQLException {
        ApplicationStageLog log = new ApplicationStageLog();
        log.setLogId(rs.getInt("logId"));
        log.setCandidateId(rs.getInt("candidateId"));
        log.setFromStage(rs.getString("fromStage"));
        log.setToStage(rs.getString("toStage"));
        log.setResult(rs.getString("result"));
        log.setReviewedBy(rs.getInt("reviewedBy"));
        log.setReviewedByName(rs.getNString("reviewedByName"));
        log.setReviewedAt(rs.getTimestamp("reviewedAt"));
        log.setNote(rs.getNString("note"));
        log.setToEmail(rs.getString("toEmail"));
        log.setEmailSubject(rs.getNString("emailSubject"));
        log.setEmailBody(rs.getNString("emailBody"));
        log.setEmailType(rs.getString("emailType"));
        log.setEmailStatus(rs.getString("emailStatus"));
        log.setSentAt(rs.getTimestamp("sentAt"));
        return log;
    }

    private static final String BASE_SELECT =
        "SELECT c.*, d.departmentName, p.positionName " +
        "FROM Candidates c " +
        "JOIN Departments d ON c.departmentId = d.departmentId " +
        "JOIN Positions p ON c.positionId = p.positionId ";

    // ==================== CANDIDATE READ ====================

    public List<Candidate> getByStage(String stage) {
        List<Candidate> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE c.stage = ? ORDER BY c.createdAt DESC";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stage);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCandidate(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get candidates by stage: " + stage, e);
        }
        return list;
    }

    public Candidate getById(int candidateId) {
        String sql = BASE_SELECT + "WHERE c.candidateId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCandidate(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get candidate by id: " + candidateId, e);
        }
        return null;
    }

    public List<Candidate> searchByName(String stage, String keyword) {
        List<Candidate> list = new ArrayList<>();
        String sql = BASE_SELECT +
                     "WHERE c.stage = ? AND c.fullName LIKE ? " +
                     "ORDER BY c.createdAt DESC";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stage);
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCandidate(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot search candidates by name: " + keyword, e);
        }
        return list;
    }

    // ==================== CANDIDATE WRITE ====================

    public int insert(Candidate c) {
        String sql = "INSERT INTO Candidates " +
                     "(candidateCode, fullName, email, phoneNumber, dateOfBirth, gender, " +
                     "address, skills, experience, certificates, degree, cvFileUrl, " +
                     "departmentId, positionId, importFileId, stage) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'APPLIED')";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCandidateCode());
            ps.setNString(2, c.getFullName());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhoneNumber());
            ps.setDate(5, c.getDateOfBirth() != null
                    ? new java.sql.Date(c.getDateOfBirth().getTime()) : null);
            ps.setNString(6, c.getGender());
            ps.setNString(7, c.getAddress());
            ps.setNString(8, c.getSkills());
            ps.setNString(9, c.getExperience());
            ps.setNString(10, c.getCertificates());
            ps.setNString(11, c.getDegree());
            ps.setString(12, c.getCvFileUrl());
            ps.setInt(13, c.getDepartmentId());
            ps.setInt(14, c.getPositionId());
            if (c.getImportFileId() != null) ps.setInt(15, c.getImportFileId());
            else ps.setNull(15, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot insert candidate: " + c.getEmail(), e);
        }
        return 0;
    }

    // Chỉ gọi SAU KHI email gửi thành công
    public boolean updateStage(int candidateId, String newStage) {
        String sql = "UPDATE Candidates SET stage = ? WHERE candidateId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStage);
            ps.setInt(2, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update stage for candidateId: " + candidateId, e);
        }
        return false;
    }

    public String generateCode() {
        String sql = "SELECT COUNT(*) FROM Candidates";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int count = rs.next() ? rs.getInt(1) : 0;
            return String.format("CAN-%05d", count + 1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot generate candidate code", e);
        }
        return "CAN-00001";
    }

    // ==================== STAGE LOG READ ====================

    public List<ApplicationStageLog> getLogsByCandidateId(int candidateId) {
        List<ApplicationStageLog> list = new ArrayList<>();
        String sql = "SELECT asl.*, u.fullName as reviewedByName " +
                     "FROM Application_Stage_Logs asl " +
                     "JOIN Employees e ON asl.reviewedBy = e.employeeId " +
                     "JOIN Users u ON e.userId = u.userId " +
                     "WHERE asl.candidateId = ? " +
                     "ORDER BY asl.reviewedAt DESC";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLog(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get logs for candidateId: " + candidateId, e);
        }
        return list;
    }

    public ApplicationStageLog getLatestLog(int candidateId) {
        String sql = "SELECT asl.*, u.fullName as reviewedByName " +
                     "FROM Application_Stage_Logs asl " +
                     "JOIN Employees e ON asl.reviewedBy = e.employeeId " +
                     "JOIN Users u ON e.userId = u.userId " +
                     "WHERE asl.candidateId = ? " +
                     "ORDER BY asl.reviewedAt DESC LIMIT 1";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapLog(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get latest log for candidateId: " + candidateId, e);
        }
        return null;
    }

    // ==================== STAGE LOG WRITE ====================

    public int insertLog(ApplicationStageLog log) {
        String sql = "INSERT INTO Application_Stage_Logs " +
                     "(candidateId, fromStage, toStage, result, reviewedBy, note, " +
                     "toEmail, emailSubject, emailBody, emailType, emailStatus) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, log.getCandidateId());
            ps.setString(2, log.getFromStage());
            ps.setString(3, log.getToStage());
            ps.setString(4, log.getResult());
            ps.setInt(5, log.getReviewedBy());
            ps.setNString(6, log.getNote());
            ps.setString(7, log.getToEmail());
            ps.setNString(8, log.getEmailSubject());
            ps.setNString(9, log.getEmailBody());
            ps.setString(10, log.getEmailType());
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot insert stage log for candidateId: " + log.getCandidateId(), e);
        }
        return 0;
    }

    // SENT → gọi tiếp updateStage()
    // FAILED → báo lỗi cho HR, không đổi stage
    public boolean updateEmailStatus(int logId, String status) {
        String sql = "UPDATE Application_Stage_Logs " +
                     "SET emailStatus = ?, sentAt = ? WHERE logId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, "SENT".equals(status)
                    ? new Timestamp(System.currentTimeMillis()) : null);
            ps.setInt(3, logId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update email status for logId: " + logId, e);
        }
        return false;
    }
}