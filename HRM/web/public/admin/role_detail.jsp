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

<jsp:include page="/public/components/adminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/adminTopBar.jsp">
        <jsp:param name="title" value="Chi tiết vai trò" />
        <jsp:param name="backUrl" value="/v1/admin/role-list" />
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
