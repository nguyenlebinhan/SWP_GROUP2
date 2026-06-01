<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Nhân viên phòng ban – HRM Manager</title>
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
            .badge-leave {
                background: #fef3c7;
                color: #92400e;
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
            .btn-detail {
                background:#e0f2fe;
                color:#0369a1;
                border:1px solid #bae6fd;
            }

            .total-label {
                font-size: 13px;
                color: #6b7280;
                padding: 14px 20px;
                border-top: 1px solid #f3f4f6;
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
        <c:choose>
            <c:when test="${fn:toUpperCase(fn:replace(sessionScope.user.roleName, ' ', '')) == 'HRMANAGER'}">
                <jsp:include page="/public/components/managerSideBar.jsp" />
            </c:when>
            <c:otherwise>
                <jsp:include page="/public/components/departmentManagerSideBar.jsp" />
            </c:otherwise>
        </c:choose>

        <div class="main-content">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Nhân viên phòng ban" />
            </jsp:include>

            <%-- Flash messages --%>
            <c:if test="${not empty error}">
                <div class="alert alert-warning alert-flash alert-dismissible fade show">
                    ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <%-- Header --%>
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <h5 class="fw-bold mb-0">
                        <c:choose>
                            <c:when test="${not empty departmentName}">Phòng ${departmentName}</c:when>
                            <c:otherwise>Nhân viên</c:otherwise>
                        </c:choose>
                    </h5>
                </div>
            </div>

            <%-- Search bar --%>
            <div class="search-bar" id="filterBar">
                <input type="text" id="filterKeyword" placeholder="Tìm theo họ và tên, email..." oninput="filterTable()" />
                <button type="button" class="btn-search" onclick="filterTable()">Tìm kiếm</button>
                <a href="javascript:void(0)" class="btn-clear" onclick="clearFilters()">Xóa lọc</a>
            </div>

            <%-- Table --%>
            <div class="card">
                <div class="card-body p-0">
                    <table class="table mb-0" id="employeeTable">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Họ và tên</th>
                                <th>Mã NV</th>
                                <th>Email</th>
                                <th>Vị trí</th>
                                <th>Số điện thoại</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty employees}">
                                    <tr>
                                        <td colspan="8">
                                            <div class="empty-state">Chưa có nhân viên nào trong phòng ban</div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="emp" items="${employees}" varStatus="loop">
                                        <tr data-name="${fn:toLowerCase(emp.fullName)}"
                                            data-email="${fn:toLowerCase(emp.email)}">
                                            <td style="color:#9ca3af;font-size:13px">
                                                ${loop.index + 1}
                                            </td>
                                            <td><strong>${emp.fullName}</strong></td>
                                            <td><code>${emp.employeeCode}</code></td>
                                            <td style="color:#6b7280">${emp.email}</td>
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
                                            <td>
                                                <a href="${pageContext.request.contextPath}/v1/manager/employee-detail?id=${emp.employeeId}"
                                                   class="btn-action btn-detail">Chi tiết</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <c:if test="${not empty employees}">
                        <div class="total-label">
                            Tổng <strong>${employees.size()}</strong> nhân viên
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            function filterTable() {
                var kw = document.getElementById('filterKeyword').value.toLowerCase().trim();
                var rows = document.querySelectorAll('#employeeTable tbody tr[data-name]');
                rows.forEach(function(row) {
                    var name = row.dataset.name || '';
                    var email = row.dataset.email || '';
                    var match = !kw || name.includes(kw) || email.includes(kw);
                    row.style.display = match ? '' : 'none';
                });
            }

            function clearFilters() {
                document.getElementById('filterKeyword').value = '';
                filterTable();
            }
        </script>
    </body>
</html>
