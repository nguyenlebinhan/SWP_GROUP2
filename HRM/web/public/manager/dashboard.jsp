<%--
    Document   : manager/dashboard.jsp
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard - HRM Manager</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: #f5f6fa;
            font-family: 'Segoe UI', sans-serif;
        }
        .main {
            margin-left: 250px;
            padding: 25px;
        }
        .card-stat {
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .chart-box {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .badge-online {
            background: #d1fae5;
            color: #059669;
        }
        .badge-leave {
            background: #fee2e2;
            color: #dc2626;
        }
        .badge-inactive {
            background: #f3f4f6;
            color: #6b7280;
        }
    </style>
</head>
<body>

    <%-- Chọn sidebar theo role (bỏ qua khoảng trắng và không phân biệt chữ hoa/thường) --%>
    <c:choose>
        <c:when test="${fn:toUpperCase(fn:replace(sessionScope.user.roleName, ' ', '')) == 'HRMANAGER'}">
            <jsp:include page="/public/components/managerSideBar.jsp" />
        </c:when>
        <c:otherwise>
            <jsp:include page="/public/components/departmentManagerSideBar.jsp" />
        </c:otherwise>
    </c:choose>

    <div class="main">
        <jsp:include page="/public/components/managerTopBar.jsp">
            <jsp:param name="title" value="Dashboard" />
        </jsp:include>

        <!-- ===== STATS ===== -->
        <div class="row g-4">
            <div class="col-md-3">
                <div class="card card-stat p-4">
                    <h2>${totalEmployees}</h2>
                    <p>Tổng nhân viên</p>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card card-stat p-4">
                    <h2>${activeEmployees}</h2>
                    <p>Đang hoạt động</p>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card card-stat p-4">
                    <h2>${inactiveEmployees}</h2>
                    <p>Không hoạt động</p>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card card-stat p-4">
                    <h2>${pendingLeaves}</h2>
                    <p>Chờ duyệt nghỉ phép</p>
                </div>
            </div>
        </div>

        <!-- ===== CHART + EMPLOYEES ===== -->
        <div class="row mt-4">
            <div class="col-md-8">
                <div class="chart-box">
                    <h5>Biểu đồ nhân viên theo phòng ban</h5>
                    <canvas id="employeeChart"></canvas>
                </div>
            </div>

        </div>
    </div>

</body>
</html>
