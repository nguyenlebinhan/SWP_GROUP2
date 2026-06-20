<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Cập nhật vai trò - HRM</title>
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

        .card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
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

        .section-label i {
            color: #ff8c00;
            margin-right: 6px;
        }

        .form-label {
            font-size: 13px;
            font-weight: 600;
            color: #374151;
            margin-bottom: 6px;
        }

        .form-control,
        .form-select {
            height: 44px;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            font-size: 14px;
            color: #1f2937;
            padding: 0 14px;
        }

        .form-control:focus,
        .form-select:focus {
            border-color: #1565c0;
            box-shadow: 0 0 0 3px rgba(21,101,192,.1);
        }

        .form-control::placeholder { color: #9ca3af; }

        .form-text { font-size: 12px; color: #9ca3af; margin-top: 4px; }

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

        .alert-flash {
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 20px;
        }

        .required { color: #ef4444; }
    </style>
</head>
<body>

<jsp:include page="/public/components/systemAdminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/systemAdminTopBar.jsp">
        <jsp:param name="title" value="Cập nhật vai trò" />
        <jsp:param name="backUrl" value="/v1/systemadmin/role-list" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash alert-dismissible fade show" role="alert">
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
            <form action="${pageContext.request.contextPath}/v1/systemadmin/update-role" method="POST">
                <div class="card">
                    <div class="card-body p-4">
                        <div class="section-label"><i class="fa fa-shield-halved"></i>Thông tin vai trò</div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <input type="hidden" value="${selectedRole.roleId}" name="roleId">
                                <label class="form-label" for="roleCode">Mã vai trò <span class="required">*</span></label>
                                <input type="text" id="roleCode" name="roleCode" class="form-control"
                                       placeholder="vd: AD, MA, EM"
                                       value="<c:out value='${selectedRole.roleCode}'/>" required/>
                                <div class="form-text"><i class="fa fa-info-circle me-1"></i>Mã duy nhất, không trùng lặp</div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label" for="roleName">Tên vai trò <span class="required">*</span></label>
                                <input type="text" id="roleName" name="roleName" class="form-control"
                                       placeholder="vd: Quản trị viên"
                                       value="<c:out value='${selectedRole.roleName}'/>" required/>
                            </div>
                        </div>
                        <div class="row g-3 mb-4">
                            <div class="col-12">
                                <label class="form-label" for="description">Mô tả</label>
                                <textarea id="description" name="description" class="form-control"
                                          style="height:90px;resize:vertical;padding:10px 14px"
                                          placeholder="Mô tả ngắn về vai trò này..."><c:out value="${selectedRole.description}"/></textarea>
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-2">
                            <button type="submit" class="btn-save">
                                <i class="fa fa-floppy-disk me-2"></i>Lưu thay đổi
                            </button>
                            <a href="${pageContext.request.contextPath}/v1/systemadmin/role-list" class="btn-cancel-link">
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
</body>
</html>
