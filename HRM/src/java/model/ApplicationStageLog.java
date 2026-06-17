package model;

import java.sql.Timestamp;

public class ApplicationStageLog {
    private int logId;
    private int candidateId;
    private String fromStage;
    private String toStage;
    private String result;           // PASSED / REJECTED
    private int reviewedBy;
    private String reviewedByName;   // join từ Employees
    private Timestamp reviewedAt;
    private String note;
    private String toEmail;
    private String emailSubject;
    private String emailBody;
    private String emailType;        // INTERVIEW_INVITE, REJECTION_CV
    private String emailStatus;      // PENDING, SENT, FAILED
    private Timestamp sentAt;

    public ApplicationStageLog() {}

    public ApplicationStageLog(int candidateId, String fromStage, String toStage,
                                String result, int reviewedBy, String note,
                                String toEmail, String ccEmails, String emailSubject,
                                String emailBody, String emailType) {
        this.candidateId = candidateId;
        this.fromStage = fromStage;
        this.toStage = toStage;
        this.result = result;
        this.reviewedBy = reviewedBy;
        this.note = note;
        this.toEmail = toEmail;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;
        this.emailType = emailType;
        this.emailStatus = "PENDING";
    }

    // Getters & Setters
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getFromStage() { return fromStage; }
    public void setFromStage(String fromStage) { this.fromStage = fromStage; }

    public String getToStage() { return toStage; }
    public void setToStage(String toStage) { this.toStage = toStage; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public int getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(int reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public Timestamp getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }

    public String getEmailSubject() { return emailSubject; }
    public void setEmailSubject(String emailSubject) { this.emailSubject = emailSubject; }

    public String getEmailBody() { return emailBody; }
    public void setEmailBody(String emailBody) { this.emailBody = emailBody; }

    public String getEmailType() { return emailType; }
    public void setEmailType(String emailType) { this.emailType = emailType; }

    public String getEmailStatus() { return emailStatus; }
    public void setEmailStatus(String emailStatus) { this.emailStatus = emailStatus; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
}