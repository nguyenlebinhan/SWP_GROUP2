<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Dashboard - Business Admin</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background:#f4f6f9;
                font-family:'Segoe UI',sans-serif;
                color:#111827;
            }
            .main {
                margin-left:250px;
                padding:25px;
            }

            /* ── stat cards ── */
            .stat-grid {
                display:grid;
                grid-template-columns:repeat(4,1fr);
                gap:16px;
                margin-bottom:24px;
            }
            .stat-card {
                background:#fff;
                border:1px solid #eef2f7;
                border-radius:12px;
                padding:20px 22px;
                box-shadow:0 2px 10px rgba(15,23,42,.05);
                display:flex;
                align-items:center;
                gap:16px;
            }
            .stat-icon {
                width:48px;
                height:48px;
                border-radius:12px;
                display:flex;
                align-items:center;
                justify-content:center;
                font-size:20px;
                flex-shrink:0;
            }
            .stat-icon.blue   {
                background:#eff6ff;
                color:#2563eb;
            }
            .stat-icon.green  {
                background:#f0fdf4;
                color:#16a34a;
            }
            .stat-icon.purple {
                background:#faf5ff;
                color:#7c3aed;
            }
            .stat-icon.amber  {
                background:#fffbeb;
                color:#d97706;
            }
            .stat-label {
                color:#6b7280;
                font-size:13px;
                font-weight:600;
                margin-bottom:4px;
            }
            .stat-value {
                font-size:26px;
                font-weight:800;
                color:#111827;
                line-height:1;
            }
            .stat-sub   {
                color:#9ca3af;
                font-size:12px;
                margin-top:4px;
            }

            /* ── chart card ── */
            .chart-card {
                background:#fff;
                border:1px solid #eef2f7;
                border-radius:12px;
                box-shadow:0 2px 10px rgba(15,23,42,.05);
                padding:24px;
                margin-bottom:24px;
            }
            .chart-card-header {
                display:flex;
                justify-content:space-between;
                align-items:center;
                margin-bottom:20px;
                flex-wrap:wrap;
                gap:12px;
            }
            .chart-title {
                font-size:17px;
                font-weight:800;
                color:#111827;
                margin:0;
            }
            .chart-subtitle {
                font-size:13px;
                color:#6b7280;
                margin-top:2px;
            }
            .year-form {
                display:flex;
                align-items:center;
                gap:8px;
            }
            .year-form select {
                border:1px solid #e5e7eb;
                border-radius:7px;
                padding:6px 10px;
                font-size:13px;
                color:#374151;
                background:#f9fafb;
                cursor:pointer;
            }
            .year-form select:focus {
                outline:none;
                border-color:#2563eb;
            }

            /* ── pending approvals list ── */
            .chart-card-compact {
                padding:16px 14px;
            }
            .pending-list {
                display:flex;
                flex-direction:column;
                gap:8px;
            }
            .pending-row {
                display:flex;
                justify-content:space-between;
                align-items:center;
                padding:9px 10px;
                border:1px solid #eef2f7;
                border-radius:9px;
                text-decoration:none;
                color:#111827;
                font-size:12px;
                font-weight:600;
                white-space:nowrap;
                transition:background .15s;
            }
            .pending-row:hover {
                background:#f9fafb;
            }
            .pending-row-total {
                background:#eff6ff;
                border-color:#bfdbfe;
                font-size:13px;
                font-weight:700;
            }
            .pending-count {
                font-size:15px;
                font-weight:800;
                color:#111827;
            }
            .pending-row-total .pending-count {
                color:#2563eb;
            }
            .text-amber { color:#d97706; }
            .text-blue  { color:#2563eb; }
            .text-purple{ color:#7c3aed; }

            /* ── tooltip custom (Chart.js) ── */
            .chartjs-tooltip {
                background:#1e293b !important;
                border-radius:8px !important;
                padding:8px 12px !important;
                font-family:'Segoe UI',sans-serif !important;
            }

            @media (max-width:1100px) {
                .stat-grid {
                    grid-template-columns:repeat(2,1fr);
                }
            }
            @media (max-width:768px)  {
                .main {
                    margin-left:0;
                    padding:16px;
                }
                .stat-grid {
                    grid-template-columns:1fr;
                }
            }
        </style>
    </head>
    <body>

        <jsp:include page="/public/components/businessAdminSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/businessAdminTopBar.jsp">
                <jsp:param name="title" value="Dashboard" />
            </jsp:include>

            <%-- ══════════════ STAT CARDS ══════════════ --%>
            <div class="stat-grid">

                <div class="stat-card">
                    <div class="stat-icon blue">
                        <i class="fa-solid fa-file-invoice-dollar"></i>
                    </div>
                    <div>
                        <div class="stat-label">Bảng lương chờ duyệt</div>
                        <div class="stat-value">${pendingPayrollCount}</div>
                        <div class="stat-sub">Kỳ ${latestPayMonth}</div>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="stat-icon green">
                        <i class="fa-solid fa-building"></i>
                    </div>
                    <div>
                        <div class="stat-label">Phòng ban hoạt động</div>
                        <div class="stat-value">${deptSize}</div>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="stat-icon purple">
                        <i class="fa-solid fa-id-badge"></i>
                    </div>
                    <div>
                        <div class="stat-label">Nhân viên</div>
                        <div class="stat-value">${employeeSize}</div>
                    </div>
                </div>

                <div class="stat-card">
                    <div class="stat-icon amber">
                        <i class="fa-solid fa-money-bill-trend-up"></i>
                    </div>
                    <div>
                        <div class="stat-label">Tổng chi lương ${salaryYear}</div>
                        <%-- Hiển thị triệu nếu < 1000M, ngược lại hiển thị tỷ --%>
                        <c:choose>
                            <c:when test="${salaryYearTotal >= 1000}">
                                <div class="stat-value">
                                    <fmt:formatNumber value="${salaryYearTotal / 1000.0}"
                                                      maxFractionDigits="1" />
                                    <span style="font-size:16px;font-weight:600;color:#6b7280"> tỷ VND</span>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stat-value">
                                    ${salaryYearTotal}
                                    <span style="font-size:16px;font-weight:600;color:#6b7280"> triệu VND</span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <div class="stat-sub">
                            Kỳ gần nhất (${latestPayMonth}):
                            <strong>${latestPaidCount}</strong> nhân sự
                        </div>
                    </div>
                </div>

            </div>

            <%-- ══════════════ BIỂU ĐỒ ══════════════ --%>
            <div class="row g-3">
                <div class="col-lg-5">
                    <div class="chart-card h-100">
                        <div class="chart-card-header">
                            <div>
                                <p class="chart-title">Tổng chi trả lương theo tháng</p>
                            </div>
                            <%-- Chọn năm --%>
                            <form method="get"
                                  action="${pageContext.request.contextPath}/v1/businessadmin/dashboard"
                                  class="year-form">
                                <input type="hidden" name="deptYear" value="${deptYear}">
                                <input type="hidden" name="deptMonth" value="${deptMonth}">
                                <label for="salaryYear"
                                       style="font-size:13px;color:#6b7280;font-weight:600;">Năm:</label>
                                <select name="salaryYear" id="salaryYear" onchange="this.form.submit()">
                                    <c:forEach var="y" items="${availableYears}">
                                        <option value="${y}" ${salaryYear == y ? 'selected' : ''}>${y}</option>
                                    </c:forEach>
                                </select>
                            </form>
                        </div>

                        <canvas id="salaryChart" height="170"></canvas>
                    </div>
                </div>
                <div class="col-lg-5">
                    <div class="chart-card h-100">
                        <div class="chart-card-header">
                            <div>
                                <p class="chart-title">Chi phí lương theo phòng ban</p>
                            </div>
                            <%-- Chọn kỳ --%>
                            <form method="get"
                                  action="${pageContext.request.contextPath}/v1/businessadmin/dashboard"
                                  class="year-form">
                                <input type="hidden" name="salaryYear" value="${salaryYear}">
                                <select name="deptMonth" onchange="this.form.submit()">
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}" ${deptMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                                    </c:forEach>
                                </select>
                                <select name="deptYear" onchange="this.form.submit()">
                                    <c:forEach var="y" items="${availableYears}">
                                        <option value="${y}" ${deptYear == y ? 'selected' : ''}>${y}</option>
                                    </c:forEach>
                                </select>
                            </form>
                        </div>

                        <canvas id="deptChart" height="170"></canvas>
                    </div>
                </div>
                <div class="col-lg-2">
                    <div class="chart-card chart-card-compact h-100">
                        <p class="chart-title mb-3">Đơn chờ duyệt</p>
                        <div class="pending-list">
                            <a href="${pageContext.request.contextPath}/v1/businessadmin/forms" class="pending-row pending-row-total">
                                <span>Tổng</span>
                                <span class="pending-count">${pendingTotalCount}</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/v1/businessadmin/forms?status=0" class="pending-row" title="Đơn OT">
                                <span><i class="fa-solid fa-business-time text-amber"></i> OT</span>
                                <span class="pending-count">${pendingOtCount}</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/v1/businessadmin/payroll-config/history" class="pending-row" title="Cấu hình lương">
                                <span><i class="fa-solid fa-sliders text-blue"></i> Lương</span>
                                <span class="pending-count">${pendingPayrollConfigCount}</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/v1/businessadmin/forms?status=0" class="pending-row" title="Đơn khác (chuyển đơn vị/thăng-giáng chức)">
                                <span><i class="fa-solid fa-file-lines text-purple"></i> Khác</span>
                                <span class="pending-count">${pendingOtherFormsCount}</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>

        </div><%-- /main --%>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js"></script>
        <script>
                            (function () {
                                const labels = ${salaryLabelsJson};
                                const raw = ${salaryDataJson};   // triệu VND, null = tháng tương lai

                                /* ── tách data thực / dự báo ── */
                                const dataSolid = raw.map(v => v);          // toàn bộ (đường liền)
                                const lastReal = raw.reduce((a, v, i) => v !== null ? i : a, -1);

                                /* Gradient fill */
                                const ctx = document.getElementById('salaryChart').getContext('2d');
                                const gradient = ctx.createLinearGradient(0, 0, 0, 320);
                                gradient.addColorStop(0, 'rgba(37,99,235,0.18)');
                                gradient.addColorStop(1, 'rgba(37,99,235,0.01)');

                                new Chart(ctx, {
                                    type: 'line',
                                    data: {
                                        labels,
                                        datasets: [{
                                                label: 'Tổng chi trả (triệu VND)',
                                                data: dataSolid,
                                                borderColor: '#2563eb',
                                                backgroundColor: gradient,
                                                borderWidth: 2.5,
                                                pointBackgroundColor: raw.map((v, i) =>
                                                    v === null ? 'transparent' : (i === lastReal ? '#2563eb' : '#fff')),
                                                pointBorderColor: raw.map(v => v === null ? 'transparent' : '#2563eb'),
                                                pointBorderWidth: 2,
                                                pointRadius: raw.map((v, i) => v === null ? 0 : (i === lastReal ? 6 : 4)),
                                                pointHoverRadius: 7,
                                                tension: 0.38,
                                                fill: true,
                                                spanGaps: false          /* ngắt đường ở tháng tương lai (null) */
                                            }]
                                    },
                                    options: {
                                        responsive: true,
                                        interaction: {mode: 'index', intersect: false},
                                        plugins: {
                                            legend: {display: false},
                                            tooltip: {
                                                backgroundColor: '#1e293b',
                                                titleColor: '#94a3b8',
                                                bodyColor: '#f1f5f9',
                                                padding: 12,
                                                cornerRadius: 8,
                                                callbacks: {
                                                    title: items => 'Tháng ' + items[0].label + ' / ${salaryYear}',
                                                    label: item =>
                                                        item.raw === null
                                                                ? '  Chưa có dữ liệu'
                                                                : '  ' + new Intl.NumberFormat('vi-VN').format(item.raw) + ' triệu VND'
                                                }
                                            }
                                        },
                                        scales: {
                                            x: {
                                                grid: {display: false},
                                                border: {display: false},
                                                ticks: {color: '#9ca3af', font: {size: 12, family: "'Segoe UI'"}}
                                            },
                                            y: {
                                                beginAtZero: true,
                                                border: {display: false, dash: [4, 4]},
                                                grid: {color: '#f1f5f9'},
                                                ticks: {
                                                    color: '#9ca3af',
                                                    font: {size: 12, family: "'Segoe UI'"},
                                                    callback: v => new Intl.NumberFormat('vi-VN').format(v) + 'M'
                                                }
                                            }
                                        }
                                    }
                                });
                            })();

                            (function () {
                                const deptLabels = ${deptLabelsJson};
                                const deptData = ${deptDataJson};

                                const ctx2 = document.getElementById('deptChart').getContext('2d');
                                new Chart(ctx2, {
                                    type: 'bar',
                                    data: {
                                        labels: deptLabels,
                                        datasets: [{
                                                label: 'Chi phí (triệu VND)',
                                                data: deptData,
                                                backgroundColor: '#7c3aed',
                                                borderRadius: 6,
                                                maxBarThickness: 40
                                            }]
                                    },
                                    options: {
                                        responsive: true,
                                        plugins: {
                                            legend: {display: false},
                                            tooltip: {
                                                backgroundColor: '#1e293b',
                                                titleColor: '#94a3b8',
                                                bodyColor: '#f1f5f9',
                                                padding: 12,
                                                cornerRadius: 8,
                                                callbacks: {
                                                    label: item => '  ' + new Intl.NumberFormat('vi-VN').format(item.raw) + ' triệu VND'
                                                }
                                            }
                                        },
                                        scales: {
                                            x: {
                                                grid: {display: false},
                                                border: {display: false},
                                                ticks: {color: '#9ca3af', font: {size: 12, family: "'Segoe UI'"}}
                                            },
                                            y: {
                                                beginAtZero: true,
                                                border: {display: false, dash: [4, 4]},
                                                grid: {color: '#f1f5f9'},
                                                ticks: {
                                                    color: '#9ca3af',
                                                    font: {size: 12, family: "'Segoe UI'"},
                                                    callback: v => new Intl.NumberFormat('vi-VN').format(v) + 'M'
                                                }
                                            }
                                        }
                                    }
                                });
                            })();
        </script>
    </body>
</html>