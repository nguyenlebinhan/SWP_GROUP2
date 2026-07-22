<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
    <title>Tạo đơn tăng ca - HRM</title>
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
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
            margin-bottom: 24px;
        }
        .employee-list-box {
            max-height: 300px;
            overflow-y: auto;
            border: 1px solid #dee2e6;
            border-radius: 6px;
            padding: 10px;
        }
    </style>
</head>
<body>

    <jsp:include page="/public/components/managerSideBar.jsp" />

    <div class="main">
        <jsp:include page="/public/components/managerTopBar.jsp">
            <jsp:param name="title" value="Tạo đơn tăng ca" />
            <jsp:param name="backUrl" value="/v1/manager/forms/ot-requests"/>
        </jsp:include>

        <c:if test="${not empty sessionScope.error}">
            <div class="alert alert-danger alert-dismissible fade show">
                <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="error" scope="session"/>
        </c:if>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <h4 class="mb-0"><i class="fa-solid fa-file-circle-plus me-2"></i>Tạo đơn yêu cầu tăng ca</h4>
        </div>

        <div class="section-card">
            <form action="${pageContext.request.contextPath}/v1/manager/forms/create-ot" method="POST">
                
                <h5 class="mb-3 text-primary"><i class="fa-solid fa-clock me-2"></i>Thông tin thời gian</h5>
                <div class="row g-3 mb-4">
                    <div class="col-md-4">
                        <label class="form-label fw-bold">Ngày tăng ca <span class="text-danger">*</span></label>
                        <input type="date" name="otDate" class="form-control" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-bold">Thời gian bắt đầu</label>
                        <input type="text" readonly class="form-control-plaintext fw-bold text-primary fs-5" value="17:00">
                        <input type="hidden" name="startTime" value="17:00">
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-bold">Thời gian kết thúc</label>
                        <input type="text" readonly class="form-control-plaintext fw-bold text-primary fs-5" value="19:00">
                        <input type="hidden" name="endTime" value="19:00">
                    </div>
                    
                    <div class="col-md-4">
                        <label class="form-label fw-bold">Loại ngày <span class="text-danger">*</span></label>
                        <input type="text" readonly class="form-control bg-light text-muted" value="Ngày thường">
                        <input type="hidden" name="dayType" value="1">
                    </div>
                    <div class="col-md-8">
                        <label class="form-label fw-bold">Mô tả <span class="text-danger">*</span></label>
                        <input type="text" name="reason" class="form-control" placeholder="Ví dụ: Fix lỗi hệ thống khẩn cấp..." required>
                    </div>
                </div>

                <h5 class="mb-3 text-primary"><i class="fa-solid fa-users me-2"></i>Danh sách nhân viên (Phòng ${department.departmentName})</h5>
                <p class="text-muted small mb-2">Chọn những nhân viên sẽ tham gia ca tăng ca này.</p>
                <div class="employee-list-box mb-4">
                    <div class="form-check mb-2 pb-2 border-bottom">
                        <input class="form-check-input" type="checkbox" id="selectAll">
                        <label class="form-check-label fw-bold" for="selectAll">
                            Chọn tất cả
                        </label>
                    </div>
                    <c:forEach var="emp" items="${departmentEmployees}">
                        <div class="form-check mb-2">
                            <input class="form-check-input emp-checkbox" type="checkbox" name="assignees" value="${emp.employeeId}" id="emp_${emp.employeeId}">
                            <label class="form-check-label" for="emp_${emp.employeeId}">
                                <strong>${emp.fullName}</strong> - <span class="text-muted">${emp.employeeCode}</span> 
                                <span class="badge bg-light text-dark ms-2 border">${emp.positionName}</span>
                            </label>
                        </div>
                    </c:forEach>
                    <c:if test="${empty departmentEmployees}">
                        <div class="text-center text-muted py-3">Không có nhân viên nào trong phòng ban.</div>
                    </c:if>
                </div>

                <div class="d-flex justify-content-end">
                    <button type="submit" class="btn btn-primary px-4">
                        <i class="fa-solid fa-paper-plane me-2"></i> Gửi đơn chờ duyệt
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.getElementById('selectAll').addEventListener('change', function() {
            var checkboxes = document.querySelectorAll('.emp-checkbox');
            for (var checkbox of checkboxes) {
                checkbox.checked = this.checked;
            }
        });
    </script>
</body>
</html>
