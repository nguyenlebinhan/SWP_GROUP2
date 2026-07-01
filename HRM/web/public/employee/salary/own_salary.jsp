<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Lương của tôi - HRM</title>
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
            .period-pill {
                display:inline-flex;
                align-items:center;
                gap:8px;
                background:#eaf2ff;
                color:#1d4ed8;
                border-radius:8px;
                padding:9px 14px;
                font-weight:700;
                font-size:14px;
                margin-bottom:18px;
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
            .table th {
                background:#f9fafb;
                color:#6b7280;
                font-size:12px;
                text-transform:uppercase;
                border-bottom:1px solid #e5e7eb;
            }
            .table td {
                vertical-align:middle;
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
                <jsp:param name="title" value="Lương của tôi" />
                <jsp:param name="backUrl" value="/v1/employee/dashboard" />
            </jsp:include>

            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success"><i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}</div>
                    <c:remove var="success" scope="session" />
                </c:if>
                <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}</div>
                    <c:remove var="error" scope="session" />
                </c:if>

            <div class="period-pill"><i class="fa-solid fa-calendar-days"></i> Tháng lương: Tháng ${selectedMonth}/${selectedYear}</div>

            <div class="panel">
                <form method="get" action="${pageContext.request.contextPath}/v1/employee/salary/own" class="row g-3 align-items-end">
                    <div class="col-md-2">
                        <label class="form-label fw-semibold">Tháng</label>
                        <select name="month" class="form-select">
                            <c:forEach var="m" begin="1" end="12">
                                <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label fw-semibold">Nam</label>
                        <input type="number" name="year" min="2000" max="2100" class="form-control" value="${selectedYear}">
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary w-100"><i class="fa-solid fa-filter me-1"></i> Lọc</button>
                    </div>
                </form>
            </div>

            <c:choose>
                <c:when test="${empty payrollPreview}">
                    <div class="panel text-center py-5">
                        <i class="fa-solid fa-circle-exclamation d-block mb-3" style="font-size:32px;color:#f59e0b"></i>
                        <div class="fw-semibold text-danger">
                            <c:out value="${empty salaryError ? 'Chưa có dữ liệu lương cho tháng này.' : salaryError}" />
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:set var="p" value="${payrollPreview.payroll}" />
                    <c:set var="totalDeduction" value="${payrollPreview.totalDeduction}" />
                    <c:set var="familyAllowance" value="${payrollPreview.familyAllowance}" />
                    <c:set var="taxableIncome" value="${payrollPreview.taxableIncome}" />
                    <div class="summary-grid">
                        <div class="summary-card"><div class="summary-label">Lương cơ bản</div><div class="summary-value blue"><fmt:formatNumber value="${p.baseSalary}" type="number" groupingUsed="true" />đ</div></div>
                        <div class="summary-card"><div class="summary-label">Tổng thu nhập</div><div class="summary-value green"><fmt:formatNumber value="${p.grossSalary}" type="number" groupingUsed="true" />đ</div></div>
                        <div class="summary-card"><div class="summary-label">Khấu trừ</div><div class="summary-value red"><fmt:formatNumber value="${totalDeduction}" type="number" groupingUsed="true" />đ</div></div>
                        <div class="summary-card"><div class="summary-label">Thực lĩnh</div><div class="summary-value orange"><fmt:formatNumber value="${p.netSalary}" type="number" groupingUsed="true" />đ</div></div>
                    </div>

                    <div class="summary-grid">
                        <div class="summary-card"><div class="summary-label">Lương hợp đồng</div><div class="summary-value blue"><fmt:formatNumber value="${payrollPreview.contractSalary}" type="number" groupingUsed="true" />đ</div></div>
                        <div class="summary-card"><div class="summary-label">Ngày công</div><div class="summary-value green">${p.workingDays}/${payrollPreview.standardWorkingDays}</div></div>
                        <div class="summary-card"><div class="summary-label">OT được tính</div><div class="summary-value orange"><fmt:formatNumber value="${payrollPreview.overtimeHours}" type="number" maxFractionDigits="2" />h</div></div>
                        <div class="summary-card"><div class="summary-label">Block phạt muộn</div><div class="summary-value red">${payrollPreview.latePenaltyBlocks} block</div></div>
                    </div>

                    <div class="panel">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <div>
                                <h5 class="fw-bold mb-1"><c:out value="${payrollPreview.fullName}" /></h5>
                                <div class="text-muted small"><c:out value="${payrollPreview.employeeCode}" /> - <c:out value="${payrollPreview.departmentName}" /></div>
                            </div>
                            <div class="d-flex align-items-center gap-2 flex-wrap">
                                <c:choose>
                                    <c:when test="${p.status == 1}">
                                        <span class="status-badge status-paid">HR đã duyệt</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge status-pending">Chờ duyệt</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead><tr><th>Khoản mục</th><th>Ghi chú</th><th class="text-end">Số tiền</th></tr></thead>
                                <tbody>
                                    <tr>
                                        <td>Giam tru gia canh</td>
                                        <td class="text-muted">Muc goc ca nhan trong config payroll + so nguoi phu thuoc x muc giam tru phu thuoc.</td>
                                        <td class="text-end fw-bold text-primary">
                                            <fmt:formatNumber value="${familyAllowance}" type="number" groupingUsed="true" />đ
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Thu nhập tính thuế</td>
                                        <td class="text-muted">Nếu sau giảm trừ nhỏ hơn 0 thì tính là 0.</td>
                                        <td class="text-end fw-bold">
                                            <fmt:formatNumber value="${taxableIncome}" type="number" groupingUsed="true" />đ
                                        </td>
                                    </tr>
                                    <c:forEach var="d" items="${payrollPreview.details}">
                                        <tr>
                                            <td><c:out value="${d.name}" /></td>
                                            <td class="text-muted"><c:out value="${d.note}" /></td>
                                            <td class="text-end fw-bold ${d.info ? 'text-muted' : (d.companyCost ? 'text-primary' : (d.deduction ? 'text-danger' : 'text-success'))}">
                                                <c:choose>
                                                    <c:when test="${d.info}">Thông tin</c:when>
                                                    <c:when test="${d.companyCost}"><fmt:formatNumber value="${d.amount}" type="number" groupingUsed="true" />đ</c:when>
                                                    <c:otherwise>${d.deduction ? '-' : '+'}<fmt:formatNumber value="${d.amount}" type="number" groupingUsed="true" />đ</c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
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



