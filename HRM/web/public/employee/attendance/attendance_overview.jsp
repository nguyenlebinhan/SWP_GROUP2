<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tổng quan chấm công - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card { background:#fff; border-radius:14px; box-shadow:0 2px 12px rgba(0,0,0,0.07); padding:24px; margin-bottom:24px; }
        .kpi { background:#fff; border-radius:14px; box-shadow:0 2px 12px rgba(0,0,0,0.06); padding:18px 20px; }
        .kpi .label { font-size:12px; color:#64748b; text-transform:uppercase; letter-spacing:.05em; }
        .kpi .value { font-size:26px; font-weight:700; color:#0B0E2A; }
        .att-table th { background:#0B0E2A; color:#fff; font-size:12.5px; font-weight:600; white-space:nowrap; }
        .att-table td { vertical-align:middle; font-size:13.5px; }
        .badge-s0 { background:#d1fae5; color:#065f46; }
        .badge-s1 { background:#fef3c7; color:#92400e; }
        .badge-s2 { background:#fee2e2; color:#991b1b; }
        .badge-s4 { background:#dbeafe; color:#1e40af; }
        .badge-s5 { background:#ede9fe; color:#5b21b6; }
        .badge-s6 { background:#f3f4f6; color:#4b5563; }
        .badge-ot { background:#fef3c7; color:#92400e; }
        .cnt { display:inline-block; min-width:26px; text-align:center; padding:3px 7px; border-radius:8px; font-size:12px; font-weight:600; }
        .progress { height:8px; border-radius:6px; }
    </style>
</head>
<body>

<jsp:include page="${sidebarPath}" />

<div class="main">
    <jsp:include page="${topbarPath}">
        <jsp:param name="title" value="Tổng quan chấm công" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show">
            <i class="fa-solid fa-circle-xmark me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${empty error}">
    <c:set var="deptParam" value="${empty selectedDepartmentId ? 0 : selectedDepartmentId}" />

    <%-- Bộ lọc + Export --%>
    <div class="section-card">
        <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
            <h5 class="mb-0">
                <c:choose>
                    <c:when test="${canViewAll}">
                        Phạm vi: <strong class="text-primary">${empty selectedDepartmentId ? 'Toàn công ty' : departmentName}</strong>
                    </c:when>
                    <c:otherwise>
                        Phòng ban: <strong class="text-primary">${departmentName}</strong>
                    </c:otherwise>
                </c:choose>
            </h5>
            <div class="d-flex gap-2 flex-wrap">
                <c:if test="${sessionScope.userPermissions.contains('IMPORT_ATTENDANCE')}">
                    <a class="btn btn-primary"
                       href="${pageContext.request.contextPath}/v1/employee/attendance/import">
                        <i class="fa-solid fa-file-import me-1"></i> Import chấm công
                    </a>
                </c:if>
                <a class="btn btn-success"
                   href="${baseUrl}/export?month=${selectedMonth}&year=${selectedYear}<c:if test="${canViewAll}">&departmentId=${deptParam}</c:if>">
                    <i class="fa-solid fa-file-excel me-1"></i> Export Attendance Report
                </a>
            </div>
        </div>
        <form method="get" action="${baseUrl}/overview" class="row g-3 align-items-end">
            <div class="col-md-3">
                <label class="form-label">Tháng</label>
                <select name="month" class="form-select">
                    <c:forEach var="m" begin="1" end="12">
                        <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label class="form-label">Năm</label>
                <select name="year" class="form-select">
                    <c:forEach var="y" begin="2022" end="2030">
                        <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                    </c:forEach>
                </select>
            </div>
            <c:if test="${canViewAll}">
                <div class="col-md-4">
                    <label class="form-label">Phòng ban</label>
                    <select name="departmentId" class="form-select">
                        <option value="0" ${empty selectedDepartmentId ? 'selected' : ''}>Toàn công ty</option>
                        <c:forEach var="d" items="${departments}">
                            <option value="${d.departmentId}" ${selectedDepartmentId == d.departmentId ? 'selected' : ''}>${d.departmentName}</option>
                        </c:forEach>
                    </select>
                </div>
            </c:if>
            <div class="col-md-2">
                <button type="submit" class="btn btn-primary w-100"><i class="fa-solid fa-filter me-1"></i> Lọc</button>
            </div>
        </form>
    </div>
    <c:set var="totWorked" value="0" />
    <c:set var="totRate" value="0" />
    <c:set var="empCount" value="${fn:length(summaries)}" />
    <c:forEach var="s" items="${summaries}">
        <c:set var="totWorked" value="${totWorked + s.workedHoursRounded}" />
        <c:set var="totRate" value="${totRate + s.attendanceRate}" />
    </c:forEach>
    <c:set var="stdHours" value="${empCount > 0 ? summaries[0].standardHours : 0}" />
    <div class="row g-3 mb-1">
        <div class="col-md-3"><div class="kpi"><div class="label">Số nhân viên</div><div class="value">${empCount}</div></div></div>
        <div class="col-md-3"><div class="kpi"><div class="label">Tổng giờ làm</div><div class="value">${totWorked}h</div></div></div>
        <div class="col-md-3"><div class="kpi"><div class="label">Giờ chuẩn / NV</div><div class="value">${stdHours}h</div></div></div>
        <div class="col-md-3"><div class="kpi"><div class="label">Tỷ lệ TB</div>
            <div class="value">${empCount > 0 ? (totRate / empCount).intValue() : 0}%</div></div></div>
    </div>

    <%-- Bảng nhân viên --%>
    <div class="section-card">
        <div class="table-responsive">
            <table class="table table-hover align-middle att-table">
                <thead>
                    <tr>
                        <th>Mã NV</th>
                        <th>Họ tên</th>
                        <c:if test="${canViewAll}"><th>Phòng ban</th></c:if>
                        <th>Vị trí</th>
                        <th class="text-center">Giờ làm / chuẩn</th>
                        <th class="text-center" title="Đúng giờ">P</th>
                        <th class="text-center" title="Đi muộn">L</th>
                        <th class="text-center" title="Nghỉ phép">Lv</th>
                        <th class="text-center" title="Vắng mặt">Ab</th>
                        <th class="text-center" title="Nghỉ lễ">Ho</th>
                        <th class="text-center" title="Cuối tuần">We</th>
                        <th class="text-center" title="Tăng ca (số ngày có đơn OT được duyệt)"> OT</th>
                        <th class="text-center" style="min-width:130px">Tỷ lệ</th>
                        <th class="text-center"></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="s" items="${pagedSummaries}">
                        <tr>
                            <td><strong>${s.employeeCode}</strong></td>
                            <td>${s.fullName}</td>
                            <c:if test="${canViewAll}"><td>${s.departmentName}</td></c:if>
                            <td>${s.positionName}</td>
                            <td class="text-center">${s.workedHoursRounded}h / ${s.standardHours}h</td>
                            <td class="text-center"><span class="cnt badge-s0">${s.presentDays}</span></td>
                            <td class="text-center"><span class="cnt badge-s1">${s.lateDays}</span></td>
                            <td class="text-center"><span class="cnt badge-s4">${s.leaveDays}</span></td>
                            <td class="text-center"><span class="cnt badge-s2">${s.absentDays}</span></td>
                            <td class="text-center"><span class="cnt badge-s5">${s.holidayDays}</span></td>
                            <td class="text-center"><span class="cnt badge-s6">${s.weekendDays}</span></td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${s.otDays > 0}">
                                        <span class="cnt badge-ot" title="${s.otDays} ngày tăng ca">
                                            ${s.otDays}
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="cnt badge-s6">0</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="d-flex justify-content-between">
                                    <small class="fw-semibold ${s.attendanceRate < 80 ? 'text-danger' : 'text-success'}">${s.attendanceRate}%</small>
                                </div>
                                <div class="progress">
                                    <div class="progress-bar ${s.attendanceRate < 80 ? 'bg-danger' : 'bg-success'}"
                                         style="width:${s.attendanceRate}%"></div>
                                </div>
                            </td>
                            <td class="text-center">
                                <a class="btn btn-sm btn-outline-primary"
                                   href="${baseUrl}/detail?employeeId=${s.employeeId}&month=${selectedMonth}&year=${selectedYear}<c:if test="${canViewAll}">&departmentId=${deptParam}</c:if>">
                                    <i class="fa-solid fa-eye me-1"></i>Chi tiết
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pagedSummaries}">
                        <tr><td colspan="${canViewAll ? 14 : 13}" class="text-center text-muted py-4">Không có dữ liệu nhân viên.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <c:if test="${totalPages > 1}">
            <c:set var="pageBase" value="${baseUrl}/overview?month=${selectedMonth}&year=${selectedYear}&departmentId=${deptParam}" />
            <nav class="mt-3">
                <ul class="pagination justify-content-center mb-0">
                    <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                        <a class="page-link" href="${pageBase}&page=${currentPage - 1}">&laquo;</a>
                    </li>
                    <c:forEach var="p" begin="1" end="${totalPages}">
                        <li class="page-item ${p == currentPage ? 'active' : ''}">
                            <a class="page-link" href="${pageBase}&page=${p}">${p}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                        <a class="page-link" href="${pageBase}&page=${currentPage + 1}">&raquo;</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </div>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
