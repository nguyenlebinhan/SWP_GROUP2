package dto;

/**
 * DTO dành riêng cho Đơn Thuyên chuyển (TRANSFER)
 * và Thăng/Giáng chức (PROMOTION_DEMOTION).
 * Kế thừa FormRequestDTO và bổ sung các trường đích.
 */
public class TransferRequestDTO extends FormRequestDTO {

    private Integer targetDepartmentId;
    private Integer targetRoleId;

    // Tên hiển thị (join từ DB)
    private String targetDepartmentName;
    private String targetRoleName;

    public TransferRequestDTO() {
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


    public String getTargetDepartmentName() {
        return targetDepartmentName;
    }

    public void setTargetDepartmentName(String targetDepartmentName) {
        this.targetDepartmentName = targetDepartmentName;
    }

    public String getTargetRoleName() {
        return targetRoleName;
    }

    public void setTargetRoleName(String targetRoleName) {
        this.targetRoleName = targetRoleName;
    }


}
