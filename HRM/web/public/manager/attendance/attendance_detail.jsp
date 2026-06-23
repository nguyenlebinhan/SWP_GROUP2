<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết chấm công - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card { background:#fff; border-radius:14px; box-shadow:0 2px 12px rgba(0,0,0,0.07); padding:24px; margin-bottom:24px; }
        .att-table th { background:#0B0E2A; color:#fff; font-size:13px; font-weight:600; }
        .att-table td { vertical-align:middle; font-size:14px; }
        .badge-st { padding:4px 12px; border-radius:20px; font-size:12px; font-weight:600; }
        .badge-s0 { background:#d1fae5; color:#065f46; }
        .badge-s1 { background:#fef3c7; color:#92400e; }
        .badge-s2 { background:#fee2e2; color:#991b1b; }
        .badge-s3 { background:#e5e7eb; color:#374151; }
        .badge-s4 { background:#dbeafe; color:#1e40af; }
        .badge-s5 { background:#ede9fe; color:#5b21b6; }
        .badge-s6 { background:#f3f4f6; color:#4b5563; }
        .hdr-chip { background:#f1f5f9; border-radius:10px; padding:8px 14px; font-size:13px; font-weight:600; color:#334155; }
    </style>
</head>
<body>

<jsp:include page="${sidebarPath}" />

<div class="main">
    <jsp:include page="${topbarPath}">
        <jsp:param name="title" value="Chi tiết chấm công" />
        <jsp:param name="backUrl" value="/v1/manager/attendance/overview" />
    </jsp:include>

    <c:set var="deptParam" value="${empty selectedDepartmentId ? 0 : selectedDepartmentId}" />

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
        <a href="${backUrl}" class="btn btn-secondary"><i class="fa-solid fa-arrow-left me-1"></i>Quay lại</a>
    </c:if>

    <c:if test="${empty error}">
    <c:set var="sm" value="${detail.summary}" />

    <div class="section-card">
        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
            <div>
                <a href="${backUrl}" class="text-decoration-none text-muted small">
                    <i class="fa-solid fa-arrow-left me-1"></i>Quay lại tổng quan</a>
                <h4 class="mb-1 mt-2">${sm.fullName} <span class="text-muted fs-6">(${sm.employeeCode})</span></h4>
                <div class="text-muted">${sm.departmentName} &middot; ${sm.positionName} &middot; Tháng ${selectedMonth}/${selectedYear}</div>
            </div>
            <div class="d-flex gap-2 flex-wrap">
                <span class="hdr-chip">Giờ làm: ${sm.workedHoursRounded}h / ${sm.standardHours}h</span>
                <span class="hdr-chip">Tỷ lệ: ${sm.attendanceRate}%</span>
            </div>
        </div>
        <div class="mt-3 d-flex gap-2 flex-wrap">
            <span class="badge-st badge-s0">Đúng giờ: ${sm.presentDays}</span>
            <span class="badge-st badge-s1">Đi muộn: ${sm.lateDays}</span>
            <span class="badge-st badge-s4">Nghỉ phép: ${sm.leaveDays}</span>
            <span class="badge-st badge-s2">Vắng mặt: ${sm.absentDays}</span>
            <span class="badge-st badge-s5">Nghỉ lễ: ${sm.holidayDays}</span>
            <span class="badge-st badge-s6">Cuối tuần: ${sm.weekendDays}</span>
        </div>
    </div>

    <div class="section-card">
        <form method="get" action="${baseUrl}/detail" class="row g-3 align-items-end mb-3">
            <input type="hidden" name="employeeId" value="${sm.employeeId}" />
            <input type="hidden" name="departmentId" value="${deptParam}" />
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
                <button type="submit" class="btn btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Tìm kiếm</button>
            </div>
        </form>
    </div>
    <div class="section-card">
        <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
            <h5 class="mb-0"><i class="fa-regular fa-calendar me-2"></i>Lịch chấm công tháng ${selectedMonth}/${selectedYear}</h5>
            <div class="cal-legend">
                <span class="cl0">Đúng giờ</span>
                <span class="cl1">Đi muộn</span>
                <span class="cl4">Nghỉ phép</span>
                <span class="cl2">Vắng mặt</span>
                <span class="cl5">Nghỉ lễ</span>
                <span class="cl6">Cuối tuần</span>
            </div>
        </div>
        <div class="cal-grid mb-1">
            <div class="cal-dow">T2</div><div class="cal-dow">T3</div><div class="cal-dow">T4</div>
            <div class="cal-dow">T5</div><div class="cal-dow">T6</div><div class="cal-dow">T7</div><div class="cal-dow">CN</div>
        </div>
        <div class="cal-grid" id="calBody"></div>
    </div>                    
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
