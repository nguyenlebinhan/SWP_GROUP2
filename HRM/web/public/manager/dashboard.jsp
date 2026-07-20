<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <title>Employee Dashboard - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }

        .main { margin-left: 250px; padding: 25px; }

        .stat-card {
            border: none;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
            display: flex;
            align-items: center;
            gap: 18px;
            background: white;
        }

        .stat-icon {
            width: 54px;
            height: 54px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
            flex-shrink: 0;
        }

        .stat-icon.blue   { background: #dbeafe; color: #2563eb; }
        .stat-icon.green  { background: #d1fae5; color: #059669; }
        .stat-icon.orange { background: #ffedd5; color: #ea580c; }
        .stat-icon.purple { background: #ede9fe; color: #7c3aed; }

        .stat-value { font-size: 28px; font-weight: 700; color: #0f172a; line-height: 1; }
        .stat-label { font-size: 13px; color: #64748b; margin-top: 4px; }

        .feature-card {
            background: white;
            border: none;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 28px 24px;
            text-decoration: none;
            color: inherit;
            display: block;
            transition: transform 0.15s, box-shadow 0.15s;
        }

        .feature-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 6px 20px rgba(0,0,0,0.12);
            color: inherit;
        }

        .feature-icon {
            width: 52px;
            height: 52px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
            margin-bottom: 16px;
        }

        .feature-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 6px; }
        .feature-desc  { font-size: 13px; color: #64748b; }

        .recent-table th { font-size: 13px; color: #6b7280; font-weight: 600; }
        .recent-table td { font-size: 14px; vertical-align: middle; }

        .badge-active   { background: #d1fae5; color: #065f46; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-inactive { background: #fee2e2; color: #991b1b; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-leave    { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }

        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
        }

        .welcome-banner {
            background: linear-gradient(135deg, #1565C0, #0B0E2A);
            border-radius: 16px;
            padding: 28px;
            color: white;
            display: flex;
            align-items: center;
            gap: 20px;
            box-shadow: 0 6px 20px rgba(21,101,192,0.25);
        }

        .welcome-text h4 { font-weight: 700; margin: 0 0 8px; }

        .welcome-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 24px;
            font-size: 14px;
            color: rgba(255,255,255,0.9);
        }

        .welcome-meta strong { color: white; }
    </style>
</head>
<body>

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Dashboard" />
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

    <div class="mb-4 d-flex align-items-center text-muted" style="font-size: 15px;">
        <strong style="color: #475569;">Xin chào, ${empty sessionScope.user.fullName ? sessionScope.user.username : sessionScope.user.fullName}! 👋</strong>
        <c:if test="${not empty myEmployee}">
            <span class="mx-2" style="color: #cbd5e1;">•</span> <span style="font-weight: 500; color: #64748b;">${myEmployee.departmentName}</span>
        </c:if>
        <span class="mx-2" style="color: #cbd5e1;">•</span> <span style="font-weight: 500; color: #64748b;">Hôm nay: ${todayDate}</span>
    </div>

    <!-- Stat Cards -->
    <div class="row g-4 mb-4">
        <div class="col-md-6">
            <div class="stat-card">
                <div class="flex-grow-1">
                    <div class="stat-value">${totalEmployees}</div>
                    <div class="stat-label">Tổng nhân viên</div>
                </div>
                <div class="stat-icon blue" style="background: #2563eb; color: white;"><i class="fa-solid fa-users"></i></div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="stat-card">
                <div class="flex-grow-1">
                    <div class="stat-value">${pendingForms}</div>
                    <div class="stat-label">Đơn chờ duyệt</div>
                </div>
                <div class="stat-icon purple" style="background: #7c3aed; color: white;"><i class="fa-solid fa-file-signature"></i></div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-lg-8">
            <!-- Employee List -->
            <div class="section-card h-100">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h5 class="mb-0 fw-bold" style="color: #1e293b;">Danh sách nhân viên — ${not empty myEmployee ? myEmployee.departmentName : 'Công ty'}</h5>
                    <a href="${pageContext.request.contextPath}/v1/manager/department/my-department-list" class="btn btn-sm px-3 rounded-pill" style="background:#e0f2fe; color:#0369a1; font-weight:600;">Xem tất cả &rarr;</a>
                </div>
                <div class="table-responsive">
            <table class="table recent-table align-middle">
                <thead class="table-light">
                    <tr>
                        <th style="width: 60px;">STT</th>
                        <th>Nhân viên</th>
                        <th>Chức vụ</th>
                        <th>Email</th>
                        <th>Ngày phép còn lại</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty departmentEmployees}">
                            <c:forEach var="emp" items="${departmentEmployees}" varStatus="status" end="9">
                                <tr>
                                    <td class="text-muted">
                                        <fmt:formatNumber value="${status.index + 1}" pattern="00"/>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <div class="rounded-circle d-flex align-items-center justify-content-center me-3" style="width: 32px; height: 32px; background: #2563eb; color: white; font-weight: 600; font-size: 14px;">
                                                ${fn:substring(emp.fullName, 0, 1)}
                                            </div>
                                            <span class="fw-semibold text-dark">${emp.fullName}</span>
                                        </div>
                                    </td>
                                    <td class="text-muted">${emp.positionName}</td>
                                    <td class="text-muted">${emp.email}</td>
                                    <td style="color: #0284c7; font-weight: 500;">${leaveBalances[emp.employeeId]} ngày</td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" class="text-center text-muted py-4">Không có nhân viên nào.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
            </div>
        </div>
        
        <div class="col-lg-4">
            <div class="section-card h-100">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h5 class="mb-0 fw-bold" style="color: #1e293b;"><i class="fa-solid fa-medal text-warning me-2"></i>Gương mẫu tháng ${prevMonthStr}</h5>
                </div>
                <div class="top-employees-list">
                    <c:choose>
                        <c:when test="${not empty topEmployees}">
                            <c:forEach var="te" items="${topEmployees}" varStatus="status">
                                <div class="d-flex align-items-center mb-3 p-3" style="background: #f8fafc; border-radius: 12px; border: 1px solid #e2e8f0;">
                                    <div class="rounded-circle d-flex align-items-center justify-content-center me-3 shadow-sm" style="width: 46px; height: 46px; background: ${status.index == 0 ? 'linear-gradient(135deg, #fcd34d, #f59e0b)' : (status.index == 1 ? 'linear-gradient(135deg, #e2e8f0, #94a3b8)' : 'linear-gradient(135deg, #fdbba7, #d97706)')}; color: white; font-weight: bold; font-size: 18px;">
                                        #${status.index + 1}
                                    </div>
                                    <div class="flex-grow-1">
                                        <div class="fw-bold" style="color: #0f172a; font-size: 15px;">${te.fullName}</div>
                                        <div class="text-muted" style="font-size: 13px;">${te.positionName}</div>
                                    </div>
                                    <div class="text-end">
                                        <div class="fw-bold text-success" style="font-size: 16px;">${te.workedHoursDisplay}h</div>
                                        <div class="text-muted" style="font-size: 11px; text-transform: uppercase; font-weight: 600;">Thời gian làm</div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center text-muted py-5">
                                <i class="fa-solid fa-folder-open mb-3" style="font-size: 32px; color: #cbd5e1;"></i>
                                <div>Chưa có dữ liệu chấm công tháng trước.</div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
