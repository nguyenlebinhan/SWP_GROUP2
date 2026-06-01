<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
        border-bottom: 1px solid rgba(255,255,255,0.1);
        margin-bottom: 8px;
    }

    .emp-sidebar .nav-section {
        padding: 8px 12px 2px;
        font-size: 11px;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: rgba(255,255,255,0.4);
        font-weight: 600;
    }

    .emp-sidebar a {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 11px 20px;
        color: rgba(255,255,255,0.85);
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
        <a href="${pageContext.request.contextPath}/v1/employee/employee-list">
             Danh sách nhân viên
        </a>
    </c:if>

    <c:if test="${sessionScope.userPermissions.contains('ASSIGN_DEPARTMENT')}">
        <a href="${pageContext.request.contextPath}/v1/employee/assign-department">
             Phân công phòng ban
        </a>
    </c:if>

    <c:if test="${sessionScope.userPermissions.contains('ADD_EMPLOYMENT_CONTRACT')}">
        <a href="${pageContext.request.contextPath}/v1/employee/add-contract">
             Thêm hợp đồng lao động
        </a>
    </c:if>

    <c:if test="${sessionScope.userPermissions.contains('VIEW_DEPARTMENTS') || sessionScope.userPermissions.contains('MANAGE_DEPARTMENTS')}">
        <div class="nav-section">Phòng ban</div>
    </c:if>
    <a href="${pageContext.request.contextPath}/v1/employee/department-list">
        Danh sách phòng ban
    </a>
    <c:if test="${sessionScope.userPermissions.contains('EDIT_DEPARTMENTS')}">
        <a href="${pageContext.request.contextPath}/v1/employee/add-department">
            Thêm phòng ban
        </a>
    </c:if>
</div>
