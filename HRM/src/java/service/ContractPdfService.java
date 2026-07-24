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
import com.lowagie.text.pdf.BaseFont;
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

    private void renderHtmlToPdf(String html, OutputStream outputStream) throws IOException, DocumentException {
        ITextRenderer renderer = new ITextRenderer();

        renderer.getFontResolver().addFont(
                "C:/Windows/Fonts/times.ttf",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
        );
        renderer.getFontResolver().addFont(
                "C:/Windows/Fonts/timesbd.ttf",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
        );
        renderer.getFontResolver().addFont(
                "C:/Windows/Fonts/timesi.ttf",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
        );

        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);

    }

    public void generateBlankPdf(String templatePath, HttpServletResponse response) throws IOException, DocumentException {
        String template = java.nio.file.Files.readString(java.nio.file.Paths.get(templatePath));

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"contract_template.pdf\"");
        renderHtmlToPdf(template, response.getOutputStream());
    }
}
