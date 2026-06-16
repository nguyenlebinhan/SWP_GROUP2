<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html>

        <head>
            <title>Chấm công của tôi - HRM</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
            <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
            <style>
                body {
                    background: #f5f6fa;
                    font-family: 'Segoe UI', sans-serif;
                }

                .main {
                    margin-left: 250px;
                    padding: 25px;
                }

                .section-card {
                    background: white;
                    border-radius: 14px;
                    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
                    padding: 24px;
                    margin-bottom: 24px;
                }

                .badge-s0 {
                    background: #d1fae5;
                    color: #065f46;
                }

                .badge-s1 {
                    background: #fef3c7;
                    color: #92400e;
                }

                .badge-s2 {
                    background: #fee2e2;
                    color: #991b1b;
                }

                .badge-s3 {
                    background: #e5e7eb;
                    color: #374151;
                }

                .badge-st {
                    padding: 4px 10px;
                    border-radius: 20px;
                    font-size: 12px;
                    font-weight: 600;
                }
            </style>
        </head>

        <body>

            <jsp:include page="${empty sidebarPage ? '/public/components/employeeSideBar.jsp' : sidebarPage}" />

            <div class="main">
                <jsp:include page="${empty topbarPage ? '/public/components/employeeTopBar.jsp' : topbarPage}">
                    <jsp:param name="title" value="Chấm công của tôi" />
                </jsp:include>

                <div class="section-card">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <thead class="table-light">
                                <tr>
                                    <th>Ngày</th>
                                    <th>Giờ vào</th>
                                    <th>Giờ ra</th>
                                    <th>Số giờ</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty attendances}">
                                        <tr>
                                            <td colspan="5" class="text-center text-muted py-4">
                                                Chưa có dữ liệu chấm công.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="a" items="${attendances}">
                                            <tr>
                                                <td>${a.workDate}</td>
                                                <td>${a.timeIn}</td>
                                                <td>${a.timeOut}</td>
                                                <td>
                                                    ${a.hoursWorkedLabel}
                                                </td>
                                                <td>
                                                    <span
                                                        class="badge-st badge-s${a.attendanceStatus}">${a.statusLabel}</span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>