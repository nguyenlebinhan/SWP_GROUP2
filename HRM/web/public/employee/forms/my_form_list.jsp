<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đơn yêu cầu của tôi - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
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
        .badge-status {
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }
        .status-0 { background: #fef3c7; color: #92400e; }
        .status-1 { background: #d1fae5; color: #065f46; }
        .status-2 { background: #fee2e2; color: #991b1b; }
        .status-3 { background: #e5e7eb; color: #374151; }
        .reason-cell { max-width: 260px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    </style>
</head>
<body>

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
        <jsp:param name="title" value="Đơn yêu cầu của tôi" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>

    <%-- Filter theo ngày / tháng / năm --%>
    <div class="section-card">
        <form method="get" action="${pageContext.request.contextPath}/v1/employee/forms/my-forms"
              class="d-flex align-items-center gap-2 flex-wrap">
            <div class="input-group" style="max-width: 520px;">
                <select name="day" class="form-select" id="select-day">
                    <option value="">Tất cả Ngày</option>
                    <c:forEach var="d" begin="1" end="31">
                        <option value="${d}" ${filterDay == d ? 'selected' : ''}>${d}</option>
                    </c:forEach>
                </select>
                <select name="month" class="form-select" id="select-month">
                    <option value="">Tất cả Tháng</option>
                    <c:forEach var="m" begin="1" end="12">
                        <option value="${m}" ${filterMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                    </c:forEach>
                </select>
                <input type="number" name="year" class="form-control" id="input-year"
                       min="2000" max="2100" value="${filterYear}" placeholder="Năm">
                <button type="submit" class="btn btn-primary" id="btn-filter-forms">Lọc</button>
            </div>
        </form>
    </div>

    <div class="section-card">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="mb-0">Danh sách đơn yêu cầu</h5>
            <a href="${pageContext.request.contextPath}/v1/employee/forms/leave/new"
               class="btn btn-primary btn-sm" id="btn-new-form">Gửi đơn mới</a>
        </div>

        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Mã đơn</th>
                        <th>Loại đơn</th>
                        <th>Trạng thái</th>
                        <th>Ngày gửi</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty forms}">
                            <tr><td colspan="7" class="text-center text-muted py-4">Bạn chưa có đơn nào.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="f" items="${forms}">
                                <tr>
                                    <td>
                                        ${f.formCode}
                                    </td>
                                    <td><c:out value="${f.formTypeName}"/></td>
                                    <td>
                                        <span class="badge-status status-${f.status}">${f.statusLabel}</span>
                                    </td>
                                    <td>${f.createdAt}</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/v1/employee/forms/detail?id=${f.formId}"
                                           class="btn btn-sm btn-outline-primary">Xem chi tiết</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
