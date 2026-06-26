<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

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
    <div class="brand">
        HRM System
    </div>

    <div class="nav-section">Tổng quan</div>
    <a href="${pageContext.request.contextPath}/v1/employee/dashboard">
        Dashboard
    </a>

    <c:if test="${sessionScope.userPermissions.contains('VIEW_EMPLOYEES')}">
        <div class="nav-section">Nhân viên</div>
        <a href="${pageContext.request.contextPath}/v1/employee/employee_info/list">
            Danh sách nhân viên
        </a>
    </c:if>

    <div class="nav-section">Phòng ban</div>
    <a href="${pageContext.request.contextPath}/v1/employee/department/list">
        Danh sách phòng ban
    </a>

    <div class="nav-section">Đơn từ</div>
    <c:if test="${sessionScope.userPermissions.contains('VIEW_ALL_FORMS')}">
        <a href="${pageContext.request.contextPath}/v1/employee/forms/all">
            Tất cả đơn
        </a>
    </c:if>
    <a href="${pageContext.request.contextPath}/v1/employee/forms/my-forms">
        Đơn của tôi
    </a>

    <c:if test="${sessionScope.userPermissions.contains('PROCESS_RECRUITMENT')}">
        <div class="nav-section">Tuyển dụng</div>
        <a href="${pageContext.request.contextPath}/v1/employee/recruitment-list">
            Danh sách ứng viên
        </a>
    </c:if>

    <div class="nav-section">Chấm công</div>
    <c:if test="${sessionScope.userPermissions.contains('VIEW_ALL_ATTENDANCE')}">
        <a href="${pageContext.request.contextPath}/v1/employee/attendance/overview">
            Tổng quan chấm công
        </a>
    </c:if>
    <c:if test="${sessionScope.userPermissions.contains('VIEW_ATTENDANCE')}">
        <a href="${pageContext.request.contextPath}/v1/employee/attendance/list">
            Xem chấm công
        </a>
    </c:if>
    <a href="${pageContext.request.contextPath}/v1/employee/attendance/own-attendance">
        Chấm công của tôi
    </a>

    <c:if test="${sessionScope.userPermissions.contains('VIEW_ALL_SALARY')}">
        <div class="nav-section">Lương</div>
        <a href="${pageContext.request.contextPath}/v1/employee/salary/all"
           class="${pageContext.request.servletPath == '/public/employee/salary/salary_list.jsp' ? 'active' : ''}">
            Xem bảng lương
        </a>
    </c:if>
</div>