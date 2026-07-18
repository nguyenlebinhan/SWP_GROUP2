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
        .badge-st { padding:4px 12px; border-radius:20px; font-size:12px; font-weight:600; }
        .badge-s0,.cl0 { background:#d1fae5; color:#065f46; }
        .badge-s1,.cl1 { background:#fef3c7; color:#92400e; }
        .badge-s2,.cl2 { background:#fee2e2; color:#991b1b; }
        .badge-s3,.cl3 { background:#e5e7eb; color:#374151; }
        .badge-s4,.cl4 { background:#dbeafe; color:#1e40af; }
        .badge-s6,.cl6 { background:#f3f4f6; color:#4b5563; }
        .badge-s7,.cl7 { background:#ffedd5; color:#9a3412; }
        .hdr-chip { background:#f1f5f9; border-radius:10px; padding:8px 14px; font-size:13px; font-weight:600; color:#334155; }

        /* Calendar */
        .cal-grid { display:grid; grid-template-columns:repeat(7,1fr); gap:8px; }
        .cal-dow { text-align:center; font-weight:600; color:#64748b; font-size:13px; padding:6px 0; }
        .cal-cell { position:relative; min-height:96px; border-radius:10px; border:1px solid #eef0f4; padding:8px; background:#fff; display:flex; flex-direction:column; }
        .cal-cell.empty { background:transparent; border:none; }
        .cal-cell.weekend { background:#fafafa; }
        .cal-cell.today { border-color:#6366f1; box-shadow:0 0 0 2px rgba(99,102,241,.18); }
        .cal-cell .d { font-size:13px; font-weight:700; color:#334155; }
        .cal-cell.off-day .d { color:#cbd5e1; }
        .cal-cell .st { margin-top:auto; font-size:11px; font-weight:600; padding:2px 8px; border-radius:10px; align-self:flex-start; }
        .cal-cell .tm { margin-top:6px; font-size:11px; color:#475569; line-height:1.45; }
        .cal-legend { display:flex; flex-wrap:wrap; gap:10px; }
        .cal-legend span { font-size:12px; font-weight:600; padding:3px 10px; border-radius:10px; }
    </style>
</head>
<body>

<jsp:include page="${sidebarPath}" />

<div class="main">
    <jsp:include page="${topbarPath}">
        <jsp:param name="title" value="Chi tiết chấm công" />
    </jsp:include>

    <c:set var="deptParam" value="${empty selectedDepartmentId ? 0 : selectedDepartmentId}" />
    <c:set var="backUrl" value="${baseUrl}/closing?month=${selectedMonth}&year=${selectedYear}&departmentId=${deptParam}" />

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
                    <i class="fa-solid fa-arrow-left me-1"></i>Quay lại</a>
                <h4 class="mb-1 mt-2">${sm.fullName} <span class="text-muted fs-6">(${sm.employeeCode})</span></h4>
                <div class="text-muted">${sm.departmentName} &middot; ${sm.positionName} &middot; Tháng ${selectedMonth}/${selectedYear}</div>
            </div>
            <div class="d-flex gap-2 flex-wrap">
                <span class="hdr-chip">Giờ làm: ${sm.workedHoursDisplay}h / ${sm.standardHours}h</span>
                <span class="hdr-chip">Tỷ lệ: ${sm.attendanceRate}%</span>
            </div>
        </div>
        <div class="mt-3 d-flex gap-2 flex-wrap">
            <span class="badge-st badge-s0">Đúng giờ: ${sm.presentDays}</span>
            <span class="badge-st badge-s1">Đi muộn: ${sm.lateDays}</span>
            <span class="badge-st badge-s4">Nghỉ phép: ${sm.leaveDays}</span>
            <span class="badge-st badge-s2">Vắng mặt: ${sm.absentDays}</span>
            <span class="badge-st badge-s6">Cuối tuần: ${sm.weekendDays}</span>
            <span class="badge-st badge-s7">Quên chấm công: ${sm.missingCheckDays}</span>
        </div>
    </div>

    <div class="section-card">
        <form method="get" action="${baseUrl}/detail" class="row g-3 align-items-end mb-0">
            <input type="hidden" name="employeeId" value="${sm.employeeId}" />
            <input type="hidden" name="departmentId" value="${deptParam}" />
            <div class="col-md-5">
                <label class="form-label">Tháng</label>
                <select name="month" class="form-select">
                    <c:forEach var="m" begin="1" end="12">
                        <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-5">
                <label class="form-label">Năm</label>
                <select name="year" class="form-select">
                    <c:forEach var="y" begin="2022" end="2030">
                        <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2">
                <button type="submit" class="btn btn-primary w-100"><i class="fa-solid fa-magnifying-glass me-1"></i> Xem</button>
            </div>
        </form>
    </div>

    <%-- Lịch chấm công --%>
    <div class="section-card">
        <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
            <h5 class="mb-0"><i class="fa-regular fa-calendar me-2"></i>Lịch chấm công tháng ${selectedMonth}/${selectedYear}</h5>
            <div class="cal-legend">
                <span class="cl0">Đúng giờ</span>
                <span class="cl1">Đi muộn</span>
                <span class="cl4">Nghỉ phép</span>
                <span class="cl2">Vắng mặt</span>
                <span class="cl6">Cuối tuần</span>
                <span class="cl7">Quên chấm công</span>
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
<c:if test="${empty error}">
<script>
    var otDays = [];
    <c:forEach var="otDay" items="${approvedOTDays}">
        otDays.push(${otDay});
    </c:forEach>

    var attData = {};
    <c:forEach var="a" items="${detail.dailyRows}">
        <fmt:formatDate value="${a.workDate}" pattern="d" var="dnum" />
        attData[${dnum}] = {
            id: ${a.attendanceId},
            status: ${a.attendanceStatus},
            label: "${a.statusLabel}",
            timeIn: "${a.timeIn}".substring(0,5),
            timeOut: "${a.timeOut}".substring(0,5),
            mins: ${a.workedMinutes},
            edited: ${a.edited},
            isOT: otDays.includes(parseInt("${dnum}", 10))
        };
    </c:forEach>

    var calMonth = ${selectedMonth};
    var calYear  = ${selectedYear};

    var STANDARD_MINS = 480; // 8 tiếng chuẩn

    function fmtHours(m) {
        if (m < 0) m = 0;
        return Math.floor(m / 60) + 'h' + ('0' + (m % 60)).slice(-2) + 'm';
    }

    function displayHours(rec, isOtDay) {
        var m = rec.mins;
        if (!(rec.isOT || isOtDay)) m = Math.min(m, STANDARD_MINS);
        return m > 0 ? fmtHours(m) : '';
    }

    function renderCalendar() {
        var body = document.getElementById('calBody');
        body.innerHTML = '';
        var daysInMonth = new Date(calYear, calMonth, 0).getDate();
        var firstDow = (new Date(calYear, calMonth - 1, 1).getDay() + 6) % 7;

        var now = new Date();
        var isCurMonth = (now.getFullYear() === calYear && (now.getMonth() + 1) === calMonth);

        for (var i = 0; i < firstDow; i++) {
            var e = document.createElement('div');
            e.className = 'cal-cell empty';
            body.appendChild(e);
        }

        for (var day = 1; day <= daysInMonth; day++) {
            var cell = document.createElement('div');
            cell.className = 'cal-cell';
            var dow = (new Date(calYear, calMonth - 1, day).getDay() + 6) % 7;
            if (dow >= 5) cell.classList.add('weekend');
            if (isCurMonth && now.getDate() === day) cell.classList.add('today');

            var rec = attData[day];
            var isOtDay = otDays.includes(day);
            var html = '<div class="d">' + day + '</div>';
            if (rec) {
                var hasIn  = rec.timeIn  && rec.timeIn.length  === 5 && rec.timeIn  !== '00:00';
                var hasOut = rec.timeOut && rec.timeOut.length === 5 && rec.timeOut !== '00:00';

                if (hasIn || hasOut) {
                    var hoursStr = displayHours(rec, isOtDay);
                    html += '<div class="tm">' + (hasIn ? rec.timeIn : 'NA')
                          + ' - ' + (hasOut ? rec.timeOut : 'NA')
                          + (hoursStr ? '<br>' + hoursStr : '') + '</div>';
                }
                html += '<div class="st cl' + rec.status + '">' + rec.label + '</div>';
                if (rec.isOT || isOtDay) {
                    html += '<div class="mt-1"><span class="badge bg-warning text-dark px-2 py-1">OT</span></div>';
                }
                if (rec.edited) {
                    html += '<div class="mt-1"><span class="badge bg-info text-dark px-2 py-1" title="Chấm công đã được chỉnh sửa"><i class="fa-solid fa-pen-to-square me-1"></i>Đã sửa</span></div>';
                }
            } else {
                cell.classList.add('off-day');
                if (isOtDay) {
                    html += '<div class="mt-1"><span class="badge bg-warning text-dark px-2 py-1">OT</span></div>';
                }
            }
            cell.innerHTML = html;
            body.appendChild(cell);
        }
    }

    document.addEventListener('DOMContentLoaded', renderCalendar);
</script>
</c:if>
</body>
</html>
