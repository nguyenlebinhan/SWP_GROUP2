package utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FieldSanitizer {

    public String sanitizeField(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        value = value.replaceAll("\\.{4,}", "");
        value = value.replaceAll("[\\p{Cntrl}]", "");
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    public LocalDate sanitizeDate(String value) {
        if (value == null) {
            return null;
        }
        value = sanitizeField(value);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    public BigDecimal sanitizeSalary(String value) {
        if (value == null) {
            return null;
        }
        value = sanitizeField(value);
        if (value == null) {
            return null;
        }
        value = value.replaceAll("[,.]", "");
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String sanitizeContractType(String value) {
        if (value == null) {
            return null;
        }
        value = sanitizeField(value);
        if (value == null) {
            return null;
        }

        String lower = value.toLowerCase().trim();
        if (lower.contains("thử việc") || lower.contains("thu viec")) {
            return "PROBATION";
        }
        if (lower.contains("thực tập") || lower.contains("thuc tap")) {
            return "INTERNSHIP";
        }
        if (lower.contains("có thời hạn") || lower.contains("co thoi han")) {
            return "FIXED_TERM";
        }
        if (lower.contains("không xác định") || lower.contains("khong xac dinh")
                || lower.contains("không thời hạn") || lower.contains("khong thoi han")) {
            return "INDEFINITE";
        }
        return null;
    }

    public java.util.Map<String, Object> sanitizeAll(java.util.Map<String, String> raw) {
        java.util.Map<String, Object> clean = new java.util.LinkedHashMap<>();
        clean.put("employeeCode", sanitizeField(raw.get("employeeCode")));
        clean.put("employeeName", sanitizeField(raw.get("employeeName")));
        clean.put("contractType", sanitizeContractType(raw.get("contractType")));
        clean.put("effectiveDate", sanitizeDate(raw.get("effectiveDate")));
        clean.put("endDate", sanitizeDate(raw.get("endDate")));
        clean.put("signedDate", sanitizeDate(raw.get("signedDate")));
        clean.put("departmentName", sanitizeField(raw.get("departmentName")));
        clean.put("positionName", sanitizeField(raw.get("positionName")));
        clean.put("salary", sanitizeSalary(raw.get("salary")));
        return clean;
    }
}
