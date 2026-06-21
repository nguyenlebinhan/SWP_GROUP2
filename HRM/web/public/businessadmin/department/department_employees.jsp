<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Nhân viên phòng ${department.departmentName} - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }

        .page-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
        }

        .dept-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
        .dept-icon-lg {
            width: 56px; height: 56px;
            border-radius: 12px;
            background: #dbeafe; color: #2563eb;
            display: flex; align-items: center; justify-content: center;
            font-size: 24px;
            flex-shrink: 0;
        }
        .dept-title { font-size: 18px; font-weight: 700; color: #0f172a; margin-bottom: 2px; }
        .dept-code  { font-size: 13px; color: #94a3b8; }

        .table th { font-size: 13px; color: #6b7280; font-weight: 600; background: #f9fafb; }
        .table td { font-size: 14px; vertical-align: middle; }

        .badge-active   { background: #d1fae5; color: #065f46; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-inactive { background: #fee2e2; color: #991b1b; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-leave    { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }

        .avatar-circle {
            width: 36px; height: 36px;
            border-radius: 50%;
            background: #2563eb;
            color: white;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            font-weight: 700;
            flex-shrink: 0;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/businessAdminSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/businessAdminTopBar.jsp">
        <jsp:param name="title" value="Nhân viên phòng ban" />
        <jsp:param name="backUrl" value="/v1/businessadmin/department" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session" />
    </c:if>

    <div class="page-card">
        <div class="dept-header">
            <div class="dept-icon-lg"><i class="fa-solid fa-sitemap"></i></div>
            <div class="flex-grow-1">
                <div class="dept-title">${department.departmentName}</div>
                <div class="dept-code">${department.departmentCode}</div>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/v1/businessadmin/update-department?id=${department.departmentId}"
                   class="btn btn-sm btn-outline-primary me-2">
                   <i class="fa-solid fa-pen"></i> Chỉnh sửa
                </a>
                <c:choose>
                    <c:when test="${department.status == 1}">
                        <span class="badge-active">Hoạt động</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge-inactive">Tạm dừng</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <c:if test="${not empty department.description}">
            <p class="text-muted small mb-4">${department.description}</p>
        </c:if>

        <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="fw-bold mb-0">Danh sách nhân viên</h6>
            <span class="text-muted small">${employees.size()} nhân viên</span>
        </div>

        <c:choose>
            <c:when test="${empty employees}">
                <div class="text-center py-5">
                    <i class="fa-solid fa-users-slash" style="font-size:48px;color:#cbd5e1;margin-bottom:16px"></i>
                    <h6 class="text-muted">Phòng ban này chưa có nhân viên nào</h6>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                            <tr>
                                <th>Nhân viên</th>
                                <th>Mã NV</th>
                                <th>Vị trí</th>
                                <th>Vai trò</th>
                                <th>Số điện thoại</th>
                                <th>Trạng thái</th>
                                <th class="text-center">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="emp" items="${employees}">
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <div class="avatar-circle">
                                                ${emp.fullName.substring(0,1).toUpperCase()}
                                            </div>
                                            <div>
                                                <div class="fw-semibold">${emp.fullName}</div>
                                                <small class="text-muted">${emp.email}</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td><code>${emp.employeeCode}</code></td>
                                    <td>${emp.positionName}</td>
                                    <td>${emp.roleName}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty emp.phoneNumber}">${emp.phoneNumber}</c:when>
                                            <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${emp.status == 1}">
                                                <span class="badge-active">Đang làm việc</span>
                                            </c:when>
                                            <c:when test="${emp.status == 2}">
                                                <span class="badge-leave">Đang nghỉ phép</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-inactive">Không hoạt động</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <a href="${pageContext.request.contextPath}/v1/businessadmin/employee-detail?id=${emp.employeeId}"
                                           class="btn btn-sm btn-outline-primary">
                                            <i class="fa-solid fa-eye"></i> Chi tiết
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
