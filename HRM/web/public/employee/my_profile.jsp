<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Profile - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
        }
        .form-label { font-weight: 600; color: #334155; }
    </style>
</head>
<body>

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/systemAdminTopBar.jsp">
        <jsp:param name="title" value="Hồ sơ của tôi" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session" />
    </c:if>

    <div class="section-card">
        <h5 class="mb-4">Thông tin cá nhân</h5>
        <div class="row mb-4">
            <div class="col-md-6 mb-3">
                <label class="form-label">Họ và tên</label>
                <input type="text" class="form-control bg-light" value="${currentUser.fullName}" readonly>
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label">Tên đăng nhập</label>
                <input type="text" class="form-control bg-light" value="${currentUser.username}" readonly>
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label">Email</label>
                <input type="text" class="form-control bg-light" value="${currentUser.email}" readonly>
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label">Mã nhân viên</label>
                <input type="text" class="form-control bg-light" value="${myEmployee != null ? myEmployee.employeeCode : 'N/A'}" readonly>
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label">Phòng ban</label>
                <input type="text" class="form-control bg-light" value="${myEmployee != null ? myEmployee.departmentName : 'N/A'}" readonly>
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label">Vị trí</label>
                <input type="text" class="form-control bg-light" value="${myEmployee != null ? myEmployee.positionName : 'N/A'}" readonly>
            </div>
        </div>
        <hr>
        <h5 class="mb-4 mt-4">Cập nhật hồ sơ (Chỉ nhân viên)</h5>
        <c:choose>
            <c:when test="${not empty myEmployee}">
                <form action="${pageContext.request.contextPath}/v1/employee/update-my-profile" method="post">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Số điện thoại</label>
                            <input type="text" name="phoneNumber" class="form-control" value="${myEmployee.phoneNumber}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Bằng cấp</label>
                            <input type="text" name="degree" class="form-control" value="${myEmployee.degree}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Kỹ năng</label>
                            <textarea name="skills" class="form-control" rows="3">${myEmployee.skills}</textarea>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Kinh nghiệm</label>
                            <textarea name="experience" class="form-control" rows="3">${myEmployee.experience}</textarea>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary mt-2"><i class="fa-solid fa-save me-2"></i>Lưu thay đổi</button>
                </form>
            </c:when>
            <c:otherwise>
                <p class="text-muted">Bạn chưa được phân công làm nhân viên chính thức nên không thể cập nhật thông tin này.</p>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
