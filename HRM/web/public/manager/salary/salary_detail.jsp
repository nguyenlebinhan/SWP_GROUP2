<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
        <jsp:include page="/public/components/managerSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Chi tiết bảng lương" />
                <jsp:param name="backUrl" value="/v1/manager/salary/all" />
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
                    <c:set var="totalDeduction" value="${p.insuranceDeduction + p.personalIncomeTax + p.penalty}" />
                    <c:set var="personalAllowance" value="${15500000}" />
                    <c:set var="taxableIncomeBeforeFloor" value="${p.grossSalary - p.insuranceDeduction - personalAllowance}" />
                    <c:set var="taxableIncome" value="${taxableIncomeBeforeFloor < 0 ? 0 : taxableIncomeBeforeFloor}" />

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

                                    <c:when test="${p.status == 3}"><span class="status-badge status-paid">HR đã duyệt</span></c:when>

                                    <c:when test="${p.status == 1}"><span class="status-badge status-approved">Chờ HR duyệt</span></c:when>

                                    <c:when test="${p.status == 2}"><span class="status-badge status-pending">Nhân viên báo sai</span></c:when>

                                    <c:when test="${p.status == 4}"><span class="status-badge status-pending">HR chưa duyệt</span></c:when>

                                    <c:otherwise><span class="status-badge status-pending">Chờ nhân viên xác nhận</span></c:otherwise>

                                </c:choose>
                                <c:if test="${p.status == 1 && canApprovePayroll}">
                                    <form method="post" action="${pageContext.request.contextPath}/v1/manager/salary/approve" class="m-0">
                                        <input type="hidden" name="payrollId" value="${p.payrollId}">
                                        <button type="submit" class="btn btn-success"
                                                onclick="return confirm('Xác nhận HR duyệt bảng lương này?');">
                                            <i class="fa-solid fa-check me-1"></i> Duyệt
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <div class="summary-grid">
                        <div class="summary-card"><div class="summary-label">Lương cơ bản</div><div class="summary-value blue"><fmt:formatNumber value="${p.baseSalary}" type="number" groupingUsed="true" />đ</div></div>
                        <div class="summary-card"><div class="summary-label">Tổng thu nhập</div><div class="summary-value green"><fmt:formatNumber value="${p.grossSalary}" type="number" groupingUsed="true" />đ</div></div>
                        <div class="summary-card"><div class="summary-label">Khấu trừ</div><div class="summary-value red"><fmt:formatNumber value="${totalDeduction}" type="number" groupingUsed="true" />đ</div></div>
                        <div class="summary-card"><div class="summary-label">Thực lĩnh</div><div class="summary-value orange"><fmt:formatNumber value="${p.netSalary}" type="number" groupingUsed="true" />đ</div></div>
                    </div>

                    <c:if test="${p.status == 1 && canApprovePayroll}">
                        <div class="panel">
                            <h5 class="fw-bold mb-3">HR chưa duyệt</h5>
                            <form method="post" action="${pageContext.request.contextPath}/v1/manager/salary/reject" class="row g-3">
                                <input type="hidden" name="payrollId" value="${p.payrollId}">
                                <div class="col-md-12">
                                    <label class="form-label fw-semibold">Lý do chưa duyệt <span class="text-danger">*</span></label>
                                    <textarea name="rejectNote" class="form-control" rows="3" required
                                              placeholder="Ghi r� l� do d? HR/nh�n vi�n ki?m tra l?i..."></textarea>
                                </div>
                                <div class="col-md-12">
                                    <button type="submit" class="btn btn-outline-danger"
                                            onclick="return confirm('Xác nhận chưa duyệt bảng lương này?');">
                                        <i class="fa-solid fa-xmark me-1"></i> Chưa duyệt
                                    </button>
                                </div>
                            </form>
                        </div>
                    </c:if>

                    <c:if test="${p.status == 4 && not empty p.rejectNote}">
                        <div class="panel">
                            <h5 class="fw-bold mb-2">Lý do HR chưa duyệt</h5>
                            <div class="text-danger"><c:out value="${p.rejectNote}" /></div>
                        </div>
                    </c:if>

                    <div class="panel">
                        <h5 class="fw-bold mb-3">Cách tính lương</h5>
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead><tr><th>Khoản mục</th><th>Ghi chú</th><th class="text-end">Số tiền</th></tr></thead>
                                <tbody>
                                    <tr>
                                        <td><td>Giảm trừ cá nhân</td></td>
                                        <td class="text-muted">Khoản này chỉ dùng để tính thuế TNCN.</td>
                                        <td class="text-end fw-bold text-primary"><fmt:formatNumber value="${personalAllowance}" type="number" groupingUsed="true" />đ</td>
                                    </tr>
                                    <tr>
                                        <td>Thu nhập tính thuế</td>
                                        <td class="text-muted">Nếu sau giảm trừ nhỏ hơn 0 thì tính là 0.</td>
                                        <td class="text-end fw-bold"><fmt:formatNumber value="${taxableIncome}" type="number" groupingUsed="true" />đ</td>
                                    </tr>
                                    <c:forEach var="d" items="${payrollPreview.details}">
                                        <tr>
                                            <td><c:out value="${d.name}" /></td>
                                            <td class="text-muted"><c:out value="${d.note}" /></td>
                                            <td class="text-end fw-bold ${d.deduction ? 'text-danger' : 'text-success'}">
                                                ${d.deduction ? '-' : '+'}<fmt:formatNumber value="${d.amount}" type="number" groupingUsed="true" />đ
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
