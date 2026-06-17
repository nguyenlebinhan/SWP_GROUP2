<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đơn Khiếu Nại - HRM</title>
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
        .section-card h5 { font-weight: 700; color: #0f172a; }
    </style>
</head>
<body>

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
        <jsp:param name="title" value="Đơn Khiếu Nại" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="section-card">
        <h5 class="mb-4">Tạo Đơn Khiếu Nại</h5>
        <form method="post" action="${pageContext.request.contextPath}/v1/employee/forms/complaint/submit"
              enctype="multipart/form-data">
            <div class="row g-3">

                <div class="col-md-12">
                    <label class="form-label fw-semibold">Nội dung khiếu nại <span class="text-danger">*</span></label>
                    <textarea id="reason" name="reason" class="form-control" rows="6" required
                              placeholder="Trình bày chi tiết nội dung khiếu nại của bạn..."><c:out value="${param.reason}"/></textarea>
                </div>

                <div class="col-md-12">
                    <label class="form-label fw-semibold">File đính kèm <span class="text-muted">(không bắt buộc)</span></label>
                    <input type="file" name="attachment" class="form-control"
                           accept=".xlsx,.pdf,.docx,.doc,.xls,.jpg,.png,.zip">
                    <small class="text-muted">Cho phép: xlsx, pdf, docx, doc, xls, jpg, png, zip</small>
                </div>

            </div>
            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-primary" id="submitBtn">Gửi đơn</button>
                <a href="${pageContext.request.contextPath}/v1/employee/form-dashboard"
                   class="btn btn-outline-secondary">Quay lại</a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
