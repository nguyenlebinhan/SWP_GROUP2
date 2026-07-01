package enums;

/**
 * Trạng thái chốt bảng chấm công theo (tháng, phòng ban).
 *
 * Luồng: OPEN -> WAITING_MANAGER -> MANAGER_CONFIRMED -> SUBMITTED_TO_BA -> LOCKED
 *
 * - OPEN: còn trong thời gian sửa chấm công (đến hết mùng 5 tháng sau).
 * - WAITING_MANAGER: HR đã đóng kỳ, chờ trưởng phòng chốt bảng chấm công phòng mình.
 * - MANAGER_CONFIRMED: trưởng phòng đã chốt.
 * - SUBMITTED_TO_BA: HR đã gửi toàn bộ (mọi phòng đã chốt) lên BusinessAdmin chờ duyệt cuối.
 * - LOCKED: BusinessAdmin đã chốt cuối cùng; được phép tính lương và khoá sửa vĩnh viễn.
 *
 * @author ADMIN
 */
public enum AttendancePeriodStatus {
    OPEN(0),
    WAITING_MANAGER(1),
    MANAGER_CONFIRMED(2),
    SUBMITTED_TO_BA(3),
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
            case SUBMITTED_TO_BA: return "Đã gửi BA duyệt";
            case LOCKED: return "Đã chốt (khoá)";
            default: return "Không xác định";
        }
    }
}
