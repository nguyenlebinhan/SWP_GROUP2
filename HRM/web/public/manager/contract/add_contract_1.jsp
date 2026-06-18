<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Thêm hợp đồng lao động - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .page-card { background: white; border-radius: 14px; box-shadow: 0 2px 12px rgba(0,0,0,0.07); padding: 28px 34px; }
        .form-label { font-size: 13px; font-weight: 600; color: #374151; }
        .form-control, .form-select { border-radius: 8px; border-color: #e2e8f0; font-size: 14px; }
    </style>
</head>
<body>
<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Thêm hợp đồng lao động" />
        <jsp:param name="backUrl" value="/v1/manager/employee/list" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="page-card">
        <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/add">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Mã hợp đồng</label>
                    <input type="text" name="contractCode" class="form-control" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Nhân viên</label>
                    <select name="employeeId" class="form-select" required>
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
                    <select name="contractType" class="form-select" required>
                        <option value="">-- Chọn loại hợp đồng --</option>
                        <option value="Probation">Thử việc</option>
                        <option value="Full-time">Chính thức</option>
                        <option value="Part-time">Bán thời gian</option>
                        <option value="Fixed-term">Có thời hạn</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Lương</label>
                    <input type="number" name="salary" class="form-control" min="0" step="1000" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Ngày bắt đầu</label>
                    <input type="date" name="startDate" class="form-control" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Ngày kết thúc</label>
                    <input type="date" name="endDate" class="form-control">
                </div>
                <div class="col-md-12">
                    <label class="form-label">Ghi chú</label>
                    <textarea name="note" class="form-control" rows="3"></textarea>
                </div>
            </div>

            <div class="d-flex gap-2 mt-4">
                <button type="submit" class="btn btn-primary">
                    <i class="fa-solid fa-file-contract me-1"></i>Thêm hợp đồng
                </button>
                <a href="${pageContext.request.contextPath}/v1/manager/employee/list" class="btn btn-outline-secondary">
                    Hủy
                </a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
