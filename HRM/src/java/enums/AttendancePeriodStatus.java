package enums;

/**

 * @author ADMIN
 */
public enum AttendancePeriodStatus {
    OPEN(0),
    WAITING_MANAGER(1),
    MANAGER_CONFIRMED(2),
    WAITING_HR_FINAL_CHECK(3),
    LOCKED(4);

    private final int relatedNum;

    private AttendancePeriodStatus(int relatedNum) {
        this.relatedNum = relatedNum;
    }

    public int getRelatedNum() {
        return relatedNum;
    }

    public static AttendancePeriodStatus fromCode(int code) {
        for (AttendancePeriodStatus s : values()) {
            if (s.relatedNum == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Mã trạng thái kỳ chấm công không hợp lệ: " + code);
    }

    public String getLabel() {
        switch (this) {
            case OPEN: return "Đang mở (được sửa)";
            case WAITING_MANAGER: return "Chờ trưởng phòng chốt";
            case MANAGER_CONFIRMED: return "Trưởng phòng đã chốt";
            case WAITING_HR_FINAL_CHECK: return "Chờ HR chốt cuối";
            case LOCKED: return "Đã chốt (khoá)";
            default: return "Không xác định";
        }
    }
}
