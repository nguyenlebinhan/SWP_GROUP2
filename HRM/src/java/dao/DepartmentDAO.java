package dao;

import dal.DBContext;
import model.Department;
import model.Position;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

public class DepartmentDAO {
    private static final Logger LOGGER = Logger.getLogger(DepartmentDAO.class.getName());
    private final DBContext dbContext;

    public DepartmentDAO() {
        this.dbContext = new DBContext();
    }

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId, d.maxHeadCount, d.status, d.region, d.foundedDate FROM Departments d ORDER BY d.departmentName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDepartment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve all departments", e);
        }
        return list;
    }

    public List<Department> getAllActiveDepartments() {
        List<Department> list = new ArrayList<>();
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId, d.maxHeadCount, d.status, d.region, d.foundedDate FROM Departments d WHERE d.status = 1 ORDER BY departmentName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDepartment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve active departments", e);
        }
        return list;
    }

    public Department getDepartmentById(int id) {
        String SQL = "SELECT d.departmentId, d.departmentCode, d.departmentName, d.description, d.managerId, d.maxHeadCount, d.status, d.region, d.foundedDate FROM Departments d WHERE d.departmentId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDepartment(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve department by id: " + id, e);
        }
        return null;
    }

    public int countEmployeesByDepartmentId(int departmentId) {
        String SQL = "SELECT COUNT(*) FROM Employees WHERE departmentId = ? AND status != 0";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot count employees for departmentId: " + departmentId, e);
        }
        return 0;
    }

    public List<Position> getAllPositions() {
        List<Position> list = new ArrayList<>();
        String SQL = "SELECT positionId, positionName, level, description FROM Positions ORDER BY level DESC, positionName";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Position p = new Position();
                p.setPositionId(rs.getInt("positionId"));
                p.setPositionName(rs.getString("positionName"));
                p.setLevel(rs.getInt("level"));
                p.setDescription(rs.getNString("description"));
                list.add(p);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve positions", e);
        }
        return list;
    }

    private Department mapDepartment(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setDepartmentId(rs.getInt("departmentId"));
        d.setDepartmentCode(rs.getString("departmentCode"));
        d.setDepartmentName(rs.getNString("departmentName"));
        d.setDescription(rs.getNString("description"));
        int managerId = rs.getInt("managerId");
        d.setManagerId(rs.wasNull() ? null : managerId);
        int maxHead = rs.getInt("maxHeadCount");
        d.setMaxHeadCount(rs.wasNull() ? null : maxHead);
        d.setStatus(rs.getInt("status"));
        d.setRegion(rs.getNString("region"));
        d.setFoundedDate(rs.getString("foundedDate"));
       
        return d;
    }
}
