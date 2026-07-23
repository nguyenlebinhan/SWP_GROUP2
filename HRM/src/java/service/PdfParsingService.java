package service;

import utils.FieldSanitizer;
import utils.ContractRegexParser;
import utils.PdfTextExtractor;
import dto.ContractImportDTO;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class PdfParsingService {

    private final PdfTextExtractor textExtractor = new PdfTextExtractor();
    private final ContractRegexParser regexParser = new ContractRegexParser();
    private final FieldSanitizer sanitizer = new FieldSanitizer();

    public ContractImportDTO parsePdf(InputStream pdfStream) throws IOException {
        String fullText = textExtractor.extractText(pdfStream);
        Map<String, String> raw = regexParser.parse(fullText);
        Map<String, Object> clean = sanitizer.sanitizeAll(raw);
        return mapToDTO(clean);
    }

    private ContractImportDTO mapToDTO(Map<String, Object> clean) {
        ContractImportDTO dto = new ContractImportDTO();
        dto.setEmployeeCode((String) clean.get("employeeCode"));
        dto.setEmployeeName((String) clean.get("employeeName"));
        dto.setContractType((String) clean.get("contractType"));
        dto.setEffectiveDate((LocalDate) clean.get("effectiveDate"));
        dto.setEndDate((LocalDate) clean.get("endDate"));
        dto.setSignedDate((LocalDate) clean.get("signedDate"));
        dto.setDepartmentName((String) clean.get("departmentName"));
        dto.setPositionName((String) clean.get("positionName"));
        dto.setSalary((BigDecimal) clean.get("salary"));
        return dto;
    }
}
