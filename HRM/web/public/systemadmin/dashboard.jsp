<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>

<html>
<head>
    <title>System Admin Dashboard - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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

        .recent-table th { font-size: 13px; color: #6b7280; font-weight: 600; }
        .recent-table td { font-size: 14px; vertical-align: middle; }

        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/systemAdminSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/systemAdminTopBar.jsp">
        <jsp:param name="title" value="Dashboard" />
    </jsp:include>

    <div class="mb-4 d-flex align-items-center text-muted" style="font-size: 15px;">
        <strong style="color: #475569;">Xin chào, ${empty sessionScope.user.fullName ? sessionScope.user.username : sessionScope.user.fullName}! 👋</strong>
        <span class="mx-2" style="color: #cbd5e1;">•</span> <span style="font-weight: 500; color: #64748b;">Vai trò: Quản trị hệ thống</span>
        <span class="mx-2" style="color: #cbd5e1;">•</span> <span style="font-weight: 500; color: #64748b;">Hôm nay: ${todayDate}</span>
    </div>

    <!-- Stat Cards -->
    <div class="row g-4 mb-4">
        <div class="col-md-4">
            <div class="stat-card">
                <div class="flex-grow-1">
                    <div class="stat-value">${userSize}</div>
                    <div class="stat-label">Tổng tài khoản (Users)</div>
                </div>
                <div class="stat-icon blue" style="background: #2563eb; color: white;"><i class="fa-solid fa-users"></i></div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="stat-card">
                <div class="flex-grow-1">
                    <div class="stat-value">${totalEmployees}</div>
                    <div class="stat-label">Tổng hồ sơ nhân viên</div>
                </div>
                <div class="stat-icon orange" style="background: #ea580c; color: white;"><i class="fa-solid fa-id-badge"></i></div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="stat-card">
                <div class="flex-grow-1">
                    <div class="stat-value">${deptSize}</div>
                    <div class="stat-label">Phòng ban hoạt động</div>
                </div>
                <div class="stat-icon green" style="background: #059669; color: white;"><i class="fa-solid fa-building"></i></div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <!-- User List -->
        <div class="col-lg-12">
            <div class="section-card h-100">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h5 class="mb-0 fw-bold" style="color: #1e293b;">Tài khoản mới cập nhật</h5>
                    <a href="${pageContext.request.contextPath}/v1/systemadmin/user-list" class="btn btn-sm px-3 rounded-pill" style="background:#e0f2fe; color:#0369a1; font-weight:600;">Quản lý users &rarr;</a>
                </div>
                <div class="table-responsive">
                    <table class="table recent-table align-middle">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 40px;">STT</th>
                                <th>Tên đăng nhập</th>
                                <th>Họ tên</th>
                                <th>Email</th>
                                <th>Vai trò</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty recentUsers}">
                                    <c:forEach var="u" items="${recentUsers}" varStatus="status">
                                        <tr>
                                            <td class="text-muted"><fmt:formatNumber value="${status.index + 1}" pattern="00"/></td>
                                            <td class="fw-semibold text-dark">${u.username}</td>
                                            <td class="fw-medium">${u.fullName}</td>
                                            <td class="text-muted">${u.email}</td>
                                            <td>
                                                <span class="badge" style="background: #e0e7ff; color: #4338ca;">${not empty u.roleName ? u.roleName : 'Chưa phân quyền'}</span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="5" class="text-center text-muted py-4">Không có tài khoản nào.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>
