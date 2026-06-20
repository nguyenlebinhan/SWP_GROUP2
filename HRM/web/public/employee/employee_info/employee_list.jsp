<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Danh sách nhân viên - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background: #f5f6fa;
                font-family: 'Segoe UI', sans-serif;
            }
            .main {
                margin-left: 250px;
                padding: 25px;
            }

            .page-card {
                background: white;
                border-radius: 14px;
                box-shadow: 0 2px 12px rgba(0,0,0,0.07);
                padding: 24px;
            }

            .table th {
                font-size: 13px;
                color: #6b7280;
                font-weight: 600;
                background: #f9fafb;
            }
            .table td {
                font-size: 14px;
                vertical-align: middle;
            }

            .badge-active   {
                background: #d1fae5;
                color: #065f46;
                padding: 4px 10px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
            }
            .badge-inactive {
                background: #fee2e2;
                color: #991b1b;
                padding: 4px 10px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
            }
            .badge-leave    {
                background: #fef3c7;
                color: #92400e;
                padding: 4px 10px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
            }

            .avatar-circle {
                width: 36px;
                height: 36px;
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

            .search-bar {
                display: flex;
                gap: 10px;
                align-items: center;
                flex-wrap: wrap;
                background: white;
                padding: 16px 20px;
                border: 1px solid #eef2f7;
                border-radius: 10px;
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
            }
            .search-bar select {
                min-width: 160px;
                padding: 8px 14px;
                border: 1px solid #e5e7eb;
                border-radius: 7px;
                font-size: 14px;
                outline: none;
                background: white;
            }
            .search-bar input[type="text"]:focus {
                border-color: #2563eb;
            }
            .search-bar select:focus {
                border-color: #2563eb;
            }
            .btn-search {
                background: #2563eb;
                color: white;
                border: none;
                padding: 8px 18px;
                border-radius: 7px;
                font-size: 14px;
                font-weight: 600;
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
                color: #374151;
            }
        </style>
    </head>
    <body>

        <jsp:include page="/public/components/employeeSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/employeeTopBar.jsp">
                <jsp:param name="title" value="Danh sách nhân viên" />
                <jsp:param name="backUrl" value="/v1/employee/dashboard" />
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
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h5 class="fw-bold mb-1">Nhân viên</h5>
                        <span class="text-muted small">${employees.size()} nhân viên</span>
                    </div>
                    <c:if test="${canAssignDept}">
                        <a href="${pageContext.request.contextPath}/v1/employee/department/assign"
                           class="btn btn-primary btn-sm" style="background:#2563eb;border:none">
                            <i class="fa-solid fa-plus me-1"></i> Phân công nhân viên
                        </a>
                    </c:if>
                </div>

                <div class="search-bar" id="filterBar">
                    <input type="text" id="filterKeyword" placeholder="Tìm theo họ và tên, email..." oninput="filterTable()" />
                    <select id="filterDepartment" onchange="filterTable()">
                        <option value="">Tất cả phòng ban</option>
                        <c:forEach var="emp" items="${employees}">
                            <c:if test="${not empty emp.departmentName}">
                                <option value="${fn:toLowerCase(emp.departmentName)}">${emp.departmentName}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                    <select id="filterStatus" onchange="filterTable()">
                        <option value="">Tất cả trạng thái</option>
                        <option value="1">Đang làm việc</option>
                        <option value="2">Đang nghỉ phép</option>
                        <option value="0">Không hoạt động</option>
                    </select>
                    <button type="button" class="btn-search" onclick="filterTable()">Tìm kiếm</button>
                    <a href="javascript:void(0)" class="btn-clear" onclick="clearFilters()">Xóa lọc</a>
                </div>

                <c:choose>
                    <c:when test="${empty employees}">
                        <div class="text-center py-5">
                            <i class="fa-solid fa-users-slash" style="font-size:48px;color:#cbd5e1;margin-bottom:16px"></i>
                            <h6 class="text-muted">Chưa có nhân viên nào</h6>
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
                                        <tr data-name="${fn:toLowerCase(emp.fullName)}"
                                            data-email="${fn:toLowerCase(emp.email)}"
                                            data-department="${fn:toLowerCase(emp.departmentName)}"
                                            data-status="${emp.status}">
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
                                            <td>${emp.departmentName}</td>
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
                                                <div class="d-flex gap-2">
                                                    <a class="btn btn-sm btn-outline-primary"
                                                       href="${pageContext.request.contextPath}/v1/employee/employee_info/detail?id=${emp.employeeId}">
                                                        <i class="fa-solid fa-eye"></i>
                                                    </a>
                                                    <c:if test="${canEditEmployee}">
                                                        <a class="btn btn-sm btn-outline-secondary"
                                                           href="${pageContext.request.contextPath}/v1/employee/employee_info/update?id=${emp.employeeId}">
                                                            <i class="fa-solid fa-pen"></i>
                                                        </a>
                                                    </c:if>
                                                    <c:if test="${canAddEmploymentContract}">
                                                        <a class="btn btn-sm btn-outline-success"
                                                           href="${pageContext.request.contextPath}/v1/employee/contract/add?employeeId=${emp.employeeId}">
                                                            <i class="fa-solid fa-file-contract"></i>
                                                        </a>
                                                    </c:if>
                                                </div>
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
        <script>
                        document.addEventListener('DOMContentLoaded', function () {
                            var seen = {};
                            var options = document.querySelectorAll('#filterDepartment option');
                            options.forEach(function (option) {
                                if (!option.value) {
                                    return;
                                }
                                if (seen[option.value]) {
                                    option.remove();
                                    return;
                                }
                                seen[option.value] = true;
                            });
                        });

                        function filterTable() {
                            var kw = document.getElementById('filterKeyword').value.toLowerCase().trim();
                            var department = document.getElementById('filterDepartment').value;
                            var status = document.getElementById('filterStatus').value;
                            var rows = document.querySelectorAll('tbody tr[data-name]');
                            rows.forEach(function (row) {
                                var name = row.dataset.name || '';
                                var email = row.dataset.email || '';
                                var rowDepartment = row.dataset.department || '';
                                var rowStatus = row.dataset.status || '';
                                var matchKeyword = !kw || name.includes(kw) || email.includes(kw);
                                var matchDepartment = !department || rowDepartment === department;
                                var matchStatus = !status || rowStatus === status;
                                var match = matchKeyword && matchDepartment && matchStatus;
                                row.style.display = match ? '' : 'none';
                            });
                        }

                        function clearFilters() {
                            document.getElementById('filterKeyword').value = '';
                            document.getElementById('filterDepartment').value = '';
                            document.getElementById('filterStatus').value = '';
                            filterTable();
                        }
        </script>
    </body>
</html>
