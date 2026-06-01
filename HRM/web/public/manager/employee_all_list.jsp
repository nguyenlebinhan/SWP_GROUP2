<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Danh sách nhân viên – HRM Manager</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main-content { margin-left: 250px; padding: 30px; }
        .search-bar {
            display: flex; gap: 10px; align-items: center; flex-wrap: wrap;
            background: white; padding: 16px 20px; border-radius: 10px;
            box-shadow: 0 1px 4px rgba(0,0,0,.06); margin-bottom: 20px;
        }
        .search-bar input[type="text"] {
            flex: 1; min-width: 200px; padding: 8px 14px;
            border: 1px solid #e5e7eb; border-radius: 7px; font-size: 14px; outline: none;
        }
        .search-bar input[type="text"]:focus { border-color: #6366f1; }
        .search-bar select {
            padding: 8px 12px; border: 1px solid #e5e7eb;
            border-radius: 7px; font-size: 14px; background: white; outline: none;
        }
        .btn-search {
            background: #6366f1; color: white; border: none;
            padding: 8px 18px; border-radius: 7px; font-size: 14px; font-weight: 600;
        }
        .btn-search:hover { background: #4f46e5; }
        .btn-clear {
            background: white; color: #6b7280; border: 1px solid #d1d5db;
            padding: 8px 14px; border-radius: 7px; font-size: 13px;
            text-decoration: none; font-weight: 500;
        }
        .btn-clear:hover { background: #f3f4f6; }
        .table thead th { font-weight: 600; font-size: 13px; padding: 14px 16px; border: none; }
        .table tbody tr { vertical-align: middle; }
        .table tbody td { padding: 12px 16px; font-size: 14px; border-bottom: 1px solid #f3f4f6; }
        .table tbody tr:hover { background: #fafafa; }
        .badge-active { background: #d1fae5; color: #065f46; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-inactive { background: #fee2e2; color: #991b1b; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-leave { background: #fef3c7; color: #92400e; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .btn-action {
            padding: 5px 12px; font-size: 13px; border-radius: 6px;
            text-decoration: none; display: inline-flex; align-items: center;
            gap: 5px; font-weight: 500; transition: opacity .2s;
        }
        .btn-action:hover { opacity: .85; }
        .btn-detail { background:#e0f2fe; color:#0369a1; border:1px solid #bae6fd; }
        .total-label { font-size: 13px; color: #6b7280; }
        .alert-flash { border-radius: 8px; font-size: 14px; margin-bottom: 20px; }
        .empty-state { text-align: center; padding: 60px 0; color: #9ca3af; }
        /* ── Pagination ── */
        .pagination-wrap {
            display: flex; justify-content: space-between; align-items: center;
            padding: 14px 20px; border-top: 1px solid #f3f4f6;
        }
        .pagination { margin: 0; }
        .page-link { color: #6366f1; border-color: #e5e7eb; font-size: 13px; padding: 5px 12px; }
        .page-item.active .page-link { background: #6366f1; border-color: #6366f1; color: white; }
        .page-item.disabled .page-link { color: #d1d5db; }
    </style>
</head>
<body>
    <jsp:include page="/public/components/managerSideBar.jsp" />

    <div class="main-content">
        <jsp:include page="/public/components/managerTopBar.jsp">
            <jsp:param name="title" value="Danh sách nhân viên" />
        </jsp:include>

        <c:if test="${not empty sessionScope.success}">
            <div class="alert alert-success alert-flash alert-dismissible fade show">
                ${sessionScope.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="success" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.error}">
            <div class="alert alert-danger alert-flash alert-dismissible fade show">
                ${sessionScope.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="error" scope="session"/>
        </c:if>

        <form method="get" action="${pageContext.request.contextPath}/v1/manager/employee-list">
            <div class="search-bar">
                <input type="text" name="keyword" placeholder="Tìm theo họ và tên, email..." value="${param.keyword}" />
                <select name="dept">
                    <option value="">-- Phòng ban --</option>
                    <c:forEach var="d" items="${departments}">
                        <option value="${d.departmentName}" ${param.dept == d.departmentName ? 'selected' : ''}>${d.departmentName}</option>
                    </c:forEach>
                </select>
                <select name="status">
                    <option value="">-- Trạng thái --</option>
                    <option value="1" ${param.status == '1' ? 'selected' : ''}>Đang làm việc</option>
                    <option value="0" ${param.status == '0' ? 'selected' : ''}>Không hoạt động</option>
                    <option value="2" ${param.status == '2' ? 'selected' : ''}>Đang nghỉ phép</option>
                </select>
                <button type="submit" class="btn-search">Tìm kiếm</button>
                <a href="${pageContext.request.contextPath}/v1/manager/employee-list" class="btn-clear">Xóa lọc</a>
            </div>
        </form>

        <div class="card">
            <div class="card-body p-0">
                <table class="table mb-0">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Họ và tên</th>
                            <th>Mã NV</th>
                            <th>Email</th>
                            <th>Phòng ban</th>
                            <th>Vị trí</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty employees}">
                                <tr>
                                    <td colspan="8"><div class="empty-state">Không tìm thấy nhân viên nào</div></td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="emp" items="${employees}" varStatus="loop">
                                    <tr>
                                        <td style="color:#9ca3af;font-size:13px">${(currentPage - 1) * 5 + loop.index + 1}</td>
                                        <td><strong>${emp.fullName}</strong></td>
                                        <td><code>${emp.employeeCode}</code></td>
                                        <td style="color:#6b7280">${emp.email}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty emp.departmentName}">
                                                    <span style="background:#e0e7ff;color:#3730a3;padding:3px 10px;border-radius:20px;font-size:12px;font-weight:600">${emp.departmentName}</span>
                                                </c:when>
                                                <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${emp.positionName}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${emp.status == 1}"><span class="badge-active">Đang làm việc</span></c:when>
                                                <c:when test="${emp.status == 2}"><span class="badge-leave">Đang nghỉ phép</span></c:when>
                                                <c:otherwise><span class="badge-inactive">Không hoạt động</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/v1/manager/employee-detail?id=${emp.employeeId}" class="btn-action btn-detail">Chi tiết</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
                <c:if test="${totalPages >= 1}">
                    <div class="pagination-wrap">
                        <span class="total-label">
                            Tổng <strong>${totalEmployees}</strong> nhân viên
                            &nbsp;|&nbsp; Trang ${currentPage}/${totalPages}
                        </span>
                        <ul class="pagination">
                            <li class="page-item <c:if test='${currentPage <= 1}'>disabled</c:if>">
                                <a class="page-link" href="?page=${currentPage - 1}&keyword=${keyword}&dept=${dept}&status=${status}">&laquo;</a>
                            </li>
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <li class="page-item <c:if test='${i == currentPage}'>active</c:if>">
                                    <a class="page-link" href="?page=${i}&keyword=${keyword}&dept=${dept}&status=${status}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item <c:if test='${currentPage >= totalPages}'>disabled</c:if>">
                                <a class="page-link" href="?page=${currentPage + 1}&keyword=${keyword}&dept=${dept}&status=${status}">&raquo;</a>
                            </li>
                        </ul>
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
