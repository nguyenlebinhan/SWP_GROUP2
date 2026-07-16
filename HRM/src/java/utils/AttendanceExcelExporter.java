package utils;

import dto.AttendanceReportDTO;
import dto.AttendanceSummaryDTO;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class AttendanceExcelExporter {

    private static final String[] HEADERS = {
        "Employee Code", "Employee Name", "Department", "Position",
        "Standard Hours", "Worked Hours",
        "Present", "Late", "Leave", "Absent", "Weekend",
        "Attendance Rate (%)"
    };

    
    public void write(AttendanceReportDTO report, OutputStream os) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Attendance");

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Báo cáo chấm công - " + report.getScopeTitle()
                    + " - Tháng " + report.getMonth() + "/" + report.getYear());
            titleCell.setCellStyle(titleStyle);

            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(HEADERS[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 3;
            for (AttendanceSummaryDTO s : report.getRows()) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                row.createCell(col++).setCellValue(nz(s.getEmployeeCode()));
                row.createCell(col++).setCellValue(nz(s.getFullName()));
                row.createCell(col++).setCellValue(nz(s.getDepartmentName()));
                row.createCell(col++).setCellValue(nz(s.getPositionName()));
                row.createCell(col++).setCellValue(s.getStandardHours());
                row.createCell(col++).setCellValue(s.getWorkedHoursRounded());
                row.createCell(col++).setCellValue(s.getPresentDays());
                row.createCell(col++).setCellValue(s.getLateDays());
                row.createCell(col++).setCellValue(s.getLeaveDays());
                row.createCell(col++).setCellValue(s.getAbsentDays());
                row.createCell(col++).setCellValue(s.getWeekendDays());
                row.createCell(col).setCellValue(s.getAttendanceRate());
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(os);
        }
    }

    private static String nz(String v) {
        return v == null ? "" : v;
    }
}
