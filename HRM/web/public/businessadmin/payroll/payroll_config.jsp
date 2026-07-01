<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
        .change-text { white-space:pre-wrap; max-width:360px; }
        .readonly-value { display:block; min-width:110px; padding:6px 0; }
        .readonly-muted { color:#6b7280; }
    </style>
</head>
<body>
<jsp:include page="${sidebarPath}" />

<div class="main">
    <jsp:include page="${topbarPath}">
        <jsp:param name="title" value="Cấu hình payroll" />
    </jsp:include>

    <c:if test="${not empty success}"><div class="alert alert-success">${success}</div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-danger">${error}</div></c:if>

    <c:if test="${canApprovePayrollConfig}">
        <div class="panel">
            <h5 class="fw-bold mb-2">Yêu cầu thay đổi chờ duyệt</h5>
            <c:choose>
                <c:when test="${empty pendingRequests}">
                    <div class="hint">Không có yêu cầu cấu hình payroll đang chờ duyệt.</div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table align-middle">
                            <thead>
                                <tr>
                                    <th>#</th><th>Thao tác</th><th>Người gửi</th><th>Thời điểm</th>
                                    <th>Trước</th><th>Sau</th><th class="text-end">Duyệt</th>
                                </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="req" items="${pendingRequests}">
                                <tr>
                                    <td>${req.requestId}</td>
                                    <td>${req.actionLabel}</td>
                                    <td><c:out value="${req.requestedByName}" /></td>
                                    <td><fmt:formatDate value="${req.requestedAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                                    <td class="change-text"><c:out value="${req.oldValue}" /></td>
                                    <td class="change-text"><c:out value="${req.newValue}" /></td>
                                    <td class="text-end">
                                        <form method="post" action="${payrollConfigBaseUrl}/request/approve" class="d-inline">
                                            <input type="hidden" name="requestId" value="${req.requestId}">
                                            <input name="note" class="form-control form-control-sm mb-1" placeholder="Ghi chú">
                                            <button class="btn btn-sm btn-success">Duyệt</button>
                                        </form>
                                        <form method="post" action="${payrollConfigBaseUrl}/request/reject" class="d-inline">
                                            <input type="hidden" name="requestId" value="${req.requestId}">
                                            <button class="btn btn-sm btn-outline-danger">Từ chối</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <c:if test="${canEditPayrollConfig && not empty pendingRequests}">
        <div class="panel">
            <h5 class="fw-bold mb-2">Yêu cầu đang chờ Business Admin duyệt</h5>
            <div class="table-responsive">
                <table class="table align-middle">
                    <thead><tr><th>#</th><th>Thao tác</th><th>Người gửi</th><th>Thời điểm</th><th>Nội dung</th></tr></thead>
                    <tbody>
                    <c:forEach var="req" items="${pendingRequests}">
                        <tr>
                            <td>${req.requestId}</td>
                            <td>${req.actionLabel}</td>
                            <td><c:out value="${req.requestedByName}" /></td>
                            <td><fmt:formatDate value="${req.requestedAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                            <td class="change-text"><c:out value="${req.newValue}" /></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <div class="panel">
        <h5 class="fw-bold mb-2">Tham số chung</h5>
        <p class="hint mb-3">
            <c:choose>
                <c:when test="${canEditPayrollConfig}">Sau khi lưu, hệ thống sẽ gửi yêu cầu cho Business Admin duyệt.</c:when>
                <c:otherwise>Business Admin chỉ xem và duyệt yêu cầu thay đổi từ HR.</c:otherwise>
            </c:choose>
        </p>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                    <tr>
                        <th>Key</th><th>Giá trị</th><th>Mô tả</th>
                        <c:if test="${canEditPayrollConfig}"><th class="text-end">Thao tác</th></c:if>
                    </tr>
                </thead>
                <tbody>
                <c:forEach var="s" items="${settings}" varStatus="st">
                    <tr>
                        <c:choose>
                            <c:when test="${canEditPayrollConfig}">
                                <td><input form="settingForm${st.index}" name="settingKey" class="form-control wide-input" value="${s.settingKey}" readonly></td>
                                <td><input form="settingForm${st.index}" name="settingValue" class="form-control mini-input" value="${s.displayValue}" required></td>
                                <td><input form="settingForm${st.index}" name="description" class="form-control" value="${fn:escapeXml(s.description)}"></td>
                            </c:when>
                            <c:otherwise>
                                <td><span class="readonly-value fw-semibold"><c:out value="${s.settingKey}" /></span></td>
                                <td><span class="readonly-value"><c:out value="${s.displayValue}" /></span></td>
                                <td><span class="readonly-value readonly-muted"><c:out value="${s.description}" /></span></td>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${canEditPayrollConfig}">
                            <td class="text-end">
                                <form id="settingForm${st.index}" method="post" action="${payrollConfigBaseUrl}/setting/save">
                                    <button class="btn btn-sm btn-outline-primary">Gửi duyệt</button>
                                </form>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="panel">
        <h5 class="fw-bold mb-2">Bảo hiểm / khoản khấu trừ</h5>
        <p class="hint mb-3">
            Tỷ lệ nhập theo phần trăm. Tổng % được tính tự động bằng công ty trả % cộng nhân viên trả %.
        </p>
        <c:if test="${canEditPayrollConfig}">
            <form method="post" action="${payrollConfigBaseUrl}/deduction/save" class="row g-2 mb-3">
                <div class="col-md-2"><input name="ruleCode" class="form-control" placeholder="CODE" required></div>
                <div class="col-md-2"><input name="ruleName" class="form-control" placeholder="Tên khoản" required></div>
                <div class="col-md-1"><input name="ruleType" class="form-control" value="INSURANCE"></div>
                <div class="col-md-2"><span class="readonly-value">Lương hợp đồng</span></div>
                <div class="col-md-1"><input name="employerRate" class="form-control employer-rate" placeholder="Công ty %" value="0"></div>
                <div class="col-md-1"><input name="employeeRate" class="form-control employee-rate" placeholder="Nhân viên %" value="0"></div>
                <div class="col-md-1"><input class="form-control total-rate" placeholder="Tổng %" value="0" readonly></div>
                <div class="col-md-2"><button class="btn btn-primary w-100">Gửi duyệt</button></div>
            </form>
        </c:if>
        <div class="table-responsive">
            <table class="table align-middle">
                <thead>
                    <tr>
                        <th>Code</th><th>Tên</th><th>Loại</th><th>Nền tính</th>
                        <th>Công ty trả %</th><th>Nhân viên trả %</th><th>Tổng %</th>
                        <c:if test="${canEditPayrollConfig}"><th></th></c:if>
                    </tr>
                </thead>
                <tbody>
                <c:forEach var="r" items="${deductionRules}" varStatus="st">
                    <fmt:formatNumber var="totalRatePercent" value="${r.rate * 100}" type="number" maxFractionDigits="4" groupingUsed="false" />
                    <fmt:formatNumber var="employerRatePercent" value="${r.employerRate * 100}" type="number" maxFractionDigits="4" groupingUsed="false" />
                    <fmt:formatNumber var="employeeRatePercent" value="${r.employeeRate * 100}" type="number" maxFractionDigits="4" groupingUsed="false" />
                    <tr>
                        <c:choose>
                            <c:when test="${canEditPayrollConfig}">
                                <td>
                                    <input form="deductionForm${st.index}" type="hidden" name="ruleId" value="${r.ruleId}">
                                    <input form="deductionForm${st.index}" name="ruleCode" class="form-control" value="${r.ruleCode}">
                                </td>
                                <td><input form="deductionForm${st.index}" name="ruleName" class="form-control" value="${fn:escapeXml(r.ruleName)}"></td>
                                <td><input form="deductionForm${st.index}" name="ruleType" class="form-control" value="${r.ruleType}"></td>
                                <td><span class="readonly-value">Hợp đồng</span></td>
                                <td><input form="deductionForm${st.index}" name="employerRate" class="form-control mini-input employer-rate" value="${employerRatePercent}"></td>
                                <td><input form="deductionForm${st.index}" name="employeeRate" class="form-control mini-input employee-rate" value="${employeeRatePercent}"></td>
                                <td><input class="form-control mini-input total-rate fw-semibold" value="${totalRatePercent}" readonly></td>
                            </c:when>
                            <c:otherwise>
                                <td><span class="readonly-value fw-semibold"><c:out value="${r.ruleCode}" /></span></td>
                                <td><span class="readonly-value"><c:out value="${r.ruleName}" /></span></td>
                                <td><span class="readonly-value"><c:out value="${r.ruleType}" /></span></td>
                                <td><span class="readonly-value">Hợp đồng</span></td>
                                <td><span class="readonly-value"><c:out value="${employerRatePercent}" /></span></td>
                                <td><span class="readonly-value"><c:out value="${employeeRatePercent}" /></span></td>
                                <td><span class="readonly-value fw-semibold"><c:out value="${totalRatePercent}" /></span></td>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${canEditPayrollConfig}">
                            <td class="text-end">
                                <form id="deductionForm${st.index}" method="post" action="${payrollConfigBaseUrl}/deduction/save" class="d-inline">
                                    <button class="btn btn-sm btn-outline-primary">Gửi duyệt</button>
                                </form>
                                <form method="post" action="${payrollConfigBaseUrl}/deduction/delete" class="d-inline">
                                    <input type="hidden" name="ruleId" value="${r.ruleId}">
                                    <button class="btn btn-sm btn-outline-danger" onclick="return confirm('Xóa khoản này?')">Xóa</button>
                                </form>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="panel">
        <h5 class="fw-bold mb-2">Bậc thuế TNCN</h5>
        <p class="hint mb-3">
            Bậc đầu tiên bắt đầu từ 0. Bậc cuối không có giới hạn trên.
        </p>
        <form method="post" action="${payrollConfigBaseUrl}/tax/save">
            <div class="table-responsive">
                <table class="table align-middle">
                    <thead><tr><th>Từ</th><th>Đến</th><th>Thuế suất</th><th class="text-end">Ghi chú</th></tr></thead>
                    <tbody>
                    <c:forEach var="b" items="${taxBrackets}" varStatus="st">
                        <tr>
                            <td>
                                <c:choose>
                                    <c:when test="${canEditPayrollConfig}">
                                        <input type="hidden" name="bracketId" value="${b.bracketId}">
                                        <input name="minIncome" class="form-control" value="${b.minIncome}" ${st.first ? 'readonly' : ''}>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="readonly-value"><fmt:formatNumber value="${b.minIncome}" type="number" groupingUsed="true" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${st.last}">
                                        <c:if test="${canEditPayrollConfig}">
                                            <input type="hidden" name="maxIncome" value="">
                                        </c:if>
                                        <div class="${canEditPayrollConfig ? 'form-control bg-light' : 'readonly-value'}">&gt; <fmt:formatNumber value="${b.minIncome}" type="number" groupingUsed="true" /></div>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${canEditPayrollConfig}">
                                                <input name="maxIncome" class="form-control" value="${b.maxIncome}">
                                            </c:when>
                                            <c:otherwise>
                                                <span class="readonly-value"><fmt:formatNumber value="${b.maxIncome}" type="number" groupingUsed="true" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${canEditPayrollConfig}">
                                        <input name="taxRate" class="form-control" value="${b.taxRate}">
                                    </c:when>
                                    <c:otherwise>
                                        <span class="readonly-value"><c:out value="${b.taxRate}" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
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
            <c:if test="${canEditPayrollConfig}">
                <div class="text-end"><button class="btn btn-primary">Gửi duyệt bậc thuế</button></div>
            </c:if>
        </form>
    </div>
</div>
<script>
    function toNumber(value) {
        var parsed = parseFloat(String(value || '').replace(',', '.'));
        return isNaN(parsed) ? 0 : parsed;
    }

    function formatPercent(value) {
        var rounded = Math.round(value * 10000) / 10000;
        return String(rounded).replace(/\.?0+$/, '');
    }

    function updateDeductionTotal(scope) {
        var employer = scope.querySelector('.employer-rate');
        var employee = scope.querySelector('.employee-rate');
        var total = scope.querySelector('.total-rate');
        if (!employer || !employee || !total) {
            return;
        }
        total.value = formatPercent(toNumber(employer.value) + toNumber(employee.value));
    }

    document.querySelectorAll('form[action$="/deduction/save"], table.table tr').forEach(function (scope) {
        if (!scope.querySelector('.employer-rate') || !scope.querySelector('.employee-rate')) {
            return;
        }
        updateDeductionTotal(scope);
        scope.querySelectorAll('.employer-rate, .employee-rate').forEach(function (input) {
            input.addEventListener('input', function () {
                updateDeductionTotal(scope);
            });
        });
    });
</script>
</body>
</html>
