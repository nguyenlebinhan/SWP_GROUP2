<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Chi tiết vai trò - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main-content { margin-left: 250px; padding: 30px; }
        .page-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 24px;
        }
        .back-btn {
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
        }
        .page-header h5 { font-weight: 700; color: #0B0E2A; margin: 0; }
        .role-card,
        .detail-panel {
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            height: 100%;
        }
        .role-card { padding: 32px 24px; text-align: center; }
        .role-icon {
            width: 96px;
            height: 96px;
            border-radius: 50%;
            background: #e0e7ff;
            color: #3730a3;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 16px;
            font-size: 40px;
        }
        .role-name { font-size: 20px; font-weight: 800; color: #111827; margin-bottom: 10px; }
        .role-code {
            display: inline-block;
            background: #eef2ff;
            color: #3730a3;
            padding: 4px 14px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
            margin-bottom: 10px;
        }
        .badge-active,
        .badge-inactive {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 14px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 700;
        }
        .badge-active { background: #d1fae5; color: #065f46; }
        .badge-inactive { background: #fee2e2; color: #991b1b; }
        .meta-list {
            margin-top: 24px;
            padding-top: 20px;
            border-top: 1px solid #f1f3f5;
            text-align: left;
        }
        .meta-item { margin-bottom: 16px; }
        .meta-label {
            display: block;
            color: #9ca3af;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: .5px;
            text-transform: uppercase;
            margin-bottom: 4px;
        }
        .meta-value { display: block; color: #111827; font-size: 14px; font-weight: 700; }
        .detail-panel { padding: 32px 34px; }
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
        .section-block + .section-block { margin-top: 30px; }
        .permission-item,
        .user-item {
            border: 1px solid #f1f3f5;
            border-radius: 8px;
            padding: 14px 16px;
            margin-bottom: 10px;
            background: #fff;
        }
        .permission-code {
            color: #0369a1;
            background: #e0f2fe;
            border-radius: 20px;
            padding: 3px 10px;
            font-size: 12px;
            font-weight: 700;
        }
        .permission-table-wrap {
            overflow: hidden;
            border: 1px solid #e8ebf0;
            border-radius: 8px;
        }
        .permission-table {
            width: 100%;
            margin: 0;
            border-collapse: collapse;
            table-layout: fixed;
        }
        .permission-table thead th {
            height: 44px;
            background: #f3f5f8;
            color: #42526e;
            border-bottom: 1px solid #e8ebf0;
            font-size: 12px;
            font-weight: 700;
            text-align: center;
        }
        .permission-table thead th:first-child {
            width: 42%;
            padding-left: 18px;
            text-align: left;
        }
        .permission-table tbody td {
            height: 52px;
            padding: 0 18px;
            border-bottom: 1px solid #eef1f5;
            color: #263238;
            font-size: 13px;
            vertical-align: middle;
            text-align: center;
        }
        .permission-table tbody tr:nth-child(even) td { background: #fbfbfd; }
        .permission-table tbody tr:last-child td { border-bottom: 0; }
        .permission-table tbody td:first-child {
            text-align: left;
            font-weight: 500;
        }
        .matrix-check {
            width: 18px;
            height: 18px;
            margin: 0;
            border-radius: 5px;
            accent-color: #1f70c8;
            pointer-events: none;
        }
        .permission-empty {
            width: 18px;
            height: 18px;
            display: inline-block;
            border-radius: 5px;
            background: #f0f2f5;
        }
        .muted { color: #6b7280; font-size: 13px; }
        .btn-edit-permissions {
            height: 40px;
            padding: 0 18px;
            border-radius: 8px;
            display: inline-flex;
            align-items: center;
            gap: 7px;
            background: #1f70c8;
            color: #fff;
            border: 1px solid #1f70c8;
            font-weight: 700;
            font-size: 14px;
            text-decoration: none;
        }
        .btn-edit-permissions:hover {
            color: #fff;
            background: #185ca6;
            border-color: #185ca6;
        }
        .btn-disabled {
            height: 40px;
            padding: 0 18px;
            border-radius: 8px;
            display: inline-flex;
            align-items: center;
            gap: 7px;
            background: #f3f4f6;
            color: #9ca3af;
            border: 1px solid #e5e7eb;
            font-weight: 700;
            font-size: 14px;
            cursor: not-allowed;
        }
        .alert-flash { border-radius: 8px; font-size: 14px; margin-bottom: 20px; }
        .empty-card {
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            text-align: center;
            padding: 58px 0;
            color: #9ca3af;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/adminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/adminTopBar.jsp">
        <jsp:param name="title" value="Chi tiết vai trò" />
    </jsp:include>

    <div class="page-header">
        <a href="${pageContext.request.contextPath}/v1/admin/role-list" class="back-btn" title="Quay lại">
            <i class="fa fa-arrow-left"></i>
        </a>
        <h5><i class="fa fa-shield-halved me-2" style="color:#ff8c00"></i>Chi tiết vai trò</h5>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash">
            <i class="fa fa-circle-exclamation me-2"></i><c:out value="${error}"/>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty selectedRole}">
            <div class="empty-card">
                <i class="fa fa-shield-halved fa-3x mb-3"></i>
                Không tìm thấy thông tin vai trò.
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <div class="col-lg-3 col-md-4">
                    <div class="role-card">
                        <div class="role-icon">
                            <i class="fa fa-shield-halved"></i>
                        </div>
                        <div class="role-name"><c:out value="${selectedRole.roleName}"/></div>
                        <div class="role-code"><c:out value="${selectedRole.roleCode}"/></div>
                        <div>
                            <c:choose>
                                <c:when test="${selectedRole.isActive == 1}">
                                    <span class="badge-active"><i class="fa fa-circle-check"></i>Hoạt động</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge-inactive"><i class="fa fa-circle-xmark"></i>Vô hiệu</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="meta-list">
                            <div class="meta-item">
                                <span class="meta-label">Mã vai trò</span>
                                <span class="meta-value"><c:out value="${selectedRole.roleCode}"/></span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Số quyền</span>
                                <span class="meta-value">${permissions.size()}</span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Số người dùng</span>
                                <span class="meta-value">${roleUsers.size()}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-9 col-md-8">
                    <div class="detail-panel">
                        <div class="d-flex gap-2 justify-content-end mb-3">
                            <span class="btn-disabled" title="Chức năng sẽ được bổ sung sau">
                                <i class="fa fa-pen"></i> Sửa vai trò
                            </span>
                            <a class="btn-edit-permissions" href="${pageContext.request.contextPath}/v1/admin/role-permissions?roleId=${selectedRole.roleId}">
                                <i class="fa fa-pen"></i> Sửa phân quyền
                            </a>
                            <span class="btn-disabled" title="Chức năng sẽ được bổ sung sau">
                                <i class="fa fa-trash"></i> Xóa vai trò
                            </span>
                        </div>

                        <div class="section-block">
                            <div class="section-title">
                                <i class="fa fa-key"></i>Quyền được cấp
                            </div>
                            <c:choose>
                                <c:when test="${empty permissions}">
                                    <div class="muted">Vai trò này chưa được gán quyền nào.</div>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="p" items="${permissions}">
                                        <div class="permission-item">
                                            <div class="d-flex justify-content-between align-items-center gap-3">
                                                <strong><c:out value="${p.permissionName}"/></strong>
                                                <span class="permission-code"><c:out value="${p.permissionCode}"/></span>
                                            </div>
                                            <div class="muted mt-1">
                                                <c:out value="${empty p.description ? 'Không có mô tả' : p.description}"/>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${not empty permissionMatrixRows}">
                                <div class="permission-table-wrap mt-3">
                                    <table class="permission-table">
                                        <thead>
                                            <tr>
                                                <th>Tính năng</th>
                                                <c:forEach items="${permissionActions}" var="action">
                                                    <th><c:out value="${action.label}"/></th>
                                                </c:forEach>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach items="${permissionMatrixRows}" var="row">
                                                <tr>
                                                    <td><c:out value="${row.featureName}"/></td>
                                                    <c:forEach items="${permissionActions}" var="action">
                                                        <c:set var="permission" value="${row.permissionsByAction[action.key]}"/>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${not empty permission}">
                                                                    <input class="matrix-check" type="checkbox" disabled
                                                                           <c:if test="${grantedPermissionMap[permission.permissionId]}">checked</c:if>/>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="permission-empty" aria-hidden="true"></span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </c:forEach>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:if>
                        </div>

                        <div class="section-block">
                            <div class="section-title">
                                <i class="fa fa-users"></i>Người dùng thuộc vai trò
                            </div>
                            <c:choose>
                                <c:when test="${empty roleUsers}">
                                    <div class="muted">Chưa có người dùng nào thuộc vai trò này.</div>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="u" items="${roleUsers}">
                                        <div class="user-item">
                                            <div class="d-flex justify-content-between align-items-center gap-3">
                                                <div>
                                                    <strong><c:out value="${empty u.fullName ? u.username : u.fullName}"/></strong>
                                                    <div class="muted"><c:out value="${u.email}"/></div>
                                                </div>
                                                <a href="${pageContext.request.contextPath}/v1/admin/view-user-detail?id=${u.userId}" class="btn btn-sm btn-outline-primary">
                                                    Chi tiết
                                                </a>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
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
