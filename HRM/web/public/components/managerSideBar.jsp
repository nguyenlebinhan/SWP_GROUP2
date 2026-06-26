<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

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
</style>

<div class="emp-sidebar">
    <div class="brand">HRM Manager</div>

    <div class="nav-section">Tổng quan</div>
    <a href="${pageContext.request.contextPath}/v1/manager/dashboard">Dashboard</a>

    <div class="nav-section">Nhân viên</div>
    <a href="${pageContext.request.contextPath}/v1/manager/department/my-department-list">Nhân viên phòng ban của tôi</a>
    <c:if test="${sessionScope.userPermissions.contains('VIEW_EMPLOYEES')}">
        <a href="${pageContext.request.contextPath}/v1/manager/employee/list">Danh sách nhân viên</a>
    </c:if>

    <c:if test="${sessionScope.userPermissions.contains('VIEW_DEPARTMENTS')}">
        <div class="nav-section">Phòng ban</div>
        <a href="${pageContext.request.contextPath}/v1/manager/department/list">Danh sách phòng ban</a>
    </c:if>

    <c:if test="${sessionScope.userPermissions.contains('APPROVE_LEAVE')}">
        <a href="${pageContext.request.contextPath}/v1/manager/leave-requests">
            Đơn nghỉ phép
        </a>
    </c:if>

    <a href="${pageContext.request.contextPath}/v1/manager/forms/dept-forms"
       class="${pageContext.request.servletPath == '/public/manager/forms/dept_form_list.jsp' ? 'active' : ''}">
        Đơn của phòng ban
    </a>

    <c:if test="${sessionScope.userPermissions.contains('VIEW_ALL_FORMS')}">
        <a href="${pageContext.request.contextPath}/v1/manager/forms/all"
           class="${pageContext.request.servletPath == '/public/manager/forms/all_form_list.jsp' ? 'active' : ''}">
            Tất cả đơn yêu cầu
        </a>
        <a href="${pageContext.request.contextPath}/v1/manager/forms/submit-promotion"
           class="${pageContext.request.servletPath == '/public/manager/forms/promotion_form.jsp' ? 'active' : ''}">
            Tạo đề xuất thăng/giáng chức
        </a>
    </c:if>

    <div class="nav-section">Hợp đồng</div>
    <c:if test="${fn:contains(sessionScope.user.roleName, 'HR') and sessionScope.userPermissions.contains('ADD_EMPLOYMENT_CONTRACT')}">
        <a href="${pageContext.request.contextPath}/v1/manager/contract/add">Tạo hợp đồng</a>
    </c:if>
    <c:if test="${fn:contains(sessionScope.user.roleName, 'HR') and sessionScope.userPermissions.contains('VIEW_PENDING_CONTRACTS')}">
        <a href="${pageContext.request.contextPath}/v1/manager/contract/pending">Hợp đồng chờ duyệt</a>
    </c:if>
    <c:if test="${sessionScope.userPermissions.contains('VIEW_OWN_CONTRACT')}">
        <a href="${pageContext.request.contextPath}/v1/manager/contract/history?scope=own">Lịch sử hợp đồng của bản thân</a>
    </c:if>
    <c:if test="${fn:contains(sessionScope.user.roleName, 'HR') and sessionScope.userPermissions.contains('VIEW_ALL_CONTRACTS')}">
        <a href="${pageContext.request.contextPath}/v1/manager/contract/history">Lịch sử hợp đồng nhân viên</a>
    </c:if>

    <div class="nav-section">Overtime</div>
    <a href="${pageContext.request.contextPath}/v1/manager/forms/ot-requests">Overtime</a>

    <div class="nav-section">Chấm công</div>
    <c:if test="${sessionScope.userPermissions.contains('VIEW_DEPARTMENT_ATTENDANCE') || sessionScope.userPermissions.contains('VIEW_ALL_ATTENDANCE')}">
        <a href="${pageContext.request.contextPath}/v1/manager/attendance/overview">Tổng quan chấm công</a>
    </c:if>
    <a href="${pageContext.request.contextPath}/v1/manager/attendance/own-attendance">Chấm công của tôi</a>
    <a href="${pageContext.request.contextPath}/v1/manager/attendance/my-department-attendance">Chấm công phòng ban</a>
    <c:if test="${sessionScope.userPermissions.contains('IMPORT_ATTENDANCE')}">
        <a href="${pageContext.request.contextPath}/v1/manager/attendance/import">Import chấm công</a>
    </c:if>

    <c:if test="${sessionScope.userPermissions.contains('VIEW_ALL_SALARY')}">
        <div class="nav-section">Lương</div>
        <a href="${pageContext.request.contextPath}/v1/manager/salary/all">Xem bảng lương</a>
    </c:if>

    <c:if test="${sessionScope.userPermissions.contains('PROCESS_RECRUITMENT')}">
        <div class="nav-section">Tuyển dụng</div>
        <a href="${pageContext.request.contextPath}/v1/manager/recruitment-list">Danh sách ứng viên</a>
    </c:if>
</div>
