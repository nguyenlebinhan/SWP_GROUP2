package dto;

import java.sql.Date;

/**
 * DTO dành riêng cho Đơn Người Phụ Thuộc (formTypeCode = "DEPENDENT").
 * Kế thừa toàn bộ trường chung từ FormRequestDTO và bổ sung
 * thông tin người phụ thuộc.
 */
public class DependentFormRequestDTO extends FormRequestDTO {

    private String dependentName;
    private String dependentRelationship;
    private java.sql.Date dependentDob;
    private String dependentTaxCode;

    public String getDependentName() {
        return dependentName;
    }

    public void setDependentName(String dependentName) {
        this.dependentName = dependentName;
    }

    public String getDependentRelationship() {
        return dependentRelationship;
    }

    public void setDependentRelationship(String dependentRelationship) {
        this.dependentRelationship = dependentRelationship;
    }

    public java.sql.Date getDependentDob() {
        return dependentDob;
    }

    public void setDependentDob(java.sql.Date dependentDob) {
        this.dependentDob = dependentDob;
    }

    public String getDependentTaxCode() {
        return dependentTaxCode;
    }

    public void setDependentTaxCode(String dependentTaxCode) {
        this.dependentTaxCode = dependentTaxCode;
    }
}
