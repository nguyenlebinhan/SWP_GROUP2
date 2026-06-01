<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Chi tiết nhân viên – HRM Manager</title>
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
            font-weight: 700;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 16px;
        }

        .profile-name {
            font-size: 18px;
            font-weight: 700;
            color: #0B0E2A;
            margin-bottom: 8px;
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
        .status-pill.leave { background: #fef3c7; color: #92400e; }

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

        .empty-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            text-align: center;
            padding: 60px 0;
            color: #9ca3af;
        }
    </style>
</head>
<body>

<c:choose>
    <c:when test="${fn:toUpperCase(fn:replace(sessionScope.user.roleName, ' ', '')) == 'HRMANAGER'}">
        <jsp:include page="/public/components/managerSideBar.jsp" />
    </c:when>
    <c:otherwise>
        <jsp:include page="/public/components/departmentManagerSideBar.jsp" />
    </c:otherwise>
</c:choose>

<div class="main-content">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Chi tiết nhân viên" />
        <jsp:param name="backUrl" value="/v1/manager/employee-list" />
    </jsp:include>

    <!-- Flash messages -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash alert-dismissible fade show" role="alert">
            <c:out value="${error}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty employee}">
            <div class="empty-card">
                Không tìm thấy thông tin nhân viên.
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <!-- LEFT: Profile card -->
                <div class="col-lg-3 col-md-4">
                    <div class="profile-card">
                        <div class="avatar-circle">
                            ${employee.fullName.substring(0,1).toUpperCase()}
                        </div>
                        <div class="profile-name"><c:out value="${employee.fullName}"/></div>
                        <div class="mb-2">
                            <span class="role-pill">
                                <c:out value="${empty employee.roleName ? '-' : employee.roleName}"/>
                            </span>
                        </div>
                        <div>
                            <c:choose>
                                <c:when test="${employee.status == 1}">
                                    <span class="status-pill active">
                                        <span class="status-dot"></span>Đang làm việc
                                    </span>
                                </c:when>
                                <c:when test="${employee.status == 2}">
                                    <span class="status-pill leave">
                                        <span class="status-dot"></span>Đang nghỉ phép
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-pill inactive">
                                        <span class="status-dot"></span>Không hoạt động
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="profile-meta">
                            <div class="meta-item">
                                <span class="meta-label">Mã nhân viên</span>
                                <span class="meta-value">
                                    <c:out value="${empty employee.employeeCode ? '-' : employee.employeeCode}"/>
                                </span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Tên đăng nhập</span>
                                <span class="meta-value">
                                    <c:out value="${empty employee.username ? '-' : employee.username}"/>
                                </span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Email</span>
                                <span class="meta-value">
                                    <c:out value="${empty employee.email ? '-' : employee.email}"/>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- RIGHT: Info panels -->
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
                                        <c:out value="${empty employee.fullName ? '-' : employee.fullName}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Email</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.email ? '-' : employee.email}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Tên đăng nhập</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.username ? '-' : employee.username}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Số điện thoại</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.phoneNumber ? '-' : employee.phoneNumber}"/>
                                    </span>
                                </div>
                            </div>
                        </div>

                        <!-- Thông tin công việc -->
                        <div class="info-section">
                            <div class="section-title">
                                Thông tin công việc
                            </div>
                            <div class="info-grid">
                                <div class="info-item">
                                    <span class="info-label">Mã nhân viên</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.employeeCode ? '-' : employee.employeeCode}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Vai trò</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.roleName ? '-' : employee.roleName}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Phòng ban</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.departmentName ? '-' : employee.departmentName}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Vị trí</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.positionName ? '-' : employee.positionName}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Trạng thái</span>
                                    <span class="info-value">
                                        <c:out value="${employee.statusLabel}"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">Bằng cấp</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.degree ? '-' : employee.degree}"/>
                                    </span>
                                </div>
                                <div class="info-item wide">
                                    <span class="info-label">Kỹ năng</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.skills ? '-' : employee.skills}"/>
                                    </span>
                                </div>
                                <div class="info-item wide">
                                    <span class="info-label">Kinh nghiệm</span>
                                    <span class="info-value">
                                        <c:out value="${empty employee.experience ? '-' : employee.experience}"/>
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
