package utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContractRegexParser {

    private static final Pattern EMPLOYEE_NAME = Pattern.compile("(?:BÊN B|Họ tên|Tên nhân viên)\\s*[:\\(].*?\\).*?:[\\s.]*(.+?)(?:\\n|\\r|$)");
    private static final Pattern EMPLOYEE_CODE = Pattern.compile("(?:Mã nhân viên|Mã NV)\\s*[:\n][\\s.]*([A-Za-z0-9]+)[\\s.]*(?:\\n|\\r|$)");
    private static final Pattern CONTRACT_TYPE = Pattern.compile("\\d+\\.\\s*(?:Loại hợp đồng|Loại HĐ)\\s*[:\n][\\s.]*(.+?)(?:\\n|\\r|$)");
    private static final Pattern EFFECTIVE_DATE = Pattern.compile("(?:Kể từ ngày|Ngày hiệu lực)\\s*[:\n][\\s.]*(\\d{2}[-/]\\d{2}[-/]\\d{4})[\\s.]*(?:\\n|\\r|$)");
    private static final Pattern END_DATE = Pattern.compile("(?:Đến ngày|Ngày kết thúc)\\s*[:\n][\\s.]*(\\d{2}[-/]\\d{2}[-/]\\d{4})[\\s.]*(?:\\n|\\r|$)");
    private static final Pattern SIGNED_DATE = Pattern.compile("(?:Ngày ký|Ký ngày|Hợp đồng được ký ngày)\\s*[:\n][\\s.]*(\\d{2}[-/]\\d{2}[-/]\\d{4})[\\s.]*(?:\\n|\\r|$)");
    private static final Pattern DEPARTMENT = Pattern.compile("\\d+\\.\\s*(?:Bộ phận công tác|Phòng ban)\\s*[:\n][\\s.]*(.+?)(?:\\n|\\r|$)");
    private static final Pattern POSITION = Pattern.compile("\\d+\\.\\s*(?:Chức danh|Chức vụ).*?[:\n][\\s.]*(.+?)(?:\\n|\\r|$)");
    private static final Pattern SALARY = Pattern.compile("Mức lương[^:\\n]*:[\\s.]*([\\d.,]+)");

    public Map<String, String> parse(String text) {
        Map<String, String> raw = new LinkedHashMap<>();
        raw.put("employeeName", extract(text, EMPLOYEE_NAME));
        raw.put("employeeCode", extract(text, EMPLOYEE_CODE));
        raw.put("contractType", extract(text, CONTRACT_TYPE));
        raw.put("effectiveDate", extract(text, EFFECTIVE_DATE));
        raw.put("endDate", extract(text, END_DATE));
        raw.put("signedDate", extract(text, SIGNED_DATE));
        raw.put("departmentName", extract(text, DEPARTMENT));
        raw.put("positionName", extract(text, POSITION));
        raw.put("salary", extract(text, SALARY));
        return raw;
    }

    private String extract(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }
}
