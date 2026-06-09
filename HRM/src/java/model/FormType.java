package model;

public class FormType {
    private int formTypeId;
    private String formTypeCode;
    private String formTypeName;

    public FormType() {
    }

    public FormType(int formTypeId, String formTypeCode, String formTypeName) {
        this.formTypeId = formTypeId;
        this.formTypeCode = formTypeCode;
        this.formTypeName = formTypeName;
    }

    public int getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }

    public String getFormTypeCode() {
        return formTypeCode;
    }

    public void setFormTypeCode(String formTypeCode) {
        this.formTypeCode = formTypeCode;
    }

    public String getFormTypeName() {
        return formTypeName;
    }

    public void setFormTypeName(String formTypeName) {
        this.formTypeName = formTypeName;
    }

    
}
