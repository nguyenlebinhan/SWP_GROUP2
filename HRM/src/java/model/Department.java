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
    private Integer managerId;
    private Integer maxHeadCount;
    private int status;
    private String region;
    private String foundedDate;

    public Department() {}

    public Department(int departmentId, String departmentCode, String departmentName, String description, Integer managerId, Integer maxHeadCount, int status, String region, String foundedDate) {
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.description = description;
        this.managerId = managerId;
        this.maxHeadCount = maxHeadCount;
        this.status = status;
        this.region = region;
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

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Integer getMaxHeadCount() {
        return maxHeadCount;
    }

    public void setMaxHeadCount(Integer maxHeadCount) {
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

    public String getFoundedDate() {
        return foundedDate;
    }

    public void setFoundedDate(String foundedDate) {
        this.foundedDate = foundedDate;
    }


}
