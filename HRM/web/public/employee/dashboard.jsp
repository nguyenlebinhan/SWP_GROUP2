<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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

        .feature-icon.blue   { background: #dbeafe; color: #2563eb; }
        .feature-icon.green  { background: #d1fae5; color: #059669; }
        .feature-icon.orange { background: #ffedd5; color: #ea580c; }
        .feature-icon.purple { background: #ede9fe; color: #7c3aed; }

        .feature-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 6px; }
        .feature-desc  { font-size: 13px; color: #64748b; }

        .recent-table th { font-size: 13px; color: #6b7280; font-weight: 600; }
        .recent-table td { font-size: 14px; vertical-align: middle; }

        .badge-active   { background: #d1fae5; color: #065f46; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-inactive { background: #fee2e2; color: #991b1b; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-leave    { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }

        .badge-status { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .status-0 { background: #fef3c7; color: #92400e; }
        .status-1 { background: #d1fae5; color: #065f46; }
        .status-2 { background: #fee2e2; color: #991b1b; }
        .status-3 { background: #e2e8f0; color: #475569; }

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

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
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

    <div class="welcome-banner">
        <div class="welcome-text">
            <h4>Xin chào, ${empty sessionScope.user.fullName ? sessionScope.user.username : sessionScope.user.fullName}!</h4>
            <c:choose>
                <c:when test="${not empty myEmployee}">
                    <div class="welcome-meta">
                        <span><i class="fa-solid fa-id-badge me-1"></i> Mã nhân viên:
                            <strong>${myEmployee.employeeCode}</strong></span>
                        <span><i class="fa-solid fa-sitemap me-1"></i> Phòng ban:
                            <strong>${myEmployee.departmentName}</strong></span>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="welcome-meta">
                        <span class="text-white-50">Bạn chưa được phân công vào phòng ban nào.</span>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <c:if test="${not empty myEmployee}">
        <div class="row g-3 mt-1">
            <div class="col-md-3 col-sm-6">
                <div class="stat-card">
                    <div class="stat-icon blue"><i class="fa-solid fa-calendar-check"></i></div>
                    <div>
                        <div class="stat-value">${attendanceSummary.presentDays + attendanceSummary.lateDays} / ${attendanceSummary.standardDays}</div>
                        <div class="stat-label">Ngày công tháng trước</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3 col-sm-6">
                <div class="stat-card">
                    <div class="stat-icon orange"><i class="fa-solid fa-file-circle-question"></i></div>
                    <div>
                        <div class="stat-value">${pendingFormsCount}</div>
                        <div class="stat-label">Đơn đang chờ duyệt</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3 col-sm-6">
                <div class="stat-card">
                    <div class="stat-icon purple"><i class="fa-solid fa-people-roof"></i></div>
                    <div>
                        <div class="stat-value">${myEmployee.dependentCount}</div>
                        <div class="stat-label">Người phụ thuộc</div>
                    </div>
                </div>
            </div>
            <div class="col-md-3 col-sm-6">
                <div class="stat-card">
                    <div class="stat-icon green"><i class="fa-solid fa-file-signature"></i></div>
                    <div>
                        <div class="stat-value" style="font-size: 18px;">
                            ${empty activeContract ? 'Chưa có' : activeContract.status.displayName}
                        </div>
                        <div class="stat-label">Trạng thái hợp đồng</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-3 mt-1">
            <div class="col-md-3 col-sm-6">
                <a class="feature-card" href="${pageContext.request.contextPath}/v1/employee/attendance/own-attendance">
                    <div class="feature-icon blue"><i class="fa-solid fa-clock"></i></div>
                    <div class="feature-title">Chấm công của tôi</div>
                    <div class="feature-desc">Xem lịch sử chấm công theo tháng</div>
                </a>
            </div>
            <div class="col-md-3 col-sm-6">
                <a class="feature-card" href="${pageContext.request.contextPath}/v1/employee/forms/my-forms">
                    <div class="feature-icon orange"><i class="fa-solid fa-file-lines"></i></div>
                    <div class="feature-title">Đơn của tôi</div>
                    <div class="feature-desc">Theo dõi trạng thái các đơn đã gửi</div>
                </a>
            </div>
            <c:if test="${sessionScope.userPermissions.contains('VIEW_ALL_SALARY')}">
                <div class="col-md-3 col-sm-6">
                    <a class="feature-card" href="${pageContext.request.contextPath}/v1/employee/salary/all">
                        <div class="feature-icon green"><i class="fa-solid fa-sack-dollar"></i></div>
                        <div class="feature-title">Bảng lương</div>
                        <div class="feature-desc">Xem chi tiết lương hàng tháng</div>
                    </a>
                </div>
            </c:if>
            <div class="col-md-3 col-sm-6">
                <a class="feature-card" href="${pageContext.request.contextPath}/v1/employee/forms/dashboard">
                    <div class="feature-icon purple"><i class="fa-solid fa-user-plus"></i></div>
                    <div class="feature-title">Gửi đơn mới</div>
                    <div class="feature-desc">Đăng ký người phụ thuộc, nghỉ phép, v.v.</div>
                </a>
            </div>
        </div>

        <div class="section-card mt-3">
            <h6 class="fw-bold mb-3">Đơn gần đây</h6>
            <c:choose>
                <c:when test="${empty recentForms}">
                    <div class="text-muted small">Bạn chưa gửi đơn nào.</div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-sm recent-table align-middle mb-0">
                            <thead>
                                <tr>
                                    <th>Mã đơn</th>
                                    <th>Loại đơn</th>
                                    <th>Trạng thái</th>
                                    <th>Ngày gửi</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="f" items="${recentForms}">
                                    <tr>
                                        <td>${f.formCode}</td>
                                        <td><c:out value="${f.formTypeName}"/></td>
                                        <td><span class="badge-status status-${f.status}">${f.statusLabel}</span></td>
                                        <td><fmt:formatDate value="${f.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
