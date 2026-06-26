package model;

/**
 * Model dành riêng cho Đơn Thuyên chuyển phòng ban (TRANSFER)
 * và Thăng/Giáng chức (PROMOTION_DEMOTION).
 * Kế thừa FormRequest và bổ sung các trường đích.
 */
public class TransferFormRequest extends FormRequest {

    private Integer targetDepartmentId;
    private Integer targetRoleId;

    public TransferFormRequest() {
    }

    public Integer getTargetDepartmentId() {
        return targetDepartmentId;
    }

    public void setTargetDepartmentId(Integer targetDepartmentId) {
        this.targetDepartmentId = targetDepartmentId;
    }

    public Integer getTargetRoleId() {
        return targetRoleId;
    }

    public void setTargetRoleId(Integer targetRoleId) {
        this.targetRoleId = targetRoleId;
    }


}