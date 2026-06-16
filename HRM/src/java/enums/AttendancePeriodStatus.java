/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package enums;

/**
 *
 * @author ADMIN
 */
public enum AttendancePeriodStatus {
    STATUS_PRIVATE(0),
    STATUS_PUBLIC(1);
    
    
    private int relatedNum;

    private AttendancePeriodStatus(int relatedNum) {
        this.relatedNum = relatedNum;
    }

    public int getRelatedNum() {
        return relatedNum;
    }

    public void setRelatedNum(int relatedNum) {
        this.relatedNum = relatedNum;
    }

}
