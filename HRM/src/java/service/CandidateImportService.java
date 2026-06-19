package service;

import dao.CandidateDAO;
import dao.DepartmentDAO;
import dao.PositionDAO;
import dto.CandidateDataDTO;
import dto.CandidateImportResultDTO;
import exception.InvalidFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Candidate;
import utils.ExcelCandidateParser;

public class CandidateImportService {

    private static final Logger LOGGER = Logger.getLogger(CandidateImportService.class.getName());

    public static final int FILE_STATUS_PENDING  = 0;
    public static final int FILE_STATUS_IMPORTED = 1;
    public static final int FILE_STATUS_FAILED   = 2;
    public static final int FILE_STATUS_PARTIAL  = 3;

    private final CandidateDAO candidateDAO;
    private final DepartmentDAO departmentDAO;
    private final PositionDAO positionDAO;
    private final ExcelCandidateParser parser;

    public CandidateImportService() {
        this.candidateDAO  = new CandidateDAO();
        this.departmentDAO = new DepartmentDAO();
        this.positionDAO   = new PositionDAO();
        this.parser        = new ExcelCandidateParser();
    }

    public CandidateImportResultDTO importCandidates(InputStream in, int fileId) {
        CandidateImportResultDTO result = new CandidateImportResultDTO();
        result.setFileId(fileId);

        List<CandidateDataDTO> rows;
        try {
            rows = parser.parse(in);
        } catch (InvalidFormatException e) {
            result.setStatus(FILE_STATUS_FAILED);
            result.setNote(e.getMessage());
            result.addError(1, null, e.getMessage());
            return result;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot read Excel file", e);
            result.setStatus(FILE_STATUS_FAILED);
            result.setNote("Không thể đọc file Excel.");
            result.addError(1, null, result.getNote());
            return result;
        }

        result.setTotalRows(rows.size());
        int imported = 0;

        for (CandidateDataDTO row : rows) {
            try {
                Candidate c = buildAndValidate(row, fileId);
                int id = candidateDAO.insert(c);
                if (id > 0) {
                    imported++;
                } else {
                    result.addError(row.getRowNumber(), row.getFullName(),
                            "Lưu dữ liệu thất bại (lỗi cơ sở dữ liệu).");
                }
            } catch (RowValidationException e) {
                result.addError(row.getRowNumber(), row.getFullName(), e.getMessage());
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Unexpected error at row " + row.getRowNumber(), e);
                result.addError(row.getRowNumber(), row.getFullName(),
                        "Lỗi không xác định: " + e.getMessage());
            }
        }

        result.setImportedRows(imported);
        result.setFailedRows(result.getTotalRows() - imported);

        if (result.getTotalRows() == 0) {
            result.setStatus(FILE_STATUS_FAILED);
            result.setNote("File không có dòng dữ liệu nào.");
        } else if (result.getFailedRows() == 0) {
            result.setStatus(FILE_STATUS_IMPORTED);
            result.setNote("Import thành công toàn bộ " + imported + " ứng viên.");
        } else if (imported == 0) {
            result.setStatus(FILE_STATUS_FAILED);
            result.setNote("Tất cả " + result.getTotalRows() + " dòng đều lỗi.");
        } else {
            result.setStatus(FILE_STATUS_PARTIAL);
            result.setNote("Import thành công " + imported + "/" + result.getTotalRows()
                    + " ứng viên, " + result.getFailedRows() + " dòng lỗi.");
        }
        return result;
    }

    private Candidate buildAndValidate(CandidateDataDTO row, int fileId)
            throws RowValidationException {

        // fullName
        String fullName = trimToNull(row.getFullName());
        if (fullName == null) throw new RowValidationException("Thiếu họ tên.");

        // email
        String email = trimToNull(row.getEmail());
        if (email == null) throw new RowValidationException("Thiếu email.");
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$"))
            throw new RowValidationException("Email không hợp lệ: " + email);

        // dateOfBirth — optional
        Date dob = null;
        String dobRaw = trimToNull(row.getDateOfBirth());
        if (dobRaw != null) {
            try { dob = Date.valueOf(dobRaw); }
            catch (IllegalArgumentException e) {
                throw new RowValidationException("Ngày sinh không hợp lệ (yêu cầu yyyy-MM-dd): " + dobRaw);
            }
        }

        // departmentCode → departmentId
        String deptCode = trimToNull(row.getDepartmentCode());
        if (deptCode == null) throw new RowValidationException("Thiếu departmentCode.");
        int departmentId = departmentDAO.getDepartmentIdByCode(deptCode);
        if (departmentId <= 0)
            throw new RowValidationException("Không tìm thấy phòng ban: " + deptCode);

        // positionName → positionId
        String posName = trimToNull(row.getPositionName());
        if (posName == null) throw new RowValidationException("Thiếu positionName.");
        int positionId = positionDAO.getPositionIdByName(posName);
        if (positionId <= 0)
            throw new RowValidationException("Không tìm thấy chức vụ: " + posName);

        Candidate c = new Candidate();
        c.setCandidateCode(candidateDAO.generateCode());
        c.setFullName(fullName);
        c.setEmail(email);
        c.setPhoneNumber(trimToNull(row.getPhoneNumber()));
        c.setDateOfBirth(dob);
        c.setGender(trimToNull(row.getGender()));
        c.setAddress(trimToNull(row.getAddress()));
        c.setSkills(trimToNull(row.getSkills()));
        c.setExperience(trimToNull(row.getExperience()));
        c.setCertificates(trimToNull(row.getCertificates()));
        c.setDegree(trimToNull(row.getDegree()));
        c.setCvFileUrl(trimToNull(row.getCvUrl()));
        c.setDepartmentId(departmentId);
        c.setPositionId(positionId);
        c.setImportFileId(fileId);
        return c;
    }

    private String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static class RowValidationException extends Exception {
        RowValidationException(String message) { super(message); }
    }
}