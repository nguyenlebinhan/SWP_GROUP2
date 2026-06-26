<%@ page contentType="text/html;charset=UTF-8" %>

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

            <a href="${pageContext.request.contextPath}/v1/systemadmin/dashboard"><i></i> Tổng quan</a>
<!--            <a><i class="fa fa-briefcase"></i> Công việc</a>-->
            <a href="${pageContext.request.contextPath}/v1/systemadmin/user-list" ><i></i> Người dùng</a>
<!--            <a><i class="fa fa-clock"></i> Chấm công</a>
            <a><i class="fa fa-user-plus"></i> Tuyển dụng</a>
            <a><i class="fa fa-money-bill"></i> Bảng lương</a>
            <a><i class="fa fa-building"></i> Phòng ban</a>-->
            <a href="${pageContext.request.contextPath}/v1/systemadmin/role-list"><i></i> Phân quyền</a>
            <a href="${pageContext.request.contextPath}/v1/systemadmin/audit-logs"><i></i> Nhật ký hệ thống</a>
<!--            <a><i class="fa fa-chart-bar"></i> Báo cáo</a>-->

</div>