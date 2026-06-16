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
import java.util.logging.Level;
import java.util.logging.Logger;
import model.UploadedFile;

/**
 *
 * @author admin
 */
public class UploadedFileDAO {

    private static final Logger LOGGER = Logger.getLogger(UploadedFileDAO.class.getName());
    private final DBContext dbContext;

    public UploadedFileDAO() {
        this.dbContext = new DBContext();
    }


    public int createUploadedFile(UploadedFile file) {
        LOGGER.log(Level.INFO, "Creating uploaded file record: {0}", file.getFileCode());
        String SQL = "INSERT INTO Uploaded_Files "
                + "(fileCode, fileType, departmentId, employeeId, fileUrl, fileName, month, year, "
                + "totalRows, importedRows, failedRows, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, file.getFileCode());
            ps.setString(2, file.getFileType());
            ps.setInt(3, file.getDepartmentId());
            if (file.getEmployeeId() != null) {
                ps.setInt(4, file.getEmployeeId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, file.getFileUrl());
            ps.setString(6, file.getFileName());
            ps.setInt(7, file.getMonth());
            ps.setInt(8, file.getYear());
            ps.setInt(9, file.getTotalRows());
            ps.setInt(10, file.getImportedRows());
            ps.setInt(11, file.getFailedRows());
            ps.setNString(12, file.getNote());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot create uploaded file record: " + file.getFileCode(), e);
        }
        return -1;
    }

    public boolean updateImportResult(int fileId, int totalRows, int importedRows, int failedRows,
            int status, String note) {
        try (Connection conn = dbContext.getConnection()) {
            return updateImportResult(conn, fileId, totalRows, importedRows, failedRows, status, note);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update import result for fileId: " + fileId, e);
        }
        return false;
    }

    /**
     * Updates the import result on the caller-supplied transaction connection. This MUST
     * be used while an import transaction is still open on {@code conn}: inserting child
     * rows into Attendance_Import_Rows takes an FK shared lock on this Uploaded_Files row,
     * so updating it through a separate connection would block on that lock and fail with
     * "Lock wait timeout exceeded". Reusing {@code conn} keeps lock owner and updater on
     * the same transaction.
     */
    public boolean updateImportResult(Connection conn, int fileId, int totalRows, int importedRows,
            int failedRows, int status, String note) throws SQLException {
        String SQL = "UPDATE Uploaded_Files SET totalRows = ?, importedRows = ?, failedRows = ?, "
                + "status = ?, note = ? WHERE fileId = ?";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, totalRows);
            ps.setInt(2, importedRows);
            ps.setInt(3, failedRows);
            ps.setInt(4, status);
            ps.setNString(5, note);
            ps.setInt(6, fileId);
            ps.executeUpdate();
            return true;
        }
    }
}
