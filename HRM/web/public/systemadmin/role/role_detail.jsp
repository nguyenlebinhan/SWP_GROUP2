<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Chi tiết vai trò - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
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
        .perm-group-title {
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: .5px;
            color: #9ca3af;
            margin: 18px 0 10px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .perm-group-title::after { content: ''; flex: 1; height: 1px; background: #f1f3f5; }
        .perm-tile {
            border: 1.5px solid #e5e7eb;
            border-radius: 10px;
            padding: 12px 14px;
            background: #fafafa;
            transition: border-color .15s;
        }
        .perm-tile.assigned {
            border-color: #6366f1;
            background: #eef2ff;
        }
        .perm-tile-name {
            font-size: 13px;
            font-weight: 700;
            color: #374151;
            margin-bottom: 3px;
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .perm-tile.assigned .perm-tile-name { color: #3730a3; }
        .perm-tile-code {
            display: inline-block;
            font-size: 10px;
            font-weight: 700;
            padding: 1px 8px;
            border-radius: 20px;
            background: #e5e7eb;
            color: #6b7280;
        }
        .perm-tile.assigned .perm-tile-code { background: #c7d2fe; color: #3730a3; }
        .perm-tile-desc { font-size: 11px; color: #9ca3af; margin-top: 4px; line-height: 1.4; }
        .perm-tile.assigned .perm-tile-desc { color: #6366f1; }
        .perm-check { color: #6366f1; font-size: 13px; }
        .perm-uncheck { color: #d1d5db; font-size: 13px; }
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
        .muted { color: #6b7280; font-size: 13px; }
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
        .btn-edit-role {
            height: 40px;
            padding: 0 18px;
            border-radius: 8px;
            display: inline-flex;
            align-items: center;
            gap: 7px;
            background: #e0f2fe;
            color: #0369a1;
            border: 1px solid #bae6fd;
            font-weight: 700;
            font-size: 14px;
            text-decoration: none;
            transition: opacity .2s;
        }
        .btn-edit-role:hover { opacity: .85; color: #0369a1; }
        .btn-add-role {
            height: 40px;
            padding: 0 18px;
            border-radius: 8px;
            display: inline-flex;
            align-items: center;
            gap: 7px;
            background: #ff8c00;
            color: white;
            border: 1px solid #ff8c00;
            font-weight: 700;
            font-size: 14px;
            text-decoration: none;
            transition: opacity .2s;
        }
        .btn-add-role:hover { opacity: .85; color: white; }
        .btn-delete-role {
            height: 40px;
            padding: 0 18px;
            border-radius: 8px;
            display: inline-flex;
            align-items: center;
            gap: 7px;
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
            font-weight: 700;
            font-size: 14px;
            text-decoration: none;
            transition: opacity .2s;
        }
        .btn-delete-role:hover { opacity: .85; color: #991b1b; }
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

<jsp:include page="/public/components/systemAdminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/systemAdminTopBar.jsp">
        <jsp:param name="title" value="Chi tiết vai trò" />
        <jsp:param name="backUrl" value="/v1/systemadmin/role-list" />
    </jsp:include>

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
            <c:set var="isSystemAdmin" value="${selectedRole.roleCode == 'SA'}"/>
            <c:set var="isBusinessAdmin" value="${selectedRole.roleCode == 'BA'}"/>
            <c:set var="isSpecialRole" value="${isSystemAdmin || isBusinessAdmin}"/>
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
                                    <span class="badge-active"><i class="fa fa-circle-check me-1"></i>Hoạt động</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge-inactive"><i class="fa fa-circle-xmark me-1"></i>Vô hiệu</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="meta-list">
                            <div class="meta-item">
                                <span class="meta-label">Mã vai trò</span>
                                <span class="meta-value"><c:out value="${selectedRole.roleCode}"/></span>
                            </div>
                            <div class="meta-item">
                                <span class="meta-label">Số quyền được gán</span>
                                <span class="meta-value">${assignedCount} / ${allPermissions.size()}</span>
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

                        <div class="section-block">
                            <div class="d-flex justify-content-between align-items-center mb-0" style="padding-bottom:10px;border-bottom:2px solid #f1f3f5;margin-bottom:20px">
                                <div class="section-title" style="border:none;padding:0;margin:0">
                                    Quyền hệ thống
                                </div>
                                <div class="d-flex align-items-center gap-2">
                                    <span style="font-size:12px;color:#6b7280">
                                        <span style="color:#6366f1;font-weight:700">${assignedCount}</span>
                                        / ${allPermissions.size()} quyền được gán
                                    </span>
                                    <c:if test="${!isSpecialRole}">
                                        <a href="${pageContext.request.contextPath}/v1/systemadmin/edit-role-permissions?id=${selectedRole.roleId}"
                                           class="btn-edit-role" style="height:34px;padding:0 14px;font-size:13px">
                                            <i class="fa fa-pen-to-square"></i> Phân quyền
                                        </a>
                                    </c:if>
                                </div>
                            </div>

                            <c:if test="${empty allPermissions}">
                                <div class="muted">Chưa có quyền nào trong hệ thống.</div>
                            </c:if>

                            <c:if test="${isSystemAdmin || !isSpecialRole}">
                                <div class="perm-group-title"><i class="fa fa-user"></i> Người dùng</div>
                                <div class="row g-2 mb-1">
                                    <c:forEach var="p" items="${allPermissions}">
                                        <c:if test="${p.permissionCode == 'VIEW_USERS' || p.permissionCode == 'ADD_USER' || p.permissionCode == 'EDIT_USER' || p.permissionCode == 'DELETE_USER'}">
                                            <div class="col-md-3">
                                                <div class="perm-tile ${assignedPermissionIds.contains(p.permissionId) ? 'assigned' : ''}">
                                                    <div class="perm-tile-name">
                                                        <c:choose>
                                                            <c:when test="${assignedPermissionIds.contains(p.permissionId)}">
                                                                <i class="fa fa-circle-check perm-check"></i>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <i class="fa fa-circle perm-uncheck"></i>
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <c:out value="${p.permissionName}"/>
                                                    </div>
                                                    <span class="perm-tile-code"><c:out value="${p.permissionCode}"/></span>
                                                    <div class="perm-tile-desc"><c:out value="${empty p.description ? '—' : p.description}"/></div>
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </c:if>

                            <c:if test="${isSystemAdmin || !isSpecialRole}">
                                <div class="perm-group-title"><i class="fa fa-shield-halved"></i> Vai trò &amp; Phân quyền</div>
                                <div class="row g-2 mb-1">
                                    <c:forEach var="p" items="${allPermissions}">
                                        <c:if test="${p.permissionCode == 'VIEW_ROLES' || p.permissionCode == 'ADD_ROLE' || p.permissionCode == 'EDIT_ROLE' || p.permissionCode == 'DELETE_ROLE' || p.permissionCode == 'MANAGE_PERMISSIONS'}">
                                            <div class="col-md-3">
                                                <div class="perm-tile ${assignedPermissionIds.contains(p.permissionId) ? 'assigned' : ''}">
                                                    <div class="perm-tile-name">
                                                        <c:choose>
                                                            <c:when test="${assignedPermissionIds.contains(p.permissionId)}">
                                                                <i class="fa fa-circle-check perm-check"></i>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <i class="fa fa-circle perm-uncheck"></i>
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <c:out value="${p.permissionName}"/>
                                                    </div>
                                                    <span class="perm-tile-code"><c:out value="${p.permissionCode}"/></span>
                                                    <div class="perm-tile-desc"><c:out value="${empty p.description ? '—' : p.description}"/></div>
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            <c:if test="${isSystemAdmin || !isSpecialRole}">
                                <div class="perm-group-title"><i class="fa fa-shield-halved"></i> Nhân viên &amp; Phân quyền</div>
                                <div class="row g-2 mb-1">
                                    <c:forEach var="p" items="${allPermissions}">
                                        <c:if test="${p.permissionCode == 'VIEW_EMPLOYEES' || p.permissionCode == 'ADD_EMPLOYEE' || p.permissionCode == 'EDIT_EMPLOYEE' || p.permissionCode == 'ADD_EMPLOYMENT_CONTRACT' || p.permissionCode == 'VIEW_CONTRACT_PREVIEW' || p.permissionCode == 'VIEW_DEPARTMENTS' || p.permissionCode == 'EDIT_DEPARTMENTS' || p.permissionCode == 'ASSIGN_DEPARTMENT' || p.permissionCode == 'VIEW_DEPARTMENT_EMPLOYEES_DETAIL' || p.permissionCode == 'UNASSIGN_DEPARTMENT' || p.permissionCode == 'REASSIGN_DEPARTMENT' || p.permissionCode == 'VIEW_ATTENDANCE' || p.permissionCode == 'IMPORT_ATTENDANCE' || p.permissionCode == 'EDIT_ATTENDANCE'}">
                                            <div class="col-md-3">
                                                <div class="perm-tile ${assignedPermissionIds.contains(p.permissionId) ? 'assigned' : ''}">
                                                    <div class="perm-tile-name">
                                                        <c:choose>
                                                            <c:when test="${assignedPermissionIds.contains(p.permissionId)}">
                                                                <i class="fa fa-circle-check perm-check"></i>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <i class="fa fa-circle perm-uncheck"></i>
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <c:out value="${p.permissionName}"/>
                                                    <span class="perm-tile-code"><c:out value="${p.permissionCode}"/></span>
                                                    <div class="perm-tile-desc"><c:out value="${empty p.description ? '—' : p.description}"/></div>
                                    <c:if test="${p.permissionCode == 'VIEW_EMPLOYEES' || p.permissionCode == 'ADD_EMPLOYEE' || p.permissionCode == 'EDIT_EMPLOYEE' || p.permissionCode == 'ADD_EMPLOYMENT_CONTRACT' || p.permissionCode == 'VIEW_DEPARTMENTS' || p.permissionCode == 'EDIT_DEPARTMENTS' || p.permissionCode == 'ASSIGN_DEPARTMENT' || p.permissionCode == 'VIEW_DEPARTMENT_EMPLOYEES_DETAIL' || p.permissionCode == 'UNASSIGN_DEPARTMENT' || p.permissionCode == 'IMPORT_ATTENDANCE' || p.permissionCode =='EDIT_ATTENDANCE' ||  p.permissionCode =='VIEW_DEPARTMENT_ATTENDANCE' || p.permissionCode == 'VIEW_ALL_ATTENDANCE'}">
 0021fc6ff7304a895cc73b7348b2d1b0141ab945:HRM/web/public/systemadmin/role/role_detail.jsp
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </c:if>

                            <c:if test="${isSystemAdmin || !isSpecialRole}">
                                <div class="perm-group-title"><i class="fa fa-file-pen"></i> Đơn yêu cầu</div>
                                <div class="row g-2 mb-1">
                                    <c:forEach var="p" items="${allPermissions}">
                                        <c:if test="${p.permissionCode == 'VIEW_ALL_FORMS' || p.permissionCode == 'VIEW_ALL_DEPT_FORMS'}">
                                            <div class="col-md-3">
                                                <div class="perm-tile ${assignedPermissionIds.contains(p.permissionId) ? 'assigned' : ''}">
                                                    <div class="perm-tile-name">
                                                        <c:choose>
                                                            <c:when test="${assignedPermissionIds.contains(p.permissionId)}">
                                                                <i class="fa fa-circle-check perm-check"></i>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <i class="fa fa-circle perm-uncheck"></i>
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <c:out value="${p.permissionName}"/>
                                                    </div>
                                                    <span class="perm-tile-code"><c:out value="${p.permissionCode}"/></span>
                                                    <div class="perm-tile-desc"><c:out value="${empty p.description ? '—' : p.description}"/></div>
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </c:if>

                            <c:if test="${isSystemAdmin || !isSpecialRole}">
                                <div class="perm-group-title"><i class="fa fa-file-contract"></i> Hợp đồng lao động</div>
                                <div class="row g-2 mb-1">
                                    <c:forEach var="p" items="${allPermissions}">
                                        <c:if test="${p.permissionCode == 'PERM_APPROVE_CONTRACT'}">
                                            <div class="col-md-3">
                                                <div class="perm-tile ${assignedPermissionIds.contains(p.permissionId) ? 'assigned' : ''}">
                                                    <div class="perm-tile-name">
                                                        <c:choose>
                                                            <c:when test="${assignedPermissionIds.contains(p.permissionId)}">
                                                                <i class="fa fa-circle-check perm-check"></i>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <i class="fa fa-circle perm-uncheck"></i>
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <c:out value="${p.permissionName}"/>
                                                    </div>
                                                    <span class="perm-tile-code"><c:out value="${p.permissionCode}"/></span>
                                                    <div class="perm-tile-desc"><c:out value="${empty p.description ? '—' : p.description}"/></div>
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </c:if>
                        </div>

                        <div class="section-block">
                            <div class="section-title">
                                Người dùng thuộc vai trò
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
                                                <a href="${pageContext.request.contextPath}/v1/systemadmin/view-user-detail?id=${u.userId}" class="btn btn-sm btn-outline-primary">
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
