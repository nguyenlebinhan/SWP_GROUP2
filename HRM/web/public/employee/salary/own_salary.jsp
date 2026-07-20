<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
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
            .salary-layout {
                display:grid;
                grid-template-columns:1fr;
                gap:20px;
                align-items:start;
            }
            .salary-section {
                border:1px solid #eef2f7;
                border-radius:8px;
                overflow:hidden;
                margin-bottom:16px;
            }
            .salary-section h6 {
                background:#f9fafb;
                border-bottom:1px solid #eef2f7;
                font-weight:800;
                margin:0;
                padding:12px 16px;
            }
            .salary-row {
                display:grid;
                grid-template-columns:1fr auto;
                gap:16px;
                padding:14px 16px;
                border-bottom:1px solid #f1f5f9;
            }
            .salary-row:last-child {
                border-bottom:0;
            }
            .salary-row strong {
                display:block;
                margin-bottom:3px;
            }
            .salary-note {
                color:#6b7280;
                font-size:13px;
                line-height:1.45;
            }
            .money {
                font-weight:800;
                white-space:nowrap;
                text-align:right;
            }
            .net-card {
                background:#fff7ed;
                border-color:#fed7aa;
            }
            .net-card .money {
                font-size:24px;
                color:#ea580c;
            }
            @media (max-width:992px) {
                .main {
                    margin-left:0;
                    padding:18px;
                }
                .summary-grid {
                    grid-template-columns:repeat(2,minmax(0,1fr));
                }
                .salary-layout {
                    grid-template-columns:1fr;
                }
            }
            @media (max-width:576px) {
                .summary-grid {
                    grid-template-columns:1fr;
                }
                .salary-row {
                    grid-template-columns:1fr;
                }
                .money {
                    text-align:left;
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
                        <label class="form-label fw-semibold">Năm</label>
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
                        <div class="summary-card"><div class="summary-label">Lương cơ bản</div><div class="summary-value blue"><fmt:formatNumber value="${p.baseSalary}" type="number" groupingUsed="true" /> VND</div></div>
                        <div class="summary-card"><div class="summary-label">Tổng thu nhập</div><div class="summary-value green"><fmt:formatNumber value="${p.grossSalary}" type="number" groupingUsed="true" /> VND</div></div>
                        <div class="summary-card"><div class="summary-label">Tổng khấu trừ</div><div class="summary-value red"><fmt:formatNumber value="${totalDeduction}" type="number" groupingUsed="true" /> VND</div></div>
                        <div class="summary-card"><div class="summary-label">Lương thực nhận</div><div class="summary-value orange"><fmt:formatNumber value="${p.netSalary}" type="number" groupingUsed="true" /> VND</div></div>
                    </div>

                    <div class="panel">
                        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
                            <div>
                                <h5 class="fw-bold mb-1"><c:out value="${payrollPreview.fullName}" /></h5>
                                <div class="text-muted small"><c:out value="${payrollPreview.employeeCode}" /> - <c:out value="${payrollPreview.departmentName}" /></div>
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
                        <h5 class="fw-bold mb-3">Tóm tắt lương</h5>
                        <div class="salary-layout mb-4">
                            <div>
                                <div class="salary-section">
                                    <h6>Thu nhập</h6>
                                    <div class="salary-row">
                                        <div><strong>Lương hợp đồng</strong><div class="salary-note">Lương cơ bản trong kỳ.</div></div>
                                        <div class="money text-success"><fmt:formatNumber value="${p.baseSalary}" type="number" groupingUsed="true" /> VND</div>
                                    </div>
                                    <div class="salary-row">
                                        <div><strong>Phụ cấp + tăng ca + thưởng</strong><div class="salary-note">Các khoản cộng thêm vào lương.</div></div>
                                        <div class="money text-success">+<fmt:formatNumber value="${p.allowance + p.overtimePay + p.bonus}" type="number" groupingUsed="true" /> VND</div>
                                    </div>
                                    <div class="salary-row bg-success-subtle">
                                        <div><strong>Tổng thu nhập</strong><div class="salary-note">Trước khi trừ bảo hiểm, thuế và ngày không làm.</div></div>
                                        <div class="money"><fmt:formatNumber value="${p.grossSalary}" type="number" groupingUsed="true" /> VND</div>
                                    </div>
                                </div>

                                <div class="salary-section">
                                    <h6>Khấu trừ</h6>
                                    <div class="salary-row">
                                        <div><strong>Bảo hiểm và các khoản trừ</strong><div class="salary-note">Trạng thái bảo hiểm: ${payrollPreview.insuranceCalculated ? 'Có tính' : 'Không tính'}.</div></div>
                                        <div class="money text-danger">-<fmt:formatNumber value="${totalDeduction - p.unpaidDeduction - p.personalIncomeTax}" type="number" groupingUsed="true" /> VND</div>
                                    </div>
                                    <div class="salary-row">
                                        <div><strong>Ngày không làm / đi muộn</strong><div class="salary-note">${payrollPreview.notWorkedDays} ngày; ${payrollPreview.lateDeductionBlocks} lượt đi muộn.</div></div>
                                        <div class="money text-danger">-<fmt:formatNumber value="${p.unpaidDeduction}" type="number" groupingUsed="true" /> VND</div>
                                    </div>
                                    <div class="salary-row">
                                        <div><strong>Thuế TNCN</strong><div class="salary-note">Thu nhập tính thuế: <fmt:formatNumber value="${taxableIncome}" type="number" groupingUsed="true" /> VND.</div></div>
                                        <div class="money text-danger">-<fmt:formatNumber value="${p.personalIncomeTax}" type="number" groupingUsed="true" /> VND</div>
                                    </div>
                                    <div class="salary-row bg-danger-subtle">
                                        <div><strong>Tổng khấu trừ</strong><div class="salary-note">Tất cả khoản trừ trong kỳ.</div></div>
                                        <div class="money">-<fmt:formatNumber value="${totalDeduction}" type="number" groupingUsed="true" /> VND</div>
                                    </div>
                                </div>
                            </div>

                            <div class="salary-section net-card">
                                <h6>Lương thực nhận</h6>
                                <div class="salary-row">
                                    <div><strong>Thực nhận trong kỳ</strong><div class="salary-note">Tổng thu nhập - tổng khấu trừ.</div></div>
                                    <div class="money"><fmt:formatNumber value="${p.netSalary}" type="number" groupingUsed="true" /> VND</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

    </body>
</html>







