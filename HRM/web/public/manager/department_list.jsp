<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <meta charset="UTF-8">
            <title>Quản lý Phòng ban - HRM Manager</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
            <style>
                body {
                    background: #f5f6fa;
                    font-family: 'Segoe UI', sans-serif;
                }

                .main {
                    margin-left: 250px;
                    padding: 25px;
                }

                .dept-card {
                    background: #fff;
                    border-radius: 14px;
                    box-shadow: 0 2px 12px rgba(11, 14, 42, 0.07);
                    border: none;
                }

                .dept-card:hover {
                    box-shadow: 0 6px 24px rgba(11, 14, 42, 0.13);
                }

                .badge-active {
                    background: #d1fae5;
                    color: #065f46;
                    font-weight: 600;
                    border-radius: 20px;
                    padding: 4px 12px;
                    font-size: 12px;
                }

                .badge-inactive {
                    background: #fee2e2;
                    color: #991b1b;
                    font-weight: 600;
                    border-radius: 20px;
                    padding: 4px 12px;
                    font-size: 12px;
                }

                .badge-manager {
                    background: #ede9fe;
                    color: #5b21b6;
                    font-weight: 600;
                    border-radius: 20px;
                    padding: 4px 12px;
                    font-size: 12px;
                }

                .badge-no-mgr {
                    background: #fef3c7;
                    color: #92400e;
                    font-weight: 600;
                    border-radius: 20px;
                    padding: 4px 12px;
                    font-size: 12px;
                }

                .dept-table th {
                    background: #0B0E2A;
                    color: #fff;
                    font-size: 13px;
                    font-weight: 600;
                }

                .dept-table td {
                    vertical-align: middle;
                    font-size: 14px;
                }

                .btn-assign {
                    background: #1565C0;
                    color: #fff;
                    border: none;
                    border-radius: 8px;
                    padding: 5px 14px;
                    font-size: 13px;
                    font-weight: 600;
                }

                .btn-assign:hover {
                    background: #0d47a1;
                    color: #fff;
                }

                .btn-unassign {
                    background: #fff;
                    color: #dc2626;
                    border: 1.5px solid #dc2626;
                    border-radius: 8px;
                    padding: 5px 14px;
                    font-size: 13px;
                    font-weight: 600;
                }

                .btn-unassign:hover {
                    background: #fee2e2;
                }

                .dept-code {
                    font-family: monospace;
                    background: #f3f4f6;
                    padding: 2px 8px;
                    border-radius: 6px;
                    font-size: 13px;
                    color: #374151;
                }

                .alert-flash {
                    border-radius: 10px;
                    font-weight: 500;
                }
            </style>
        </head>

        <body>
            <jsp:include page="/public/components/managerSideBar.jsp" />

            <div class="main">
                <jsp:include page="/public/components/managerTopBar.jsp">
                    <jsp:param name="title" value="Quản lý Phòng ban" />
                </jsp:include>

                <c:if test="${not empty success}">
                    <div class="alert alert-success alert-flash alert-dismissible fade show" role="alert">
                        ${success}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-flash alert-dismissible fade show" role="alert">
                        ${error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <div class="row g-3 mb-4">
                    <div class="col-md-3">
                        <div class="dept-card p-4 d-flex align-items-center gap-3">
                            <div>
                                <div style="font-size:22px;font-weight:700;color:#0B0E2A;">${departments.size()}</div>
                                <div style="font-size:13px;color:#6b7280;">Tổng phòng ban</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="dept-card p-4 d-flex align-items-center gap-3">
                            <div>
                                <c:set var="activeCount" value="0" />
                                <c:forEach var="d" items="${departments}">
                                    <c:if test="${d.status == 1}">
                                        <c:set var="activeCount" value="${activeCount + 1}" />
                                    </c:if>
                                </c:forEach>
                                <div style="font-size:22px;font-weight:700;color:#0B0E2A;">${activeCount}</div>
                                <div style="font-size:13px;color:#6b7280;">Đang hoạt động</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="dept-card p-4 d-flex align-items-center gap-3">
                            <div>
                                <c:set var="hasManagerCount" value="0" />
                                <c:forEach var="d" items="${departments}">
                                    <c:if test="${not empty managerMap[d.departmentId]}">
                                        <c:set var="hasManagerCount" value="${hasManagerCount + 1}" />
                                    </c:if>
                                </c:forEach>
                                <div style="font-size:22px;font-weight:700;color:#0B0E2A;">${hasManagerCount}</div>
                                <div style="font-size:13px;color:#6b7280;">Đã có Manager</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="dept-card p-4 d-flex align-items-center gap-3">
                            <div>
                                <div style="font-size:22px;font-weight:700;color:#0B0E2A;">${activeCount -
                                    hasManagerCount}</div>
                                <div style="font-size:13px;color:#6b7280;">Chưa có Manager</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="dept-card">
                    <div class="p-4 border-bottom d-flex justify-content-between align-items-center">
                        <h5 class="mb-0 fw-bold" style="color:#0B0E2A;">Danh sách Phòng ban</h5>
                    </div>
                    <div class="p-0">
                        <table class="table dept-table mb-0">
                            <thead>
                                <tr>
                                    <th class="ps-4" style="width:50px">#</th>
                                    <th>Mã phòng ban</th>
                                    <th>Tên phòng ban</th>
                                    <th>Manager hiện tại</th>
                                    <th>Trạng thái</th>
                                    <th class="text-center" style="width:340px">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty departments}">
                                        <tr>
                                            <td colspan="6" class="text-center py-5 text-muted">Chưa có phòng ban nào
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="dept" items="${departments}" varStatus="st">
                                            <tr>
                                                <td class="ps-4 text-muted">${st.index + 1}</td>
                                                <td><span class="dept-code">${dept.departmentCode}</span></td>
                                                <td>
                                                    <span class="fw-semibold">${dept.departmentName}</span>
                                                    <c:if test="${not empty dept.description}">
                                                        <div style="font-size:12px;color:#9ca3af;">${dept.description}
                                                        </div>
                                                    </c:if>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty managerMap[dept.departmentId]}">
                                                            <c:set var="mgr" value="${managerMap[dept.departmentId]}" />
                                                            <div class="d-flex align-items-center gap-2">
                                                                <div>
                                                                    <div class="fw-semibold" style="font-size:13px;">
                                                                        ${mgr.fullName}</div>
                                                                    <span class="badge-manager">${mgr.roleName}</span>
                                                                </div>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise><span class="badge-no-mgr">Chưa có manager</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${dept.status == 1}"><span
                                                                class="badge-active">Hoạt động</span></c:when>
                                                        <c:otherwise><span class="badge-inactive">Tạm dừng</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-center">
                                                    <c:if test="${dept.status == 1}">
                                                        <a href="${pageContext.request.contextPath}/v1/manager/department/assign?id=${dept.departmentId}"
                                                            class="btn btn-assign me-1">
                                                            <c:choose>
                                                                <c:when
                                                                    test="${not empty managerMap[dept.departmentId]}">
                                                                    Đổi MGR</c:when>
                                                                <c:otherwise>Assign MGR</c:otherwise>
                                                            </c:choose>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/v1/manager/assign-department"
                                                            class="btn btn-assign me-1">Assign NV</a>
                                                        <a href="${pageContext.request.contextPath}/v1/manager/department/employees?id=${dept.departmentId}"
                                                            class="btn btn-assign me-1"
                                                            style="background:#e0f2fe;color:#0369a1;border-color:#bae6fd;">Xem
                                                            NV</a>
                                                        <c:if test="${not empty managerMap[dept.departmentId]}">
                                                            <form method="post"
                                                                action="${pageContext.request.contextPath}/v1/manager/department/unassign"
                                                                style="display:inline">
                                                                <input type="hidden" name="departmentId"
                                                                    value="${dept.departmentId}" />
                                                                <button type="submit" class="btn btn-unassign"
                                                                    onclick="return confirm('Bạn có chắc muốn gỡ manager khỏi phòng ban &quot;${dept.departmentName}&quot;?')">Gỡ
                                                                    MGR</button>
                                                            </form>
                                                        </c:if>
                                                    </c:if>
                                                    <c:if test="${dept.status != 1}"><span class="text-muted"
                                                            style="font-size:12px;">—</span></c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>