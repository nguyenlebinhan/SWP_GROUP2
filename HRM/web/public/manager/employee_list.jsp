<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Nhân viên phòng ban - HRM Manager</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .page-card { background: white; border-radius: 14px; box-shadow: 0 2px 12px rgba(0,0,0,0.07); padding: 24px; }
        .table th { font-size: 13px; color: #6b7280; font-weight: 600; background: #f9fafb; }
        .table td { font-size: 14px; vertical-align: middle; }
        .badge-active { background: #d1fae5; color: #065f46; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-inactive { background: #fee2e2; color: #991b1b; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-leave { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .avatar-circle { width: 36px; height: 36px; border-radius: 50%; background: #2563eb; color: white; display: inline-flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; flex-shrink: 0; }
        .search-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; background: white; padding: 16px 20px; border: 1px solid #eef2f7; border-radius: 10px; margin-bottom: 20px; }
        .search-bar input[type="text"] { flex: 1; min-width: 200px; padding: 8px 14px; border: 1px solid #e5e7eb; border-radius: 7px; font-size: 14px; outline: none; }
        .search-bar select { min-width: 160px; padding: 8px 14px; border: 1px solid #e5e7eb; border-radius: 7px; font-size: 14px; outline: none; background: white; }
        .search-bar input[type="text"]:focus, .search-bar select:focus { border-color: #2563eb; }
        .btn-search { background: #2563eb; color: white; border: none; padding: 8px 18px; border-radius: 7px; font-size: 14px; font-weight: 600; }
        .btn-clear { background: white; color: #6b7280; border: 1px solid #d1d5db; padding: 8px 14px; border-radius: 7px; font-size: 13px; text-decoration: none; font-weight: 500; }
        .btn-clear:hover { background: #f3f4f6; color: #374151; }
        .pagination-wrap { display: flex; justify-content: space-between; align-items: center; padding: 14px 0 0; }
        .pagination { margin: 0; }
        .page-link { color: #2563eb; border-color: #e5e7eb; font-size: 13px; padding: 5px 12px; }
        .page-item.active .page-link { background: #2563eb; border-color: #2563eb; color: white; }
        .page-item.disabled .page-link { color: #d1d5db; }
    </style>
</head>
<body>
<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Nhân viên phòng ban của tôi" />
        <jsp:param name="backUrl" value="/v1/manager/dashboard" />
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
                        <c:otherwise>Nhân viên phòng ban</c:otherwise>
                    </c:choose>
                </h5>
                <span class="text-muted small">${totalEmployees} nhân viên</span>
            </div>
        </div>

        <form method="get" action="${pageContext.request.contextPath}/v1/manager/my-department-list">
            <div class="search-bar">
                <input type="text" name="keyword" placeholder="Tìm theo họ và tên, email..." value="${keyword}" />
                <select name="status">
                    <option value="">Tất cả trạng thái</option>
                    <option value="1" ${status == '1' ? 'selected' : ''}>Đang làm việc</option>
                    <option value="2" ${status == '2' ? 'selected' : ''}>Đang nghỉ phép</option>
                    <option value="0" ${status == '0' ? 'selected' : ''}>Không hoạt động</option>
                </select>
                <button type="submit" class="btn-search">Tìm kiếm</button>
                <a href="${pageContext.request.contextPath}/v1/manager/my-department-list" class="btn-clear">Xóa lọc</a>
            </div>
        </form>

        <c:choose>
            <c:when test="${empty employees}">
                <div class="text-center py-5">
                    <i class="fa-solid fa-users-slash" style="font-size:48px;color:#cbd5e1;margin-bottom:16px"></i>
                    <h6 class="text-muted">Chưa có nhân viên nào trong phòng ban</h6>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                            <tr>
                                <th>Nhân viên</th>
                                <th>Mã NV</th>
                                <th>Phòng ban</th>
                                <th>Vị trí</th>
                                <th>Số điện thoại</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="emp" items="${employees}">
                                <tr>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <div class="avatar-circle">${emp.fullName.substring(0,1).toUpperCase()}</div>
                                            <div>
                                                <div class="fw-semibold">${emp.fullName}</div>
                                                <small class="text-muted">${emp.email}</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td><code>${emp.employeeCode}</code></td>
                                    <td>${emp.departmentName}</td>
                                    <td>${emp.positionName}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty emp.phoneNumber}">${emp.phoneNumber}</c:when>
                                            <c:otherwise><span class="text-muted">-</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${emp.status == 1}"><span class="badge-active">Đang làm việc</span></c:when>
                                            <c:when test="${emp.status == 2}"><span class="badge-leave">Đang nghỉ phép</span></c:when>
                                            <c:otherwise><span class="badge-inactive">Không hoạt động</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a class="btn btn-sm btn-outline-primary"
                                           href="${pageContext.request.contextPath}/v1/manager/employee-detail?id=${emp.employeeId}">
                                            <i class="fa-solid fa-eye"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <c:if test="${totalPages >= 1}">
                    <div class="pagination-wrap">
                        <span class="text-muted small">Trang ${currentPage}/${totalPages}</span>
                        <ul class="pagination">
                            <li class="page-item <c:if test='${currentPage <= 1}'>disabled</c:if>">
                                <a class="page-link" href="${pageContext.request.contextPath}/v1/manager/my-department-list?page=${currentPage - 1}&keyword=${keyword}&status=${status}">&laquo;</a>
                            </li>
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <li class="page-item <c:if test='${i == currentPage}'>active</c:if>">
                                    <a class="page-link" href="${pageContext.request.contextPath}/v1/manager/my-department-list?page=${i}&keyword=${keyword}&status=${status}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item <c:if test='${currentPage >= totalPages}'>disabled</c:if>">
                                <a class="page-link" href="${pageContext.request.contextPath}/v1/manager/my-department-list?page=${currentPage + 1}&keyword=${keyword}&status=${status}">&raquo;</a>
                            </li>
                        </ul>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
