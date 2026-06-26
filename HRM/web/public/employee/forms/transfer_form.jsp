<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đơn Thuyên Chuyển - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
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

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
        <jsp:param name="title" value="Tạo đơn yêu cầu" />
    </jsp:include>

    <jsp:include page="/public/employee/forms/form_tabs.jsp">
        <jsp:param name="active" value="transfer" />
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
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h5 class="mb-0">Tạo Đơn Thuyên Chuyển</h5>
        </div>

        <form action="${pageContext.request.contextPath}/v1/employee/forms/transfer/submit" method="POST">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label fw-bold">Phòng ban mong muốn <span class="text-danger">*</span></label>
                    <select name="targetDepartmentId" class="form-select" required>
                        <option value="">-- Chọn phòng ban --</option>
                        <c:forEach var="dept" items="${departments}">
                            <option value="${dept.departmentId}">${dept.departmentName}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-12 mt-4">
                    <label class="form-label fw-bold">Lý do thuyên chuyển <span class="text-danger">*</span></label>
                    <textarea name="reason" class="form-control" rows="4" 
                              placeholder="Trình bày lý do bạn muốn thuyên chuyển sang phòng ban này..." required></textarea>
                </div>

                <div class="col-12 mt-4 text-end">
                    <a href="${pageContext.request.contextPath}/v1/employee/forms/my-forms" 
                       class="btn btn-light me-2">Hủy</a>
                    <button type="submit" class="btn btn-primary" id="btn-submit-transfer">
                        <i class="fas fa-paper-plane me-1"></i> Gửi Đơn
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
