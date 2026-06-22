/*
 * Tính giờ làm thực tế có trừ giờ nghỉ trưa.
 */
package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;

/**
 * Gom logic tính giờ làm để mọi nơi (import, sửa tay, hiển thị nhãn) dùng chung,
 * tránh tình trạng nơi trừ nơi không trừ giờ nghỉ trưa.
 *
 * Quy ước: giờ nghỉ trưa cố định 12:00–13:00 (1 tiếng). Giờ làm = (giờ ra - giờ vào)
 * trừ đi phần GIAO với khoảng nghỉ trưa. Nhờ vậy:
 *  - Làm 08:00–17:00 (9 tiếng thô) -> trừ 1 tiếng nghỉ trưa -> 8 tiếng.
 *  - Làm 08:00–12:00 hoặc 13:00–17:00 (không chạm giờ nghỉ) -> không bị trừ.
 *  - Làm lệch một phần vào giờ nghỉ -> chỉ trừ đúng phần giao nhau.
 */
public final class WorkHoursCalculator {

    private static final Time LUNCH_START = Time.valueOf("12:00:00");
    private static final Time LUNCH_END = Time.valueOf("13:00:00");

    private WorkHoursCalculator() {
    }

    /**
     * Số phút làm thực tế (đã trừ giờ nghỉ trưa). Không âm.
     */
    public static long workedMinutes(Time timeIn, Time timeOut) {
        if (timeIn == null || timeOut == null) {
            return 0;
        }
        long grossMillis = timeOut.getTime() - timeIn.getTime();
        if (grossMillis <= 0) {
            return 0;
        }
        long lunchMillis = overlapMillis(timeIn, timeOut, LUNCH_START, LUNCH_END);
        long netMillis = grossMillis - lunchMillis;
        if (netMillis < 0) {
            netMillis = 0;
        }
        return netMillis / 60000L;
    }

    /**
     * Số giờ làm thực tế (đã trừ giờ nghỉ trưa), DECIMAL scale 2.
     */
    public static BigDecimal hoursWorked(Time timeIn, Time timeOut) {
        long minutes = workedMinutes(timeIn, timeOut);
        return new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
    }

    /**
     * Nhãn giờ làm dạng "8h00m" từ số phút.
     */
    public static String label(long minutes) {
        if (minutes < 0) {
            minutes = 0;
        }
        return (minutes / 60) + "h" + String.format("%02d", minutes % 60) + "m";
    }

    /** Số mili-giây giao nhau giữa [aStart, aEnd] và [bStart, bEnd]. */
    private static long overlapMillis(Time aStart, Time aEnd, Time bStart, Time bEnd) {
        long start = Math.max(aStart.getTime(), bStart.getTime());
        long end = Math.min(aEnd.getTime(), bEnd.getTime());
        return Math.max(0L, end - start);
    }
}
