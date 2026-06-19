package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PositionDAO {

    private static final Logger LOGGER = Logger.getLogger(PositionDAO.class.getName());
    private final DBContext dbContext;

    public PositionDAO() {
        this.dbContext = new DBContext();
    }

    public int getPositionIdByName(String name) {
        String sql = "SELECT positionId FROM Positions WHERE positionName = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot get positionId by name: " + name, e);
        }
        return -1;
    }
}