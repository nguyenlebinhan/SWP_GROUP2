<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Thêm hợp đồng lao động - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: "Segoe UI", sans-serif; }
        .main { margin-left: 250px; padding: 24px; }
        .page-card { background: #fff; border-radius: 16px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08); padding: 28px; }
        @media (max-width: 768px) { .main { margin-left: 0; padding: 16px; } }
    </style>
</head>
<body>
<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Thêm hợp đồng lao động" />
        <jsp:param name="backUrl" value="/v1/manager/employee_info/list" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="page-card">
        <form id="contractForm" method="post" action="${pageContext.request.contextPath}/v1/manager/contract/add">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Mã hợp đồng</label>
                    <input type="text" name="contractCode" id="contractCode" class="form-control"
                           value="${param.contractCode}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Nhân viên</label>
                    <select name="employeeId" id="employeeId" class="form-select" required>
                        <option value="">-- Chọn nhân viên --</option>
                        <c:forEach var="emp" items="${employees}">
                            <option value="${emp.employeeId}" ${param.employeeId == emp.employeeId ? 'selected' : ''}>
                                ${emp.fullName} (${emp.employeeCode})
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Loại hợp đồng</label>
                    <select name="contractType" id="contractType" class="form-select" required>
                        <option value="">-- Chọn loại hợp đồng --</option>
                        <option value="PROBATION" ${param.contractType == 'PROBATION' ? 'selected' : ''}>Thử việc</option>
                        <option value="INTERNSHIP" ${param.contractType == 'INTERNSHIP' ? 'selected' : ''}>Thực tập</option>
                        <option value="FIXED_TERM" ${param.contractType == 'FIXED_TERM' ? 'selected' : ''}>Có thời hạn</option>
                        <option value="INDEFINITE" ${param.contractType == 'INDEFINITE' ? 'selected' : ''}>Không xác định thời hạn</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Lương</label>
                    <input type="number" name="salary" id="salary" class="form-control" min="0" step="1000"
                           value="${param.salary}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Ngày hiệu lực</label>
                    <input type="date" name="effectiveDate" id="effectiveDate" class="form-control"
                           value="${param.effectiveDate}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Ngày kết thúc</label>
                    <input type="date" name="endDate" id="endDate" class="form-control"
                           value="${param.endDate}">
                </div>
                <div class="col-12">
                    <label class="form-label">Ghi chú</label>
                    <textarea name="note" id="note" class="form-control" rows="3">${param.note}</textarea>
                </div>
            </div>

            <div class="d-flex gap-2 mt-4 flex-wrap">
                <button type="submit" class="btn btn-primary" name="action" value="submit">
                    <i class="fa-solid fa-paper-plane me-1"></i>Gửi duyệt
                </button>
                <a href="${pageContext.request.contextPath}/v1/manager/employee_info/list" class="btn btn-outline-secondary"
                   onclick="return confirm('Bạn có chắc muốn hủy? Thông tin chưa lưu sẽ mất.');">
                    <i class="fa-solid fa-ban me-1"></i>Hủy
                </a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
