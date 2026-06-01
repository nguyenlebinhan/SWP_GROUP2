<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    <style>
        .sidebar {
            width: 250px;
            height: 100vh;
            background: #0B0E2A;
            position: fixed;
            top: 0;
            left: 0;
            color: white;
            overflow-y: auto;
            z-index: 100;
        }

        .sidebar-brand {
            padding: 20px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .sidebar-brand i {
            font-size: 22px;
            color: #ff8c00;
        }

        .sidebar-brand h4 {
            margin: 0;
            font-size: 16px;
            font-weight: 700;
            color: white;
        }

        .sidebar-nav {
            padding: 12px 0;
        }

        .sidebar-section-label {
            padding: 10px 20px 6px;
            font-size: 10px;
            font-weight: 700;
            letter-spacing: .8px;
            text-transform: uppercase;
            color: rgba(255, 255, 255, 0.4);
        }

        .sidebar a {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 11px 20px;
            color: rgba(255, 255, 255, 0.75);
            text-decoration: none;
            font-size: 14px;
            font-weight: 500;
            transition: all .2s;
        }

        .sidebar a i {
            width: 18px;
            text-align: center;
            font-size: 15px;
        }

        .sidebar a:hover,
        .sidebar a.active {
            background: rgba(255, 255, 255, 0.1);
            color: white;
            border-right: 3px solid #ff8c00;
        }

        .sidebar-divider {
            height: 1px;
            background: rgba(255, 255, 255, 0.08);
            margin: 10px 20px;
        }

        .sidebar a.logout-link:hover {
            background: rgba(185, 28, 28, 0.3);
            color: #fca5a5;
            border-right-color: #b91c1c;
        }
    </style>

    <div class="sidebar">
        <div class="sidebar-brand">
            <i class="fa fa-users-cog"></i>
            <h4>HRM Manager</h4>
        </div>

        <nav class="sidebar-nav">
            <div class="sidebar-section-label">Chính</div>
            <a href="${pageContext.request.contextPath}/v1/manager/dashboard"
                class="${pageContext.request.servletPath == '/public/manager/dashboard.jsp' ? 'active' : ''}">
                <i class="fa fa-chart-pie"></i> Tổng quan
            </a>

            <div class="sidebar-divider"></div>
            <div class="sidebar-section-label">Quản lý</div>
            <c:if test="${sessionScope.userPermissions.contains('VIEW_EMPLOYEES')}">
                <a href="${pageContext.request.contextPath}/v1/manager/employee-list">
                    <i class="fa fa-users"></i> Nhân viên
                </a>
            </c:if>
            <c:if test="${sessionScope.userPermissions.contains('VIEW_DEPARTMENTS')}">
                <a href="${pageContext.request.contextPath}/v1/manager/department"
                    class="${pageContext.request.servletPath == '/public/manager/department_list.jsp' || pageContext.request.servletPath == '/public/manager/department_assign.jsp' ? 'active' : ''}">
                    <i class="fa fa-building"></i> Phòng ban
                </a>
            </c:if>
            <c:if test="${sessionScope.userPermissions.contains('APPROVE_LEAVE')}">
                <a href="${pageContext.request.contextPath}/v1/manager/leave-requests">
                    <i class="fa fa-calendar-check"></i> Đơn nghỉ phép
                </a>
            </c:if>
            <c:if test="${sessionScope.userPermissions.contains('VIEW_LEAVE_BALANCE')}">
                <a href="${pageContext.request.contextPath}/v1/manager/leave-balances">
                    <i class="fa fa-chart-bar"></i> Ngày phép
                </a>
            </c:if>

            <div class="sidebar-divider"></div>
            <div class="sidebar-section-label">Hệ thống</div>
            <a href="${pageContext.request.contextPath}/v1/manager/my-profile">
                <i class="fa fa-user-gear"></i> Hồ sơ của tôi
            </a>
            <a href="${pageContext.request.contextPath}/v1/auth/logout" class="logout-link">
                <i class="fa fa-right-from-bracket"></i> Đăng xuất
            </a>
        </nav>
    </div>