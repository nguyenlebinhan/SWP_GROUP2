/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

/**
 *
 * @author admin
 */
public class EmployeeDetailDTO {
    private int employeeId;
    private String employeeCode;
    private int userId;
    private int departmentId;
    private int positionId;
    private String phoneNumber;
    private String skills;
    private String experience;
    private String degree;
    private int status;
    private Integer managerId;
    private String fullName;
    private String email;
    private String username;
    private String departmentName;
    private String positionName;
    private String roleName;

    public EmployeeDetailDTO() {
    }

    public EmployeeDetailDTO(int employeeId, String employeeCode, int userId, int departmentId, int positionId, String phoneNumber, String skills, String experience, String degree, int status, Integer managerId, String fullName, String email, String username, String departmentName, String positionName, String roleName) {
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.userId = userId;
        this.departmentId = departmentId;
        this.positionId = positionId;
        this.phoneNumber = phoneNumber;
        this.skills = skills;
        this.experience = experience;
        this.degree = degree;
        this.status = status;
        this.managerId = managerId;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.departmentName = departmentName;
        this.positionName = positionName;
        this.roleName = roleName;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getStatusLabel() {
        switch (status) {
            case 0: return "Không hoạt động";
            case 1: return "Đang làm việc";
            case 2: return "Đang nghỉ phép";
            default: return "Không xác định";
        }
    }
}

