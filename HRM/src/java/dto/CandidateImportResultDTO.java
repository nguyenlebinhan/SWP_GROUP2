package dto;

import java.util.ArrayList;
import java.util.List;

public class CandidateImportResultDTO {
    private int fileId;
    private int totalRows;
    private int importedRows;
    private int failedRows;
    private int status;
    private String note;
    private String fileName;
    private List<String> errors = new ArrayList<>();

    public void addError(int row, String name, String message) {
        errors.add("Dòng " + row + (name != null ? " [" + name + "]" : "") + ": " + message);
    }

    // Getters & Setters
    public int getFileId() { return fileId; }
    public void setFileId(int fileId) { this.fileId = fileId; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getImportedRows() { return importedRows; }
    public void setImportedRows(int importedRows) { this.importedRows = importedRows; }

    public int getFailedRows() { return failedRows; }
    public void setFailedRows(int failedRows) { this.failedRows = failedRows; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public List<String> getErrors() { return errors; }
}