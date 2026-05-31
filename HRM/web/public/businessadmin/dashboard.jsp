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


        <style>

            body{
                background:#f5f6fa;
                font-family:Segoe UI;
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
        <jsp:include page="/public/components/businessAdminSideBar.jsp" />
        
        <!-- ================= MAIN ================= -->
        <div class="main">
            <jsp:include page="/public/components/businessAdminTopBar.jsp">
                <jsp:param name="title" value="Dashboard" />
            </jsp:include>
        <div class="main">


            <!-- ===== STATS ===== -->
            <div class="row g-4">

                <div class="col-md-3">
                    <div class="card card-stat p-4">
                        <h2>${userSize}</h2>
                        <p>Tổng người dùng</p>
                    </div>
                </div>


                <div class="col-md-3">
                    <div class="card card-stat p-4">
                        <h2>NA</h2>
                        <p>Phòng ban hoạt động</p>
                    </div>
                </div>


        </div>




    </body>
</html>
