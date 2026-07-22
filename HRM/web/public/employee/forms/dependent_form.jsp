<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đăng ký người phụ thuộc - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 28px;
            margin-bottom: 24px;
        }
        .form-type-tabs .nav-link {
            color: #475569;
            font-weight: 600;
            border-radius: 10px;
            padding: 10px 20px;
        }
        .form-type-tabs .nav-link.active {
            background: #0d6efd;
            color: #fff;
        }
    </style>
</head>
<body>
<c:choose>
    <c:when test="${managerForm}">
        <jsp:include page="/public/components/managerSideBar.jsp" />
    </c:when>
    <c:otherwise>
        <jsp:include page="/public/components/employeeSideBar.jsp" />
    </c:otherwise>
</c:choose>

<div class="main">
    <c:choose>
        <c:when test="${managerForm}">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Đăng ký người phụ thuộc" />
            </jsp:include>
        </c:when>
        <c:otherwise>
            <jsp:include page="/public/components/employeeTopBar.jsp">
                <jsp:param name="title" value="Đăng ký người phụ thuộc" />
            </jsp:include>
        </c:otherwise>
    </c:choose>

    <c:if test="${not managerForm}">
        <jsp:include page="/public/employee/forms/form_tabs.jsp">
            <jsp:param name="active" value="dependent" />
        </jsp:include>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <div class="section-card">
        <form method="post" action="${formAction}">
            <div class="mb-3">
                <label class="form-label" for="fullName">Tên người phụ thuộc</label>
                <input class="form-control" id="fullName" name="fullName" required maxlength="150">
            </div>
            <div class="mb-3">
                <label class="form-label" for="relationship">Quan hệ phụ thuộc</label>
                <input class="form-control" id="relationship" name="relationship" required maxlength="100">
            </div>
            <div class="mb-3">
                <label class="form-label" for="dateOfBirth">Ngày sinh</label>
                <input class="form-control" id="dateOfBirth" name="dateOfBirth" type="date" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="taxCode">Mã số thuế</label>
                <input class="form-control" id="taxCode" name="taxCode" inputmode="numeric" pattern="[0-9]*" maxlength="50"
                       title="Mã số thuế chỉ được nhập số">
            </div>
            <div class="mb-3">
                <label class="form-label" for="note">Ghi chú</label>
                <textarea class="form-control" id="note" name="note" rows="4" maxlength="500"></textarea>
            </div>
            <div class="d-flex justify-content-end gap-2">
                <a href="${cancelUrl}" class="btn btn-outline-secondary">Hủy</a>
                <button type="submit" class="btn btn-primary">Gửi đơn</button>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
