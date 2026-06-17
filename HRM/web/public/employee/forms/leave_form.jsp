<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đơn Nghỉ Phép - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 28px;
            margin-bottom: 24px;
        }
        .section-card h5 { font-weight: 700; color: #0f172a; }
    </style>
</head>
<body>

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
        <jsp:param name="title" value="Đơn Nghỉ Phép" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="section-card">
        <h5 class="mb-4">Tạo Đơn Xin Nghỉ Phép</h5>
        <form method="post" action="${pageContext.request.contextPath}/v1/employee/forms/leave/submit"
              enctype="multipart/form-data">
            <div class="row g-3">

                <div class="col-md-4">
                    <label class="form-label fw-semibold">Ngày bắt đầu <span class="text-danger">*</span></label>
                    <input type="date" id="startDate" name="startDate" class="form-control" required
                           value="${param.startDate}">
                    <small class="text-muted">Không được chọn ngày trong quá khứ</small>
                </div>

                <div class="col-md-4">
                    <label class="form-label fw-semibold">Ngày kết thúc <span class="text-danger">*</span></label>
                    <input type="date" id="endDate" name="endDate" class="form-control" required
                           value="${param.endDate}">
                </div>

                <div class="col-md-4">
                    <label class="form-label fw-semibold">Số ngày nghỉ</label>
                    <input type="text" id="totalDays" class="form-control" readonly
                           placeholder="Tự động tính">
                </div>

                <div class="col-md-12">
                    <label class="form-label fw-semibold">Lý do xin nghỉ <span class="text-danger">*</span></label>
                    <textarea id="reason" name="reason" class="form-control" rows="4" required
                              placeholder="Mô tả chi tiết lý do xin nghỉ phép..."><c:out value="${param.reason}"/></textarea>
                </div>

                <div class="col-md-12">
                    <label class="form-label fw-semibold">File đính kèm <span class="text-muted">(không bắt buộc)</span></label>
                    <input type="file" name="attachment" class="form-control"
                           accept=".xlsx,.pdf,.docx,.doc,.xls,.jpg,.png,.zip">
                    <small class="text-muted">Cho phép: xlsx, pdf, docx, doc, xls, jpg, png, zip</small>
                </div>

            </div>
            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-primary" id="submitBtn">Gửi đơn</button>
                <a href="${pageContext.request.contextPath}/v1/employee/form-dashboard"
                   class="btn btn-outline-secondary">Quay lại</a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('startDate').min = today;
    document.getElementById('endDate').min = today;

    function calcTotalDays() {
        const s = document.getElementById('startDate').value;
        const e = document.getElementById('endDate').value;
        if (s && e) {
            const diff = (new Date(e) - new Date(s)) / (1000 * 60 * 60 * 24) + 1;
            document.getElementById('totalDays').value = diff > 0 ? diff + ' ngày' : '';
        } else {
            document.getElementById('totalDays').value = '';
        }
    }

    document.getElementById('startDate').addEventListener('change', function () {
        const endInput = document.getElementById('endDate');
        endInput.min = this.value;
        if (endInput.value && endInput.value < this.value) {
            endInput.value = this.value;
        }
        calcTotalDays();
    });
    document.getElementById('endDate').addEventListener('change', calcTotalDays);
</script>
</body>
</html>
