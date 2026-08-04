package dto;

import java.sql.Date;

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

    public Date getDependentDob() {
        return dependentDob;
    }

    public void setDependentDob(Date dependentDob) {
        this.dependentDob = dependentDob;
    }

    public String getDependentTaxCode() {
        return dependentTaxCode;
    }

    public void setDependentTaxCode(String dependentTaxCode) {
        this.dependentTaxCode = dependentTaxCode;
    }
}
