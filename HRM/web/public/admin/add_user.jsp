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
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }

        .sidebar {
            width: 250px; height: 100vh; background: #0B0E2A;
            position: fixed; top: 0; left: 0; color: white; z-index: 100;
        }
        .sidebar h4 { padding: 20px; color: #ff8c00; margin: 0; }
        .sidebar a {
            display: block; padding: 14px 20px; color: #cbd5e1;
            text-decoration: none; transition: background 0.2s;
        }
        .sidebar a:hover { background: #1565C0; color: white; border-radius: 5px; }
        .sidebar a.active-menu { background: #2b6cb0; color: white; border-radius: 5px; }

        .topbar {
            height: 70px; background: white;
            display: flex; justify-content: space-between; align-items: center;
            padding: 0 30px; border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05); margin-bottom: 24px;
        }
        .admin-avatar {
            width: 40px; height: 40px; background: #2b6cb0; border-radius: 50%;
        }
        .bell { font-size: 20px; position: relative; cursor: pointer; }
        .bell::after {
            content: ''; position: absolute; top: 2px; right: -2px;
            width: 8px; height: 8px; background: red; border-radius: 50%;
        }

        .main { margin-left: 250px; padding: 25px; }

        .form-card {
            background: white; border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.06); padding: 32px;
            max-width: 720px;
        }
        .form-card .section-title {
            font-size: 1.1rem; font-weight: 600;
            color: #0B0E2A; margin-bottom: 20px;
            padding-bottom: 10px; border-bottom: 2px solid #e2e8f0;
        }
        .form-label { font-weight: 500; color: #374151; }
        .btn-save {
            background: #2b6cb0; color: white; border: none;
            padding: 10px 28px; border-radius: 8px; font-weight: 500;
        }
        .btn-save:hover { background: #1a4f8a; color: white; }
        .btn-cancel {
            background: #e2e8f0; color: #374151; border: none;
            padding: 10px 24px; border-radius: 8px; font-weight: 500;
        }
        .btn-cancel:hover { background: #cbd5e1; }
    </style>
</head>
<body>


<div class="sidebar">
    <h4><i class="fa fa-building me-2"></i>HRM System</h4>
    <a href="${pageContext.request.contextPath}/"><i class="fa fa-chart-line me-2"></i>Tổng quan</a>
    <a><i class="fa fa-briefcase me-2"></i>Công việc</a>
    <a href="${pageContext.request.contextPath}/v1/admin/users" class="active-menu">
        <i class="fa fa-users me-2"></i>Người dùng
    </a>
    <a><i class="fa fa-clock me-2"></i>Chấm công</a>
    <a><i class="fa fa-user-plus me-2"></i>Tuyển dụng</a>
    <a><i class="fa fa-money-bill me-2"></i>Bảng lương</a>
    <a><i class="fa fa-building me-2"></i>Phòng ban</a>
    <a><i class="fa fa-key me-2"></i>Phân quyền</a>
    <a><i class="fa fa-chart-bar me-2"></i>Báo cáo</a>
</div>


<div class="main">

    <!-- Topbar -->
    <div class="topbar">
        <div class="d-flex align-items-center gap-2">
            <a href="${pageContext.request.contextPath}/v1/admin/users" class="text-decoration-none text-secondary">
                <i class="fa fa-arrow-left me-1"></i>
            </a>
            <h5 class="mb-0">Thêm người dùng mới</h5>
        </div>
        <div class="d-flex align-items-center gap-3">
            <div class="bell"><i class="fa-solid fa-bell"></i></div>
            <div class="d-flex align-items-center gap-2">
                <div class="admin-avatar"></div>
                <span>Admin</span>
            </div>
        </div>
    </div>

    <!-- Alerts -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mb-3" role="alert" style="max-width:720px;">
            <i class="fa fa-circle-exclamation me-2"></i><c:out value="${error}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success alert-dismissible fade show mb-3" role="alert" style="max-width:720px;">
            <i class="fa fa-circle-check me-2"></i><c:out value="${success}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Form Card -->
    <div class="form-card">
        <div class="section-title"><i class="fa fa-user-plus me-2"></i>Thông tin tài khoản</div>

        <form action="${pageContext.request.contextPath}/v1/admin/add-user" method="POST" novalidate>

            <div class="row g-3 mb-4">
                <div class="col-md-6">
                    <label class="form-label" for="email">Email <span class="text-danger">*</span></label>
                    <input type="email" id="email" name="email" class="form-control"
                           placeholder="example@company.com" required
                           value="<c:out value='${param.email}'/>"/>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="password">Mật khẩu tạm thời <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <input type="password" id="password" name="password" class="form-control"
                               placeholder="Nhập mật khẩu tạm thời" required/>
                        <button class="btn btn-outline-secondary" type="button" id="togglePassword"
                                title="Hiện/ẩn mật khẩu">
                            <i class="fa fa-eye" id="eyeIcon"></i>
                        </button>
                    </div>
                    <div class="form-text">Mật khẩu sẽ được gửi đến email người dùng.</div>
                </div>
            </div>

            <div class="section-title mt-2"><i class="fa fa-id-card me-2"></i>Thông tin cá nhân</div>

            <div class="row g-3 mb-4">
                <div class="col-md-6">
                    <label class="form-label" for="fullName">Họ và tên <span class="text-danger">*</span></label>
                    <input type="text" id="fullName" name="fullName" class="form-control"
                           placeholder="Nguyễn Văn A" required
                           value="<c:out value='${param.fullName}'/>"/>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="dob">Ngày sinh</label>
                    <input type="date" id="dob" name="dob" class="form-control"
                           value="<c:out value='${param.dob}'/>"/>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="gender">Giới tính</label>
                    <select id="gender" name="gender" class="form-select">
                        <option value="" disabled <c:if test="${empty param.gender}">selected</c:if>>-- Chọn giới tính --</option>
                        <option value="Nam"  <c:if test="${param.gender == 'Nam'}">selected</c:if>>Nam</option>
                        <option value="Nữ"   <c:if test="${param.gender == 'Nữ'}">selected</c:if>>Nữ</option>
                        <option value="Khác" <c:if test="${param.gender == 'Khác'}">selected</c:if>>Khác</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="roleId">Vai trò <span class="text-danger">*</span></label>
                    <select id="roleId" name="role_selection" class="form-select" required>
                        <option value="" disabled <c:if test="${empty param.roleId}">selected</c:if>>-- Chọn vai trò --</option>
                        <c:forEach items="${roles}" var ="r">
                            <option value="${r.roleId}">${r.roleName}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-12">
                    <label class="form-label" for="address">Địa chỉ</label>
                    <input type="text" id="address" name="address" class="form-control"
                           placeholder="Số nhà, đường, quận/huyện, tỉnh/thành phố"
                           value="<c:out value='${param.address}'/>"/>
                </div>
            </div>

            <div class="d-flex gap-3 pt-2">
                <button type="submit" class="btn btn-save">
                    <i class="fa fa-plus me-1"></i>Thêm người dùng
                </button>
                <a href="${pageContext.request.contextPath}/v1/admin/users" class="btn btn-cancel">
                    Hủy
                </a>
            </div>
        </form>
    </div>

</div><!-- /main -->

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
