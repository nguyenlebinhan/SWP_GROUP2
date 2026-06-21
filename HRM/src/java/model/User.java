/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author ADMIN
 */
public class User {
    private int userId;
    private String username;
    private String email;
    private String password;
    private String fullName;    
    private String dob;
    private String gender;
    private String address;
    private String roleId;
    private String roleName;
    private String avatar;
    private boolean isTemporaryPassword;
    private int isActive;

    public User() {
    }

    public User(int userId, String username, String email, String password, String fullName, String dob,String gender, String address, String roleName, boolean isTemporaryPassword, int isActive) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.dob = dob;
        this.gender =gender;
        this.address = address;
        this.roleName = roleName;
        this.isTemporaryPassword = isTemporaryPassword;
        this.isActive = isActive;
    }

    public User(int userId, String username, String email, String password, String fullName, String dob, String gender, String address, String roleId, String roleName, String avatar, boolean isTemporaryPassword, int isActive) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.roleId = roleId;
        this.roleName = roleName;
        this.avatar = avatar;
        this.isTemporaryPassword = isTemporaryPassword;
        this.isActive = isActive;
    }





    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean getIsTemporaryPassword() {
        return isTemporaryPassword;
    }

    public void setIsTemporaryPassword(boolean isTemporaryPassword) {
        this.isTemporaryPassword = isTemporaryPassword;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    
    
    
}

