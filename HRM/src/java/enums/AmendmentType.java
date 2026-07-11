/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package enums;

/**
 *
 * @author Admin
 */
public enum AmendmentType {

    TRANSFER("Chuyển phòng ban"),
    SALARY_CHANGE("Thay đổi lương"),
    POSITION_CHANGE("Thay đổi chức danh"),
    COMBINED("Thay đổi nhiều field cùng lúc");
    
    private final String displayName;

    private AmendmentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
}
