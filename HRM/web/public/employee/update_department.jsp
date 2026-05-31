<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Cập nhật phòng ban - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }

        .page-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 32px 36px;
        }

        .page-header {
            display: flex;
            align-items: center;
            gap: 14px;
            padding-bottom: 20px;
            border-bottom: 1px solid #f1f5f9;
            margin-bottom: 28px;
        }

        .page-header-icon {
            width: 48px; height: 48px;
            border-radius: 12px;
            background: #dbeafe;
            color: #2563eb;
            display: flex; align-items: center; justify-content: center;
            font-size: 20px;
            flex-shrink: 0;
        }

        .section-title {
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: .08em;
            color: #94a3b8;
            margin: 24px 0 16px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .section-title::after {
            content: '';
            flex: 1;
            height: 1px;
            background: #f1f5f9;
        }

        .h-form-row {
            display: grid;
            align-items: start;
            gap: 0 20px;
            margin-bottom: 18px;
        }
        .h-form-row.cols-1 { grid-template-columns: 160px 1fr; }
        .h-form-row.cols-2 { grid-template-columns: 160px 1fr 160px 1fr; }

        .h-label {
            font-size: 13px;
            font-weight: 600;
            color: #374151;
            padding-top: 10px;
            text-align: right;
            padding-right: 4px;
        }

        .h-field { display: flex; flex-direction: column; gap: 4px; }

        .form-control, .form-select {
            border-radius: 8px;
            border: 1px solid #e2e8f0;
            font-size: 13.5px;
            padding: 9px 12px;
            background: #fafafa;
            transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
        }
        .form-control:focus, .form-select:focus {
            border-color: #2563eb;
            box-shadow: 0 0 0 3px rgba(37,99,235,0.1);
            background: white;
            outline: none;
        }
        textarea.form-control { resize: vertical; min-height: 80px; }

        .required-dot {
            display: inline-block;
            width: 5px; height: 5px;
            background: #ef4444;
            border-radius: 50%;
            vertical-align: super;
            margin-left: 3px;
        }

        .hint { font-size: 11.5px; color: #94a3b8; }

        .action-bar {
            display: flex;
            align-items: center;
            gap: 12px;
            padding-top: 24px;
            margin-top: 8px;
            border-top: 1px solid #f1f5f9;
        }

        .btn-submit {
            background: #2563eb;
            border: none;
            border-radius: 8px;
            padding: 10px 24px;
            font-weight: 600;
            font-size: 13.5px;
            color: white;
            display: flex; align-items: center; gap: 8px;
        }
        .btn-submit:hover { background: #1d4ed8; color: white; }

        .btn-cancel {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 10px 20px;
            font-weight: 600;
            font-size: 13.5px;
            color: #64748b;
            text-decoration: none;
            display: flex; align-items: center; gap: 8px;
        }
        .btn-cancel:hover { background: #f8fafc; color: #374151; }
    </style>
</head>
<body>

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
        <jsp:param name="title" value="Cập nhật phòng ban" />
        <jsp:param name="backUrl" value="/v1/employee/department-detail?id=${department.departmentId}" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session"/>
    </c:if>

    <div class="page-card">

        <div class="page-header">
            <div class="page-header-icon">
                <i class="fa-solid fa-pen-to-square"></i>
            </div>
            <div>
                <h5 class="fw-bold mb-1" style="color:#0f172a">Cập nhật thông tin phòng ban</h5>
                <p class="text-muted mb-0" style="font-size:13px">
                    Chỉnh sửa thông tin phòng ban. Các trường có <span class="required-dot"></span> là bắt buộc.
                </p>
            </div>
        </div>

        <form action="${pageContext.request.contextPath}/v1/employee/update-department"
              method="post" id="updateDeptForm" novalidate>
            
            <input type="hidden" name="departmentId" value="${department.departmentId}">

            <div class="section-title">Thông tin bắt buộc</div>

            <div class="h-form-row cols-2">
                <div class="h-label">Mã phòng ban</div>
                <div class="h-field">
                    <input type="text" class="form-control" id="departmentCode" name="departmentCode"
                           value="${department.departmentCode}" disabled>
                    <span class="hint">Mã định danh không thể thay đổi</span>
                </div>

                <div class="h-label">Tên phòng ban <span class="required-dot"></span></div>
                <div class="h-field">
                    <input type="text" class="form-control" id="departmentName" name="departmentName"
                           placeholder="VD: Phòng Công nghệ thông tin"
                           value="${department.departmentName}" maxlength="100">
                </div>
            </div>

            <div class="section-title">Thông tin bổ sung <span style="font-weight:400;text-transform:none;letter-spacing:0">(tùy chọn)</span></div>

            <div class="h-form-row cols-1">
                <div class="h-label" style="padding-top:10px">Mô tả</div>
                <div class="h-field">
                    <textarea class="form-control" id="description" name="description"
                              rows="3" placeholder="Mô tả chức năng, nhiệm vụ của phòng ban...">${department.description}</textarea>
                </div>
            </div>

            <div class="section-title">Vai trò được phép <span style="font-weight:400;text-transform:none;letter-spacing:0">(tùy chọn)</span></div>

            <div class="h-form-row cols-1">
                <div class="h-label" style="padding-top:10px">Vai trò hợp lệ</div>
                <div class="h-field">
                    <c:choose>
                        <c:when test="${empty roles}">
                            <span class="hint">Chưa có vai trò nào trong hệ thống.</span>
                        </c:when>
                        <c:otherwise>
                            <div style="display:flex; flex-direction:column; gap:10px;">
                                <c:forEach var="role" items="${roles}">
                                    <label style="display:flex; align-items:center; gap:8px; font-size:13.5px; font-weight:500; color:#374151; cursor:pointer;">
                                        <input type="checkbox" name="roleIds" value="${role.roleId}"
                                               <c:if test="${not empty selectedRoleIds and selectedRoleIds.contains(role.roleId)}">checked</c:if>>
                                        <span>${role.roleName}</span>
                                        <c:if test="${not empty role.description}">
                                            <span class="hint" style="font-weight:400;">— ${role.description}</span>
                                        </c:if>
                                    </label>
                                </c:forEach>
                            </div>
                            <span class="hint" style="margin-top:8px;">
                                Để trống = phòng ban nhận mọi vai trò. Nếu tick, chỉ nhân viên có vai trò được chọn mới được phân công vào phòng này.
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="action-bar">
                <button type="submit" class="btn btn-submit">
                    <i class="fa-solid fa-save"></i> Lưu thay đổi
                </button>
                <a href="${pageContext.request.contextPath}/v1/employee/department-detail?id=${department.departmentId}" class="btn-cancel">
                    <i class="fa-solid fa-xmark"></i> Hủy
                </a>
                <span class="text-muted ms-auto" style="font-size:12px">
                    <span class="required-dot"></span> Bắt buộc điền
                </span>
            </div>

        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.getElementById('updateDeptForm').addEventListener('submit', function (e) {
        const missing = [];
        if (!document.getElementById('departmentName').value.trim()) missing.push('Tên phòng ban');
        if (missing.length) {
            e.preventDefault();
            alert('Vui lòng điền: ' + missing.join(', ') + '.');
        }
    });
</script>
</body>
</html>
