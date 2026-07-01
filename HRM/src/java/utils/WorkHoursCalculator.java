/*
 * Tính giờ làm thực tế có trừ giờ nghỉ trưa.
 */
package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;


public final class WorkHoursCalculator {

    private static final Time LUNCH_START = Time.valueOf("12:00:00");
    private static final Time LUNCH_END = Time.valueOf("13:00:00");

    /** Độ dài một block tính công, tính theo phút. */
    public static final int BLOCK_MINUTES = 30;

    private static final int MINUTES_PER_DAY = 24 * 60;

    private WorkHoursCalculator() {
    }

    public static Time ceilToBlock(Time t) {
        if (t == null) {
            return null;
        }
        int min = t.toLocalTime().toSecondOfDay() / 60;
        int rounded = ((min + BLOCK_MINUTES - 1) / BLOCK_MINUTES) * BLOCK_MINUTES;
        if (rounded >= MINUTES_PER_DAY) {
            return Time.valueOf("23:59:59");
        }
        return Time.valueOf(java.time.LocalTime.ofSecondOfDay(rounded * 60L));
    }

    public static Time floorToBlock(Time t) {
        if (t == null) {
            return null;
        }
        int min = t.toLocalTime().toSecondOfDay() / 60;
        int rounded = (min / BLOCK_MINUTES) * BLOCK_MINUTES;
        return Time.valueOf(java.time.LocalTime.ofSecondOfDay(rounded * 60L));
    }

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


    public static BigDecimal hoursWorked(Time timeIn, Time timeOut) {
        long minutes = workedMinutes(timeIn, timeOut);
        return new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
    }


    public static String label(long minutes) {
        if (minutes < 0) {
            minutes = 0;
        }
        return (minutes / 60) + "h" + String.format("%02d", minutes % 60) + "m";
    }

    private static long overlapMillis(Time aStart, Time aEnd, Time bStart, Time bEnd) {
        long start = Math.max(aStart.getTime(), bStart.getTime());
        long end = Math.min(aEnd.getTime(), bEnd.getTime());
        return Math.max(0L, end - start);
    }
}
