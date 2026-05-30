<%--
    Document   : manager/employee_list.jsp
    Hiển thị nhân viên thuộc phòng ban của người quản lý đang đăng nhập.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Nhân viên phòng ban - HRM Manager</title>
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

        .table th { font-size: 13px; color: #6b7280; font-weight: 600; background: #f9fafb; }
        .table td { font-size: 14px; vertical-align: middle; }

        .badge-active   { background: #d1fae5; color: #065f46; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-inactive { background: #fee2e2; color: #991b1b; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-leave    { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }

        .avatar-circle {
            width: 36px; height: 36px;
            border-radius: 50%;
            background: #ff8c00;
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

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Nhân viên phòng ban" />
        <jsp:param name="backUrl" value="/v1/manager/dashboard" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-warning alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-triangle-exclamation me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="page-card">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h5 class="fw-bold mb-1">
                    <c:choose>
                        <c:when test="${not empty departmentName}">Phòng ${departmentName}</c:when>
                        <c:otherwise>Nhân viên</c:otherwise>
                    </c:choose>
                </h5>
                <span class="text-muted small">${employees.size()} nhân viên</span>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty employees}">
                <div class="text-center py-5">
                    <i class="fa-solid fa-users-slash" style="font-size:48px;color:#cbd5e1;margin-bottom:16px"></i>
                    <h6 class="text-muted">Chưa có nhân viên nào trong phòng ban của bạn</h6>
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
                                <th>Số điện thoại</th>
                                <th>Trạng thái</th>
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
