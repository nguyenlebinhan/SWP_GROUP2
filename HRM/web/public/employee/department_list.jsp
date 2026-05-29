<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Danh sách phòng ban - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }

        .page-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
        }

        .dept-card {
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 12px;
            padding: 20px;
            transition: box-shadow 0.15s;
        }
        .dept-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.10); }

        .dept-icon {
            width: 46px; height: 46px;
            border-radius: 10px;
            display: flex; align-items: center; justify-content: center;
            font-size: 20px;
            margin-bottom: 14px;
        }

        .dept-name { font-size: 15px; font-weight: 700; color: #0f172a; margin-bottom: 4px; }
        .dept-code { font-size: 12px; color: #94a3b8; margin-bottom: 8px; }
        .dept-desc { font-size: 13px; color: #64748b; margin-bottom: 12px; min-height: 38px; }

        .dept-stat {
            display: flex; align-items: center; gap: 6px;
            font-size: 13px; color: #374151; font-weight: 600;
        }
        .dept-stat .count { font-size: 22px; font-weight: 700; color: #2563eb; }

        .badge-active   { background: #d1fae5; color: #065f46; padding: 3px 9px; border-radius: 20px; font-size: 11px; font-weight: 600; }
        .badge-inactive { background: #fee2e2; color: #991b1b; padding: 3px 9px; border-radius: 20px; font-size: 11px; font-weight: 600; }

        .dept-colors { background: #dbeafe; color: #2563eb; }
        .dept-colors:nth-child(2n)  > .dept-card .dept-icon { background: #d1fae5; color: #059669; }
        .dept-colors:nth-child(3n)  > .dept-card .dept-icon { background: #ede9fe; color: #7c3aed; }
        .dept-colors:nth-child(4n)  > .dept-card .dept-icon { background: #ffedd5; color: #ea580c; }
    </style>
</head>
<body>

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/adminTopBar.jsp">
        <jsp:param name="title" value="Danh sách phòng ban" />
        <jsp:param name="backUrl" value="/v1/employee/dashboard" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>

    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h5 class="fw-bold mb-1">Phòng ban</h5>
            <span class="text-muted small">${departments.size()} phòng ban</span>
        </div>
        <c:if test="${canManageDepts}">
            <a href="${pageContext.request.contextPath}/v1/employee/add-department"
               class="btn btn-sm" style="background:#059669;color:white;border-radius:8px;padding:8px 16px;font-weight:600;font-size:13px">
                <i class="fa-solid fa-plus me-1"></i> Thêm phòng ban
            </a>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${empty departments}">
            <div class="page-card text-center py-5">
                <i class="fa-solid fa-sitemap" style="font-size:48px;color:#cbd5e1;margin-bottom:16px"></i>
                <h6 class="text-muted">Chưa có phòng ban nào</h6>
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <c:forEach var="dept" items="${departments}" varStatus="loop">
                    <div class="col-md-4 col-lg-3 dept-colors">
                        <div class="dept-card h-100">
                            <div class="dept-icon" style="background:#dbeafe;color:#2563eb">
                                <i class="fa-solid fa-sitemap"></i>
                            </div>
                            <div class="dept-name">${dept.departmentName}</div>
                            <div class="dept-code">${dept.departmentCode}</div>
                            <div class="dept-desc">
                                <c:choose>
                                    <c:when test="${not empty dept.description}">${dept.description}</c:when>
                                    <c:otherwise><span class="text-muted">Chưa có mô tả</span></c:otherwise>
                                </c:choose>
                            </div>

                            <div class="d-flex align-items-center justify-content-between mt-auto">
                                <div class="dept-stat">
                                    <span class="count">${empCounts[dept.departmentId]}</span>
                                    <span>nhân viên</span>
                                </div>
                                <c:choose>
                                    <c:when test="${dept.status == 1}">
                                        <span class="badge-active">Hoạt động</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-inactive">Tạm dừng</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <c:if test="${not empty dept.region}">
                                <div class="mt-2 small text-muted">
                                    <i class="fa-solid fa-location-dot me-1"></i>${dept.region}
                                </div>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
