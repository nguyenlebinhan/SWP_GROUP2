/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import model.AttendancePeriod;


public class AttendancePeriodService {

    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final Clock clock;

    public AttendancePeriodService() {
        this(Clock.system(ZONE));
    }

    public AttendancePeriodService(Clock clock) {
        this.clock = clock;
    }

    public LocalDate getEditDeadlineDate(int month, int year) {
        return YearMonth.of(year, month).plusMonths(1).atDay(3);
    }

    public ZonedDateTime getLockTime(int month, int year) {
        return YearMonth.of(year, month).plusMonths(1).atDay(4).atStartOfDay(ZONE);
    }

    public boolean isLocked(int month, int year) {
        return !ZonedDateTime.now(clock).isBefore(getLockTime(month, year));
    }

    public boolean isDateInPeriod(LocalDate workDate, int month, int year) {
        return workDate != null && workDate.getMonthValue() == month && workDate.getYear() == year;
    }


    public String checkImport(AttendancePeriod period, int month, int year) {
        if (isLocked(month, year)) {
            return "Kỳ chấm công " + month + "/" + year + " đã khóa từ "
                    + getLockTime(month, year).toLocalDate() + ", không thể import.";
        }
        if (period != null && period.isPublished()) {
            return "Kỳ chấm công " + month + "/" + year
                    + " đang công khai. Hãy chuyển về riêng tư trước khi import.";
        }
        return null;
    }

    public boolean canImport(AttendancePeriod period, int month, int year) {
        return checkImport(period, month, year) == null;
    }


    public String checkEdit(Integer periodStatus, int month, int year) {
        if (isLocked(month, year)) {
            return "Kỳ chấm công " + month + "/" + year + " đã khóa từ "
                    + getLockTime(month, year).toLocalDate() + ", không thể chỉnh sửa.";
        }
        if (periodStatus != null && periodStatus == AttendancePeriod.STATUS_PUBLIC) {
            return "Kỳ chấm công " + month + "/" + year
                    + " đang công khai. Hãy chuyển về riêng tư trước khi chỉnh sửa.";
        }
        return null;
    }

    public boolean canEdit(Integer periodStatus, int month, int year) {
        return checkEdit(periodStatus, month, year) == null;
    }

    public String checkPublish(AttendancePeriod period) {
        if (period == null) {
            return "Chưa có kỳ chấm công để công khai (chưa import dữ liệu).";
        }
        if (period.isPublished()) {
            return "Kỳ chấm công đã ở trạng thái công khai.";
        }
        return null;
    }

    public boolean canPublish(AttendancePeriod period) {
        return checkPublish(period) == null;
    }


    public String checkUnpublish(AttendancePeriod period) {
        if (period == null || !period.isPublished()) {
            return "Kỳ chấm công chưa công khai nên không thể chuyển về riêng tư.";
        }
        if (isLocked(period.getMonth(), period.getYear())) {
            return "Kỳ chấm công " + period.getMonth() + "/" + period.getYear()
                    + " đã khóa, không thể chuyển từ công khai về riêng tư.";
        }
        return null;
    }

    public boolean canUnpublish(AttendancePeriod period) {
        return checkUnpublish(period) == null;
    }


    public String checkTransition(AttendancePeriod period, int targetStatus) {
        if (targetStatus == AttendancePeriod.STATUS_PUBLIC) {
            return checkPublish(period);
        }
        if (targetStatus == AttendancePeriod.STATUS_PRIVATE) {
            return checkUnpublish(period);
        }
        return "Trạng thái kỳ chấm công không hợp lệ.";
    }
}
