<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Quản lý người dùng – HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
            body {
                background: #f5f6fa;
                font-family: 'Segoe UI', sans-serif;
            }

            .main-content {
                margin-left: 250px;
                padding: 30px;
            }

            /* ── Search bar ── */
            .search-bar {
                display: flex;
                gap: 10px;
                align-items: center;
                flex-wrap: wrap;
                background: white;
                padding: 16px 20px;
                border-radius: 10px;
                box-shadow: 0 1px 4px rgba(0,0,0,.06);
                margin-bottom: 20px;
            }
            .search-bar input[type="text"] {
                flex: 1;
                min-width: 200px;
                padding: 8px 14px;
                border: 1px solid #e5e7eb;
                border-radius: 7px;
                font-size: 14px;
                outline: none;
                transition: border .2s;
            }
            .search-bar input[type="text"]:focus {
                border-color: #6366f1;
            }
            .search-bar select {
                padding: 8px 12px;
                border: 1px solid #e5e7eb;
                border-radius: 7px;
                font-size: 14px;
                background: white;
                outline: none;
            }
            .btn-search {
                background: #6366f1;
                color: white;
                border: none;
                padding: 8px 18px;
                border-radius: 7px;
                font-size: 14px;
                font-weight: 600;
                cursor: pointer;
            }
            .btn-search:hover {
                background: #4f46e5;
            }
            .btn-clear {
                background: white;
                color: #6b7280;
                border: 1px solid #d1d5db;
                padding: 8px 14px;
                border-radius: 7px;
                font-size: 13px;
                text-decoration: none;
                font-weight: 500;
            }
            .btn-clear:hover {
                background: #f3f4f6;
            }

            /* ── Table ── */
            .table thead th {
                font-weight: 600;
                font-size: 13px;
                letter-spacing: .4px;
                padding: 14px 16px;
                border: none;
            }
            .table tbody tr {
                vertical-align: middle;
            }
            .table tbody td {
                padding: 12px 16px;
                font-size: 14px;
                border-bottom: 1px solid #f3f4f6;
            }
            .table tbody tr:hover {
                background: #fafafa;
            }

            .badge-active {
                background: #d1fae5;
                color: #065f46;
                padding: 4px 12px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
            }
            .badge-inactive {
                background: #fee2e2;
                color: #991b1b;
                padding: 4px 12px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
            }

            .btn-action {
                padding: 5px 12px;
                font-size: 13px;
                border-radius: 6px;
                text-decoration: none;
                display: inline-flex;
                align-items: center;
                gap: 5px;
                font-weight: 500;
                transition: opacity .2s;
            }
            .btn-action:hover {
                opacity: .85;
            }
            .btn-edit      {
                background:#e0f2fe;
                color:#0369a1;
                border:1px solid #bae6fd;
            }
            .btn-activate  {
                background:#d1fae5;
                color:#065f46;
                border:1px solid #6ee7b7;
            }
            .btn-deactivate{
                background:#fef3c7;
                color:#92400e;
                border:1px solid #fde68a;
            }

            .btn-add {
                background: #ff8c00;
                color: white;
                border: none;
                padding: 9px 20px;
                border-radius: 8px;
                font-weight: 600;
                font-size: 14px;
                text-decoration: none;
                display: flex;
                align-items: center;
                gap: 8px;
            }
            .btn-add:hover {
                background: #e07b00;
                color: white;
            }

            /* ── Pagination ── */
            .pagination-wrap {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 14px 20px;
                border-top: 1px solid #f3f4f6;
            }
            .pagination {
                margin: 0;
            }
            .page-link {
                color: #6366f1;
                border-color: #e5e7eb;
                font-size: 13px;
                padding: 5px 12px;
            }
            .page-item.active .page-link {
                background: #6366f1;
                border-color: #6366f1;
                color: white;
            }
            .page-item.disabled .page-link {
                color: #d1d5db;
            }
            .total-label {
                font-size: 13px;
                color: #6b7280;
            }

            .alert-flash {
                border-radius: 8px;
                font-size: 14px;
                margin-bottom: 20px;
            }
            .empty-state {
                text-align: center;
                padding: 60px 0;
                color: #9ca3af;
            }
        </style>
    </head>
    <body>

        <jsp:include page="/public/components/adminSideBar.jsp" />

        <div class="main-content">
            <jsp:include page="/public/components/adminTopBar.jsp">
                <jsp:param name="title" value="Quản lý người dùng" />
            </jsp:include>

            <%-- Flash messages --%>
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

            <%-- Top action row --%>
            <div class="d-flex justify-content-end mb-3">
                <a href="${pageContext.request.contextPath}/v1/admin/add-user" class="btn-add">
                    + Thêm người dùng
                </a>
            </div>

            <%-- Search & filter bar --%>
            <form method="get"
                  action="${pageContext.request.contextPath}/v1/admin/user-list"
                  class="search-bar">

                <input type="text" name="keyword" value="${keyword}"
                       placeholder="Tìm theo username..." />

                <%-- Dropdown role giữ nguyên --%>
                <select name="role">
                    <option value="">-- Vai trò --</option>
                    <c:forEach var="r" items="${roles}">
                        <option value="${r.roleName}"
                                <c:if test="${r.roleName eq role}">selected</c:if>>
                            ${r.roleName}
                        </option>
                    </c:forEach>
                </select>

                <button type="submit" class="btn-search">🔍 Tìm kiếm</button>
                <a href="${pageContext.request.contextPath}/v1/admin/user-list" class="btn-clear">
                    Xóa lọc
                </a>
            </form>

            <%-- Table --%>
            <div class="card">
                <div class="card-body p-0">
                    <table class="table mb-0">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Tên đăng nhập</th>
                                <th>Họ và tên</th>
                                <th>Email</th>
                                <th>Vai trò</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty list}">
                                    <tr>
                                        <td colspan="7">
                                            <div class="empty-state">Không tìm thấy người dùng nào</div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="u" items="${list}" varStatus="loop">
                                        <tr>
                                            <%-- Số thứ tự theo trang --%>
                                            <td style="color:#9ca3af;font-size:13px">
                                                ${loop.index + 1}
                                            </td>
                                            <td><strong>${u.username}</strong></td>
                                            <td>${u.fullName}</td>
                                            <td style="color:#6b7280">${u.email}</td>
                                            <td>
                                                <span style="background:#e0e7ff;color:#3730a3;padding:3px 10px;
                                                      border-radius:20px;font-size:12px;font-weight:600">
                                                    ${u.roleName}
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${u.isActive == 1}">
                                                        <span class="badge-active">Hoạt động</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge-inactive">Vô hiệu</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div style="display:flex;gap:6px;">
                                                    <a href="${pageContext.request.contextPath}/v1/admin/view-user-detail?id=${u.userId}"
                                                       class="btn-action btn-edit">Chi tiết</a>
                                                    <a href="${pageContext.request.contextPath}/v1/admin/update-user?id=${u.userId}"
                                                       class="btn-action btn-edit">Cập nhật</a>
                                                    <c:choose>
                                                        <c:when test="${u.isActive == 1}">
                                                            <a href="${pageContext.request.contextPath}/v1/admin/change-status?id=${u.userId}&status=0"
                                                               class="btn-action btn-deactivate"
                                                               onclick="return confirm('Vô hiệu hóa tài khoản ${u.fullName}?')">
                                                                Vô hiệu hóa
                                                            </a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <a href="${pageContext.request.contextPath}/v1/admin/change-status?id=${u.userId}&status=1"
                                                               class="btn-action btn-activate"
                                                               onclick="return confirm('Kích hoạt tài khoản ${u.fullName}?')">
                                                                Kích hoạt
                                                            </a>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <%-- Pagination --%>
                    <c:if test="${totalPages >= 1}">
                        <div class="pagination-wrap">
                            <span class="total-label">
                                Tổng <strong>${totalUsers}</strong> người dùng
                                &nbsp;|&nbsp; Trang ${currentPage}/${totalPages}
                            </span>
                            <ul class="pagination">

                                <li class="page-item <c:if test='${currentPage <= 1}'>disabled</c:if>">
                                        <a class="page-link"
                                           href="?page=${currentPage - 1}&keyword=${keyword}&role=${role}">
                                        &laquo;
                                    </a>
                                </li>

                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <li class="page-item <c:if test='${i == currentPage}'>active</c:if>">
                                            <a class="page-link"
                                               href="?page=${i}&keyword=${keyword}&role=${role}">
                                            ${i}
                                        </a>
                                    </li>
                                </c:forEach>

                                <li class="page-item <c:if test='${currentPage >= totalPages}'>disabled</c:if>">
                                        <a class="page-link"
                                           href="?page=${currentPage + 1}&keyword=${keyword}&role=${role}">
                                        &raquo;
                                    </a>
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