<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Hồ sơ của tôi - HRM</title>
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

        .page-header h5 {
            font-weight: 700;
            color: #0B0E2A;
            margin: 0;
        }

        .profile-card,
        .info-panel {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            height: 100%;
        }

        .profile-card {
            padding: 32px 24px;
            text-align: center;
        }

        .avatar-circle {
            width: 96px;
            height: 96px;
            border-radius: 50%;
            background: #dbe7f6;
            color: #1267c5;
            font-size: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 16px;
        }

        .profile-name {
            font-size: 18px;
            font-weight: 700;
            color: #111827;
            margin: 0 0 10px;
        }

        .role-pill {
            display: inline-block;
            background: #e0e7ff;
            color: #3730a3;
            padding: 4px 14px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
            margin-bottom: 8px;
        }

        .status-pill {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 14px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
            background: #d1fae5;
            color: #065f46;
        }

        .status-dot {
            width: 7px;
            height: 7px;
            border-radius: 50%;
            background: currentColor;
        }

        .profile-meta {
            margin-top: 24px;
            padding-top: 20px;
            border-top: 1px solid #f1f3f5;
            text-align: left;
        }

        .meta-item {
            margin-bottom: 16px;
        }

        .meta-label {
            display: block;
            font-size: 11px;
            font-weight: 600;
            color: #9ca3af;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 4px;
        }

        .meta-value {
            display: block;
            font-size: 14px;
            font-weight: 600;
            color: #111827;
            word-break: break-all;
        }

        .info-panel {
            padding: 32px 34px;
        }

        .section-title {
            font-size: 14px;
            font-weight: 700;
            color: #0B0E2A;
            text-transform: uppercase;
            letter-spacing: .6px;
            margin: 0 0 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #f1f3f5;
        }

        .section-title i {
            color: #ff8c00;
            margin-right: 6px;
        }

        .form-label {
            font-size: 13px;
            font-weight: 600;
            color: #374151;
            margin-bottom: 6px;
        }

        .form-control {
            height: 44px;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            font-size: 14px;
            color: #1f2937;
            padding: 0 14px;
        }

        .form-control:focus {
            border-color: #1565c0;
            box-shadow: 0 0 0 3px rgba(21,101,192,.1);
        }

        textarea.form-control {
            height: 92px;
            padding-top: 12px;
            resize: vertical;
        }

        .readonly-field {
            background: #f9fafb;
            color: #6b7280;
        }

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

        .btn-save:hover {
            background: #e07b00;
            color: white;
        }

        .alert-flash {
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 20px;
        }

        .required {
            color: #ef4444;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/adminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/adminTopBar.jsp">
        <jsp:param name="title" value="Hồ sơ của tôi" />
    </jsp:include>


    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-flash alert-dismissible fade show" role="alert">
            <i class="fa fa-circle-check me-2"></i><c:out value="${sessionScope.success}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash alert-dismissible fade show" role="alert">
            <i class="fa fa-circle-exclamation me-2"></i><c:out value="${error}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${not empty currentUser}">
        <c:set var="displayName" value="${empty currentUser.fullName ? currentUser.username : currentUser.fullName}"/>
        <div class="row g-4">
            <div class="col-lg-3 col-md-4">
                <div class="profile-card">
                    <div class="avatar-circle">
                        <i class="fa fa-user"></i>
                    </div>
                    <div class="profile-name"><c:out value="${displayName}"/></div>
                    <div class="mb-2">
                        <span class="role-pill">
                            <c:out value="${empty currentUser.roleName ? '-' : currentUser.roleName}"/>
                        </span>
                    </div>
                    <c:if test="${currentUser.isActive == 1}">
                        <span class="status-pill">
                            <span class="status-dot"></span>Hoạt động
                        </span>
                    </c:if>

                    <div class="profile-meta">
                        <div class="meta-item">
                            <span class="meta-label">Mã nhân viên</span>
                            <span class="meta-value">NV<c:out value="${currentUser.userId}"/></span>
                        </div>
                        <div class="meta-item">
                            <span class="meta-label">Tên đăng nhập</span>
                            <span class="meta-value"><c:out value="${currentUser.username}"/></span>
                        </div>
                        <div class="meta-item">
                            <span class="meta-label">Email</span>
                            <span class="meta-value"><c:out value="${currentUser.email}"/></span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-9 col-md-8">
                <div class="info-panel">
                    <form action="${pageContext.request.contextPath}/v1/admin/my-profile" method="POST">
                        <div class="section-title">
                            <i class="fa fa-id-card"></i>Thông tin cá nhân
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label class="form-label" for="username">Tên đăng nhập <span class="required">*</span></label>
                                <input type="text" id="username" name="username" class="form-control"
                                       value="<c:out value='${currentUser.username}'/>" required/>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="fullName">Họ và tên <span class="required">*</span></label>
                                <input type="text" id="fullName" name="fullName" class="form-control"
                                       value="<c:out value='${currentUser.fullName}'/>" required/>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="dob">Ngày sinh</label>
                                <input type="date" id="dob" name="dob" class="form-control"
                                       value="<c:out value='${currentUser.dob}'/>"/>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="email">Email</label>
                                <input type="email" id="email" class="form-control readonly-field"
                                       value="<c:out value='${currentUser.email}'/>" readonly/>
                            </div>
                            <div class="col-12">
                                <label class="form-label" for="address">Địa chỉ</label>
                                <textarea id="address" name="address" class="form-control"><c:out value="${currentUser.address}"/></textarea>
                            </div>
                        </div>

                        <div class="section-title">
                            <i class="fa fa-lock"></i>Thông tin tài khoản
                        </div>
                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label class="form-label" for="roleName">Vai trò</label>
                                <input type="text" id="roleName" class="form-control readonly-field"
                                       value="<c:out value='${currentUser.roleName}'/>" readonly/>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="accountStatus">Trạng thái</label>
                                <input type="text" id="accountStatus" class="form-control readonly-field"
                                       value="${currentUser.isActive == 1 ? 'Hoạt động' : 'Vô hiệu'}" readonly/>
                            </div>
                        </div>

                        <button type="submit" class="btn-save">
                            <i class="fa fa-floppy-disk me-2"></i>Lưu thay đổi
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
