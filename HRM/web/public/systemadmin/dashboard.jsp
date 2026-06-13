

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <title>HRM Admin Dashboard</title>


        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">


        <style>

            body{
                background:#f5f6fa;
                font-family:Segoe UI;
            }



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


            <jsp:include page="/public/components/systemAdminSideBar.jsp" />


        <div class="main">
            <jsp:include page="/public/components/systemAdminTopBar.jsp">
                <jsp:param name="title" value="Dashboard" />
            </jsp:include>



            <div class="row g-4">

                <div class="col-md-3">
                    <div class="card card-stat p-4">
                        <h2>${userSize}</h2>
                        <p>Tổng người dùng</p>
                    </div>
                </div>



            </div>

            <div class="row mt-4">


                <div class="col-md-8">
                    <div class="chart-box">
                        <h5>Biểu đồ nhân viên theo phòng ban</h5>
                        <canvas id="employeeChart"></canvas>
                    </div>
                </div>


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






    </body>
</html>
