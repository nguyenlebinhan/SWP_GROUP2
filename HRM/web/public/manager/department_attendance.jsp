<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Chấm công phòng ban - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
            margin-bottom: 24px;
        }
        .badge-s0 { background:#d1fae5; color:#065f46; }
        .badge-s1 { background:#fef3c7; color:#92400e; }
        .badge-s2 { background:#fee2e2; color:#991b1b; }
        .badge-s3 { background:#e5e7eb; color:#374151; }
        .badge-private { background:#fef3c7; color:#92400e; }
        .badge-public  { background:#d1fae5; color:#065f46; }
        .badge-st { padding:4px 10px; border-radius:20px; font-size:12px; font-weight:600; }
    </style>
</head>
<body>

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Chấm công phòng ban" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show">
            <i class="fa-solid fa-circle-xmark me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${empty error}">
    <div class="section-card">
        <h5 class="mb-4">Phòng ban: <strong class="text-primary">${departmentName}</strong></h5>
        <form method="get" action="${pageContext.request.contextPath}/v1/manager/department-attendance" class="row g-3">
            <div class="col-md-2">
                <label class="form-label">Tháng</label>
                <select name="month" class="form-select">
                    <option value="">Tất cả</option>
                    <c:forEach var="m" begin="1" end="12">
                        <option value="${m}" ${filterMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label">Năm</label>
                <input type="number" name="year" class="form-control" min="2000" max="2100" value="${filterYear}">
            </div>
            <div class="col-md-3">
                <label class="form-label">Mã nhân viên</label>
                <input type="text" name="employeeCode" class="form-control"
                       value="<c:out value='${filterEmployeeCode}'/>" placeholder="VD: EMP001">
            </div>
            <div class="col-md-2 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100">
                    <i class="fa-solid fa-magnifying-glass me-1"></i> Lọc
                </button>
            </div>
        </form>
    </div>
    <div class="section-card">
        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Mã NV</th>
                        <th>Họ tên</th>
                        <th>Ngày</th>
                        <th>Giờ vào</th>
                        <th>Giờ ra</th>
                        <th>Số giờ</th>
                        <th>Trạng thái</th>
                        <th>Kỳ</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty attendances}">
                            <tr><td colspan="8" class="text-center text-muted py-4">Không có dữ liệu chấm công.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="a" items="${attendances}">
                                <tr>
                                    <td>${a.employeeCode}</td>
                                    <td><c:out value="${a.fullName}" /></td>
                                    <td>${a.workDate}</td>
                                    <td>${a.timeIn}</td>
                                    <td>${a.timeOut}</td>
                                    <td><c:out value="${a.hoursWorked}" /></td>
                                    <td>
                                        <span class="badge-st badge-s${a.attendanceStatus}">${a.statusLabel}</span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${a.periodStatus == 1}">
                                                <span class="badge-st badge-public">Công khai</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-st badge-private">Riêng tư</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
