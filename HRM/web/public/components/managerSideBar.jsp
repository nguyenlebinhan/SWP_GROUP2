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
            <div class="brand">HRM Manager</div>

            <div class="nav-section">Tổng quan</div>
            <a href="${pageContext.request.contextPath}/v1/manager/dashboard"
                class="${pageContext.request.servletPath == '/public/manager/dashboard.jsp' ? 'active' : ''}">
                Dashboard
            </a>

            <div class="nav-section">Nhân viên</div>
            <a href="${pageContext.request.contextPath}/v1/manager/department/my-department-list"
                class="${pageContext.request.servletPath == '/public/manager/employee_list.jsp' ? 'active' : ''}">
                Nhân viên phòng ban của tôi
            </a>

            <c:if test="${sessionScope.userPermissions.contains('VIEW_EMPLOYEES')}">
                <a href="${pageContext.request.contextPath}/v1/manager/employee/list"
                    class="${pageContext.request.servletPath == '/public/manager/employee_list.jsp' ? 'active' : ''}">
                    Danh sách nhân viên
                </a>
            </c:if>

            <c:if test="${sessionScope.userPermissions.contains('VIEW_DEPARTMENTS')}">
                <div class="nav-section">Phòng ban</div>
                <a href="${pageContext.request.contextPath}/v1/manager/department/list"
                    class="${pageContext.request.servletPath == '/public/manager/department_list.jsp' ? 'active' : ''}">
                    Danh sách phòng ban
                </a>
            </c:if>

            <c:if test="${sessionScope.userPermissions.contains('APPROVE_LEAVE')}">
                <a href="${pageContext.request.contextPath}/v1/manager/leave-requests">
                    Đơn nghỉ phép
                </a>
            </c:if>

            <a href="${pageContext.request.contextPath}/v1/manager/forms/dept-forms">
                Đơn của phòng ban
            </a>

            <c:if test="${sessionScope.userPermissions.contains('VIEW_ALL_FORMS')}">
                <a href="${pageContext.request.contextPath}/v1/manager/forms/all">
                    Tất cả đơn
                </a>
            </c:if>

            <c:if test="${sessionScope.userPermissions.contains('VIEW_LEAVE_BALANCE')}">
                <a href="${pageContext.request.contextPath}/v1/manager/leave-balances">
                    Ngày phép
                </a>
            </c:if>

            <c:if test="${sessionScope.userPermissions.contains('PROCESS_RECRUITMENT')}">
                <div class="nav-section">Tuyển dụng</div>
                <a href="${pageContext.request.contextPath}/v1/manager/recruitment-list">
                    Danh sách ứng viên
                </a>
            </c:if>

            <div class="nav-section">Quản lý Overtime</div>
            <a href="${pageContext.request.contextPath}/v1/manager/forms/ot-requests"
                class="${pageContext.request.servletPath == '/public/manager/forms/ot_requests.jsp' || pageContext.request.servletPath == '/public/manager/forms/ot_create.jsp' ? 'active' : ''}">
                Overtime
            </a>

            <div class="nav-section">Chấm công</div>
            <c:if
                test="${sessionScope.userPermissions.contains('VIEW_DEPARTMENT_ATTENDANCE') || sessionScope.userPermissions.contains('VIEW_ALL_ATTENDANCE')}">
                <a href="${pageContext.request.contextPath}/v1/manager/attendance/overview"
                    class="${pageContext.request.servletPath == '/public/shared/attendance/attendance_overview.jsp' || pageContext.request.servletPath == '/public/shared/attendance/attendance_detail.jsp' ? 'active' : ''}">
                    Tổng quan chấm công
                </a>
            </c:if>
            <a href="${pageContext.request.contextPath}/v1/manager/attendance/own-attendance"
                class="${pageContext.request.servletPath == '/public/manager/attendance/own_attendance.jsp' ? 'active' : ''}">
                Chấm công của tôi
            </a>
            <a href="${pageContext.request.contextPath}/v1/manager/attendance/my-department-attendance"
                class="${pageContext.request.servletPath == '/public/manager/department_attendance.jsp' ? 'active' : ''}">
                Chấm công phòng ban
            </a>

            <c:if test="${sessionScope.userPermissions.contains('IMPORT_ATTENDANCE')}">
                <a href="${pageContext.request.contextPath}/v1/employee/attendance/import">
                    Import chấm công
                </a>
            </c:if>
        </div>