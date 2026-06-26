<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
        .st0,.cl0 { background:#d1fae5; color:#065f46; }
        .st1,.cl1 { background:#fef3c7; color:#92400e; }
        .st2,.cl2 { background:#fee2e2; color:#991b1b; }
        .cl3 { background:#e5e7eb; color:#374151; }
        .st4,.cl4 { background:#dbeafe; color:#1e40af; }
        .st5,.cl5 { background:#ede9fe; color:#5b21b6; }
        .st6,.cl6 { background:#f3f4f6; color:#4b5563; }
        .stx { background:#eef2ff; color:#3730a3; }

        /* Calendar */
        .cal-grid { display:grid; grid-template-columns:repeat(7,1fr); gap:8px; }
        .cal-dow { text-align:center; font-weight:600; color:#64748b; font-size:13px; padding:6px 0; }
        .cal-cell { min-height:96px; border-radius:10px; border:1px solid #eef0f4; padding:8px; background:#fff; display:flex; flex-direction:column; }
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

    <%-- Bộ lọc tháng/năm --%>
    <div class="section-card">
        <form method="get" action="${pageContext.request.contextPath}/v1/employee/attendance/own-attendance"
              class="row g-3 align-items-end">
            <div class="col-md-4">
                <label class="form-label">Tháng</label>
                <select name="month" class="form-select">
                    <c:forEach var="m" begin="1" end="12">
                        <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label">Năm</label>
                <select name="year" class="form-select">
                    <c:forEach var="y" begin="2022" end="2030">
                        <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-4">
                <button type="submit" class="btn btn-primary w-100">
                    <i class="fa-solid fa-magnifying-glass me-1"></i> Xem
                </button>
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
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    var otDays = [];
    <c:forEach var="otDay" items="${approvedOTDays}">
        otDays.push(${otDay});
    </c:forEach>

    var attData = {};
    <c:forEach var="a" items="${monthRows}">
        <fmt:formatDate value="${a.workDate}" pattern="d" var="dnum" />
        attData[${dnum}] = {
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

    // Không có đơn OT thì giới hạn hiển thị tối đa 8 tiếng, dù đi sớm/về muộn.
    // Nếu làm dưới 8 tiếng thì giữ nguyên giờ thực tế.
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
                if (rec.timeIn && rec.timeIn.length === 5 && rec.timeIn !== '00:00') {
                    var hoursStr = displayHours(rec, isOtDay);
                    html += '<div class="tm">' + rec.timeIn
                          + (rec.timeOut && rec.timeOut.length === 5 ? ' - ' + rec.timeOut : '')
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
</body>
</html>
