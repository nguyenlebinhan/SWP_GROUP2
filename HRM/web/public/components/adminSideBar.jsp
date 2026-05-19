<%-- 
    Document   : adminSideBar
    Created on : May 19, 2026, 2:17:24 PM
    Author     : ADMIN
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<%-- 
    Document   : admin/dashboard.jsp
    Created on : May 18, 2026, 8:37:12 PM
    Author     : DucDucNguyen
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <title>HRM Admin Dashboard</title>

        <!-- Bootstrap -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

        <!-- Icons -->
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">

        <!-- ChartJS -->
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

        <style>

            body{
                background:#f5f6fa;
                font-family:Segoe UI;
            }

            .topbar{
                height:70px;
                background:white;
                display:flex;
                justify-content:space-between;
                align-items:center;
                padding:0 30px;
                border-radius:10px;
                box-shadow:0 2px 8px rgba(0,0,0,0.05);
                margin-bottom:20px;
            }

            .admin-box{
                display:flex;
                align-items:center;
                gap:15px;
            }

            .bell{
                font-size:20px;
                position:relative;
                cursor:pointer;
            }

            .bell::after{
                content:'';
                position:absolute;
                top:2px;
                right:-2px;
                width:8px;
                height:8px;
                background:red;
                border-radius:50%;
            }

            .admin-avatar{
                width:40px;
                height:40px;
                background:#2b6cb0;
                border-radius:50%;
            }

            /* ===== SIDEBAR ===== */

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

            /* ===== MAIN ===== */

            .main{
                margin-left:250px;
                padding:25px;
            }

            .card-stat{
                border:none;
                border-radius:12px;
                box-shadow:0 2px 10px rgba(0,0,0,0.05);
            }

            .chart-box{
                background:white;
                padding:20px;
                border-radius:12px;
            }

            .employee-item{
                display:flex;
                justify-content:space-between;
                margin-bottom:15px;
            }

            .badge-online{
                background:#d1fae5;
                color:#059669;
            }

            .badge-leave{
                background:#fee2e2;
                color:#dc2626;
            }

        </style>
    </head>

    <body>

        <!-- ================= SIDEBAR ================= -->
        <div class="sidebar">

            <h4>HRM System</h4>

            <a><i class="fa fa-chart-line"></i> Tổng quan</a>
            <a><i class="fa fa-briefcase"></i> Công việc</a>
            <a href="${pageContext.request.contextPath}/v1/admin/users" ><i class="fa fa-users"></i> Người dùng</a>
            <a><i class="fa fa-clock"></i> Chấm công</a>
            <a><i class="fa fa-user-plus"></i> Tuyển dụng</a>
            <a><i class="fa fa-money-bill"></i> Bảng lương</a>
            <a><i class="fa fa-building"></i> Phòng ban</a>
            <a><i class="fa fa-key"></i> Phân quyền</a>
            <a><i class="fa fa-chart-bar"></i> Báo cáo</a>

        </div>

        <!-- ===== TOPBAR ===== -->
        <div class="topbar">

            <h4 class="mb-0">Dashboard</h4>

            <div class="admin-box">

                <!-- Notification -->
                <div class="bell">
                    <i class="fa-solid fa-bell"></i>
                </div>

                <!-- Admin -->
                <div class="d-flex align-items-center gap-2">
                    <div class="admin-avatar"></div>
                    <span>Admin</span>
                </div>

            </div>

        </div>
    </body>    

</html>
