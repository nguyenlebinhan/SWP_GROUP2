<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Tạo đơn yêu cầu - HRM</title>
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

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
        <jsp:param name="title" value="Tạo đơn yêu cầu" />
    </jsp:include>

    <jsp:include page="/public/employee/forms/form_tabs.jsp">
        <jsp:param name="active" value="" />
    </jsp:include>

    <div class="section-card text-center py-5">
        <h5 class="mb-3 text-muted">Vui lòng chọn một loại đơn từ các tab ở trên</h5>
        <p class="text-muted mb-0">Hệ thống hỗ trợ gửi đơn nghỉ phép, khiếu nại chấm công và thuyên chuyển phòng ban.</p>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
