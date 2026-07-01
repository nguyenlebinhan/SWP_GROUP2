
package dao;

import dal.DBContext;
import java.math.BigDecimal;
import model.ContractAmendment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContractAmendmentDAO {

    private final DBContext dbContext = new DBContext();

    public int addAmendment(Connection conn, ContractAmendment amendment) throws SQLException {
        String sql = "INSERT INTO Contract_Amendments "
                + "(contractId, amendmentCode, amendmentType, effectiveDate, "
                + "oldDepartmentId, newDepartmentId, oldPositionId, newPositionId, "
                + "oldSalary, newSalary, reason, sourceFormId, status, createdBy, approvedBy, approvedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, amendment.getContractId());
            ps.setString(2, amendment.getAmendmentCode());
            ps.setString(3, amendment.getAmendmentType());
            ps.setDate(4, amendment.getEffectiveDate());

            setNullableInt(ps, 5, amendment.getOldDepartmentId());
            setNullableInt(ps, 6, amendment.getNewDepartmentId());
            setNullableInt(ps, 7, amendment.getOldPositionId());
            setNullableInt(ps, 8, amendment.getNewPositionId());

            setNullableBigDecimal(ps, 9, amendment.getOldSalary());
            setNullableBigDecimal(ps, 10, amendment.getNewSalary());
            ps.setString(11, amendment.getReason());

            setNullableInt(ps, 12, amendment.getSourceFormId());

            ps.setString(13, amendment.getStatus());
            ps.setInt(14, amendment.getCreatedBy());

            setNullableInt(ps, 15, amendment.getApprovedBy());
            ps.setTimestamp(16, new Timestamp(System.currentTimeMillis()));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                return -1;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public List<ContractAmendment> getByContractId(int contractId) {
        List<ContractAmendment> list = new ArrayList<>();

        String sql = "SELECT * FROM Contract_Amendments "
                + "WHERE contractId = ? "
                + "ORDER BY effectiveDate DESC, amendmentId DESC";

        try (Connection conn = dbContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contractId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAmendment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private ContractAmendment mapAmendment(ResultSet rs) throws SQLException {
        ContractAmendment amendment = new ContractAmendment();

        amendment.setAmendmentId(rs.getInt("amendmentId"));
        amendment.setContractId(rs.getInt("contractId"));
        amendment.setAmendmentCode(rs.getString("amendmentCode"));
        amendment.setAmendmentType(rs.getString("amendmentType"));
        amendment.setEffectiveDate(rs.getDate("effectiveDate"));

        int oldDepartmentId = rs.getInt("oldDepartmentId");
        amendment.setOldDepartmentId(rs.wasNull() ? null : oldDepartmentId);

        int newDepartmentId = rs.getInt("newDepartmentId");
        amendment.setNewDepartmentId(rs.wasNull() ? null : newDepartmentId);

        int oldPositionId = rs.getInt("oldPositionId");
        amendment.setOldPositionId(rs.wasNull() ? null : oldPositionId);

        int newPositionId = rs.getInt("newPositionId");
        amendment.setNewPositionId(rs.wasNull() ? null : newPositionId);

        amendment.setOldSalary(rs.getBigDecimal("oldSalary"));
        amendment.setNewSalary(rs.getBigDecimal("newSalary"));

        amendment.setReason(rs.getString("reason"));

        int sourceFormId = rs.getInt("sourceFormId");
        amendment.setSourceFormId(rs.wasNull() ? null : sourceFormId);

        amendment.setStatus(rs.getString("status"));
        amendment.setCreatedBy(rs.getInt("createdBy"));

        int approvedBy = rs.getInt("approvedBy");
        amendment.setApprovedBy(rs.wasNull() ? null : approvedBy);

        amendment.setCreatedAt(rs.getDate("createdAt"));
        amendment.setApprovedAt(rs.getDate("approvedAt"));

        return amendment;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void setNullableBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DECIMAL);
        } else {
            ps.setBigDecimal(index, value);
        }
    }
}
