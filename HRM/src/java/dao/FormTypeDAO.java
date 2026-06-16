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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.FormType;

/**
 *
 * @author admin
 */
public class FormTypeDAO {
    private static final Logger LOGGER = Logger.getLogger(FormTypeDAO.class.getName());
    private final DBContext dbContext;
    
    public FormTypeDAO() {
        this.dbContext = new DBContext();
    }
    
    public List<FormType> getAll() {
        List<FormType> formTypes = new ArrayList<>();
        String SQL = "SELECT formTypeId, formTypeCode, formTypeName FROM form_types ORDER BY formTypeId";
        
        try(Connection conn = dbContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL);
                ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                FormType ft = new FormType();
                ft.setFormTypeId(rs.getInt("formTypeId"));
                ft.setFormTypeCode(rs.getString("formTypeCode"));
                ft.setFormTypeName(rs.getString("formTypeName"));
                formTypes.add(ft);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot load form types", e);
        }
        return formTypes;
    }

    public FormType getById(int id) {
        String SQL = "SELECT formTypeId, formTypeCode, formTypeName FROM form_types WHERE formTypeId = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FormType ft = new FormType();
                    ft.setFormTypeId(rs.getInt("formTypeId"));
                    ft.setFormTypeCode(rs.getString("formTypeCode"));
                    ft.setFormTypeName(rs.getString("formTypeName"));
                    return ft;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot load form type by id=" + id, e);
        }
        return null;
    }
    public FormType getByCode(String code) {
        String SQL = "SELECT formTypeId, formTypeCode, formTypeName FROM form_types WHERE formTypeCode = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FormType ft = new FormType();
                    ft.setFormTypeId(rs.getInt("formTypeId"));
                    ft.setFormTypeCode(rs.getString("formTypeCode"));
                    ft.setFormTypeName(rs.getString("formTypeName"));
                    return ft;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot load form type by code=" + code, e);
        }
        return null;
    }

}
