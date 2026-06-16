<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử hợp đồng - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body { background-color: #f8f9fa; }
        .card { border-radius: 0.5rem; box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,0.075); }
        .badge-status { font-size: 0.75rem; padding: 0.35em 0.65em; }
    </style>
</head>
<body>
    <div class="container py-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="mb-0"><i class="bi bi-clock-history me-2"></i>Lịch sử thay đổi hợp đồng</h2>
            <a href="${pageContext.request.contextPath}/v1/employee/dashboard" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left me-1"></i> Quay lại Dashboard
            </a>
        </div>

        <div class="card mb-4">
            <div class="card-header bg-white border-0">
                <h5 class="mb-0"><i class="bi bi-search me-2"></i>Tìm kiếm lịch sử</h5>
            </div>
            <div class="card-body">
                <form method="GET" action="${pageContext.request.contextPath}/contracts/history" class="row g-3">
                    <div class="col-md-3">
                        <label class="form-label">Nhân viên (ID)</label>
                        <input type="number" name="employeeId" class="form-control" value="${employeeId}" min="1">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Từ khóa tên</label>
                        <input type="text" name="keyword" class="form-control" value="${keyword}" placeholder="Nhập tên nhân viên...">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Phòng ban (ID)</label>
                        <input type="number" name="deptId" class="form-control" value="${deptId}" min="1">
                    </div>
                    <div class="col-md-3 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary w-100"><i class="bi bi-search me-1"></i> Tìm kiếm</button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card">
            <div class="card-header bg-white border-0 d-flex justify-content-between align-items-center">
                <h5 class="mb-0">Kết quả (${history.size} bản ghi)</h5>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 140px;">Thời gian</th>
                                <th style="width: 100px;">Hợp đồng ID</th>
                                <th style="width: 130px;">Trạng thái cũ</th>
                                <th style="width: 130px;">Trạng thái mới</th>
                                <th style="width: 150px;">Người thực hiện</th>
                                <th>Lý do hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="log" items="${history}">
                                <tr>
                                    <td>
                                        <fmt:formatDate value="${log.changeDate}" pattern="dd/MM/yyyy HH:mm:ss"/>
                                    </td>
                                    <td>${log.contractId}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty log.oldStatus}">
                                                <span class="badge bg-secondary badge-status">Khởi tạo</span>
                                            </c:when>
                                            <c:when test="${log.oldStatus eq 'DRAFT'}">
                                                <span class="badge bg-secondary badge-status">${log.oldStatus}</span>
                                            </c:when>
                                            <c:when test="${log.oldStatus eq 'PENDING_APPROVAL'}">
                                                <span class="badge bg-warning text-dark badge-status">${log.oldStatus}</span>
                                            </c:when>
                                            <c:when test="${log.oldStatus eq 'PENDING_ACTIVATION'}">
                                                <span class="badge bg-info badge-status">${log.oldStatus}</span>
                                            </c:when>
                                            <c:when test="${log.oldStatus eq 'ACTIVE'}">
                                                <span class="badge bg-success badge-status">${log.oldStatus}</span>
                                            </c:when>
                                            <c:when test="${log.oldStatus eq 'EXPIRED'}">
                                                <span class="badge bg-danger badge-status">${log.oldStatus}</span>
                                            </c:when>
                                            <c:when test="${log.oldStatus eq 'TERMINATED'}">
                                                <span class="badge bg-danger badge-status">${log.oldStatus}</span>
                                            </c:when>
                                            <c:when test="${log.oldStatus eq 'CANCELLED'}">
                                                <span class="badge bg-dark badge-status">${log.oldStatus}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-light text-dark badge-status">${log.oldStatus}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${log.newStatus eq 'DRAFT'}">
                                                <span class="badge bg-secondary badge-status">${log.newStatus}</span>
                                            </c:when>
                                            <c:when test="${log.newStatus eq 'PENDING_APPROVAL'}">
                                                <span class="badge bg-warning text-dark badge-status">${log.newStatus}</span>
                                            </c:when>
                                            <c:when test="${log.newStatus eq 'PENDING_ACTIVATION'}">
                                                <span class="badge bg-info badge-status">${log.newStatus}</span>
                                            </c:when>
                                            <c:when test="${log.newStatus eq 'ACTIVE'}">
                                                <span class="badge bg-success badge-status">${log.newStatus}</span>
                                            </c:when>
                                            <c:when test="${log.newStatus eq 'EXPIRED'}">
                                                <span class="badge bg-danger badge-status">${log.newStatus}</span>
                                            </c:when>
                                            <c:when test="${log.newStatus eq 'TERMINATED'}">
                                                <span class="badge bg-danger badge-status">${log.newStatus}</span>
                                            </c:when>
                                            <c:when test="${log.newStatus eq 'CANCELLED'}">
                                                <span class="badge bg-dark badge-status">${log.newStatus}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-light text-dark badge-status">${log.newStatus}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${log.changedByName}</td>
                                    <td>${log.actionReason}</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty history}">
                                <tr>
                                    <td colspan="6" class="text-center py-5 text-muted">
                                        <i class="bi bi-journal-x fs-1 d-block mb-2"></i>
                                        Không tìm thấy dữ liệu lịch sử.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</body>
</html>