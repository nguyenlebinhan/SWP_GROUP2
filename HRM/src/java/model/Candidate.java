package model;

import java.sql.Timestamp;
import java.util.Date;

public class Candidate {
    private int candidateId;
    private String candidateCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Date dateOfBirth;
    private String gender;
    private String address;
    private String skills;
    private String experience;
    private String certificates;
    private String degree;
    private String cvFileUrl;
    private int departmentId;
    private String departmentName;   // join từ Departments
    private int positionId;
    private String positionName;     // join từ Positions
    private Integer importFileId;
    private String stage;            // APPLIED, INTERVIEW, PROBATION, REJECTED
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Candidate() {}

    public Candidate(int candidateId, String candidateCode, String fullName, String email,
                     String phoneNumber, Date dateOfBirth, String gender, String address,
                     String skills, String experience, String certificates, String degree,
                     String cvFileUrl, int departmentId, String departmentName,
                     int positionId, String positionName, Integer importFileId,
                     String stage, Timestamp createdAt, Timestamp updatedAt) {
        this.candidateId = candidateId;
        this.candidateCode = candidateCode;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.skills = skills;
        this.experience = experience;
        this.certificates = certificates;
        this.degree = degree;
        this.cvFileUrl = cvFileUrl;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.positionId = positionId;
        this.positionName = positionName;
        this.importFileId = importFileId;
        this.stage = stage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters & Setters
    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getCandidateCode() { return candidateCode; }
    public void setCandidateCode(String candidateCode) { this.candidateCode = candidateCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getCertificates() { return certificates; }
    public void setCertificates(String certificates) { this.certificates = certificates; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getCvFileUrl() { return cvFileUrl; }
    public void setCvFileUrl(String cvFileUrl) { this.cvFileUrl = cvFileUrl; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public int getPositionId() { return positionId; }
    public void setPositionId(int positionId) { this.positionId = positionId; }

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }

    public Integer getImportFileId() { return importFileId; }
    public void setImportFileId(Integer importFileId) { this.importFileId = importFileId; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}