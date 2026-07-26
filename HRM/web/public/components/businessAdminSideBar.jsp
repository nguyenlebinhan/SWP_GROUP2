<%@ page contentType="text/html;charset=UTF-8" %>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">

<style>
    .emp-sidebar {
        width: 250px;
        height: 100vh;
        background: #0B0E2A;
        position: fixed;
        top: 0;
        left: 0;
        color: white;
        overflow-y: auto;
    }

    .emp-sidebar .brand {
        padding: 20px 20px 10px;
        font-size: 18px;
        font-weight: 700;
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        margin-bottom: 8px;
    }

    .emp-sidebar .nav-section {
        padding: 8px 12px 2px;
        font-size: 11px;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: rgba(255, 255, 255, 0.4);
        font-weight: 600;
    }

    .emp-sidebar a {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 11px 20px;
        color: rgba(255, 255, 255, 0.85);
        text-decoration: none;
        font-size: 14px;
        border-radius: 6px;
        margin: 2px 8px;
        transition: background 0.15s;
    }

    .emp-sidebar a:hover,
    .emp-sidebar a.active {
        background: #1565C0;
        color: white;
    }

    .emp-sidebar a i {
        width: 18px;
        text-align: center;
        font-size: 14px;
    }
</style>

<div class="emp-sidebar">
    <div class="brand">HRM Business Admin</div>

    <div class="nav-section">Tổng quan</div>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/dashboard"
       class="${pageContext.request.servletPath == '/public/businessadmin/dashboard.jsp' ? 'active' : ''}">
        Dashboard
    </a>


    <div class="nav-section">Nhân viên</div>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/employee-list">
        Danh sách nhân viên
    </a>

    <div class="nav-section">Phòng ban</div>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/department">
        Danh sách phòng ban
    </a>

    <div class="nav-section">Chấm công & Lương</div>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/attendance/closing">
        Chấm công
    </a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/salary/all">
        Xem bảng lương
    </a>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/payroll-config">
        Cấu hình lương
    </a>

    <div class="nav-section">Khác</div>
    <a href="${pageContext.request.contextPath}/v1/businessadmin/forms">
        Quản lý tăng ca
    </a>


</div>
