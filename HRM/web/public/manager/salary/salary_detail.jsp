<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
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
            .allowance-scroll {
                max-height:260px;
                overflow-y:auto;
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
                                    <c:when test="${p.status == 2}">
                                        <span class="status-badge status-paid">Đã chốt</span>
                                    </c:when>
                                    <c:when test="${p.status == 1}">
                                        <span class="status-badge status-approved">HR đã duyệt - chờ chốt</span>
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

                    <div class="row g-3">
                    <div class="col-lg-8">
                    <div class="panel h-100">
                        <h5 class="fw-bold mb-3">Chi tiết tính lương</h5>

                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead><tr><th>Khoản mục</th><th>Công thức tính</th><th class="text-end">Số tiền</th></tr></thead>
                                <tbody>
                                    <tr class="table-light"><td colspan="3" class="fw-bold">Khoản thu nhập</td></tr>
                                    <tr>
                                        <td>Lương cơ bản</td>
                                        <td>
                                            <div>Theo hợp đồng lao động</div>
                                            <div class="small text-muted">Lương tháng đang hiệu lực</div>
                                        </td>
                                        <td class="text-end fw-bold text-success"><fmt:formatNumber value="${p.baseSalary}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr>
                                        <td>Phụ cấp</td>
                                        <td>
                                            <div>Theo bảng phụ cấp</div>
                                            <div class="small text-muted">Tổng các khoản phụ cấp</div>
                                        </td>
                                        <td class="text-end fw-bold text-success">+<fmt:formatNumber value="${p.allowance}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr>
                                        <td>Tiền tăng ca</td>
                                        <td>
                                            <div>${payrollPreview.overtimeBlocks} x <fmt:formatNumber value="${payrollPreview.overtimeWorkdayMultiplier}" type="number" maxFractionDigits="4" groupingUsed="false" /> x <fmt:formatNumber value="${payrollPreview.overtimeBaseBlockAmount}" type="number" groupingUsed="true" /></div>
                                            <div class="small text-muted">Số lượt tăng ca &times; hệ số &times; đơn giá/lượt</div>
                                        </td>
                                        <td class="text-end fw-bold text-success">+<fmt:formatNumber value="${p.overtimePay}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr>
                                        <td>Thưởng</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.bonus > 0}">
                                                    <div><fmt:formatNumber value="${p.baseSalary}" type="number" groupingUsed="true" /> x <fmt:formatNumber value="${payrollPreview.attendanceBonusRatePercent}" type="number" maxFractionDigits="2" groupingUsed="false" />%</div>
                                                    <div class="small text-muted">Lương cơ bản &times; tỷ lệ thưởng chuyên cần</div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="small text-muted">Không đủ điều kiện chuyên cần (có ngày nghỉ không phép/đi muộn trong tháng)</div>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end fw-bold text-success">+<fmt:formatNumber value="${p.bonus}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr class="table-success">
                                        <td class="fw-bold">Tổng thu nhập</td>
                                        <td>
                                            <div><fmt:formatNumber value="${p.baseSalary}" type="number" groupingUsed="true" /> + <fmt:formatNumber value="${p.allowance}" type="number" groupingUsed="true" /> + <fmt:formatNumber value="${p.overtimePay}" type="number" groupingUsed="true" /> + <fmt:formatNumber value="${p.bonus}" type="number" groupingUsed="true" /></div>
                                            <div class="small text-muted">Lương cơ bản + Phụ cấp + Tiền tăng ca + Thưởng</div>
                                        </td>
                                        <td class="text-end fw-bold"><fmt:formatNumber value="${p.grossSalary}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr class="table-light"><td colspan="3" class="fw-bold">Khoản khấu trừ</td></tr>
                                    <tr>
                                        <td>Trạng thái tính bảo hiểm</td>
                                        <td>
                                            <div>${payrollPreview.notWorkedDays}/${payrollPreview.insuranceNotWorkedDaysThreshold} ngày không làm (đã làm ${payrollPreview.standardWorkingDays - payrollPreview.notWorkedDays}/${payrollPreview.standardWorkingDays} ngày)</div>
                                            <div class="small text-muted">Số ngày không làm / ngưỡng tính bảo hiểm</div>
                                        </td>
                                        <td class="text-end fw-bold ${payrollPreview.insuranceCalculated ? 'text-success' : 'text-muted'}">${payrollPreview.insuranceCalculated ? 'Có tính' : 'Không tính'}</td>
                                    </tr>
                                    <tr>
                                        <td>Bảo hiểm / phí công đoàn</td>
                                        <td>
                                            <div><fmt:formatNumber value="${payrollPreview.contractSalary}" type="number" groupingUsed="true" /> + <fmt:formatNumber value="${payrollPreview.insuranceApplicableAllowance}" type="number" groupingUsed="true" /> = <fmt:formatNumber value="${p.insuranceSalaryBase}" type="number" groupingUsed="true" /></div>
                                            <div class="small text-muted">Lương hợp đồng + phụ cấp tính BHXH, mức trần <fmt:formatNumber value="${payrollPreview.insuranceSalaryCap}" type="number" groupingUsed="true" /></div>
                                        </td>
                                        <td class="text-end fw-bold text-danger">-<fmt:formatNumber value="${p.insuranceDeduction}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr>
                                        <td>Khấu trừ ngày không làm</td>
                                        <td>
                                            <div>${payrollPreview.notWorkedDays} x <fmt:formatNumber value="${payrollPreview.dailyRate}" type="number" groupingUsed="true" /> + ${payrollPreview.lateDeductionBlocks} x <fmt:formatNumber value="${payrollPreview.lateDeductionBlockAmount}" type="number" groupingUsed="true" /></div>
                                            <div class="small text-muted">Ngày không làm &times; đơn giá ngày + Số lượt đi muộn &times; đơn giá/lượt</div>
                                        </td>
                                        <td class="text-end fw-bold text-danger">-<fmt:formatNumber value="${p.unpaidDeduction}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr>
                                        <td>Thuế thu nhập cá nhân</td>
                                        <td>
                                            <div><c:out value="${payrollPreview.personalIncomeTaxFormula}" /></div>
                                            <div class="small text-muted">Thu nhập tính thuế <fmt:formatNumber value="${taxableIncome}" type="number" groupingUsed="true" /> (giảm trừ gia cảnh <fmt:formatNumber value="${familyAllowance}" type="number" groupingUsed="true" />, ${payrollPreview.dependentCount} người phụ thuộc)</div>
                                        </td>
                                        <td class="text-end fw-bold text-danger">-<fmt:formatNumber value="${p.personalIncomeTax}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr class="table-danger">
                                        <td class="fw-bold">Tổng khấu trừ</td>
                                        <td>
                                            <div><fmt:formatNumber value="${p.insuranceDeduction}" type="number" groupingUsed="true" /> + <fmt:formatNumber value="${p.unpaidDeduction}" type="number" groupingUsed="true" /> + <fmt:formatNumber value="${p.personalIncomeTax}" type="number" groupingUsed="true" /></div>
                                            <div class="small text-muted">Bảo hiểm/phí công đoàn + Khấu trừ ngày không làm + Thuế TNCN</div>
                                        </td>
                                        <td class="text-end fw-bold">-<fmt:formatNumber value="${totalDeduction}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                    <tr class="table-warning">
                                        <td class="fw-bold">Lương thực nhận</td>
                                        <td>
                                            <div><fmt:formatNumber value="${p.grossSalary}" type="number" groupingUsed="true" /> - <fmt:formatNumber value="${totalDeduction}" type="number" groupingUsed="true" /></div>
                                            <div class="small text-muted">Tổng thu nhập - Tổng khấu trừ</div>
                                        </td>
                                        <td class="text-end fw-bold"><fmt:formatNumber value="${p.netSalary}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    </div>
                    <div class="col-lg-4">
                    <div class="panel">
                        <h5 class="fw-bold mb-3">Bảo hiểm</h5>
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead><tr><th>Loại bảo hiểm</th><th class="text-end">Số tiền</th></tr></thead>
                                <tbody>
                                    <c:forEach var="d" items="${payrollPreview.details}">
                                        <c:if test="${d.deduction and d.code ne 'PERSONAL_INCOME_TAX' and d.code ne 'UNPAID_DEDUCTION'}">
                                            <tr>
                                                <td>
                                                    <div><c:out value="${d.name}" /></div>
                                                    <div class="small text-muted">
                                                        <fmt:formatNumber value="${d.base}" type="number" groupingUsed="true" /> x <fmt:formatNumber value="${d.employeeRatePercent}" type="number" maxFractionDigits="2" groupingUsed="false" />%
                                                    </div>
                                                </td>
                                                <td class="text-end fw-bold text-danger">-<fmt:formatNumber value="${d.amount}" type="number" groupingUsed="true" /> VND</td>
                                            </tr>
                                        </c:if>
                                    </c:forEach>
                                    <tr class="fw-bold">
                                        <td>Tổng bảo hiểm</td>
                                        <td class="text-end text-danger">-<fmt:formatNumber value="${p.insuranceDeduction}" type="number" groupingUsed="true" /> VND</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="panel mt-3">
                        <h5 class="fw-bold mb-3">Phụ cấp</h5>
                        <div class="table-responsive allowance-scroll">
                            <table class="table align-middle">
                                <thead><tr><th>Tên phụ cấp</th><th class="text-end">Số tiền</th></tr></thead>
                                <tbody>
                                    <c:set var="totalAllowanceTypes" value="${0}" />
                                    <c:forEach var="a" items="${allowanceTypes}">
                                        <c:set var="totalAllowanceTypes" value="${totalAllowanceTypes + a.amount}" />
                                        <tr>
                                            <td>
                                                <c:out value="${a.allowanceName}" />
                                                <c:if test="${a.insuranceApplicable}">
                                                    <span class="text-danger fw-bold">*</span>
                                                </c:if>
                                            </td>
                                            <td class="text-end"><fmt:formatNumber value="${a.amount}" type="number" groupingUsed="true" /> VND</td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty allowanceTypes}">
                                        <tr><td colspan="2" class="text-muted text-center">Không có phụ cấp nào.</td></tr>
                                    </c:if>
                                    <c:if test="${not empty allowanceTypes}">
                                        <tr class="fw-bold">
                                            <td>Tổng phụ cấp</td>
                                            <td class="text-end"><fmt:formatNumber value="${totalAllowanceTypes}" type="number" groupingUsed="true" /> VND</td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                        <div class="small text-muted mt-2">
                            <span class="text-danger fw-bold">*</span> Khoản tiền được tính vào lương làm căn cứ đóng bảo hiểm xã hội.
                        </div>
                    </div>
                    </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>







