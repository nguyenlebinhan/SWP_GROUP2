/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package enums;

/**
 *
 * @author ADMIN
 */
public enum AttendanceStatus {
    PRESENT(0),
    LATE(1),
    ABSENT(2),
    UNEXCUSED(3),   // giữ lại cho dữ liệu cũ, không còn được tạo khi import
    LEAVE(4),
    HOLIDAY(5),
    WEEKEND(6),
    MISSING_CHECK(7);   // Quên chấm công: chỉ có giờ vào hoặc chỉ có giờ ra

    private final int relatedNum;

    private AttendanceStatus(int relatedNum) {
        this.relatedNum = relatedNum;
    }

    public int getRelatedNum() {
        return relatedNum;
    }

    public static AttendanceStatus fromCode(int code) {
        for (AttendanceStatus s : values()) {
            if (s.relatedNum == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("Mã trạng thái chấm công không hợp lệ: " + code);
    }

}
