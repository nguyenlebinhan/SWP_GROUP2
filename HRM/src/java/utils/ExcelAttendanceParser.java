/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import dto.AttendanceDataDTO;
import exception.InvalidFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author admin
 */
public class ExcelAttendanceParser {

    public static final String[] EXPECTED_HEADERS = {
        "employeeCode", "fullName", "Department", "workDate", "timeIn", "timeOut"
    };

    
    public List<AttendanceDataDTO> parse(InputStream in) throws IOException {
        List<AttendanceDataDTO> attendanceDataDTOs = new ArrayList<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new InvalidFormatException("File Excel không có sheet nào.");
            }
            Sheet sheet = workbook.getSheetAt(0);

            Row header = sheet.getRow(sheet.getFirstRowNum());
            validateHeader(header);

            int lastRow = sheet.getLastRowNum();
            for (int i = sheet.getFirstRowNum() + 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                AttendanceDataDTO attendanceDTO = new AttendanceDataDTO();
                attendanceDTO.setRowNumber(i+1);
                attendanceDTO.setEmployeeCode(getCellString(row.getCell(0)));
                attendanceDTO.setFullName(getCellString(row.getCell(1)));
                attendanceDTO.setDepartmentName(getCellString(row.getCell(2)));
                attendanceDTO.setWorkDate(getCellString(row.getCell(3)));
                attendanceDTO.setTimeIn(getCellString(row.getCell(4)));
                attendanceDTO.setTimeOut(getCellString(row.getCell(5)));
                attendanceDataDTOs.add(attendanceDTO);
            }
        }
        return attendanceDataDTOs;
    }

    private void validateHeader(Row header) throws InvalidFormatException {
        if (header == null) {
            throw new InvalidFormatException("File Excel thiếu dòng tiêu đề (header).");
        }
        for (int c = 0; c < EXPECTED_HEADERS.length; c++) {
            String actual = getCellString(header.getCell(c));
            if (!EXPECTED_HEADERS[c].equalsIgnoreCase(actual.trim())) {
                throw new InvalidFormatException(
                        "Header cột " + (c + 1) + " phải là \"" + EXPECTED_HEADERS[c]
                        + "\" nhưng đang là \"" + actual + "\". "
                        + "Thứ tự cột yêu cầu: employeeCode, fullName, Department, workDate, timeIn, timeOut.");
            }
        }
    }
    

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < EXPECTED_HEADERS.length; c++) {
            if (!getCellString(row.getCell(c)).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    double v = cell.getNumericCellValue();
                    java.util.Date d = DateUtil.getJavaDate(v);
                    if (v < 1.0d) {
                        return new SimpleDateFormat("HH:mm").format(d);
                    }
                    return new SimpleDateFormat("yyyy-MM-dd").format(d);
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }
}
