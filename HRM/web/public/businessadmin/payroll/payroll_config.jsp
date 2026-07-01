<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cấu hình payroll</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background:#f5f6fa; font-family:'Segoe UI', sans-serif; }
        .main { margin-left:250px; padding:25px; }
        .panel { background:#fff; border-radius:14px; box-shadow:0 2px 12px rgba(11,14,42,.07); padding:22px; margin-bottom:22px; }
        .table th { background:#0B0E2A; color:#fff; font-size:13px; }
        .mini-input { min-width:110px; }
        .wide-input { min-width:170px; }
        .hint { color:#6b7280; font-size:13px; }
    </style>
</head>
<body>
<jsp:include page="/public/components/businessAdminSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/businessAdminTopBar.jsp">
        <jsp:param name="title" value="Cấu hình payroll" />
    </jsp:include>

    <c:if test="${not empty success}"><div class="alert alert-success">${success}</div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-danger">${error}</div></c:if>

    <div class="panel">
        <h5 class="fw-bold mb-2">Tham số chung</h5>
        <p class="hint mb-3">Các tham số này do hệ thống hỗ trợ sẵn. Business Admin chỉ được chỉnh giá trị và mô tả.</p>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead><tr><th>Key</th><th>Giá trị</th><th>Mô tả</th><th class="text-end">Thao tác</th></tr></thead>
                <tbody>
                <c:forEach var="s" items="${settings}">
                    <tr>
                        <form method="post" action="${pageContext.request.contextPath}/v1/businessadmin/payroll-config/setting/save">
                            <td><input name="settingKey" class="form-control wide-input" value="${s.settingKey}" readonly></td>
                            <td>
                                <c:choose>
                                    <c:when test="${s.settingKey == 'WORK_START' || s.settingKey == 'WORK_END' || s.settingKey == 'WORK_START_MINUTES' || s.settingKey == 'WORK_END_MINUTES'}">
                                        <input type="time" name="settingValue" class="form-control mini-input" value="${s.displayValue}" required>
                                    </c:when>
                                    <c:otherwise>
                                        <input name="settingValue" class="form-control mini-input" value="${s.displayValue}" required>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><input name="description" class="form-control" value="<c:out value='${s.description}'/>"></td>
                            <td class="text-end"><button class="btn btn-sm btn-outline-primary">Lưu</button></td>
                        </form>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="panel">
        <h5 class="fw-bold mb-2">Bảo hiểm / khoản khấu trừ</h5>
        <p class="hint mb-3">
            Tỷ lệ tổng là tổng nghĩa vụ bảo hiểm. Công ty trả nhập riêng, phần nhân viên trả sẽ tự tính bằng tổng trừ công ty.
            Các khoản này luôn tính theo phần trăm, luôn active và luôn trừ trước thuế.
        </p>
        <form method="post" action="${pageContext.request.contextPath}/v1/businessadmin/payroll-config/deduction/save" class="row g-2 mb-3">
            <div class="col-md-2"><input name="ruleCode" class="form-control" placeholder="CODE" required></div>
            <div class="col-md-2"><input name="ruleName" class="form-control" placeholder="Tên khoản" required></div>
            <div class="col-md-2"><input name="ruleType" class="form-control" value="INSURANCE"></div>
            <div class="col-md-2">
                <select name="baseType" class="form-select">
                    <option value="CONTRACT_SALARY">Lương hợp đồng</option>
                    <option value="GROSS_SALARY">Gross</option>
                    <option value="TAXABLE_INCOME">Thu nhập tính thuế</option>
                </select>
            </div>
            <div class="col-md-1"><input name="rate" class="form-control" placeholder="Tổng %" value="0"></div>
            <div class="col-md-1"><input name="employerRate" class="form-control" placeholder="Công ty %" value="0"></div>
            <div class="col-md-2"><button class="btn btn-primary w-100">Thêm</button></div>
        </form>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                    <tr>
                        <th>Code</th><th>Tên</th><th>Loại</th><th>Nền tính</th>
                        <th>Tổng %</th><th>Công ty trả %</th><th>Nhân viên trả %</th><th></th>
                    </tr>
                </thead>
                <tbody>
                <c:forEach var="r" items="${deductionRules}">
                    <fmt:formatNumber var="totalRatePercent" value="${r.rate * 100}" type="number" maxFractionDigits="4" groupingUsed="false" />
                    <fmt:formatNumber var="employerRatePercent" value="${r.employerRate * 100}" type="number" maxFractionDigits="4" groupingUsed="false" />
                    <tr>
                        <form method="post" action="${pageContext.request.contextPath}/v1/businessadmin/payroll-config/deduction/save">
                            <input type="hidden" name="ruleId" value="${r.ruleId}">
                            <td><input name="ruleCode" class="form-control" value="${r.ruleCode}"></td>
                            <td><input name="ruleName" class="form-control" value="<c:out value='${r.ruleName}'/>"></td>
                            <td><input name="ruleType" class="form-control" value="${r.ruleType}"></td>
                            <td>
                                <select name="baseType" class="form-select">
                                    <option value="CONTRACT_SALARY" ${r.baseType == 'CONTRACT_SALARY' ? 'selected' : ''}>Hợp đồng</option>
                                    <option value="GROSS_SALARY" ${r.baseType == 'GROSS_SALARY' ? 'selected' : ''}>Gross</option>
                                    <option value="TAXABLE_INCOME" ${r.baseType == 'TAXABLE_INCOME' ? 'selected' : ''}>Taxable</option>
                                </select>
                            </td>
                            <td><input name="rate" class="form-control mini-input" value="${totalRatePercent}"></td>
                            <td><input name="employerRate" class="form-control mini-input" value="${employerRatePercent}"></td>
                            <td class="fw-semibold"><fmt:formatNumber value="${r.employeeRate * 100}" type="number" maxFractionDigits="4" /></td>
                            <td class="text-end">
                                <button class="btn btn-sm btn-outline-primary">Lưu</button>
                        </form>
                                <form method="post" action="${pageContext.request.contextPath}/v1/businessadmin/payroll-config/deduction/delete" class="d-inline">
                                    <input type="hidden" name="ruleId" value="${r.ruleId}">
                                    <button class="btn btn-sm btn-outline-danger" onclick="return confirm('Xóa khoản này?')">Xóa</button>
                                </form>
                            </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="panel">
        <h5 class="fw-bold mb-2">Bậc thuế TNCN</h5>
        <p class="hint mb-3">
            Chỉ được xem và sửa các bậc thuế đã seed. Bậc đầu tiên luôn bắt đầu từ 0.
            Bậc cuối không có giới hạn trên, ví dụ hiển thị là &gt; 40.000.000.
            Khi lưu, mốc “Đến” của một bậc phải bằng mốc “Từ” của bậc tiếp theo.
        </p>
        <form method="post" action="${pageContext.request.contextPath}/v1/businessadmin/payroll-config/tax/save">
            <div class="table-responsive">
                <table class="table align-middle">
                    <thead><tr><th>Từ</th><th>Đến</th><th>Thuế suất</th><th class="text-end">Ghi chú</th></tr></thead>
                    <tbody>
                    <c:forEach var="b" items="${taxBrackets}" varStatus="st">
                        <tr>
                            <td>
                                <input type="hidden" name="bracketId" value="${b.bracketId}">
                                <input name="minIncome" class="form-control" value="${b.minIncome}" ${st.first ? 'readonly' : ''}>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${st.last}">
                                        <input type="hidden" name="maxIncome" value="">
                                        <div class="form-control bg-light">&gt; <fmt:formatNumber value="${b.minIncome}" type="number" groupingUsed="true" /></div>
                                    </c:when>
                                    <c:otherwise>
                                        <input name="maxIncome" class="form-control" value="${b.maxIncome}">
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td><input name="taxRate" class="form-control" value="${b.taxRate}"></td>
                            <td class="text-end text-muted">
                                <c:choose>
                                    <c:when test="${st.first}">Mốc từ cố định là 0</c:when>
                                    <c:when test="${st.last}">Không có trần trên</c:when>
                                    <c:otherwise>Khoảng giữa</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <div class="text-end"><button class="btn btn-primary">Lưu bậc thuế</button></div>
        </form>
    </div>
</div>
</body>
</html>
