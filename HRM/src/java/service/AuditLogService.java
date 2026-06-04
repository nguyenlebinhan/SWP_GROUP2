/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AuditLogDAO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.AuditLog;

/**

 * @author admin
 */
public class AuditLogService {

    private static final Logger LOGGER = Logger.getLogger(AuditLogService.class.getName());
    private static final ExecutorService AUDIT_EXECUTOR = Executors.newFixedThreadPool(4);

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    public void logAsync(AuditLog log) {
        if (log == null) {
            return;
        }
        AUDIT_EXECUTOR.submit(() -> {
            try {
                auditLogDAO.insertAuditLog(log);
            } catch (RuntimeException e) {
                
                LOGGER.log(Level.SEVERE, "Async audit log failed (action=" + log.getAction() + ")", e);
            }
        });
    }


    public void logAsync(Integer userId, String action, String tableName, Integer recordId,
            String oldValue, String newValue, String ipAddress, String userAgent) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        log.setUserAgent(truncate(userAgent, 255));
        log.setStatus("SUCCESS");
        logAsync(log);
    }

    public void logAsync(Integer userId, String action, String tableName, Integer recordId,
            String oldValue, String newValue, String ipAddress, String userAgent, String status) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        log.setUserAgent(truncate(userAgent, 255));
        log.setStatus(status);
        logAsync(log);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
    
    public void shutdown() {
        AUDIT_EXECUTOR.shutdown();
    }
}
