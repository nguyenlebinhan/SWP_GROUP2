<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Cập nhật nhân viên - HRM</title>
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
        <jsp:param name="title" value="Cập nhật nhân viên" />
        <jsp:param name="backUrl" value="/v1/manager/employee-list" />
    </jsp:include>

    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session" />
    </c:if>

    <div class="page-card">
        <div class="mb-4">
            <h5 class="fw-bold mb-1"><c:out value="${employee.fullName}"/></h5>
            <div class="text-muted small"><c:out value="${employee.employeeCode}"/> - <c:out value="${employee.email}"/></div>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/v1/manager/update-employee">
            <input type="hidden" name="employeeId" value="${employee.employeeId}">

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Trạng thái</label>
                    <select name="status" class="form-select" required>
                        <option value="1" ${employee.status == 1 ? 'selected' : ''}>Đang làm việc</option>
                        <option value="2" ${employee.status == 2 ? 'selected' : ''}>Đang nghỉ phép</option>
                        <option value="0" ${employee.status == 0 ? 'selected' : ''}>Không hoạt động</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Số điện thoại</label>
                    <input type="text" name="phoneNumber" class="form-control" value="<c:out value='${employee.phoneNumber}'/>">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Kỹ năng</label>
                    <textarea name="skills" class="form-control" rows="3"><c:out value="${employee.skills}"/></textarea>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Kinh nghiệm</label>
                    <textarea name="experience" class="form-control" rows="3"><c:out value="${employee.experience}"/></textarea>
                </div>
                <div class="col-md-12">
                    <label class="form-label">Bằng cấp</label>
                    <input type="text" name="degree" class="form-control" value="<c:out value='${employee.degree}'/>">
                </div>
            </div>

            <div class="d-flex gap-2 mt-4">
                <button type="submit" class="btn btn-primary">
                    <i class="fa-solid fa-check me-1"></i>Lưu thay đổi
                </button>
                <a href="${pageContext.request.contextPath}/v1/manager/employee-detail?id=${employee.employeeId}" class="btn btn-outline-secondary">
                    Hủy
                </a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
