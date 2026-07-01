package service;

import dao.PayrollConfigChangeRequestDAO;
import dao.PayrollConfigDAO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.PayrollConfigChangeRequest;
import model.PayrollDeductionRule;
import model.PayrollSetting;
import model.PayrollTaxBracket;
import model.User;

public class PayrollConfigWorkflowService {

    private final PayrollConfigDAO payrollConfigDAO = new PayrollConfigDAO();
    private final PayrollConfigChangeRequestDAO requestDAO = new PayrollConfigChangeRequestDAO();

    public List<PayrollConfigChangeRequest> getPendingRequests() {
        return requestDAO.getRequests(PayrollConfigChangeRequest.STATUS_PENDING);
    }

    public boolean approveRequest(int requestId, User user, String note) {
        return user != null && requestDAO.approve(requestId, user.getUserId(), trim(note));
    }

    public boolean rejectRequest(int requestId, User user, String note) {
        return user != null && requestDAO.reject(requestId, user.getUserId(), trim(note));
    }

    public int requestSettingChange(PayrollSetting setting, User requestedBy) {
        PayrollConfigChangeRequest request = new PayrollConfigChangeRequest();
        request.setRequestType(PayrollConfigChangeRequest.TYPE_SETTING_SAVE);
        request.setActionLabel("Cập nhật tham số payroll");
        request.setTargetKey(setting.getSettingKey());
        request.setSettingKey(setting.getSettingKey());
        request.setSettingValue(setting.getSettingValue());
        request.setSettingDescription(setting.getDescription());
        request.setOldValue(settingOldValue(setting.getSettingKey()));
        request.setNewValue("key=" + setting.getSettingKey()
                + "; value=" + setting.getSettingValue()
                + "; description=" + setting.getDescription());
        request.setRequestedBy(requestedBy.getUserId());
        return requestDAO.createRequest(request);
    }

    public int requestDeductionSave(PayrollDeductionRule rule, User requestedBy) {
        PayrollConfigChangeRequest request = new PayrollConfigChangeRequest();
        request.setRequestType(PayrollConfigChangeRequest.TYPE_DEDUCTION_SAVE);
        request.setActionLabel(rule.getRuleId() > 0 ? "Cập nhật khoản khấu trừ" : "Thêm khoản khấu trừ");
        request.setTargetId(rule.getRuleId() > 0 ? rule.getRuleId() : null);
        bindDeduction(request, rule);
        request.setOldValue(rule.getRuleId() > 0 ? deductionOldValue(rule.getRuleId()) : "Thêm mới");
        request.setNewValue(deductionValue(rule));
        request.setRequestedBy(requestedBy.getUserId());
        return requestDAO.createRequest(request);
    }

    public int requestDeductionDelete(int ruleId, User requestedBy) {
        PayrollConfigChangeRequest request = new PayrollConfigChangeRequest();
        request.setRequestType(PayrollConfigChangeRequest.TYPE_DEDUCTION_DELETE);
        request.setActionLabel("Xóa khoản khấu trừ");
        request.setTargetId(ruleId);
        request.setOldValue(deductionOldValue(ruleId));
        request.setNewValue("Xóa khoản khấu trừ ruleId=" + ruleId);
        request.setRequestedBy(requestedBy.getUserId());
        return requestDAO.createRequest(request);
    }

    public int requestTaxBracketSave(List<PayrollTaxBracket> brackets, User requestedBy) {
        PayrollConfigChangeRequest request = new PayrollConfigChangeRequest();
        request.setRequestType(PayrollConfigChangeRequest.TYPE_TAX_SAVE);
        request.setActionLabel("Cập nhật bậc thuế TNCN");
        request.setOldValue(taxValue(payrollConfigDAO.getTaxBrackets(false)));
        request.setNewValue(taxValue(brackets));
        request.setTaxPayload(requestDAO.serializeTaxBrackets(brackets));
        request.setRequestedBy(requestedBy.getUserId());
        return requestDAO.createRequest(request);
    }

    public PayrollSetting buildSetting(String key, String rawValue, String description) {
        PayrollSetting setting = new PayrollSetting();
        setting.setSettingKey(trim(key).toUpperCase());
        setting.setSettingValue(parsePayrollSettingValue(setting.getSettingKey(), rawValue));
        setting.setDescription(trim(description));
        return setting;
    }

    public String validatePayrollSetting(PayrollSetting setting) {
        if (setting == null || isBlank(setting.getSettingKey()) || setting.getSettingValue() == null) {
            return "Dữ liệu tham số payroll không hợp lệ.";
        }
        String key = setting.getSettingKey();
        BigDecimal value = setting.getSettingValue();
        if (isWorkScheduleSetting(key)) {
            return "Cáº¥u hĂ¬nh giá» lĂ m viá»‡c thuá»™c module chĂ¢m cĂ´ng, khĂ´ng chá»‰nh trong payroll.";
        }
        if (isWorkTimeSetting(key)
                && (value.signum() < 0 || value.compareTo(new BigDecimal("1439")) > 0)) {
            return "Giờ làm chuẩn phải đúng định dạng HH:mm.";
        }
        if ("WORK_BREAK_MINUTES".equals(key) && value.signum() < 0) {
            return "Số phút nghỉ không được âm.";
        }
        if (isWorkTimeSetting(key) || "WORK_BREAK_MINUTES".equals(key)) {
            Map<String, BigDecimal> settings = payrollConfigDAO.getSettingsMap();
            settings.put(key, value);
            BigDecimal start = settingValue(settings, "WORK_START", "WORK_START_MINUTES");
            BigDecimal end = settingValue(settings, "WORK_END", "WORK_END_MINUTES");
            BigDecimal breakMinutes = settings.get("WORK_BREAK_MINUTES");
            if (start != null && end != null && breakMinutes != null
                    && end.subtract(start).subtract(breakMinutes).signum() <= 0) {
                return "Giờ ra phải lớn hơn giờ vào sau khi trừ thời gian nghỉ.";
            }
        }
        return null;
    }

    public PayrollDeductionRule buildDeductionRule(String ruleIdRaw, String code, String name,
            String type, String employerRateRaw, String employeeRateRaw) {
        PayrollDeductionRule rule = new PayrollDeductionRule();
        Integer ruleId = parseInt(ruleIdRaw);
        BigDecimal employerRate = parsePercentParam(employerRateRaw);
        BigDecimal employeeRate = parsePercentParam(employeeRateRaw);
        rule.setRuleId(ruleId == null ? 0 : ruleId);
        rule.setRuleCode(trim(code).toUpperCase());
        rule.setRuleName(trim(name));
        rule.setRuleType(trim(type).toUpperCase());
        rule.setCalculationType(PayrollDeductionRule.CALC_PERCENT);
        rule.setRate(employerRate == null || employeeRate == null ? null : employerRate.add(employeeRate));
        rule.setEmployerRate(employerRate);
        rule.setFixedAmount(BigDecimal.ZERO);
        rule.setTaxableDeduction(true);
        rule.setActive(true);
        return rule;
    }

    public String validateDeductionRule(PayrollDeductionRule rule) {
        if (rule == null || isBlank(rule.getRuleCode()) || isBlank(rule.getRuleName())
                || rule.getRate() == null || rule.getEmployerRate() == null
                || rule.getRate().signum() < 0 || rule.getEmployerRate().signum() < 0
                || rule.getEmployeeRate().signum() < 0) {
            return "Dữ liệu khoản khấu trừ không hợp lệ.";
        }
        return null;
    }

    public List<PayrollTaxBracket> buildTaxBrackets(String[] ids, String[] minIncomes,
            String[] maxIncomes, String[] taxRates) {
        List<PayrollTaxBracket> brackets = new ArrayList<>();
        if (ids == null || minIncomes == null || maxIncomes == null || taxRates == null
                || ids.length != minIncomes.length || ids.length != maxIncomes.length
                || ids.length != taxRates.length) {
            return brackets;
        }
        for (int i = 0; i < ids.length; i++) {
            Integer id = parseInt(ids[i]);
            PayrollTaxBracket bracket = new PayrollTaxBracket();
            bracket.setBracketId(id == null ? 0 : id);
            bracket.setMinIncome(parseDecimal(minIncomes[i]));
            bracket.setMaxIncome(parseDecimal(maxIncomes[i]));
            bracket.setTaxRate(parseDecimal(taxRates[i]));
            brackets.add(bracket);
        }
        return brackets;
    }

    public String validateTaxBrackets(List<PayrollTaxBracket> brackets) {
        if (brackets == null || brackets.isEmpty()) {
            return "Dữ liệu bậc thuế không hợp lệ.";
        }
        for (PayrollTaxBracket bracket : brackets) {
            if (bracket.getBracketId() <= 0 || bracket.getMinIncome() == null || bracket.getTaxRate() == null) {
                return "Dữ liệu bậc thuế không hợp lệ.";
            }
            if (bracket.getMinIncome().signum() < 0 || bracket.getTaxRate().signum() < 0) {
                return "Mốc thu nhập và thuế suất không được âm.";
            }
            if (bracket.getTaxRate().compareTo(BigDecimal.ONE) > 0) {
                return "Thuế suất phải nhập dạng decimal, ví dụ 5% là 0.05.";
            }
        }
        if (brackets.get(0).getMinIncome().compareTo(BigDecimal.ZERO) != 0) {
            return "Bậc thuế đầu tiên phải bắt đầu từ 0.";
        }
        for (int i = 0; i < brackets.size(); i++) {
            PayrollTaxBracket current = brackets.get(i);
            boolean last = i == brackets.size() - 1;
            if (last) {
                if (current.getMaxIncome() != null) {
                    return "Bậc thuế cuối phải để trống cột Đến để hiểu là trên mức cao nhất.";
                }
            } else {
                PayrollTaxBracket next = brackets.get(i + 1);
                if (current.getMaxIncome() == null || current.getMaxIncome().compareTo(current.getMinIncome()) <= 0) {
                    return "Mỗi bậc thuế phải có mức Đến lớn hơn mức Từ.";
                }
                if (current.getMaxIncome().compareTo(next.getMinIncome()) != 0) {
                    return "Mức Đến của một bậc phải bằng mức Từ của bậc tiếp theo.";
                }
            }
        }
        return null;
    }

    public String displayPayrollSettingValue(PayrollSetting setting) {
        if (setting == null || setting.getSettingValue() == null) {
            return "";
        }
        if (isWorkTimeSetting(setting.getSettingKey())) {
            return minutesToClock(setting.getSettingValue());
        }
        return setting.getSettingValue().stripTrailingZeros().toPlainString();
    }

    public String buildStandardWorkSchedule(Map<String, BigDecimal> settings) {
        BigDecimal start = settingValue(settings, "WORK_START", "WORK_START_MINUTES");
        BigDecimal end = settingValue(settings, "WORK_END", "WORK_END_MINUTES");
        BigDecimal breakMinutes = settings == null ? null : settings.get("WORK_BREAK_MINUTES");
        if (start == null || end == null || breakMinutes == null) {
            return null;
        }
        BigDecimal workingMinutes = end.subtract(start).subtract(breakMinutes);
        if (workingMinutes.signum() <= 0) {
            return "Cấu hình giờ làm chuẩn không hợp lệ.";
        }
        BigDecimal hours = workingMinutes.divide(new BigDecimal("60"), 2, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return minutesToClock(start) + " - " + minutesToClock(end)
                + "; nghỉ " + breakMinutes.stripTrailingZeros().toPlainString()
                + " phút; số giờ làm/ngày = " + hours.toPlainString() + "h";
    }

    private BigDecimal parsePayrollSettingValue(String key, String rawValue) {
        if (isWorkTimeSetting(key)) {
            Integer minutes = parseClockToMinutes(rawValue);
            return minutes == null ? null : new BigDecimal(minutes);
        }
        return parseDecimal(rawValue);
    }

    private boolean isWorkTimeSetting(String key) {
        return "WORK_START".equals(key) || "WORK_END".equals(key)
                || "WORK_START_MINUTES".equals(key) || "WORK_END_MINUTES".equals(key);
    }

    private boolean isWorkScheduleSetting(String key) {
        return isWorkTimeSetting(key) || "WORK_BREAK_MINUTES".equals(key);
    }

    private BigDecimal settingValue(Map<String, BigDecimal> settings, String primaryKey, String legacyKey) {
        if (settings == null) {
            return null;
        }
        BigDecimal value = settings.get(primaryKey);
        return value != null ? value : settings.get(legacyKey);
    }

    private Integer parseClockToMinutes(String raw) {
        if (isBlank(raw) || !raw.matches("^\\d{2}:\\d{2}$")) {
            return null;
        }
        String[] parts = raw.split(":");
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return null;
            }
            return hour * 60 + minute;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parsePercentParam(String raw) {
        BigDecimal value = parseDecimal(raw);
        if (value == null) {
            return null;
        }
        return value.divide(new BigDecimal("100"));
    }

    private BigDecimal parseDecimal(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String settingOldValue(String settingKey) {
        for (PayrollSetting setting : payrollConfigDAO.getAllSettings()) {
            if (setting.getSettingKey().equals(settingKey)) {
                return "key=" + setting.getSettingKey()
                        + "; value=" + setting.getSettingValue()
                        + "; description=" + setting.getDescription();
            }
        }
        return "Chưa có giá trị hiện tại.";
    }

    private String deductionOldValue(int ruleId) {
        PayrollDeductionRule rule = payrollConfigDAO.getDeductionRuleById(ruleId);
        return rule == null ? "Không tìm thấy khoản khấu trừ hiện tại." : deductionValue(rule);
    }

    private String deductionValue(PayrollDeductionRule rule) {
        return "code=" + rule.getRuleCode()
                + "; name=" + rule.getRuleName()
                + "; type=" + rule.getRuleType()
                + "; totalRate=" + rule.getRate()
                + "; employerRate=" + rule.getEmployerRate()
                + "; employeeRate=" + rule.getEmployeeRate();
    }

    private String taxValue(List<PayrollTaxBracket> brackets) {
        StringBuilder sb = new StringBuilder();
        if (brackets == null) {
            return "";
        }
        for (PayrollTaxBracket b : brackets) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append("[").append(b.getMinIncome()).append(" - ")
                    .append(b.getMaxIncome() == null ? "MAX" : b.getMaxIncome())
                    .append(": ").append(b.getTaxRate()).append("]");
        }
        return sb.toString();
    }

    private void bindDeduction(PayrollConfigChangeRequest request, PayrollDeductionRule rule) {
        request.setRuleId(rule.getRuleId() > 0 ? rule.getRuleId() : null);
        request.setRuleCode(rule.getRuleCode());
        request.setRuleName(rule.getRuleName());
        request.setRuleType(rule.getRuleType());
        request.setCalculationType(rule.getCalculationType());
        request.setRate(rule.getRate());
        request.setEmployerRate(rule.getEmployerRate());
        request.setFixedAmount(rule.getFixedAmount());
        request.setTaxableDeduction(rule.isTaxableDeduction());
        request.setActive(rule.isActive());
    }

    private String minutesToClock(BigDecimal minutesValue) {
        int minutes = minutesValue.intValue();
        int hour = minutes / 60;
        int minute = minutes % 60;
        return String.format("%02d:%02d", hour, minute);
    }

    private String trim(String raw) {
        return raw == null ? "" : raw.trim();
    }

    private boolean isBlank(String raw) {
        return raw == null || raw.trim().isEmpty();
    }
}
