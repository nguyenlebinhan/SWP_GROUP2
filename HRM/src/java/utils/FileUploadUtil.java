/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Paths;
import model.UploadedFileInfo;

/**
 *
 * @author admin
 */
public class FileUploadUtil {

    private static final String[] ALLOWED_FILES = {
        ".xlsx", ".pdf", ".docx", ".doc", ".xls", ".jpg", ".png", ".zip"
    };

    public static UploadedFileInfo saveAttachment(Part filePart, int employeeId, String formTypeCode, ServletContext ctx) throws IOException {
        if (filePart == null || filePart.getSize() == 0) return null;

        if (filePart.getSize() >= 10L * 1024 * 1024) {
            throw new IllegalArgumentException("Dung lượng file không được quá 10MB");
        }

        String submitted = filePart.getSubmittedFileName();
        if (submitted == null || submitted.isBlank()) return null;

        String ext = submitted.contains(".") ? submitted.substring(submitted.lastIndexOf('.')).toLowerCase() : "";
        boolean allowed = false;
        for (String a : ALLOWED_FILES) {
            if (a.equals(ext)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) throw new IllegalArgumentException("Định dạng file " + ext + " không hợp lệ");

        //String safeCode = formTypeCode.toUpperCase().replaceAll("^[A-Z0-9]", "");
        String serverName = formTypeCode + "_" + employeeId + "_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8) + ext;
        String uploadDir = "/uploads/forms";
        Path dir = Paths.get(ctx.getRealPath("/") + uploadDir);
        Files.createDirectory(dir);       
        try (InputStream is = filePart.getInputStream()) {
            Files.copy(is, dir.resolve(serverName));
        }
        
        return new UploadedFileInfo(uploadDir + "/" + serverName, sanitize(submitted));
    }
    
    private static String sanitize(String name) {
        return name.replaceAll("[^\\w.\\-]", "_");
    }
}
