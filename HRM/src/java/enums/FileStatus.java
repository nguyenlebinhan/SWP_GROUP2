/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package enums;

/**
 *
 * @author ADMIN
 */
public enum FileStatus {
    FILE_STATUS_PENDING(0),
    FILE_STATUS_IMPORTED(1),
    FILE_STATUS_FAILED(2),
    FILE_STATUS_PARTIAL(3);
    
    
    private int relatedNum;

    private FileStatus(int relatedNum) {
        this.relatedNum = relatedNum;
    }

    public int getRelatedNum() {
        return relatedNum;
    }

    public void setRelatedNum(int relatedNum) {
        this.relatedNum = relatedNum;
    }
    
}
