package utils;

import dto.CandidateDataDTO;
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

public class ExcelCandidateParser {

    public static final String[] EXPECTED_HEADERS = {
        "fullName", "email", "phoneNumber", "dateOfBirth", "gender",
        "address", "skills", "experience", "certificates", "degree",
        "departmentCode", "positionName", "cvUrl"
    };

    public List<CandidateDataDTO> parse(InputStream in) throws IOException {
        List<CandidateDataDTO> list = new ArrayList<>();
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

                CandidateDataDTO dto = new CandidateDataDTO();
                dto.setRowNumber(i + 1);
                dto.setFullName(getCellString(row.getCell(0)));
                dto.setEmail(getCellString(row.getCell(1)));
                dto.setPhoneNumber(getCellString(row.getCell(2)));
                dto.setDateOfBirth(getCellString(row.getCell(3)));
                dto.setGender(getCellString(row.getCell(4)));
                dto.setAddress(getCellString(row.getCell(5)));
                dto.setSkills(getCellString(row.getCell(6)));
                dto.setExperience(getCellString(row.getCell(7)));
                dto.setCertificates(getCellString(row.getCell(8)));
                dto.setDegree(getCellString(row.getCell(9)));
                dto.setDepartmentCode(getCellString(row.getCell(10)));
                dto.setPositionName(getCellString(row.getCell(11)));
                dto.setCvUrl(getCellString(row.getCell(12)));
                list.add(dto);
            }
        }
        return list;
    }

    private void validateHeader(Row header) throws InvalidFormatException {
        if (header == null) {
            throw new InvalidFormatException("File Excel thiếu dòng tiêu đề.");
        }
        for (int c = 0; c < EXPECTED_HEADERS.length; c++) {
            Cell cell = header.getCell(c);
            String actual = (cell == null) ? "" : getCellString(cell);  // thêm null check
            if (!EXPECTED_HEADERS[c].equalsIgnoreCase(actual.trim())) {
                throw new InvalidFormatException(
                        "Header cột " + (c + 1) + " phải là \"" + EXPECTED_HEADERS[c]
                        + "\" nhưng đang là \"" + actual + "\".");
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
                    return new SimpleDateFormat("yyyy-MM-dd").format(d);
                }
                double num = cell.getNumericCellValue();
                return num == Math.floor(num)
                        ? String.valueOf((long) num)
                        : String.valueOf(num);
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
