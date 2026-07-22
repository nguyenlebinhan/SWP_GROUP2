package service;

import dto.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import model.*;
import dao.*;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.lowagie.text.DocumentException;
import enums.ContractType;
import jakarta.servlet.http.HttpServletResponse;

public class ContractPdfService {

    private final EmploymentContractDAO contractDAO;
    private final EmployeeDAO employeeDAO;
    private final UserDAO userDAO;

    private static final String COMPANY_NAME = "CÔNG TY CỔ PHẦN ...";
    private static final String COMPANY_ADDRESS = "...";
    private static final String COMPANY_PHONE = "...";
    private static final String COMPANY_EMAIL = "...";
    private static final String TAX_CODE = "...";
    private static final String REPRESENTATIVE = "Giám đốc";
    private static final String REPRESENTATIVE_TITLE = "Giám đốc";

    public ContractPdfService() {
        this.contractDAO = new EmploymentContractDAO();
        this.employeeDAO = new EmployeeDAO();
        this.userDAO = new UserDAO();
    }

    public ContractTemplateData buildData(EmploymentContract contract) {
        ContractTemplateData data = new ContractTemplateData();

        data.setCompanyName(COMPANY_NAME);
        data.setCompanyAddress(COMPANY_ADDRESS);
        data.setCompanyPhone(COMPANY_PHONE);
        data.setCompanyEmail(COMPANY_EMAIL);
        data.setTaxCode(TAX_CODE);
        data.setRepresentative(REPRESENTATIVE);
        data.setRepresentativeTitle(REPRESENTATIVE_TITLE);

        EmployeeDetailDTO emp = employeeDAO.getEmployeeById(contract.getEmployeeId());
        if (emp != null) {
            data.setEmployeeCode(emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "");
            User user = userDAO.getUserById(emp.getUserId());
            if (user != null) {
                data.setEmployeeFullName(user.getFullName() != null ? user.getFullName() : "");
                data.setDateOfBirth(user.getDob() != null ? new SimpleDateFormat("dd/MM/yyyy").format(user.getDob()) : "");
                data.setGender(user.getGender() != null ? user.getGender() : "");
                data.setAddress(user.getAddress() != null ? user.getAddress() : "");
            }
        }

        data.setContractCode(contract.getContractCode() != null ? contract.getContractCode() : "");
        data.setContractType(contract.getContractType() != null ? contract.getContractType().getDisplayName() : "");
        data.setDepartmentName(contract.getDepartmentName() != null ? contract.getDepartmentName() : "");
        data.setPositionName(contract.getPositionName() != null ? contract.getPositionName() : "");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        data.setSignedDate(contract.getSignedDate() != null ? sdf.format(contract.getSignedDate()) : "");
        data.setEffectiveDate(contract.getEffectiveDate() != null ? sdf.format(contract.getEffectiveDate()) : "");
        data.setEndDate(contract.getEndDate() != null ? sdf.format(contract.getEndDate()) : "");

        if (contract.getSalary() != null) {
            DecimalFormat df = new DecimalFormat("#,##0");
            data.setSalary(df.format(contract.getSalary()));
        } else {
            data.setSalary("0");
        }

        return data;
    }

    public void generatePdf(int contractId, String templatePath, HttpServletResponse response, boolean inline)
            throws IOException, DocumentException {
        EmploymentContract contract = contractDAO.getContractById(contractId);
        if (contract == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Contract not found");
            return;
        }

        ContractTemplateData data = buildData(contract);
        String template = java.nio.file.Files.readString(java.nio.file.Paths.get(templatePath));
        String html = replacePlaceholders(template, data);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                (inline ? "inline" : "attachment") + "; filename=\"contract-" + contract.getContractCode() + ".pdf\"");
        renderHtmlToPdf(html, response.getOutputStream());
    }

    private void renderHtmlToPdf(String html, OutputStream outputStream) throws IOException, DocumentException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);
    }

    private String replacePlaceholders(String template, ContractTemplateData data) {
        template = template.replace("{{companyName}}", data.getCompanyName());
        template = template.replace("{{companyAddress}}", data.getCompanyAddress());
        template = template.replace("{{companyPhone}}", data.getCompanyPhone());
        template = template.replace("{{companyEmail}}", data.getCompanyEmail());
        template = template.replace("{{taxCode}}", data.getTaxCode());
        template = template.replace("{{representative}}", data.getRepresentative());
        template = template.replace("{{representativeTitle}}", data.getRepresentativeTitle());
        template = template.replace("{{employeeCode}}", data.getEmployeeCode());
        template = template.replace("{{employeeFullName}}", data.getEmployeeFullName());
        template = template.replace("{{dateOfBirth}}", data.getDateOfBirth());
        template = template.replace("{{gender}}", data.getGender());
        template = template.replace("{{address}}", data.getAddress());
        template = template.replace("{{contractCode}}", data.getContractCode());
        template = template.replace("{{contractType}}", data.getContractType());
        template = template.replace("{{departmentName}}", data.getDepartmentName());
        template = template.replace("{{positionName}}", data.getPositionName());
        template = template.replace("{{signedDate}}", data.getSignedDate());
        template = template.replace("{{effectiveDate}}", data.getEffectiveDate());
        template = template.replace("{{endDate}}", data.getEndDate());
        template = template.replace("{{salary}}", data.getSalary());
        return template;
    }

    public void generateBlankPdf(String templatePath, HttpServletResponse response) throws IOException, DocumentException {
        String template = java.nio.file.Files.readString(java.nio.file.Paths.get(templatePath));
        String html = replaceBlankPlaceholders(template);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"contract_template.pdf\"");
        renderHtmlToPdf(html, response.getOutputStream());
    }

    private String replaceBlankPlaceholders(String template) {
        String blank = "........................";
        template = template.replace("{{employeeCode}}", blank);
        template = template.replace("{{employeeFullName}}", blank);
        template = template.replace("{{dateOfBirth}}", blank);
        template = template.replace("{{gender}}", blank);
        template = template.replace("{{address}}", blank);
        template = template.replace("{{contractCode}}", blank);
        template = template.replace("{{contractType}}", blank);
        template = template.replace("{{departmentName}}", blank);
        template = template.replace("{{positionName}}", blank);
        template = template.replace("{{signedDate}}", blank);
        template = template.replace("{{effectiveDate}}", blank);
        template = template.replace("{{endDate}}", blank);
        template = template.replace("{{salary}}", blank);
        return template;
    }
}
