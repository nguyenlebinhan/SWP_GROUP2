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

            /* ===== TOPBAR ===== */

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
            <jsp:include page="/public/components/adminSideBar.jsp" />

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

        <!-- ================= MAIN ================= -->
        <div class="main">

            <h3>Dashboard</h3>
            <p>Xin chào, Admin. Hôm nay: ${todayDate}</p>

            <!-- ===== STATS ===== -->
            <div class="row g-4">

                <div class="col-md-3">
                    <div class="card card-stat p-4">
                        <h2>128</h2>
                        <p>Tổng nhân viên</p>
                    </div>
                </div>

                <div class="col-md-3">
                    <div class="card card-stat p-4">
                        <h2>115</h2>
                        <p>Đang làm việc</p>
                    </div>
                </div>

                <div class="col-md-3">
                    <div class="card card-stat p-4">
                        <h2>8</h2>
                        <p>Nghỉ phép hôm nay</p>
                    </div>
                </div>

                <div class="col-md-3">
                    <div class="card card-stat p-4">
                        <h2>12</h2>
                        <p>Phòng ban</p>
                    </div>
                </div>

            </div>
            <!-- ===== CHART + EMPLOYEE ===== -->
            <div class="row mt-4">

                <!-- Chart -->
                <div class="col-md-8">
                    <div class="chart-box">
                        <h5>Biểu đồ nhân viên theo phòng ban</h5>
                        <canvas id="employeeChart"></canvas>
                    </div>
                </div>

                <!-- Latest Employees -->
                <div class="col-md-4">
                    <div class="chart-box">
                        <h5>Nhân viên mới nhất</h5>

                        <div class="employee-item">
                            <div>Nguyễn Văn A<br><small>IT</small></div>
                            <span class="badge badge-online">Đang làm</span>
                        </div>

                        <div class="employee-item">
                            <div>Trần Thị B<br><small>HR</small></div>
                            <span class="badge badge-leave">Nghỉ phép</span>
                        </div>

                        <div class="employee-item">
                            <div>Lê Văn C<br><small>Sales</small></div>
                            <span class="badge badge-online">Đang làm</span>
                        </div>

                        <div class="employee-item">
                            <div>Phạm Thị D<br><small>Finance</small></div>
                            <span class="badge badge-online">Đang làm</span>
                        </div>

                    </div>
                </div>

            </div>

        </div>



        <!-- ================= CHART SCRIPT ================= -->


    </body>
</html>
