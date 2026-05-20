<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Quản lý người dùng – HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
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
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
        }

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

        .table thead {
            background: #0B0E2A;
            color: white;
        }

        .table thead th {
            font-weight: 600;
            font-size: 13px;
            letter-spacing: 0.4px;
            padding: 14px 16px;
            border: none;
        }

        .table tbody tr {
            vertical-align: middle;
        }

        .table tbody td {
            padding: 12px 16px;
            font-size: 14px;
            color: #374151;
            border-bottom: 1px solid #f1f3f5;
        }

        .table tbody tr:last-child td {
            border-bottom: none;
        }

        .table tbody tr:hover {
            background: #f8f9ff;
        }

        .badge-active {
            background: #d1fae5;
            color: #065f46;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }

        .badge-inactive {
            background: #fee2e2;
            color: #991b1b;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }

        .btn-action {
            padding: 5px 12px;
            font-size: 13px;
            border-radius: 6px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 5px;
            font-weight: 500;
            transition: opacity .2s;
        }

        .btn-action:hover { opacity: .85; }

        .btn-edit {
            background: #e0f2fe;
            color: #0369a1;
            border: 1px solid #bae6fd;
        }

        .btn-delete {
            background: #fee2e2;
            color: #b91c1c;
            border: 1px solid #fecaca;
        }

        .btn-activate {
            background: #d1fae5;
            color: #065f46;
            border: 1px solid #6ee7b7;
        }

        .btn-deactivate {
            background: #fef3c7;
            color: #92400e;
            border: 1px solid #fde68a;
        }

        .btn-add {
            background: #ff8c00;
            color: white;
            border: none;
            padding: 9px 20px;
            border-radius: 8px;
            font-weight: 600;
            font-size: 14px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: background .2s;
        }

        .btn-add:hover {
            background: #e07b00;
            color: white;
        }

        .alert-flash {
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 20px;
        }

        .empty-state {
            text-align: center;
            padding: 60px 0;
            color: #9ca3af;
        }

        .empty-state i {
            font-size: 48px;
            margin-bottom: 12px;
            display: block;
        }
    </style>
</head>
<body>

    <jsp:include page="/public/components/adminSideBar.jsp" />

    <div class="main-content">

        <!-- Flash messages -->
        <c:if test="${not empty sessionScope.success}">
            <div class="alert alert-success alert-flash alert-dismissible fade show" role="alert">
                <i class="fa fa-circle-check me-2"></i>${sessionScope.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="success" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.error}">
            <div class="alert alert-danger alert-flash alert-dismissible fade show" role="alert">
                <i class="fa fa-circle-xmark me-2"></i>${sessionScope.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="error" scope="session"/>
        </c:if>

        <!-- Header -->
        <div class="page-header">
            <h5><i class="fa fa-users me-2" style="color:#ff8c00"></i>Quản lý người dùng</h5>
            <a href="${pageContext.request.contextPath}/v1/admin/add-user" class="btn-add">
                <i class="fa fa-user-plus"></i> Thêm người dùng
            </a>
        </div>

        <!-- Table -->
        <div class="card">
            <div class="card-body p-0">
                <table class="table mb-0">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Tên đăng nhập</th>
                            <th>Họ và tên</th>
                            <th>Email</th>
                            <th>Vai trò</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty list}">
                                <tr>
                                    <td colspan="7">
                                        <div class="empty-state">
                                            <i class="fa fa-users-slash"></i>
                                            Chưa có người dùng nào
                                        </div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="u" items="${list}" varStatus="loop">
                                    <tr>
                                        <td style="color:#9ca3af;font-size:13px">${loop.index + 1}</td>
                                        <td><strong>${u.username}</strong></td>
                                        <td>${u.fullName}</td>
                                        <td style="color:#6b7280">${u.email}</td>
                                        <td>
                                            <span style="background:#e0e7ff;color:#3730a3;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600">
                                                ${u.roleName}
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${u.isActive == 1}">
                                                    <span class="badge-active"><i class="fa fa-circle-check me-1"></i>Hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-inactive"><i class="fa fa-circle-xmark me-1"></i>Vô hiệu</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display:flex;gap:6px;flex-wrap:wrap">
                                                <a href="${pageContext.request.contextPath}/v1/admin/view-user-detail?id=${u.userId}"
                                                   class="btn-action btn-edit">
                                                    <i class="fa fa-eye"></i> Chi tiết
                                                </a>

                                                <c:choose>
                                                    <c:when test="${u.isActive == 1}">
                                                        <a href="${pageContext.request.contextPath}/v1/admin/change-status?id=${u.userId}&status=0"
                                                           class="btn-action btn-deactivate"
                                                           onclick="return confirm('Vô hiệu hóa tài khoản ${u.fullName}?')">
                                                            <i class="fa fa-ban"></i> Vô hiệu hóa
                                                        </a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <a href="${pageContext.request.contextPath}/v1/admin/change-status?id=${u.userId}&status=1"
                                                           class="btn-action btn-activate"
                                                           onclick="return confirm('Kích hoạt tài khoản ${u.fullName}?')">
                                                            <i class="fa fa-circle-check"></i> Kích hoạt
                                                        </a>
                                                    </c:otherwise>
                                                </c:choose>

                                                <a href="${pageContext.request.contextPath}/v1/admin/delete-user?id=${u.userId}"
                                                   class="btn-action btn-delete"
                                                   onclick="return confirm('Xóa tài khoản ${u.fullName}? Hành động này không thể hoàn tác.')">
                                                    <i class="fa fa-trash"></i> Xóa
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
