<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Phân quyền vai trò - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main-content { margin-left: 250px; padding: 30px; }

        .card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
        }

        .role-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: #eef2ff;
            color: #3730a3;
            border-radius: 10px;
            padding: 8px 16px;
            font-weight: 700;
            font-size: 14px;
            margin-bottom: 6px;
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
        .section-label i { color: #ff8c00; margin-right: 6px; }

        .group-title {
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: .5px;
            color: #6b7280;
            margin: 20px 0 10px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .group-title::after {
            content: '';
            flex: 1;
            height: 1px;
            background: #e5e7eb;
        }

        .perm-card {
            border: 1.5px solid #e5e7eb;
            border-radius: 10px;
            padding: 14px 16px;
            cursor: pointer;
            transition: border-color .15s, background .15s, box-shadow .15s;
            user-select: none;
            height: 100%;
        }
        .perm-card:hover {
            border-color: #a5b4fc;
            background: #f5f3ff;
        }
        .perm-card.selected {
            border-color: #6366f1;
            background: #eef2ff;
            box-shadow: 0 0 0 3px rgba(99,102,241,.12);
        }
        .perm-card input[type=checkbox] { display: none; }

        .perm-name {
            font-weight: 700;
            font-size: 14px;
            color: #111827;
            margin-bottom: 4px;
        }
        .perm-code {
            display: inline-block;
            background: #e0f2fe;
            color: #0369a1;
            border-radius: 20px;
            padding: 2px 10px;
            font-size: 11px;
            font-weight: 700;
            margin-bottom: 6px;
        }
        .perm-desc {
            color: #6b7280;
            font-size: 12px;
            line-height: 1.5;
        }
        .check-icon {
            float: right;
            color: #6366f1;
            font-size: 16px;
            display: none;
        }
        .perm-card.selected .check-icon { display: inline; }

        .toolbar {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 18px;
        }
        .count-badge {
            background: #6366f1;
            color: white;
            border-radius: 20px;
            padding: 3px 12px;
            font-size: 13px;
            font-weight: 700;
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

        .btn-select-all, .btn-clear-all {
            height: 34px;
            padding: 0 14px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            border: 1px solid;
            cursor: pointer;
        }
        .btn-select-all { background: #eef2ff; color: #4338ca; border-color: #c7d2fe; }
        .btn-select-all:hover { background: #e0e7ff; }
        .btn-clear-all { background: #fff7ed; color: #c2410c; border-color: #fed7aa; }
        .btn-clear-all:hover { background: #ffedd5; }
    </style>
</head>
<body>

<jsp:include page="/public/components/systemAdminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/systemAdminTopBar.jsp">
        <jsp:param name="title" value="Phân quyền vai trò" />
        <jsp:param name="backUrl" value="/v1/systemadmin/role-list" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" style="border-radius:8px;font-size:14px;margin-bottom:20px" role="alert">
            <i class="fa fa-circle-exclamation me-2"></i><c:out value="${error}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty selectedRole}">
            <div class="alert alert-warning">
                <i class="fa fa-exclamation-triangle me-2"></i>Không tìm thấy thông tin vai trò.
            </div>
            <a href="${pageContext.request.contextPath}/v1/systemadmin/role-list" class="btn-cancel-link">
                <i class="fa fa-arrow-left me-2"></i>Quay lại danh sách
            </a>
        </c:when>
        <c:otherwise>
            <form action="${pageContext.request.contextPath}/v1/systemadmin/edit-role-permissions" method="POST" id="permForm">
                <input type="hidden" name="roleId" value="${selectedRole.roleId}"/>

                <div class="card mb-4">
                    <div class="card-body p-4">
                        <div class="section-label">Vai trò đang chỉnh sửa</div>
                        <div class="role-badge">
                           
                            <span><c:out value="${selectedRole.roleName}"/></span>
                            <span style="color:#818cf8;font-weight:400">·</span>
                            <span style="font-size:12px;color:#6366f1"><c:out value="${selectedRole.roleCode}"/></span>
                        </div>
                        <c:if test="${not empty selectedRole.description}">
                            <div style="color:#6b7280;font-size:13px;margin-top:6px"><c:out value="${selectedRole.description}"/></div>
                        </c:if>
                    </div>
                </div>

                <div class="card">
                    <div class="card-body p-4">
                        <div class="section-label">Chọn quyền cho vai trò</div>

                        <div class="toolbar">
                            <span>Đã chọn: <span class="count-badge" id="countBadge">0</span></span>
                            <button type="button" class="btn-select-all" onclick="selectAll()">
                                Chọn tất cả
                            </button>
                            <button type="button" class="btn-clear-all" onclick="clearAll()">
                               Bỏ chọn tất cả
                            </button>
                        </div>

                        <div class="group-title">Người dùng</div>
                        <div class="row g-3 mb-2">
                            <c:forEach var="p" items="${allPermissions}">
                                <c:if test="${p.permissionCode == 'VIEW_USERS' || p.permissionCode == 'ADD_USER' || p.permissionCode == 'EDIT_USER' || p.permissionCode == 'DELETE_USER'}">
                                    <div class="col-md-3">
                                        <label class="perm-card ${assignedPermissionIds.contains(p.permissionId) ? 'selected' : ''}">
                                            <input type="checkbox" name="permissionIds" value="${p.permissionId}" ${assignedPermissionIds.contains(p.permissionId) ? 'checked' : ''}/>
                                         
                                            <div class="perm-name"><c:out value="${p.permissionName}"/></div>
                                            <div><span class="perm-code"><c:out value="${p.permissionCode}"/></span></div>
                                            <div class="perm-desc"><c:out value="${empty p.description ? '—' : p.description}"/></div>
                                        </label>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>

                        <%-- Nhóm: Vai trò & Phân quyền --%>
                        <div class="group-title">Vai trò &amp; Phân quyền</div>
                        <div class="row g-3 mb-2">
                            <c:forEach var="p" items="${allPermissions}">
                                <c:if test="${p.permissionCode == 'VIEW_ROLES' || p.permissionCode == 'ADD_ROLE' || p.permissionCode == 'EDIT_ROLE' || p.permissionCode == 'DELETE_ROLE' || p.permissionCode == 'MANAGE_PERMISSIONS'}">
                                    <div class="col-md-3">
                                        <label class="perm-card ${assignedPermissionIds.contains(p.permissionId) ? 'selected' : ''}">
                                            <input type="checkbox" name="permissionIds" value="${p.permissionId}" ${assignedPermissionIds.contains(p.permissionId) ? 'checked' : ''}/>
                                            
                                            <div class="perm-name"><c:out value="${p.permissionName}"/></div>
                                            <div><span class="perm-code"><c:out value="${p.permissionCode}"/></span></div>
                                            <div class="perm-desc"><c:out value="${empty p.description ? '—' : p.description}"/></div>
                                        </label>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                        
                        <%-- Nhóm:  Nhân viên --%>
                        <div class="group-title">Vai trò &amp; Nhân viên</div>
                        <div class="row g-3 mb-2">
                            <c:forEach var="p" items="${allPermissions}">
                                <c:if test="${p.permissionCode == 'VIEW_EMPLOYEES' || p.permissionCode == 'ADD_EMPLOYEE' || p.permissionCode == 'EDIT_EMPLOYEE' || p.permissionCode == 'ADD_EMPLOYMENT_CONTRACT' || p.permissionCode == 'VIEW_DEPARTMENTS' || p.permissionCode == 'EDIT_DEPARTMENTS' || p.permissionCode == 'ASSIGN_DEPARTMENT' || p.permissionCode == 'VIEW_DEPARTMENT_EMPLOYEES_DETAIL' || p.permissionCode == 'UNASSIGN_DEPARTMENT' || p.permissionCode == 'VIEW_ATTENDANCE' || p.permissionCode == 'IMPORT_ATTENDANCE' || p.permissionCode =='EDIT_ATTENDANCE' }">
                                    <div class="col-md-3">
                                        <label class="perm-card ${assignedPermissionIds.contains(p.permissionId) ? 'selected' : ''}">
                                            <input type="checkbox" name="permissionIds" value="${p.permissionId}" ${assignedPermissionIds.contains(p.permissionId) ? 'checked' : ''}/>
                                            
                                            <div class="perm-name"><c:out value="${p.permissionName}"/></div>
                                            <div><span class="perm-code"><c:out value="${p.permissionCode}"/></span></div>
                                            <div class="perm-desc"><c:out value="${empty p.description ? '—' : p.description}"/></div>
                                        </label>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                        
                        <%-- Nhóm: Đơn yêu cầu --%>
                        <div class="group-title">Đơn yêu cầu</div>
                        <div class="row g-3 mb-2">
                            <c:forEach var="p" items="${allPermissions}">
                                <c:if test="${p.permissionCode == 'SUBMIT_FORM' || p.permissionCode == 'APPROVE_FORM' || p.permissionCode == 'VIEW_ALL_FORMS' || p.permissionCode == 'VIEW_MY_FORM' || p.permissionCode == 'VIEW_DEPT_FORMS'}">
                                    <div class="col-md-3">
                                        <label class="perm-card ${assignedPermissionIds.contains(p.permissionId) ? 'selected' : ''}">
                                            <input type="checkbox" name="permissionIds" value="${p.permissionId}" ${assignedPermissionIds.contains(p.permissionId) ? 'checked' : ''}/>
                                            
                                            <div class="perm-name"><c:out value="${p.permissionName}"/></div>
                                            <div><span class="perm-code"><c:out value="${p.permissionCode}"/></span></div>
                                            <div class="perm-desc"><c:out value="${empty p.description ? '—' : p.description}"/></div>
                                        </label>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                        
                        <div class="d-flex gap-3 pt-2 border-top">
                            <button type="submit" class="btn-save mt-3">
                                Lưu phân quyền
                            </button>
                            <a href="${pageContext.request.contextPath}/v1/systemadmin/role-detail?id=${selectedRole.roleId}" class="btn-cancel-link mt-3">
                                Hủy
                            </a>
                        </div>
                    </div>
                </div>
            </form>
        </c:otherwise>
    </c:choose>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // Checkbox nằm trong <label> nên click vào card đã tự toggle checkbox.
    // Chỉ lắng nghe sự kiện change để đồng bộ giao diện (tránh double-toggle làm mất giá trị).
    document.querySelectorAll('#permForm input[type=checkbox]').forEach(function (cb) {
        cb.addEventListener('change', function () {
            this.closest('label').classList.toggle('selected', this.checked);
            updateCount();
        });
    });

    function updateCount() {
        const total = document.querySelectorAll('#permForm input[type=checkbox]:checked').length;
        document.getElementById('countBadge').textContent = total;
    }

    function selectAll() {
        document.querySelectorAll('#permForm input[type=checkbox]').forEach(cb => {
            cb.checked = true;
            cb.closest('label').classList.add('selected');
        });
        updateCount();
    }

    function clearAll() {
        document.querySelectorAll('#permForm input[type=checkbox]').forEach(cb => {
            cb.checked = false;
            cb.closest('label').classList.remove('selected');
        });
        updateCount();
    }

    document.addEventListener('DOMContentLoaded', updateCount);
</script>
</body>
</html>
