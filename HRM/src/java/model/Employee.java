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
    private int dependentCount;
    private boolean unionMember;
    private int status;
    private Integer managerId;


    public Employee() {}

    public Employee(int employeeId, String employeeCode, int userId, int departmentId, int positionId, String phoneNumber, String skills, String experience, String degree, int status, Integer managerId) {
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

    public int getDependentCount() {
        return dependentCount;
    }

    public void setDependentCount(int dependentCount) {
        this.dependentCount = Math.max(0, dependentCount);
    }

    public boolean isUnionMember() {
        return unionMember;
    }

    public void setUnionMember(boolean unionMember) {
        this.unionMember = unionMember;
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

}
