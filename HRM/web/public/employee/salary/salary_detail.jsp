<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html>
    <head>
        <title>Chi tiết bảng lương - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background:#f4f6f9;
                font-family:'Segoe UI', sans-serif;
                color:#111827;
            }
            .main {
                margin-left:250px;
                padding:25px;
            }
            .panel {
                background:#fff;
                border:1px solid #eef2f7;
                border-radius:8px;
                box-shadow:0 2px 10px rgba(15,23,42,.05);
                padding:22px;
                margin-bottom:20px;
            }
            .summary-grid {
                display:grid;
                grid-template-columns:repeat(4,minmax(160px,1fr));
                gap:16px;
                margin-bottom:20px;
            }
            .summary-card {
                background:#fff;
                border:1px solid #eef2f7;
                border-radius:8px;
                padding:18px 20px;
                box-shadow:0 2px 10px rgba(15,23,42,.05);
            }
            .summary-label {
                color:#6b7280;
                font-size:13px;
                font-weight:600;
                margin-bottom:8px;
            }
            .summary-value {
                font-size:22px;
                font-weight:800;
                white-space:nowrap;
            }
            .blue {
                color:#2563eb;
            }
            .green {
                color:#16a34a;
            }
            .red {
                color:#dc2626;
            }
            .orange {
                color:#f59e0b;
            }
            .status-badge {
                display:inline-flex;
                padding:5px 10px;
                border-radius:999px;
                font-size:12px;
                font-weight:700;
            }
            .status-paid {
                background:#dcfce7;
                color:#166534;
            }
            .status-approved {
                background:#dbeafe;
                color:#1e40af;
            }
            .status-pending {
                background:#fef3c7;
                color:#92400e;
            }
            .table th {
                background:#f9fafb;
                color:#6b7280;
                font-size:12px;
                text-transform:uppercase;
            }
            @media (max-width:992px) {
                .main {
                    margin-left:0;
                    padding:18px;
                }
                .summary-grid {
                    grid-template-columns:repeat(2,minmax(0,1fr));
                }
            }
            @media (max-width:576px) {
                .summary-grid {
                    grid-template-columns:1fr;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/employeeSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/employeeTopBar.jsp">
                <jsp:param name="title" value="Chi tiết bảng lương" />
                <jsp:param name="backUrl" value="/v1/employee/salary/all" />
            </jsp:include>

            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success"><i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}</div>
                    <c:remove var="success" scope="session" />
                </c:if>
                <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}</div>
                    <c:remove var="error" scope="session" />
                </c:if>

            <c:choose>
                <c:when test="${empty payrollPreview}">
                    <div class="panel text-center py-5">
                        <i class="fa-solid fa-circle-exclamation d-block mb-3" style="font-size:32px;color:#f59e0b"></i>
                        <div class="fw-semibold text-danger">
                            <c:out value="${empty salaryError ? 'Không tìm thấy bảng lương.' : salaryError}" />
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:set var="p" value="${payrollPreview.payroll}" />
                    <c:set var="totalDeduction" value="${payrollPreview.totalDeduction}" />
                    <c:set var="familyAllowance" value="${payrollPreview.familyAllowance}" />
                    <c:set var="taxableIncome" value="${payrollPreview.taxableIncome}" />

                    <div class="panel">
                        <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                            <div>
                                <h5 class="fw-bold mb-1"><c:out value="${payrollPreview.fullName}" /></h5>
                                <div class="text-muted">
                                    <c:out value="${payrollPreview.employeeCode}" /> -
                                    <c:out value="${payrollPreview.departmentName}" /> -
                                    <c:out value="${payrollPreview.positionName}" />
                                </div>
                                <div class="small text-muted mt-1">
                                    Tháng lương:
                                    <fmt:formatDate value="${p.periodStart}" pattern="dd/MM/yyyy" /> -
                                    <fmt:formatDate value="${p.periodEnd}" pattern="dd/MM/yyyy" />
                                </div>
                            </div>
                            <div class="d-flex align-items-center gap-2 flex-wrap">
                                <c:choose>
                                    <c:when test="${p.status == 1}">
                                        <span class="status-badge status-paid">Nhân sự đã duyệt</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge status-pending">Chờ duyệt</span>
                                    </c:otherwise>
                                </c:choose>
                                                            </div>
                        </div>
                    </div>

                    <div class="summary-grid">
                        <div class="summary-card"><div class="summary-label">Lương cơ bản</div><div class="summary-value blue"><fmt:formatNumber value="${p.baseSalary}" type="number" groupingUsed="true" /> VND</div></div>
                        <div class="summary-card"><div class="summary-label">Tổng thu nhập</div><div class="summary-value green"><fmt:formatNumber value="${p.grossSalary}" type="number" groupingUsed="true" /> VND</div></div>
                        <div class="summary-card"><div class="summary-label">Tổng khấu trừ</div><div class="summary-value red"><fmt:formatNumber value="${totalDeduction}" type="number" groupingUsed="true" /> VND</div></div>
                        <div class="summary-card"><div class="summary-label">Lương thực nhận</div><div class="summary-value orange"><fmt:formatNumber value="${p.netSalary}" type="number" groupingUsed="true" /> VND</div></div>
                    </div>

                    <div class="summary-grid">
                        <div class="summary-card"><div class="summary-label">Phụ cấp</div><div class="summary-value green"><fmt:formatNumber value="${p.allowance}" type="number" groupingUsed="true" /> VND</div></div>
                        <div class="summary-card"><div class="summary-label">Tiền tăng ca</div><div class="summary-value orange"><fmt:formatNumber value="${p.overtimePay}" type="number" groupingUsed="true" /> VND</div></div>
                        <div class="summary-card"><div class="summary-label">Thưởng</div><div class="summary-value green"><fmt:formatNumber value="${p.bonus}" type="number" groupingUsed="true" /> VND</div></div>
                        <div class="summary-card"><div class="summary-label">Khấu trừ ngày không làm</div><div class="summary-value red"><fmt:formatNumber value="${p.unpaidDeduction}" type="number" groupingUsed="true" /> VND</div></div>
                    </div>

                    <div class="panel">
                        <h5 class="fw-bold mb-3">Chi tiết tính lương</h5>
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead><tr><th>Khoản mục</th><th>Diễn giải</th><th class="text-end">Số tiền</th></tr></thead>
                                <tbody>
                                    <tr class="table-light"><td colspan="3" class="fw-bold">Khoản thu nhập</td></tr>
                                    <tr>
                                        <td>Lương cơ bản</td>
                                        <td class="text-muted">Lương tháng theo hợp đồng đang hiệu lực.</td>
                                        <td class="text-end fw-bold text-success"><fmt:formatNumber value="${p.baseSalary}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr>
                                        <td>Phụ cấp</td>
                                        <td class="text-muted">Các khoản phụ cấp bổ sung.</td>
                                        <td class="text-end fw-bold text-success">+<fmt:formatNumber value="${p.allowance}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr>
                                        <td>Tiền tăng ca</td>
                                        <td class="text-muted">Tăng ca: ${payrollPreview.overtimeBlocks} lượt. Mỗi lượt ${payrollPreview.overtimeBlockMinutes} phút, tính theo hệ số <fmt:formatNumber value="${payrollPreview.overtimeWorkdayMultiplier}" type="number" maxFractionDigits="4" groupingUsed="false" /> lần lương giờ = <fmt:formatNumber value="${payrollPreview.overtimeBlockAmount}" type="number" groupingUsed="true" /> VND/lượt.</td>
                                        <td class="text-end fw-bold text-success">+<fmt:formatNumber value="${p.overtimePay}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr>
                                        <td>Thưởng</td>
                                        <td class="text-muted">Thưởng chuyên cần.</td>
                                        <td class="text-end fw-bold text-success">+<fmt:formatNumber value="${p.bonus}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr class="table-success">
                                        <td class="fw-bold">Tổng thu nhập</td>
                                        <td class="text-muted">Lương hợp đồng + phụ cấp + tiền tăng ca + thưởng.</td>
                                        <td class="text-end fw-bold"><fmt:formatNumber value="${p.grossSalary}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr class="table-light"><td colspan="3" class="fw-bold">Khoản khấu trừ</td></tr>
                                    <tr>
                                        <td>Trạng thái tính bảo hiểm</td>
                                        <td class="text-muted">Chỉ tính bảo hiểm khi số ngày không làm nhỏ hơn ${payrollPreview.insuranceNotWorkedDaysThreshold} ngày.</td>
                                        <td class="text-end fw-bold ${payrollPreview.insuranceCalculated ? 'text-success' : 'text-muted'}">${payrollPreview.insuranceCalculated ? 'Có tính' : 'Không tính'}</td>
                                    </tr>
                                    <c:forEach var="d" items="${payrollPreview.details}">
                                        <c:if test="${d.deduction and d.code ne 'PERSONAL_INCOME_TAX' and d.code ne 'UNPAID_DEDUCTION'}">
                                            <tr>
                                                <td><c:out value="${d.name}" /></td>
                                                <td class="text-muted"><c:out value="${d.note}" /></td>
                                                <td class="text-end fw-bold text-danger">-<fmt:formatNumber value="${d.amount}" type="number" groupingUsed="true" /> VND</td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                    <tr>
                                        <td>Khấu trừ ngày không làm</td>
                                        <td class="text-muted">Ngày không làm: ${payrollPreview.notWorkedDays} ngày x <fmt:formatNumber value="${payrollPreview.dailyRate}" type="number" groupingUsed="true" /> VND; khấu trừ đi muộn: ${payrollPreview.lateDeductionBlocks} lượt (${payrollPreview.lateDeductionBlockMinutes} phút/lượt) x <fmt:formatNumber value="${payrollPreview.lateDeductionBlockAmount}" type="number" groupingUsed="true" /> VND.</td>
                                        <td class="text-end fw-bold text-danger">-<fmt:formatNumber value="${p.unpaidDeduction}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr>
                                        <td>Thuế thu nhập cá nhân</td>
                                        <td class="text-muted">Thu nhập tính thuế sau bảo hiểm, khấu trừ ngày không làm và giảm trừ gia cảnh. Giảm trừ gia cảnh: <fmt:formatNumber value="${familyAllowance}" type="number" groupingUsed="true" /> VND; thu nhập tính thuế: <fmt:formatNumber value="${taxableIncome}" type="number" groupingUsed="true" /> VND.</td>
                                        <td class="text-end fw-bold text-danger">-<fmt:formatNumber value="${p.personalIncomeTax}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr class="table-danger">
                                        <td class="fw-bold">Tổng khấu trừ</td>
                                        <td class="text-muted">Bảo hiểm + khấu trừ ngày không làm + thuế thu nhập cá nhân.</td>
                                        <td class="text-end fw-bold">-<fmt:formatNumber value="${totalDeduction}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr class="table-warning">
                                        <td class="fw-bold">Lương thực nhận</td>
                                        <td class="text-muted">Tổng thu nhập - Tổng khấu trừ.</td>
                                        <td class="text-end fw-bold"><fmt:formatNumber value="${p.netSalary}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>







