/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dto.AttendanceDataDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author admin
 */
public class AttendanceImportRowDAO {

    public int insertRow(Connection conn, int fileId, AttendanceDataDTO row) throws SQLException {
        String SQL = "INSERT INTO Attendance_Import_Rows "
                + "(fileId, rowNumber, employeeCode, workDate, timeIn, timeOut, attendanceStatus, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, fileId);
            ps.setInt(2, row.getRowNumber());
            ps.setString(3, row.getEmployeeCode());
            ps.setString(4, row.getWorkDate());
            ps.setString(5, row.getTimeIn());
            ps.setString(6, row.getTimeOut());
            ps.setString(7, row.getAttendanceStatus());
            ps.setNString(8, row.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public void markRow(Connection conn, int importRowId, boolean valid, String errorMessage)
            throws SQLException {
        String SQL = "UPDATE Attendance_Import_Rows SET validateStatus = ?, errorMessage = ? "
                + "WHERE importRowId = ?";
        try (PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, valid ? 1 : 0);
            ps.setNString(2, errorMessage);
            ps.setInt(3, importRowId);
            ps.executeUpdate();
        }
    }
}
