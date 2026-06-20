<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Chấm công của tôi - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card { background:#fff; border-radius:14px; box-shadow:0 2px 12px rgba(0,0,0,0.07); padding:24px; margin-bottom:24px; }
        .stat { border-radius:12px; padding:14px 16px; text-align:center; }
        .stat .num { font-size:22px; font-weight:700; line-height:1; }
        .stat .lbl { font-size:12px; font-weight:600; margin-top:4px; }
        .badge-s0,.st0 { background:#d1fae5; color:#065f46; }
        .badge-s1,.st1 { background:#fef3c7; color:#92400e; }
        .badge-s2,.st2 { background:#fee2e2; color:#991b1b; }
        .badge-s3 { background:#e5e7eb; color:#374151; }
        .badge-s4,.st4 { background:#dbeafe; color:#1e40af; }
        .badge-s5,.st5 { background:#ede9fe; color:#5b21b6; }
        .badge-s6,.st6 { background:#f3f4f6; color:#4b5563; }
        .stx { background:#eef2ff; color:#3730a3; }
        .badge-st { padding:4px 10px; border-radius:20px; font-size:12px; font-weight:600; }
    </style>
</head>
<body>

<jsp:include page="${empty sidebarPage ? '/public/components/employeeSideBar.jsp' : sidebarPage}" />

<div class="main">
    <jsp:include page="${empty topbarPage ? '/public/components/employeeTopBar.jsp' : topbarPage}">
        <jsp:param name="title" value="Chấm công của tôi" />
    </jsp:include>

    <%-- Tổng hợp trong tháng --%>
    <div class="section-card">
        <h5 class="mb-3">
            <i class="fa-solid fa-chart-pie me-2"></i>
            Tổng hợp tháng ${selectedMonth}/${selectedYear}
        </h5>
        <div class="row g-2 mb-3">
            <div class="col"><div class="stat st0"><div class="num">${summary.presentDays}</div><div class="lbl">Đúng giờ</div></div></div>
            <div class="col"><div class="stat st1"><div class="num">${summary.lateDays}</div><div class="lbl">Đi muộn</div></div></div>
            <div class="col"><div class="stat st4"><div class="num">${summary.leaveDays}</div><div class="lbl">Nghỉ phép</div></div></div>
            <div class="col"><div class="stat st2"><div class="num">${summary.absentDays}</div><div class="lbl">Vắng mặt</div></div></div>
            <div class="col"><div class="stat st5"><div class="num">${summary.holidayDays}</div><div class="lbl">Nghỉ lễ</div></div></div>
            <div class="col"><div class="stat st6"><div class="num">${summary.weekendDays}</div><div class="lbl">Cuối tuần</div></div></div>
        </div>
        <div class="row g-2">
            <div class="col"><div class="stat stx"><div class="num">${summary.workedHoursRounded}h</div><div class="lbl">Giờ làm thực tế</div></div></div>
            <div class="col"><div class="stat stx"><div class="num">${summary.standardHours}h</div><div class="lbl">Giờ chuẩn</div></div></div>
            <div class="col"><div class="stat stx"><div class="num">${summary.attendanceRate}%</div><div class="lbl">Tỷ lệ chuyên cần</div></div></div>
        </div>
    </div>

    <%-- Bộ lọc --%>
    <div class="section-card">
        <form method="get" action="${pageContext.request.contextPath}/v1/employee/attendance/own-attendance"
              class="row g-3 align-items-end">
            <div class="col-md-3">
                <label class="form-label">Ngày</label>
                <select name="day" class="form-select">
                    <option value="0" ${selectedDay == 0 ? 'selected' : ''}>Tất cả ngày</option>
                    <c:forEach var="d" begin="1" end="31">
                        <option value="${d}" ${selectedDay == d ? 'selected' : ''}>Ngày ${d}</option>
                    </c:forEach>
                </select>
            </div>
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
            <div class="col-md-3">
                <button type="submit" class="btn btn-primary w-100">
                    <i class="fa-solid fa-magnifying-glass me-1"></i> Tìm kiếm
                </button>
            </div>
        </form>
    </div>

    <%-- Bảng chi tiết --%>
    <div class="section-card">
        <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="text-muted">Tổng ${totalItems} bản ghi</span>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Ngày</th>
                        <th>Giờ vào</th>
                        <th>Giờ ra</th>
                        <th>Số giờ</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty attendances}">
                            <tr><td colspan="5" class="text-center text-muted py-4">Không có dữ liệu chấm công.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="a" items="${attendances}">
                                <tr>
                                    <td>${a.workDate}</td>
                                    <td>${a.timeIn}</td>
                                    <td>${a.timeOut}</td>
                                    <td>${a.hoursWorkedLabel}</td>
                                    <td><span class="badge-st badge-s${a.attendanceStatus}">${a.statusLabel}</span></td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <%-- Phân trang --%>
        <c:if test="${totalPages > 1}">
            <c:url var="pageBase" value="/v1/employee/attendance/own-attendance">
                <c:param name="day" value="${selectedDay}" />
                <c:param name="month" value="${selectedMonth}" />
                <c:param name="year" value="${selectedYear}" />
            </c:url>
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
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
