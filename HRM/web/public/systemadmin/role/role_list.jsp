<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <title>Quản lý vai trò - HRM</title>
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
            .stat-card,
            .table-card {
                background: #fff;
                border: none;
                border-radius: 12px;
                box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            }
            .stat-card {
                padding: 22px;
                height: 100%;
            }
            .stat-label {
                color: #6b7280;
                font-size: 13px;
                font-weight: 600;
                margin-bottom: 6px;
            }
            .stat-value {
                color: #111827;
                font-size: 28px;
                font-weight: 800;
                margin: 0;
            }
            .table thead {
                background: #0B0E2A;
                color: white;
            }
            .table thead th {
                font-weight: 600;
                font-size: 13px;
                letter-spacing: .4px;
                padding: 14px 16px;
                border: none;
            }
            .table tbody td {
                padding: 13px 16px;
                font-size: 14px;
                color: #374151;
                vertical-align: middle;
                border-bottom: 1px solid #f1f3f5;
            }
            .role-code {
                background: #eef2ff;
                color: #3730a3;
                padding: 4px 10px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 700;
            }
            .badge-active,
            .badge-inactive {
                padding: 4px 12px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 700;
            }
            .badge-active {
                background: #d1fae5;
                color: #065f46;
            }
            .badge-inactive {
                background: #fee2e2;
                color: #991b1b;
            }
            .count-pill {
                display: inline-flex;
                min-width: 34px;
                justify-content: center;
                background: #f3f4f6;
                color: #374151;
                border-radius: 20px;
                padding: 4px 10px;
                font-size: 12px;
                font-weight: 700;
            }
            .btn-action {
                padding: 6px 12px;
                font-size: 13px;
                border-radius: 6px;
                text-decoration: none;
                display: inline-flex;
                align-items: center;
                gap: 5px;
                font-weight: 600;
            }
            .btn-view {
                background: #e0f2fe;
                color: #0369a1;
                border: 1px solid #bae6fd;
            }
            .btn-deactivate {
                background: #fee2e2;
                color: #991b1b;
                border: 1px solid #fecaca;
            }
            .btn-activate {
                background: #d1fae5;
                color: #065f46;
                border: 1px solid #a7f3d0;
            }
            .btn-delete {
                background: #fee2e2;
                color: #991b1b;
                border: 1px solid #fecaca;
            }
            .btn-add {
                background: #ff8c00;
                color: #fff;
                border: none;
                padding: 9px 18px;
                border-radius: 8px;
                font-weight: 600;
                font-size: 14px;
                text-decoration: none;
                display: inline-flex;
                align-items: center;
                gap: 8px;
            }
            .btn-add:hover {
                background: #e07b00;
                color: #fff;
            }
            .btn-disabled {
                background: #f3f4f6;
                color: #9ca3af;
                border: 1px solid #e5e7eb;
                cursor: not-allowed;
            }
            .empty-state {
                text-align: center;
                padding: 56px 0;
                color: #9ca3af;
            }
            .empty-state i {
                font-size: 44px;
                display: block;
                margin-bottom: 10px;
            }
        </style>
    </head>
    <body>

        <jsp:include page="/public/components/systemAdminSideBar.jsp" />

        <div class="main-content">
            <jsp:include page="/public/components/systemAdminTopBar.jsp">
                <jsp:param name="title" value="Quản lý phân quyền" />
            </jsp:include>

            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success alert-dismissible fade show" style="border-radius:8px;font-size:14px;margin-bottom:20px" role="alert">
                    <i class="fa fa-circle-check me-2"></i><c:out value="${sessionScope.success}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="success" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-danger alert-dismissible fade show" style="border-radius:8px;font-size:14px;margin-bottom:20px" role="alert">
                    <i class="fa fa-circle-exclamation me-2"></i><c:out value="${sessionScope.error}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="error" scope="session"/>
            </c:if>

            <div class="d-flex justify-content-end mb-4">
                <a href="${pageContext.request.contextPath}/v1/systemadmin/add-role" class="btn-add">
                    Thêm vai trò
                </a>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="stat-label">Tổng vai trò</div>
                        <p class="stat-value">${roles.size()}</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="stat-label">Đang hoạt động</div>
                        <p class="stat-value">${activeRoleCount}</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="stat-label">User đã gán</div>
                        <p class="stat-value">${totalUserAssignments}</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card">
                        <div class="stat-label">Quyền đã gán</div>
                        <p class="stat-value">${totalPermissionAssignments}</p>
                    </div>
                </div>
            </div>

            <div class="table-card">
                <div class="card-body p-0">
                    <table class="table mb-0">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Mã vai trò</th>
                                <th>Tên vai trò</th>
                                <th>Mô tả</th>
                                <th>Người dùng</th>
                                <th>Quyền</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty roles}">
                                    <tr>
                                        <td colspan="8">
                                            <div class="empty-state">
                                                Chưa có vai trò nào
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="r" items="${roles}" varStatus="loop">
                                        <tr>
                                            <td style="color:#9ca3af">${loop.index + 1}</td>
                                            <td><span class="role-code"><c:out value="${r.roleCode}"/></span></td>
                                            <td><strong><c:out value="${r.roleName}"/></strong></td>
                                            <td style="color:#6b7280;max-width:200px">
                                                <c:choose>
                                                    <c:when test="${not empty r.description}"><c:out value="${r.description}"/></c:when>
                                                    <c:otherwise><span style="color:#d1d5db">—</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><span class="count-pill">${userCounts[r.roleId]}</span></td>
                                            <td><span class="count-pill">${permissionCounts[r.roleId]}</span></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${r.isActive == 1}">
                                                        <span class="badge-active"></i>Hoạt động</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge-inactive"></i>Vô hiệu</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div style="display:flex;gap:6px;flex-wrap:wrap">
                                                    <a href="${pageContext.request.contextPath}/v1/systemadmin/role-detail?id=${r.roleId}" class="btn-action btn-view">
                                                        Chi tiết
                                                    </a>
                                                    <a href="${pageContext.request.contextPath}/v1/systemadmin/update-role?id=${r.roleId}" class="btn-action btn-view">
                                                        Sửa
                                                    </a>
                                                    <c:if test="${r.roleCode != 'SA' && r.roleCode != 'BA'}">
                                                        <a href="${pageContext.request.contextPath}/v1/systemadmin/edit-role-permissions?id=${r.roleId}" class="btn-action" style="background:#fef3c7;color:#92400e;border:1px solid #fde68a">
                                                            Phân quyền
                                                        </a>
                                                    </c:if>
                                                    <c:choose>
                                                        <c:when test="${r.roleName == 'Admin'}">
                                                            <span class="btn-action btn-disabled" title="Không thể thay đổi trạng thái vai trò ADMIN">
                                                                Được bảo vệ
                                                            </span>
                                                        </c:when>
                                                        <c:when test="${r.isActive == 1}">
                                                            <a href="${pageContext.request.contextPath}/v1/systemadmin/change-status-role?id=${r.roleId}&status=0"
                                                               class="btn-action btn-deactivate"
                                                               onclick="return confirm('Vô hiệu hóa vai trò ${r.roleName}?')">
                                                                Vô hiệu hóa
                                                            </a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <a href="${pageContext.request.contextPath}/v1/systemadmin/change-status-role?id=${r.roleId}&status=1"
                                                               class="btn-action btn-activate"
                                                               onclick="return confirm('Kích hoạt vai trò ${r.roleName}?')">
                                                                Kích hoạt
                                                            </a>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <c:choose>    
                                                        <c:when test="${r.roleName == 'Admin'}">
                                                            <span class="btn-action btn-disabled" title="Không thể xóa vai trò ADMIN">
                                                                Được bảo vệ
                                                            </span>
                                                        </c:when>  
                                                        <c:otherwise>
                                                            <a href="${pageContext.request.contextPath}/v1/systemadmin/delete-role?id=${r.roleId}" class="btn-action btn-delete">
                                                                Xóa
                                                            </a>                                        
                                                        </c:otherwise>

                                                    </c:choose>
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
