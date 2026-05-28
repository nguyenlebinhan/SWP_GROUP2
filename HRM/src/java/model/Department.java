/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to edit this template
 */
package model;

import java.time.LocalDate;

/**
 *
 * @author admin
 */
public class Department {
    private int departmentId;
    private String departmentCode;
    private String departmentName;
    private String description;
    private int managerId;
    private int maxHeadCount;
    private int status;
    private String region;
    private double budget;
    private LocalDate foundedDate;

    public Department() {
    }

    public Department(int departmentId, String departmentCode, String departmentName, String description, int managerId, int maxHeadCount, int status, String region, double budget, LocalDate foundedDate) {
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.description = description;
        this.managerId = managerId;
        this.maxHeadCount = maxHeadCount;
        this.status = status;
        this.region = region;
        this.budget = budget;
        this.foundedDate = foundedDate;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public int getMaxHeadCount() {
        return maxHeadCount;
    }

    public void setMaxHeadCount(int maxHeadCount) {
        this.maxHeadCount = maxHeadCount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public LocalDate getFoundedDate() {
        return foundedDate;
    }

    public void setFoundedDate(LocalDate foundedDate) {
        this.foundedDate = foundedDate;
    }

    

}
