<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Thêm người dùng – HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
    <style>
        body {
            background: #f5f6fa;
            font-family: 'Segoe UI', sans-serif;
        }

        .main-content {
            margin-left: 250px;
            padding: 30px;
        }

        .page-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 24px;
        }

        .page-header a {
            width: 36px;
            height: 36px;
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #6b7280;
            text-decoration: none;
            transition: background .2s;
        }

        .page-header a:hover { background: #f3f4f6; }

        .page-header h5 {
            font-weight: 700;
            color: #0B0E2A;
            margin: 0;
        }

        .card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
        }

        .section-label {
            font-size: 13px;
            font-weight: 700;
            color: #0B0E2A;
            text-transform: uppercase;
            letter-spacing: .6px;
            padding-bottom: 10px;
            border-bottom: 2px solid #f1f3f5;
            margin-bottom: 20px;
        }

        .section-label i {
            color: #ff8c00;
            margin-right: 6px;
        }

        .form-label {
            font-size: 13px;
            font-weight: 600;
            color: #374151;
            margin-bottom: 6px;
        }

        .form-control,
        .form-select {
            height: 44px;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            font-size: 14px;
            color: #1f2937;
            padding: 0 14px;
        }

        .form-control:focus,
        .form-select:focus {
            border-color: #1565c0;
            box-shadow: 0 0 0 3px rgba(21,101,192,.1);
        }

        .form-control::placeholder { color: #9ca3af; }

        .input-group .form-control { border-right: none; }

        .input-group .btn-outline-secondary {
            border: 1px solid #e5e7eb;
            border-left: none;
            border-radius: 0 8px 8px 0;
            color: #9ca3af;
            background: white;
        }

        .input-group .btn-outline-secondary:hover { background: #f9fafb; }

        .form-text { font-size: 12px; color: #9ca3af; margin-top: 4px; }

        .btn-save {
            height: 44px;
            padding: 0 28px;
            background: #ff8c00;
            color: white;
            border: none;
            border-radius: 8px;
            font-weight: 600;
            font-size: 14px;
            transition: background .2s;
        }

        .btn-save:hover { background: #e07b00; color: white; }

        .btn-cancel-link {
            height: 44px;
            padding: 0 24px;
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            font-weight: 600;
            font-size: 14px;
            color: #6b7280;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            transition: background .2s;
        }

        .btn-cancel-link:hover { background: #f9fafb; color: #374151; }

        .alert-flash {
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 20px;
        }

        .required { color: #ef4444; }
    </style>
</head>
<body>

<jsp:include page="/public/components/adminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/adminTopBar.jsp">
        <jsp:param name="title" value="Cập nhật người dùng" />
        <jsp:param name="backUrl" value="/v1/admin/user-list" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash alert-dismissible fade show" role="alert">
            <i class="fa fa-circle-exclamation me-2"></i><c:out value="${error}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success alert-flash alert-dismissible fade show" role="alert">
            <i class="fa fa-circle-check me-2"></i><c:out value="${success}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Form -->
    <form action="${pageContext.request.contextPath}/v1/admin/update-user" method="POST">
        <div class="card">
            <div class="card-body p-4">
                <div class="section-label"><i class="fa fa-lock"></i>Thông tin tài khoản</div>
                <div class="row g-3 mb-4">
                    <div class="col-md-4">
                        <input type="hidden" value="${userId}" name="userId">
                        <label class="form-label" for="username">Tên đăng nhập <span class="required">*</span></label>
                        <input type="text" id="username" name="username" class="form-control"
                               placeholder="vd: nguyenvana"
                               value="<c:out value='${user.username}'/>" required/>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="email">Email <span class="required">*</span></label>
                        <input type="email" id="email" name="email" class="form-control"
                               placeholder="vd: nguyenvana@company.com"
                               value="<c:out value='${user.email}'/>" required/>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label" for="password">Mật khẩu<span class="required">*</span></label>
                        <div class="input-group">
                            <input type="password" id="password" name="password" class="form-control"
                                   placeholder="············" required/>
                            <button type="button" class="btn btn-outline-secondary" id="togglePassword">
                                <i class="fa fa-eye" id="eyeIcon"></i>
                            </button>
                        </div>
                        <div class="form-text"><i class="fa fa-envelope me-1"></i>Sẽ được gửi tới email người dùng</div>
                    </div>
                </div>

                <!-- Thông tin cá nhân -->
                <div class="section-label"><i class="fa fa-id-card"></i>Thông tin cá nhân</div>
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label class="form-label" for="fullName">Họ và tên <span class="required">*</span></label>
                        <input type="text" id="fullName" name="fullName" class="form-control"
                               placeholder="vd: Nguyễn Văn A"
                               value="<c:out value='${user.fullName}'/>" required/>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label" for="dob">Ngày sinh</label>
                        <input type="date" id="dob" name="dob" class="form-control"
                               value="<c:out value='${user.dob}'/>" required/>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label" for="gender">Giới tính</label>
                        <select id="gender" name="gender" class="form-select" required>
                            <option value="Nam"  <c:if test="${user.gender == 'Nam'}">selected</c:if>>Nam</option>
                            <option value="Nữ"   <c:if test="${user.gender == 'Nữ'}">selected</c:if>>Nữ</option>
                            <option value="Khác" <c:if test="${user.gender == 'Khác'}">selected</c:if>>Khác</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="address">Địa chỉ</label>
                        <input type="text" id="address" name="address" class="form-control"
                               placeholder="Số nhà, đường, quận/huyện, tỉnh/thành phố"
                               value="<c:out value='${user.address}'/>"/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="roleId">Vai trò <span class="required">*</span></label>
                        <select id="roleId" name="role_selection" class="form-select" required>
                            <c:forEach items="${roles}" var="r">
                                <option value="${r.roleId}"
                                    <c:if test="${user.roleId == r.roleId}">selected</c:if>>
                                    ${r.roleName}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <!-- Actions -->
                <div class="d-flex gap-3 pt-2">
                    <button type="submit" class="btn-save">
                        <i class="fa fa-user-plus me-2"></i>Cập nhật người dùng
                    </button>
                    <a href="${pageContext.request.contextPath}/v1/admin/user-list" class="btn-cancel-link">
                        Hủy
                    </a>
                </div>

            </div>
        </div>
    </form>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.getElementById('togglePassword').addEventListener('click', function () {
        const pwd = document.getElementById('password');
        const icon = document.getElementById('eyeIcon');
        if (pwd.type === 'password') {
            pwd.type = 'text';
            icon.classList.replace('fa-eye', 'fa-eye-slash');
        } else {
            pwd.type = 'password';
            icon.classList.replace('fa-eye-slash', 'fa-eye');
        }
    });
</script>

</body>
</html>
