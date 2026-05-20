<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Xóa vai trò - HRM</title>
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
        .page-header a {
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
            transition: background .2s;
        }
        .page-header a:hover { background: #f3f4f6; }
        .page-header h5 { font-weight: 700; color: #0B0E2A; margin: 0; }
        .confirm-card {
            background: #fff;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 36px 40px;
            max-width: 520px;
        }
        .confirm-icon {
            width: 72px;
            height: 72px;
            border-radius: 50%;
            background: #fee2e2;
            color: #991b1b;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            font-size: 32px;
        }
        .confirm-title {
            font-size: 20px;
            font-weight: 800;
            color: #111827;
            text-align: center;
            margin-bottom: 10px;
        }
        .confirm-desc {
            text-align: center;
            color: #6b7280;
            font-size: 14px;
            margin-bottom: 30px;
            line-height: 1.6;
        }
        .role-info {
            background: #f9fafb;
            border-radius: 8px;
            padding: 16px 20px;
            margin-bottom: 28px;
            border: 1px solid #e5e7eb;
        }
        .role-info-label {
            font-size: 11px;
            font-weight: 700;
            color: #9ca3af;
            text-transform: uppercase;
            letter-spacing: .5px;
            margin-bottom: 4px;
        }
        .role-info-value {
            font-size: 15px;
            font-weight: 700;
            color: #111827;
        }
        .btn-danger-confirm {
            height: 44px;
            padding: 0 28px;
            background: #991b1b;
            color: white;
            border: none;
            border-radius: 8px;
            font-weight: 600;
            font-size: 14px;
            transition: background .2s;
        }
        .btn-danger-confirm:hover { background: #7f1d1d; color: white; }
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
        .alert-flash { border-radius: 8px; font-size: 14px; margin-bottom: 20px; }
    </style>
</head>
<body>

<jsp:include page="/public/components/adminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/adminTopBar.jsp">
        <jsp:param name="title" value="Xóa vai trò" />
    </jsp:include>

    <div class="page-header">
        <a href="${pageContext.request.contextPath}/v1/admin/role-list" title="Quay lại">
            <i class="fa fa-arrow-left"></i>
        </a>
        <h5><i class="fa fa-trash me-2" style="color:#991b1b"></i>Xóa vai trò</h5>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash">
            <i class="fa fa-circle-exclamation me-2"></i><c:out value="${error}"/>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty selectedRole}">
            <div class="alert alert-warning">
                <i class="fa fa-exclamation-triangle me-2"></i>Không tìm thấy thông tin vai trò.
            </div>
            <a href="${pageContext.request.contextPath}/v1/admin/role-list" class="btn-cancel-link">
                <i class="fa fa-arrow-left me-2"></i>Quay lại danh sách
            </a>
        </c:when>
        <c:otherwise>
            <div class="confirm-card">
                <div class="confirm-icon">
                    <i class="fa fa-triangle-exclamation"></i>
                </div>
                <div class="confirm-title">Xác nhận xóa vai trò</div>
                <div class="confirm-desc">
                    Bạn có chắc chắn muốn xóa vai trò này?<br/>
                    Hành động này <strong>không thể hoàn tác</strong>.
                </div>

                <div class="role-info">
                    <div class="role-info-label">Mã vai trò</div>
                    <div class="role-info-value"><c:out value="${selectedRole.roleCode}"/></div>
                    <div class="role-info-label" style="margin-top:10px">Tên vai trò</div>
                    <div class="role-info-value"><c:out value="${selectedRole.roleName}"/></div>
                </div>

                <form action="${pageContext.request.contextPath}/v1/admin/delete-role" method="POST">
                    <input type="hidden" name="roleId" value="${selectedRole.roleId}"/>
                    <div class="d-flex gap-3 justify-content-center">
                        <button type="submit" class="btn-danger-confirm">
                            <i class="fa fa-trash me-2"></i>Xóa vai trò
                        </button>
                        <a href="${pageContext.request.contextPath}/v1/admin/role-list" class="btn-cancel-link">
                            Hủy
                        </a>
                    </div>
                </form>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
