<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Import chấm công - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
            margin-bottom: 24px;
        }
        .section-card h5 { font-weight: 700; color: #0f172a; }
    </style>
</head>
<body>

<jsp:include page="${empty sidebarPage ? '/public/components/employeeSideBar.jsp' : sidebarPage}" />

<div class="main">
    <jsp:include page="${empty topbarPage ? '/public/components/employeeTopBar.jsp' : topbarPage}">
        <jsp:param name="title" value="Import chấm công" />
        <jsp:param name="backUrl" value="/v1/employee/attendance/overview"/>
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="section-card">
        <form method="post" action="${pageContext.request.contextPath}/v1/employee/attendance/import"
              enctype="multipart/form-data">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Tháng <span class="text-danger">*</span></label>
                    <select name="month" class="form-select" required>
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Năm <span class="text-danger">*</span></label>
                    <input type="number" name="year" class="form-control" min="2000" max="2100"
                           value="${empty selectedYear ? 2026 : selectedYear}" required>
                </div>

                <div class="col-md-12">
                    <label class="form-label">File Excel (.xlsx) <span class="text-danger">*</span></label>
                    <input type="file" name="attendanceFile" class="form-control" accept=".xlsx" required>
                    <small class="text-muted">
                        Cột yêu cầu (dòng 1 là header): employeeCode, fullName, Department, workDate, timeIn, timeOut.
                        Trạng thái được tự suy từ giờ vào/ra. Khi chọn "Tất cả phòng ban", hệ thống tự xác định phòng của từng nhân viên. Tối đa 10MB.
                    </small>
                </div>
            </div>
            <div class="mt-3">
                <button type="submit" class="btn btn-primary">
                    <i class="fa-solid fa-upload me-1"></i> Upload &amp; Import
                </button>
            </div>
        </form>
    </div>

    <c:if test="${not empty importResult}">
        <div class="section-card">
            <h5 class="mb-3"><i class="fa-solid fa-list-check me-2"></i>Kết quả import</h5>
            <c:choose>
                <c:when test="${importResult.status == 1}">
                    <div class="alert alert-success">${importResult.note}</div>
                </c:when>
                <c:when test="${importResult.status == 3}">
                    <div class="alert alert-warning">${importResult.note}</div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-danger">${importResult.note}</div>
                </c:otherwise>
            </c:choose>

            <div class="row text-center mb-3">
                <div class="col">
                    <div class="fs-4 fw-bold">${importResult.totalRows}</div>
                    <div class="text-muted">Tổng số dòng</div>
                </div>
                <div class="col">
                    <div class="fs-4 fw-bold text-success">${importResult.importedRows}</div>
                    <div class="text-muted">Thành công</div>
                </div>
                <div class="col">
                    <div class="fs-4 fw-bold text-danger">${importResult.failedRows}</div>
                    <div class="text-muted">Lỗi</div>
                </div>
            </div>

            <c:if test="${not empty importResult.errors}">
                <h6 class="fw-bold">Danh sách lỗi</h6>
                <div class="table-responsive">
                    <table class="table table-sm table-bordered">
                        <thead class="table-light">
                            <tr>
                                <th style="width:90px">Dòng</th>
                                <th style="width:160px">employeeCode</th>
                                <th>Lỗi</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="err" items="${importResult.errors}">
                                <tr>
                                    <td>${err.rowNumber}</td>
                                    <td><c:out value="${err.employeeCode}" /></td>
                                    <td><c:out value="${err.message}" /></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>

            <a href="${pageContext.request.contextPath}/v1/employee/attendance/overview?month=${selectedMonth}&year=${selectedYear}"
               class="btn btn-outline-primary btn-sm">
                <i class="fa-solid fa-table me-1"></i> Xem dữ liệu chấm công
            </a>
        </div>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
