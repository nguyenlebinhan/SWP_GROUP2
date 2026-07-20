<%@ page contentType="text/html;charset=UTF-8" %>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">

<style>
    .sidebar {
        width: 250px;
        height: 100vh;
        background: #0B0E2A;
        position: fixed;
        color: white;
    }

    .sidebar h4 {
        padding: 20px;
    }

    .sidebar a {
        display: block;
        padding: 14px 20px;
        color: white;
        text-decoration: none;
    }

    .sidebar a:hover {
        background: #1565C0;
        border-radius: 5px;
    }
</style>

<div class="sidebar">
    <h4>HRM System</h4>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/dashboard">Tổng quan</a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/employee-list">Nhân viên</a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/assign-department">Phân công nhân viên</a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/department">Phòng ban</a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/add-department">Thêm phòng ban</a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/attendance/closing">Chấm công</a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/salary/all"> Xem bảng lương</a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/payroll-config">Cấu hình payroll</a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/forms">Quản lý đơn từ</a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/my-profile">Hồ sơ của tôi</a>
    <a href="${pageContext.request.contextPath}/v1/auth/logout">Đăng xuất</a>
</div>
