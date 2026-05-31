<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Chi tiết người dùng – HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <style>
        body {
            background: #f5f6fa;
            font-family: 'Segoe UI', sans-serif;
        }

        .main-content {
            margin-left: 250px;
            padding: 30px;
        }


        .alert-flash {
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 20px;
        }

        /* Profile card */
        .profile-card {
            background: white;
            border-radius: 12px;
            padding: 32px 24px;
            text-align: center;
            height: 100%;
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
        }

        .status-pill.active { background: #d1fae5; color: #065f46; }
        .status-pill.inactive { background: #fee2e2; color: #991b1b; }

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

        .meta-item:last-child { margin-bottom: 0; }

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
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 32px 34px;
            height: 100%;
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

        .section-title i { color: #ff8c00; margin-right: 6px; }

        .info-section + .info-section {
            margin-top: 30px;
            padding-top: 24px;
        }

        .info-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            column-gap: 40px;
        }

        .info-item {
            padding: 12px 0;
            border-bottom: 1px solid #f1f3f5;
        }

        .info-item.wide { grid-column: 1 / -1; }

        .info-label {
            display: block;
            font-size: 11px;
            font-weight: 600;
            color: #9ca3af;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 5px;
        }

        .info-value {
            display: block;
            font-size: 14px;
            font-weight: 600;
            color: #111827;
        }

        .badge-active {
            background: #d1fae5; color: #065f46;
            padding: 3px 10px; border-radius: 20px;
            font-size: 12px; font-weight: 600;
        }

        .badge-inactive {
            background: #fee2e2; color: #991b1b;
            padding: 3px 10px; border-radius: 20px;
            font-size: 12px; font-weight: 600;
        }

        .badge-temp {
            background: #fef3c7; color: #92400e;
            padding: 3px 10px; border-radius: 20px;
            font-size: 12px; font-weight: 600;
        }

        .badge-set {
            background: #d1fae5; color: #065f46;
            padding: 3px 10px; border-radius: 20px;
            font-size: 12px; font-weight: 600;
        }

        .empty-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            text-align: center;
            padding: 60px 0;
            color: #9ca3af;
        }

        .empty-card i { font-size: 48px; margin-bottom: 12px; display: block; }
    </style>
</head>
<body>

<jsp:include page="/public/components/systemAdminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/systemAdminTopBar.jsp">
        <jsp:param name="title" value="Chi tiết người dùng" />
        <jsp:param name="backUrl" value="/v1/systemadmin/user-list" />
    </jsp:include>

    <!-- Flash messages -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash alert-dismissible fade show" role="alert">
            <i class="fa fa-circle-exclamation me-2"></i><c:out value="${error}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty selectedUser}">
            <div class="empty-card">
                
                Không tìm thấy thông tin người dùng.
            </div>
        </c:when>
        <c:otherwise>
            <c:set var="displayName" value="${empty selectedUser.fullName ? selectedUser.username : selectedUser.fullName}"/>
            <div class="row g-4">
                <div class="col-lg-3 col-md-4">
                    <div class="profile-card">
                        <div class="avatar-circle">
                            <i class="fa fa-user"></i>
                        </div>
                        <div class="profile-name"><c:out value="${displayName}"/></div>
                        <div class="mb-2">
                            <span class="role-pill">
                                <c:out value="${empty selectedUser.roleName ? '-' : selectedUser.roleName}"/>
                            </span>
                        </div>
                        <div>
                            <c:choose>
                                <c:when test="${selectedUser.isActive == 1}">
                                    <span class="status-pill active">
                                        <span class="status-dot"></span>Hoạt động
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-pill inactive">
                                        <span class="status-dot"></span>Vô hiệu
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="profile-meta">
                            <div class="meta-item">
                                <span class="meta-label">Vai trò</span>
                                <span class="meta-value">
                                    <c:out value="${empty selectedUser.roleName ? '-' : selectedUser.roleName}"/>
                                </span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Tên đăng nhập</span>
                                <span class="meta-value">
                                    <c:out value="${empty selectedUser.username ? '-' : selectedUser.username}"/>
                                </span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Email</span>
                                <span class="meta-value">
                                    <c:out value="${empty selectedUser.email ? '-' : selectedUser.email}"/>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Right: Info panels -->
                <div class="col-lg-9 col-md-8">
                    <div class="info-panel">

                        <!-- Thông tin cơ bản -->
                        <div class="info-section">
                            <div class="section-title">
                                Thông tin cơ bản
                            </div>
                            <div class="info-grid">
                                <div class="info-item">
                                    <span class="info-label">Họ và tên</span>
                                    <span class="info-value">
                                        <c:out value="${empty selectedUser.fullName ? '-' : selectedUser.fullName}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Ngày sinh</span>
                                    <span class="info-value">
                                        <c:out value="${empty selectedUser.dob ? '-' : selectedUser.dob}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Tên đăng nhập</span>
                                    <span class="info-value">
                                        <c:out value="${empty selectedUser.username ? '-' : selectedUser.username}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Email</span>
                                    <span class="info-value">
                                        <c:out value="${empty selectedUser.email ? '-' : selectedUser.email}"/>
                                    </span>
                                </div>
                                <div class="info-item wide">
                                    <span class="info-label">Địa chỉ</span>
                                    <span class="info-value">
                                        <c:out value="${empty selectedUser.address ? '-' : selectedUser.address}"/>
                                    </span>
                                </div>
                            </div>
                        </div>

                        <!-- Thông tin tài khoản -->
                        <div class="info-section">
                            <div class="section-title">
                                Thông tin tài khoản
                            </div>
                            <div class="info-grid">
                                <div class="info-item">
                                    <span class="info-label">Mã nhân viên</span>
                                    <span class="info-value">NV<c:out value="${selectedUser.userId}"/></span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Vai trò</span>
                                    <span class="info-value">
                                        <c:out value="${empty selectedUser.roleName ? '-' : selectedUser.roleName}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Trạng thái tài khoản</span>
                                    <span class="info-value">
                                        <c:choose>
                                            <c:when test="${selectedUser.isActive == 1}">
                                                <span class="badge-active">
                                                    >Hoạt động
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-inactive">
                                                    Vô hiệu
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Trạng thái mật khẩu</span>
                                    <span class="info-value">
                                        <c:choose>
                                            <c:when test="${selectedUser.isTemporaryPassword}">
                                                <span class="badge-temp">
                                                    Mật khẩu tạm thời
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-set">
                                                    Đã thiết lập
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>

            </div>
        </c:otherwise>
    </c:choose>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
