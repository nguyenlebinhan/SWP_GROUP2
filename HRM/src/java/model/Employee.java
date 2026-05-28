/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;

/**
 *
 * @author admin
 */
public class Employee {
    private int employeeId;
    private String employeeCode;
    private int userId;
    private int departmentId;
    private int positionId;
    private String phoneNumber;
    private String skills;
    private String experience;
    private String degree;
    private LocalDate hireDate;
    private LocalDate probationEndDate;
    private int status;
    private Integer managerId;
    private String nationalId;
    private String contractType;
    
    public Employee() {
    }
    
    public Employee(int employeeId, String employeeCode, int userId, int departmentId, int positionId, String phoneNumber, String skills, String experience, String degree, LocalDate hireDate, LocalDate probationEndDate, int status, Integer managerId, String nationalId, String contractType) {
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.userId = userId;
        this.departmentId = departmentId;
        this.positionId = positionId;
        this.phoneNumber = phoneNumber;
        this.skills = skills;
        this.experience = experience;
        this.degree = degree;
        this.hireDate = hireDate;
        this.probationEndDate = probationEndDate;
        this.status = status;
        this.managerId = managerId;
        this.nationalId = nationalId;
        this.contractType = contractType;
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

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDate getProbationEndDate() {
        return probationEndDate;
    }

    public void setProbationEndDate(LocalDate probationEndDate) {
        this.probationEndDate = probationEndDate;
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

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }
    
    
}
