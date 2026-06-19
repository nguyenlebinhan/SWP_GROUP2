package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Holiday;

/**
 *
 * @author admin
 */
public class HolidayDAO {

    private static final Logger LOGGER = Logger.getLogger(HolidayDAO.class.getName());

    private final DBContext dbContext;

    public HolidayDAO() {
        this.dbContext = new DBContext();
    }


    public boolean isHoliday(Connection conn, Date workDate) throws SQLException {
        String sql = "SELECT 1 FROM Holiday "
                + "WHERE isActive = 1 AND ? BETWEEN startDate AND endDate LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    public List<Holiday> getAllHolidays() {
        List<Holiday> list = new ArrayList<>();
        String sql = "SELECT holidayId, holidayName, startDate, endDate, isActive "
                + "FROM Holiday ORDER BY startDate DESC";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapHoliday(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve holiday list", e);
        }
        return list;
    }

    public Holiday getHolidayById(int holidayId) {
        String sql = "SELECT holidayId, holidayName, startDate, endDate, isActive "
                + "FROM Holiday WHERE holidayId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, holidayId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapHoliday(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve holiday by id: " + holidayId, e);
        }
        return null;
    }

    public int addHoliday(Holiday h) {
        String sql = "INSERT INTO Holiday (holidayName, startDate, endDate, isActive) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, h.getHolidayName());
            ps.setDate(2, h.getStartDate());
            ps.setDate(3, h.getEndDate());
            ps.setInt(4, h.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot add holiday: " + h.getHolidayName(), e);
        }
        return -1;
    }

    public boolean updateHoliday(Holiday h) {
        String sql = "UPDATE Holiday SET holidayName = ?, startDate = ?, endDate = ?, isActive = ? "
                + "WHERE holidayId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, h.getHolidayName());
            ps.setDate(2, h.getStartDate());
            ps.setDate(3, h.getEndDate());
            ps.setInt(4, h.isActive() ? 1 : 0);
            ps.setInt(5, h.getHolidayId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot update holiday id: " + h.getHolidayId(), e);
        }
        return false;
    }

    public boolean deleteHoliday(int holidayId) {
        String sql = "DELETE FROM Holiday WHERE holidayId = ?";
        try (Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, holidayId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot delete holiday id: " + holidayId, e);
        }
        return false;
    }

    private Holiday mapHoliday(ResultSet rs) throws SQLException {
        Holiday h = new Holiday();
        h.setHolidayId(rs.getInt("holidayId"));
        h.setHolidayName(rs.getNString("holidayName"));
        h.setStartDate(rs.getDate("startDate"));
        h.setEndDate(rs.getDate("endDate"));
        h.setActive(rs.getInt("isActive") == 1);
        return h;
    }
}
