<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Quản lý kỳ chấm công - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
            margin-bottom: 24px;
        }
        .badge-private { background:#fef3c7; color:#92400e; }
        .badge-public  { background:#d1fae5; color:#065f46; }
        .badge-none    { background:#e5e7eb; color:#374151; }
        .badge-locked  { background:#fee2e2; color:#991b1b; }
        .badge-open    { background:#dbeafe; color:#1e40af; }
        .badge-st { padding:4px 10px; border-radius:20px; font-size:12px; font-weight:600; }
    </style>
</head>
<body>

<jsp:include page="${empty sidebarPage ? '/public/components/employeeSideBar.jsp' : sidebarPage}" />

<div class="main">
    <jsp:include page="${empty topbarPage ? '/public/components/employeeTopBar.jsp' : topbarPage}">
        <jsp:param name="title" value="Quản lý kỳ chấm công" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show">
            <i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show">
            <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session"/>
    </c:if>

    <div class="section-card">
        <form method="get" action="${pageContext.request.contextPath}/v1/employee/attendance-periods" class="row g-3">
            <div class="col-md-3">
                <label class="form-label">Tháng</label>
                <select name="month" class="form-select">
                    <c:forEach var="m" begin="1" end="12">
                        <option value="${m}" ${filterMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label class="form-label">Năm</label>
                <input type="number" name="year" class="form-control" min="2000" max="2100" value="${filterYear}">
            </div>
            <div class="col-md-2 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100">
                    <i class="fa-solid fa-magnifying-glass me-1"></i> Xem
                </button>
            </div>
            <div class="col-md-4 d-flex align-items-end justify-content-end">
                <div class="text-end">
                    <div>Hạn chỉnh sửa: <strong>${editDeadline}</strong></div>
                    <c:choose>
                        <c:when test="${periodLocked}">
                            <span class="badge-st badge-locked"><i class="fa-solid fa-lock me-1"></i>Đã khóa chỉnh sửa</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge-st badge-open"><i class="fa-solid fa-lock-open me-1"></i>Còn được chỉnh sửa</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </form>
    </div>

    <div class="section-card">
        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Phòng ban</th>
                        <th>Trạng thái kỳ</th>
                        <th>Số file</th>
                        <th>Dòng hợp lệ</th>
                        <th>Dòng lỗi</th>
                        <th>Công khai bởi</th>
                        <th>Thời điểm công khai</th>
                        <c:if test="${canEditAttendance}">
                            <th></th>
                        </c:if>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${periods}">
                        <tr>
                            <td><c:out value="${p.departmentName}" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.status == null}">
                                        <span class="badge-st badge-none">Chưa có dữ liệu</span>
                                    </c:when>
                                    <c:when test="${p.status == 1}">
                                        <span class="badge-st badge-public">Công khai</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-st badge-private">Riêng tư</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>${p.fileCount}</td>
                            <td class="text-success fw-semibold">${p.importedRows}</td>
                            <td class="${p.failedRows > 0 ? 'text-danger fw-semibold' : 'text-muted'}">${p.failedRows}</td>
                            <td><c:out value="${p.publishedByName}" /></td>
                            <td>${p.publishedAt}</td>
                            <c:if test="${canEditAttendance}">
                                <td class="text-end">
                                    <%-- Nút chỉ hiển thị khi hợp lệ; backend vẫn kiểm tra lại mọi điều kiện. --%>
                                    <c:if test="${p.status == 0}">
                                        <form method="post" class="d-inline"
                                              action="${pageContext.request.contextPath}/v1/employee/attendance-period-status"
                                              onsubmit="return confirm('Công khai kỳ chấm công cho nhân viên phòng này?');">
                                            <input type="hidden" name="departmentId" value="${p.departmentId}">
                                            <input type="hidden" name="month" value="${filterMonth}">
                                            <input type="hidden" name="year" value="${filterYear}">
                                            <input type="hidden" name="targetStatus" value="1">
                                            <button type="submit" class="btn btn-sm btn-success">
                                                <i class="fa-solid fa-eye me-1"></i> Công khai
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${p.status == 1 && !periodLocked}">
                                        <form method="post" class="d-inline"
                                              action="${pageContext.request.contextPath}/v1/employee/attendance-period-status"
                                              onsubmit="return confirm('Chuyển kỳ về riêng tư để chỉnh sửa? Nhân viên sẽ không xem được dữ liệu.');">
                                            <input type="hidden" name="departmentId" value="${p.departmentId}">
                                            <input type="hidden" name="month" value="${filterMonth}">
                                            <input type="hidden" name="year" value="${filterYear}">
                                            <input type="hidden" name="targetStatus" value="0">
                                            <button type="submit" class="btn btn-sm btn-outline-warning">
                                                <i class="fa-solid fa-eye-slash me-1"></i> Chuyển riêng tư
                                            </button>
                                        </form>
                                    </c:if>
                                </td>
                            </c:if>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
