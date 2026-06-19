/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package enums;

/**
 *
 * @author ADMIN
 */
public enum AttendanceStatus {
    PRESENT(0),
    LATE(1),
    ABSENT(2),
    UNEXCUSED(3),
    LEAVE(4),
    HOLIDAY(5),
    WEEKEND(6);

    private int relatedNum;

    private AttendanceStatus(int relatedNum) {
        this.relatedNum = relatedNum;
    }

    public int getRelatedNum() {
        return relatedNum;
    }

    public void setRelatedNum(int relatedNum) {
        this.relatedNum = relatedNum;
    }

}
