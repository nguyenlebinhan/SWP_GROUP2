<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<<<<<<< HEAD
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Thêm người dùng – HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
    <style>
        * { box-sizing: border-box; }
        body { background: #f5f6f8; font-family: 'Inter', 'Segoe UI', sans-serif; margin: 0; }

        /* ===== SIDEBAR ===== */
        .sidebar {
            width: 250px; height: 100vh; background: #0B0E2A;
            position: fixed; top: 0; left: 0; color: white; z-index: 100;
            display: flex; flex-direction: column;
        }
        .sidebar-brand {
            padding: 20px 24px; color: #ff8c00;
            font-size: 18px; font-weight: 700; border-bottom: 1px solid rgba(255,255,255,0.08);
        }
        .sidebar a {
            display: flex; align-items: center; gap: 10px;
            padding: 13px 20px; color: #cbd5e1;
            text-decoration: none; font-size: 14px; transition: background 0.2s;
        }
        .sidebar a:hover { background: #1565C0; color: white; }
        .sidebar a.active-menu { background: #1565C0; color: white; }
        .sidebar a i { width: 18px; text-align: center; }

        /* ===== TOPBAR ===== */
        .main { margin-left: 250px; padding: 0; }
        .topbar {
            height: 64px; background: white;
            display: flex; justify-content: space-between; align-items: center;
            padding: 0 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
        }
        .topbar-title {
            font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0;
        }
        .topbar-right { display: flex; align-items: center; gap: 12px; }
        .bell-btn {
            width: 36px; height: 36px; background: #f5f6f8;
            border-radius: 50%; display: flex; align-items: center; justify-content: center;
            cursor: pointer; font-size: 16px; border: none;
        }
        .admin-avatar {
            width: 36px; height: 36px; background: #1565c0;
            border-radius: 50%; cursor: pointer;
        }

        /* ===== CONTENT ===== */
        .content-area { padding: 24px; }

        .form-card {
            background: white; border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            padding: 24px; display: flex; flex-direction: column; gap: 20px;
        }

        .section-title {
            font-size: 16px; font-weight: 700; color: #1a1a2e; margin: 0;
        }

        .form-group { display: flex; flex-direction: column; gap: 6px; }

        .form-group label {
            font-size: 13px; font-weight: 600; color: #333;
        }

        .form-group input,
        .form-group select {
            height: 44px; border: 1px solid #e0e0e0; border-radius: 8px;
            padding: 0 14px; font-size: 13px; color: #1a1a2e;
            background: white; width: 100%; outline: none;
            transition: border-color 0.2s;
            appearance: none; -webkit-appearance: none;
        }
        .form-group input::placeholder { color: #bbb; }
        .form-group select { background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23888' d='M6 8L1 3h10z'/%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 14px center; cursor: pointer; }
        .form-group select option[value=""] { color: #bbb; }
        .form-group input:focus,
        .form-group select:focus { border-color: #1565c0; }

        .password-wrapper { position: relative; }
        .password-wrapper input { padding-right: 42px; }
        .toggle-pw {
            position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
            background: none; border: none; cursor: pointer; color: #888; font-size: 14px;
        }

        .cols-3 { display: grid; grid-template-columns: 1fr 1fr 256px; gap: 20px; }
        .cols-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .col-fields { display: flex; flex-direction: column; gap: 16px; }

        /* ===== ALERTS ===== */
        .alert { border-radius: 8px; font-size: 14px; }

        /* ===== BUTTONS ===== */
        .form-actions {
            display: flex; justify-content: flex-end; gap: 12px;
        }
        .btn-cancel-custom {
            height: 44px; width: 120px; border: 1px solid #e0e0e0; background: white;
            border-radius: 8px; font-size: 14px; font-weight: 600; color: #888;
            cursor: pointer; transition: background 0.2s;
        }
        .btn-cancel-custom:hover { background: #f5f6f8; }
        .btn-save-custom {
            height: 44px; width: 160px; background: #1565c0; border: none;
            border-radius: 8px; font-size: 14px; font-weight: 600; color: white;
            cursor: pointer; transition: background 0.2s;
        }
        .btn-save-custom:hover { background: #1251a3; }
    </style>
</head>
<body>

<!-- SIDEBAR -->
<div class="sidebar">
    <div class="sidebar-brand"><i class="fa fa-building me-2"></i>HRM System</div>
    <a href="${pageContext.request.contextPath}/"><i class="fa fa-chart-line"></i>Tổng quan</a>
<!--    <a><i class="fa fa-briefcase"></i>Công việc</a>-->
    <a href="${pageContext.request.contextPath}/v1/admin/users" class="active-menu">
        <i class="fa fa-users"></i>Người dùng
    </a>
<!--    <a><i class="fa fa-clock"></i>Chấm công</a>
    <a><i class="fa fa-user-plus"></i>Tuyển dụng</a>
    <a><i class="fa fa-money-bill"></i>Bảng lương</a>
    <a><i class="fa fa-building"></i>Phòng ban</a>-->
    <a><i class="fa fa-key"></i>Phân quyền</a>
<!--    <a><i class="fa fa-chart-bar"></i>Báo cáo</a>-->
</div>

<!-- MAIN -->
<div class="main">

    <!-- TOPBAR -->
    <div class="topbar">
        <h5 class="topbar-title">Thêm người dùng mới</h5>
        <div class="topbar-right">
            <button class="bell-btn" title="Thông báo">
                <i class="fa-solid fa-bell"></i>
            </button>
            <div class="admin-avatar"></div>
        </div>
    </div>

    <!-- CONTENT -->
    <div class="content-area">

        <!-- Alerts -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show mb-3" role="alert">
                <i class="fa fa-circle-exclamation me-2"></i>${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success alert-dismissible fade show mb-3" role="alert">
                <i class="fa fa-circle-check me-2"></i>${success}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <!-- FORM CARD -->
        <form action="${pageContext.request.contextPath}/v1/admin/add-user" method="POST" novalidate>
        <div class="form-card">

            <!-- SECTION: Thông tin cơ bản -->
            <p class="section-title">Thông tin cơ bản</p>

            <div class="cols-3">
                <!-- Cột 1 -->
                <div class="col-fields">
                    <div class="form-group">
                        <label for="phone">Username </label>
                        <input type="text" id="username" name="username"
                               placeholder="nguyelebinhan"
                               value="${param.username}"/>
                    </div>                    
                    <div class="form-group">
                        <label for="fullName">Họ và tên <span class="text-danger">*</span></label>
                        <input type="text" id="fullName" name="fullName"
                               placeholder="Nhập họ và tên đầy đủ" required
                               value="${param.fullName}"/>
                    </div>
                    <div class="form-group">
                        <label for="email">Email <span class="text-danger">*</span></label>
                        <input type="email" id="email" name="email"
                               placeholder="example@company.com" required
                               value="${param.email}"/>
                    </div>
                </div>

                <!-- Cột 2 -->
                <div class="col-fields">
                    <div class="form-group">
                        <label for="roleId">Role <span class="text-danger">*</span></label>
                        <select id="roleId" name="role_selection" required>
                            <option value="" disabled <c:if test="${empty param.role_selection}">selected</c:if>>Chọn role</option>
                            <c:forEach items="${roles}" var="r">
                                <option value="${r.roleId}"
                                    <c:if test="${param.role_selection == r.roleId}">selected</c:if>>
                                    ${r.roleName}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="phone">Số điện thoại</label>
                        <input type="text" id="phone" name="phone"
                               placeholder="0901 234 567"
                               value="${param.phone}"/>
                    </div>                            
                </div>

                <!-- Cột 3 -->
                <div class="col-fields">
                    <div class="form-group">
                        <label for="password">Mật khẩu</label>
                        <div class="password-wrapper">
                            <input type="password" id="password" name="password"
                                   placeholder="············" required/>
                            <button type="button" class="toggle-pw" id="togglePassword" title="Hiện/ẩn mật khẩu">
                                <i class="fa fa-eye" id="eyeIcon"></i>
                            </button>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="gender">Giới tính</label>
                        <select id="gender" name="gender">
                            <option value="" disabled <c:if test="${empty param.gender}">selected</c:if>>Chọn giới tính</option>
                            <option value="Nam"  <c:if test="${param.gender == 'Nam'}">selected</c:if>>Nam</option>
                            <option value="Nữ"   <c:if test="${param.gender == 'Nữ'}">selected</c:if>>Nữ</option>
                            <option value="Khác" <c:if test="${param.gender == 'Khác'}">selected</c:if>>Khác</option>
                        </select>
=======
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

            .topbar {
                height: 70px;
                background: white;
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 0 30px;
                border-radius: 10px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.05);
                margin-bottom: 24px;
            }
            .admin-avatar {
                width: 40px;
                height: 40px;
                background: #2b6cb0;
                border-radius: 50%;
            }
            .bell {
                font-size: 20px;
                position: relative;
                cursor: pointer;
            }
            .bell::after {
                content: '';
                position: absolute;
                top: 2px;
                right: -2px;
                width: 8px;
                height: 8px;
                background: red;
                border-radius: 50%;
            }

            .main {
                margin-left: 250px;
                padding: 25px;
            }

            .form-card {
                background: white;
                border-radius: 12px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.06);
                padding: 32px;
                max-width: 720px;
            }
            .form-card .section-title {
                font-size: 1.1rem;
                font-weight: 600;
                color: #0B0E2A;
                margin-bottom: 20px;
                padding-bottom: 10px;
                border-bottom: 2px solid #e2e8f0;
            }
            .form-label {
                font-weight: 500;
                color: #374151;
            }
            .btn-save {
                background: #2b6cb0;
                color: white;
                border: none;
                padding: 10px 28px;
                border-radius: 8px;
                font-weight: 500;
            }
            .btn-save:hover {
                background: #1a4f8a;
                color: white;
            }
            .btn-cancel {
                background: #e2e8f0;
                color: #374151;
                border: none;
                padding: 10px 24px;
                border-radius: 8px;
                font-weight: 500;
            }
            .btn-cancel:hover {
                background: #cbd5e1;
            }
        </style>
    </head>
    <body>


        <jsp:include page="/public/components/adminSideBar.jsp" />


        <div class="main">

            <!-- Topbar -->
            <div class="topbar">
                <div class="d-flex align-items-center gap-2">
                    <a href="${pageContext.request.contextPath}/v1/admin/user-list" class="text-decoration-none text-secondary">
                        <i class="fa fa-arrow-left me-1"></i>
                    </a>
                    <h5 class="mb-0">Thêm người dùng mới</h5>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <div class="bell"><i class="fa-solid fa-bell"></i></div>
                    <div class="d-flex align-items-center gap-2">
                        <div class="admin-avatar"></div>
                        <span>Admin</span>
>>>>>>> main
                    </div>
                </div>
            </div>

<<<<<<< HEAD
            
            <p class="section-title">Thông tin cá nhân</p>

            <div class="cols-2">
                <!-- Cột trái -->
                <div class="col-fields">
                    <div class="form-group">
                        <label for="dob">Ngày sinh</label>
                        <input type="date" id="dob" name="dob"
                               value="${param.dob}"/>
                    </div>
                    <div class="form-group">
                        <label for="address">Địa chỉ</label>
                        <input type="text" id="address" name="address"
                               placeholder="Nhập địa chỉ"
                               value="${param.address}"/>
                    </div>
                </div>

                
                <div class="col-fields">
                    <div class="form-group">
                        <label for="region">Khu vực</label>
                        <input type="text" id="region" name="region"
                               placeholder="Nhập khu vực"
                               value="${param.region}"/>
                    </div>
                </div>
            </div>

            
            <div class="form-actions">
                <a href="${pageContext.request.contextPath}/v1/admin/users"
                   class="btn-cancel-custom d-flex align-items-center justify-content-center text-decoration-none">
                    Hủy
                </a>
                <button type="submit" class="btn-save-custom">Lưu nhân viên</button>
            </div>

        </div>
        </form>

    </div><!-- /content-area -->
</div><!-- /main -->
=======
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
>>>>>>> main

    </body>
</html>
