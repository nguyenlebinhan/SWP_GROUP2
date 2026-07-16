package hrm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Kiểm thử end-to-end luồng DUYỆT ĐƠN NGHỈ PHÉP bằng Selenium.
 *
 * <p>Luồng test tự tạo dữ liệu nên không phụ thuộc đơn có sẵn:
 * <ol>
 *   <li>Nhân viên (it_emp1) đăng nhập, gửi 1 đơn nghỉ phép mới.</li>
 *   <li>Lấy mã đơn vừa tạo ở trang "Đơn của tôi".</li>
 *   <li>Manager (it_mgr) đăng nhập, mở đúng đơn đó ở "Đơn phòng ban" và bấm Duyệt.</li>
 *   <li>Kiểm tra trạng thái đơn chuyển thành "Đã duyệt".</li>
 * </ol>
 *
 * <p>Yêu cầu: ứng dụng HRM đang chạy (mặc định http://localhost:8080/HRM).
 * Có thể override qua system property, ví dụ:
 * <pre>mvn test -Dbase.url=http://localhost:8080/HRM -Dheadless=true</pre>
 */
@DisplayName("E2E - Duyệt đơn nghỉ phép")
public class LeaveApprovalE2ETest {

    private static final String BASE_URL = System.getProperty("base.url", "http://localhost:8080/HRM");

    private static final String EMP_USER = System.getProperty("emp.user", "it_emp1");
    private static final String EMP_PASS = System.getProperty("emp.pass", "123456");
    private static final String MGR_USER = System.getProperty("mgr.user", "it_mgr");
    private static final String MGR_PASS = System.getProperty("mgr.pass", "123456");

    private static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("headless", "false"));

    private ChromeDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        if (HEADLESS) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1400,1000");
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Nhân viên gửi đơn -> Manager duyệt -> Trạng thái = Đã duyệt")
    void leaveRequestGetsApprovedByManager() {
        // Dùng lý do duy nhất để có thể nhận diện đơn của lần chạy này.
        String token = "AUTO-" + System.currentTimeMillis();
        String reason = "Selenium E2E test - " + token;

        // ---- 1. Nhân viên đăng nhập & gửi đơn nghỉ phép ----
        login(EMP_USER, EMP_PASS);
        String formCode = submitLeaveRequest(reason);
        logout();

        // ---- 2. Manager đăng nhập & duyệt đúng đơn đó ----
        login(MGR_USER, MGR_PASS);
        openDeptFormDetail(formCode);
        approveOnDetailPage("Đồng ý cho nghỉ - " + token);

        // ---- 3. Kiểm tra kết quả ----
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".badge-status.status-1")));
        String status = driver.findElement(By.cssSelector(".badge-status")).getText().trim();
        assertTrue(status.contains("Đã duyệt"),
                "Trạng thái đơn phải là 'Đã duyệt' sau khi manager duyệt, nhưng nhận được: " + status);
        assertTrue(driver.getPageSource().contains(formCode),
                "Trang chi tiết phải hiển thị đúng mã đơn " + formCode);
    }

    // ---------------------------------------------------------------------
    // Các bước con
    // ---------------------------------------------------------------------

    private void login(String username, String password) {
        driver.get(BASE_URL + "/v1/auth/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("pwdInput")).sendKeys(password);
        driver.findElement(By.cssSelector("button.btn-submit")).click();
        // Sau khi login thành công sẽ rời khỏi trang login.
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/v1/auth/login")));
        assertFalse(hasErrorAlert(),
                "Đăng nhập thất bại cho tài khoản '" + username + "'.");
    }

    private void logout() {
        driver.get(BASE_URL + "/v1/auth/logout");
        wait.until(ExpectedConditions.urlContains("/v1/auth/login"));
    }

    /**
     * Điền form nghỉ phép, gửi, rồi lấy mã đơn vừa tạo từ trang "Đơn của tôi".
     * Dùng ngày trong tương lai, lệch theo phút để tránh trùng lịch giữa các lần chạy.
     */
    private String submitLeaveRequest(String reason) {
        driver.get(BASE_URL + "/v1/employee/forms/leave/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("startDate")));

        long minuteOffset = (System.currentTimeMillis() / 60000L) % 3000L;
        LocalDate day = LocalDate.now().plusDays(60 + minuteOffset);
        String iso = day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        setDateValue("startDate", iso);
        setDateValue("endDate", iso);
        WebElement reasonBox = driver.findElement(By.id("reason"));
        reasonBox.clear();
        reasonBox.sendKeys(reason);

        driver.findElement(By.id("submitBtn")).click();

        // Không được có lỗi (ví dụ hết ngày phép / trùng lịch).
        wait.until(ExpectedConditions.urlContains("/v1/employee/forms"));
        if (hasErrorAlert()) {
            fail("Gửi đơn nghỉ phép thất bại: " + errorAlertText());
        }

        // Lấy mã đơn mới nhất (dòng đầu tiên) ở danh sách đơn của tôi.
        driver.get(BASE_URL + "/v1/employee/forms/my-forms");
        WebElement firstRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("table tbody tr")));
        List<WebElement> cells = firstRow.findElements(By.tagName("td"));
        assertTrue(cells.size() >= 3, "Danh sách đơn của tôi rỗng - đơn chưa được tạo?");

        String formCode = cells.get(0).getText().trim();
        String type = cells.get(1).getText().trim();
        String statusLabel = cells.get(2).getText().trim();

        assertTrue(type.contains("Nghỉ phép"),
                "Đơn mới nhất phải là loại 'Nghỉ phép' nhưng là: " + type);
        assertTrue(statusLabel.contains("Chờ duyệt"),
                "Đơn mới tạo phải ở trạng thái 'Chờ duyệt' nhưng là: " + statusLabel);
        assertFalse(formCode.isEmpty(), "Không đọc được mã đơn vừa tạo.");
        return formCode;
    }

    /** Trên trang "Đơn phòng ban" của manager, tìm dòng có mã đơn và mở chi tiết. */
    private void openDeptFormDetail(String formCode) {
        driver.get(BASE_URL + "/v1/manager/forms/dept-forms");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody")));

        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(formCode)) {
                row.findElement(By.linkText("Xem chi tiết")).click();
                wait.until(ExpectedConditions.urlContains("/v1/manager/forms/detail"));
                return;
            }
        }
        fail("Không tìm thấy đơn '" + formCode + "' trong danh sách đơn phòng ban của manager. "
                + "Kiểm tra it_mgr có phải trưởng phòng của it_emp1 không.");
    }

    /** Trên trang chi tiết, điền ghi chú và bấm nút Duyệt. */
    private void approveOnDetailPage(String note) {
        WebElement approveBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[formaction*='/manager/forms/approve']")));
        WebElement noteBox = driver.findElement(By.id("note"));
        noteBox.clear();
        noteBox.sendKeys(note);
        approveBtn.click();
    }

    // ---------------------------------------------------------------------
    // Tiện ích
    // ---------------------------------------------------------------------

    /** Set giá trị cho input type=date và bắn sự kiện change (JS format: yyyy-MM-dd). */
    private void setDateValue(String id, String isoDate) {
        ((JavascriptExecutor) driver).executeScript(
                "const el = document.getElementById(arguments[0]);"
                        + "el.value = arguments[1];"
                        + "el.dispatchEvent(new Event('change', { bubbles: true }));",
                id, isoDate);
    }

    private boolean hasErrorAlert() {
        return !driver.findElements(By.cssSelector(".alert-danger")).isEmpty();
    }

    private String errorAlertText() {
        List<WebElement> alerts = driver.findElements(By.cssSelector(".alert-danger"));
        return alerts.isEmpty() ? "(không rõ)" : alerts.get(0).getText().trim();
    }
}
