<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html>
<head>
    <title>Bảng lương - Business Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background:#f4f6f9; font-family:'Segoe UI',sans-serif; color:#111827; }
        .main { margin-left:250px; padding:25px; }
        .toolbar { display:flex; justify-content:space-between; gap:16px; align-items:center; flex-wrap:wrap; margin-bottom:22px; }
        .period-pill { display:inline-flex; align-items:center; gap:8px; background:#eaf2ff; color:#1d4ed8; border-radius:8px; padding:9px 14px; font-weight:700; font-size:14px; }
        .btn-blue { background:#2563eb; color:#fff; border:0; border-radius:7px; padding:9px 14px; font-size:14px; font-weight:700; text-decoration:none; display:inline-flex; align-items:center; gap:6px; }
        .btn-blue:hover { background:#1d4ed8; color:#fff; }
        .filter-card { background:#fff; border:1px solid #eef2f7; border-radius:8px; box-shadow:0 2px 10px rgba(15,23,42,.05); padding:18px; margin-bottom:20px; }
        .stat-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); gap:16px; margin-bottom:20px; }
        .stat-card { background:#fff; border:1px solid #eef2f7; border-radius:8px; padding:18px 20px; box-shadow:0 2px 10px rgba(15,23,42,.05); }
        .stat-label { color:#6b7280; font-size:13px; font-weight:600; margin-bottom:8px; }
        .stat-value { font-size:25px; font-weight:800; line-height:1.1; }
        .stat-blue { color:#2563eb; } .stat-green { color:#16a34a; } .stat-orange { color:#f59e0b; } .stat-purple { color:#7c3aed; }
        .table-card { background:#fff; border:1px solid #eef2f7; border-radius:8px; box-shadow:0 2px 10px rgba(15,23,42,.05); padding:0; overflow:hidden; }
        .table-head { display:flex; justify-content:space-between; align-items:center; padding:20px 22px; border-bottom:1px solid #eef2f7; }
        .table-head h5 { margin:0; font-size:18px; font-weight:800; }
        .table { margin-bottom:0; }
        .table th { background:#f9fafb; color:#6b7280; font-size:12px; text-transform:uppercase; border-bottom:1px solid #e5e7eb; padding:13px 16px; white-space:nowrap; }
        .table td { padding:15px 16px; vertical-align:middle; font-size:14px; border-bottom:1px solid #f1f5f9; }
        .employee-name { font-weight:700; color:#111827; }
        .employee-code { display:block; color:#6b7280; font-size:12px; margin-top:2px; }
        .money { font-weight:700; white-space:nowrap; }
        .money-green { color:#16a34a; } .money-red { color:#dc2626; } .money-net { color:#111827; font-weight:800; }
        .status-badge { display:inline-flex; align-items:center; min-width:92px; justify-content:center; padding:5px 10px; border-radius:999px; font-size:12px; font-weight:700; }
        .status-paid { background:#dcfce7; color:#166534; }
        .status-pending { background:#fef3c7; color:#92400e; }
        .empty-state { text-align:center; color:#6b7280; padding:42px 12px; }
        .table-footer { display:flex; justify-content:space-between; align-items:center; gap:12px; padding:15px 20px; border-top:1px solid #eef2f7; color:#6b7280; font-size:13px; }
        .read-only-badge { background:#f3f4f6; color:#6b7280; border:1px solid #e5e7eb; border-radius:6px; padding:6px 12px; font-size:13px; font-weight:600; display:inline-flex; align-items:center; gap:6px; }
        @media (max-width:992px) { .main { margin-left:0; padding:18px; } }
    </style>
</head>
<body>

<jsp:include page="/public/components/businessAdminSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/businessAdminTopBar.jsp">
        <jsp:param name="title" value="Bảng lương" />
        <jsp:param name="backUrl" value="/v1/businessadmin/dashboard" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success"><i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}</div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger"><i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}</div>
        <c:remove var="error" scope="session" />
    </c:if>

    <c:set var="totalEmployees" value="${0}" />
    <c:set var="totalCompanyInsurance" value="${0}" />
    <c:set var="totalEmployeeInsurance" value="${0}" />
    <c:set var="totalNetSalary" value="${0}" />
    <c:set var="totalCompanyCost" value="${0}" />
    <c:forEach var="row" items="${payrollPreviews}">
        <c:if test="${not row.generationBlocked}">
            <c:set var="totalEmployees" value="${totalEmployees + 1}" />
            <c:set var="totalEmployeeInsurance" value="${totalEmployeeInsurance + row.payroll.insuranceDeduction}" />
            <c:set var="totalNetSalary" value="${totalNetSalary + row.payroll.netSalary}" />
            <c:set var="totalCompanyCost" value="${totalCompanyCost + row.payroll.netSalary + row.payroll.insuranceDeduction + row.payroll.personalIncomeTax}" />
            <c:forEach var="detail" items="${row.details}">
                <c:if test="${detail.companyCost}">
                    <c:set var="totalCompanyInsurance" value="${totalCompanyInsurance + detail.amount}" />
                    <c:set var="totalCompanyCost" value="${totalCompanyCost + detail.amount}" />
                </c:if>
            </c:forEach>
        </c:if>
    </c:forEach>

    <div class="toolbar">
        <div class="period-pill">
            <i class="fa-solid fa-calendar-days"></i>
            Tháng lương: Tháng ${selectedMonth}/${selectedYear}
        </div>
        <div class="d-flex align-items-center gap-3 flex-wrap">
            <%-- Badge chỉ-xem (Business Admin không thao tác) --%>
            <span class="read-only-badge">
                <i class="fa-solid fa-eye"></i> Chỉ xem
            </span>
            <%-- Nút xuất Excel — chỉ hiển thị khi bảng chấm công đã chốt --%>
            <c:if test="${canExportPayroll}">
                <a class="btn-blue"
                   href="${pageContext.request.contextPath}/v1/businessadmin/salary/export?month=${selectedMonth}&year=${selectedYear}<c:if test='${selectedDepartmentId != null}'>&departmentId=${selectedDepartmentId}</c:if>">
                    <i class="fa-solid fa-file-export"></i> Xuất Excel
                </a>
            </c:if>
        </div>
    </div>

    <%-- Cảnh báo chưa chốt công --%>
    <c:if test="${not attendanceLocked}">
        <div class="alert alert-warning">
            <i class="fa-solid fa-circle-info me-2"></i>
            Bảng chấm công kỳ này chưa được chốt nên chưa thể xuất bảng lương.
        </div>
    </c:if>

    <%-- Bộ lọc --%>
    <div class="filter-card">
        <form method="get" action="${pageContext.request.contextPath}/v1/businessadmin/salary/all"
              class="row g-3 align-items-end">
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
                <input type="number" name="year" min="2000" max="2100"
                       class="form-control" value="${selectedYear}">
            </div>
            <div class="col-md-3">
                <label class="form-label fw-semibold">Phòng ban</label>
                <select name="departmentId" class="form-select">
                    <option value="">Tất cả phòng ban</option>
                    <c:forEach var="d" items="${departments}">
                        <option value="${d.departmentId}"
                                ${selectedDepartmentId == d.departmentId ? 'selected' : ''}>
                            ${d.departmentName}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2">
                <button type="submit" class="btn btn-primary w-100">
                    <i class="fa-solid fa-filter me-1"></i> Lọc
                </button>
            </div>
            <div class="col-md-2">
                <a href="${pageContext.request.contextPath}/v1/businessadmin/salary/all"
                   class="btn btn-outline-secondary w-100">Xóa lọc</a>
            </div>
        </form>
    </div>

    <%-- Thẻ thống kê --%>
    <div class="stat-grid">
        <div class="stat-card">
            <div class="stat-label">Nhân viên tính lương</div>
            <div class="stat-value stat-blue">
                <fmt:formatNumber value="${totalEmployees}" type="number" groupingUsed="true" />
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-label">BH công ty trả</div>
            <div class="stat-value stat-green">
                <fmt:formatNumber value="${totalCompanyInsurance / 1000000}" maxFractionDigits="0" />M
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-label">BH nhân viên trả</div>
            <div class="stat-value stat-orange">
                <fmt:formatNumber value="${totalEmployeeInsurance / 1000000}" maxFractionDigits="0" />M
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Nhân viên nhận</div>
            <div class="stat-value stat-purple">
                <fmt:formatNumber value="${totalNetSalary / 1000000}" maxFractionDigits="0" />M
            </div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Tổng chi trả</div>
            <div class="stat-value stat-blue">
                <fmt:formatNumber value="${totalCompanyCost / 1000000}" maxFractionDigits="0" />M
            </div>
        </div>
    </div>


    <div class="table-card">
        <div class="table-head">
            <h5>Bảng lương tháng ${selectedMonth}/${selectedYear}</h5>
        </div>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>Nhân viên</th>
                        <th>Phòng ban</th>
                        <th>Lương cơ bản</th>
                        <th>Tổng thu nhập</th>
                        <th>Tổng khấu trừ</th>
                        <th>Lương thực nhận</th>
                        <th>Trạng thái</th>
                        <th>Chi tiết</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty payrollPreviews}">
                            <tr>
                                <td colspan="9" class="empty-state">
                                    <i class="fa-solid fa-money-bill-wave d-block mb-2"
                                       style="font-size:28px;color:#cbd5e1"></i>
                                    <c:choose>
                                        <c:when test="${not empty salaryError}">
                                            <div class="fw-semibold text-danger">
                                                <c:out value="${salaryError}" />
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            Chưa có dữ liệu bảng lương cho tháng này.
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="row" items="${payrollPreviews}" varStatus="st">
                                <c:choose>
                                    <c:when test="${row.generationBlocked}">
                                        <tr>
                                            <td>${st.index + 1}</td>
                                            <td>
                                                <span class="employee-name"><c:out value="${row.fullName}" /></span>
                                                <span class="employee-code"><c:out value="${row.employeeCode}" /></span>
                                            </td>
                                            <td><c:out value="${row.departmentName}" /></td>
                                            <td colspan="4" class="text-danger fw-semibold">
                                                <i class="fa-solid fa-circle-exclamation me-1"></i>
                                                <c:out value="${row.generationError}" />
                                            </td>
                                            <td><span class="status-badge status-pending">Chưa đủ thông tin</span></td>
                                            <td>-</td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="deduction"
                                               value="${row.payroll.insuranceDeduction + row.payroll.personalIncomeTax + row.payroll.unpaidDeduction}" />
                                        <tr>
                                            <td>${st.index + 1}</td>
                                            <td>
                                                <span class="employee-name"><c:out value="${row.fullName}" /></span>
                                                <span class="employee-code"><c:out value="${row.employeeCode}" /></span>
                                            </td>
                                            <td><c:out value="${row.departmentName}" /></td>
                                            <td class="money">
                                                <fmt:formatNumber value="${row.payroll.baseSalary}" type="number" groupingUsed="true" />đ
                                            </td>
                                            <td class="money money-green">
                                                <fmt:formatNumber value="${row.payroll.grossSalary}" type="number" groupingUsed="true" />đ
                                            </td>
                                            <td class="money money-red">
                                                -<fmt:formatNumber value="${deduction}" type="number" groupingUsed="true" />đ
                                            </td>
                                            <td class="money money-net">
                                                <fmt:formatNumber value="${row.payroll.netSalary}" type="number" groupingUsed="true" />đ
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${row.payroll.status == 1}">
                                                        <span class="status-badge status-paid">Nhân sự đã duyệt</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge status-pending">Chờ duyệt</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <a class="btn btn-sm btn-outline-primary"
                                                   href="${pageContext.request.contextPath}/v1/businessadmin/salary/detail?id=${row.payroll.payrollId}">
                                                    <i class="fa-solid fa-eye me-1"></i> Xem
                                                </a>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
        <div class="table-footer">
            <span>Hiển thị ${fn:length(payrollPreviews)} nhân sự</span>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>