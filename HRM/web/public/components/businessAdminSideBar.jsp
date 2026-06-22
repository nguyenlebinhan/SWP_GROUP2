<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">

<style>
    .sidebar{
        width:250px;
        height:100vh;
        background:#0B0E2A;
        position:fixed;
        color:white;
    }

    .sidebar h4{
        padding:20px;
    }

    .sidebar a{
        display:block;
        padding:14px 20px;
        color:white;
        text-decoration:none;
    }

    .sidebar a:hover{
        background:#1565C0;
        border-radius: 5px;
    }

</style>

<div class="sidebar">

            <h4>HRM System</h4>
          <a href="${pageContext.request.contextPath}/v1/businessadmin/dashboard"> Tổng quan</a>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/employee-list"> Nhân viên</a>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/assign-department"> Phân công nhân viên</a>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/department"> Phòng ban</a>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/add-department"> Thêm phòng ban</a>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/attendance/overview"> Chấm công</a>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/holiday"> Ngày lễ</a>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/forms"> Quản lý Đơn từ</a>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/my-profile"> Hồ sơ của tôi</a>
            <a href="${pageContext.request.contextPath}/v1/auth/logout"> Đăng xuất</a>
            <hr style="border-color: #2a2d4a; margin: 10px 0;">
            <small class="text-muted px-3">HỢP ĐỒNG</small>

            <c:if test="${sessionScope.permissions != null && sessionScope.permissions.contains('PERM_APPROVE_CONTRACT')}">
                <a href="${pageContext.request.contextPath}/contracts/approve"><i class="fa fa-check-circle"></i> Duyệt hợp đồng</a>
            </c:if>

            <a href="${pageContext.request.contextPath}/contracts/history"><i class="fa fa-clock-rotate-left"></i> Lịch sử hợp đồng</a>

            <a href="${pageContext.request.contextPath}/v1/auth/logout"><i class="fa fa-right-from-bracket"></i> Đăng xuất</a>
          
</div>

