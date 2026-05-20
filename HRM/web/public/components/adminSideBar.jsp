<%@ page contentType="text/html;charset=UTF-8" %>

<!-- Bootstrap -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

<!-- Icons -->
<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">

<!-- ChartJS -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

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
        color:#ff8c00;
    }

    .sidebar a{
        display:block;
        padding:14px 20px;
        color:#cbd5e1;
        text-decoration:none;
    }

    .sidebar a:hover{
        background:#1565C0;
        color:white;
        border-radius: 5px;
    }

    .active-menu{
        background:#2b6cb0;
    }

</style>

<div class="sidebar">

            <h4>HRM System</h4>

            <a href="${pageContext.request.contextPath}/v1/admin/dashboard"><i class="fa fa-chart-line"></i> Tổng quan</a>
<!--            <a><i class="fa fa-briefcase"></i> Công việc</a>-->
            <a href="${pageContext.request.contextPath}/v1/admin/user-list" ><i class="fa fa-users"></i> Người dùng</a>
<!--            <a><i class="fa fa-clock"></i> Chấm công</a>
            <a><i class="fa fa-user-plus"></i> Tuyển dụng</a>
            <a><i class="fa fa-money-bill"></i> Bảng lương</a>
            <a><i class="fa fa-building"></i> Phòng ban</a>-->
            <a><i class="fa fa-key"></i> Phân quyền</a>
<!--            <a><i class="fa fa-chart-bar"></i> Báo cáo</a>-->

        </div>