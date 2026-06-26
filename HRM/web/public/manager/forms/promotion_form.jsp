<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đề Xuất Thăng/Giáng Chức - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 28px;
            margin-bottom: 24px;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Tạo Đề Xuất Thăng/Giáng Chức" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session" />
    </c:if>

    <div class="section-card">
        <div class="mb-4">
            <h5 class="mb-1">Đề Xuất Thăng/Giáng Chức</h5>
            <p class="text-muted">Đề xuất thay đổi chức vụ (Role) cho nhân viên. Đơn sẽ được gửi lên Business Admin để phê duyệt.</p>
        </div>

        <form action="${pageContext.request.contextPath}/v1/manager/forms/submit-promotion" method="POST">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label fw-bold">Nhân viên <span class="text-danger">*</span></label>
                    <select name="employeeId" class="form-select" required>
                        <option value="">-- Chọn nhân viên --</option>
                        <c:forEach var="emp" items="${employees}">
                            <option value="${emp.employeeId}">${emp.employeeCode} - ${emp.fullName}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-6">
                    <label class="form-label fw-bold">Vai trò (Role) mới <span class="text-danger">*</span></label>
                    <select name="targetRoleId" class="form-select" required>
                        <option value="">-- Chọn vai trò mới --</option>
                        <c:forEach var="role" items="${roles}">
                            <option value="${role.roleId}">${role.roleName}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-12 mt-4">
                    <label class="form-label fw-bold">Lý do đề xuất <span class="text-danger">*</span></label>
                    <textarea name="reason" class="form-control" rows="4" 
                              placeholder="Trình bày lý do đề xuất thăng chức hoặc giáng chức..." required></textarea>
                </div>

                <div class="col-12 mt-4 text-end">
                    <a href="${pageContext.request.contextPath}/v1/manager/forms/all" 
                       class="btn btn-light me-2">Hủy</a>
                    <button type="submit" class="btn btn-primary" id="btn-submit-promotion">
                        <i class="fas fa-paper-plane me-1"></i> Gửi Đề Xuất
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
