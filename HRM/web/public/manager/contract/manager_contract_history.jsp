<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Lịch Sử Hợp Đồng Lao Động</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background: #f5f7fa;
                font-family: 'Segoe UI', sans-serif;
            }
            .content-wrapper {
                margin-left: 260px;
                padding: 32px;
                min-height: 100vh;
            }
            .page-header {
                margin-bottom: 24px;
            }
            .page-title {
                font-size: 24px;
                font-weight: 700;
                color: #1a1a2e;
            }
            .card {
                border: none;
                border-radius: 12px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.05);
            }
            .card-header {
                background: #fff;
                border-bottom: 1px solid #eee;
                padding: 16px 24px;
            }
            .card-header h5 {
                margin: 0;
                font-weight: 600;
                color: #1a1a2e;
            }
            .filter-box {
                background: #f8f9fa;
                border-radius: 8px;
                padding: 16px;
                margin-bottom: 20px;
            }
            .form-label {
                font-size: 13px;
                font-weight: 600;
                color: #555;
            }
            .table th {
                font-weight: 600;
                color: #666;
                font-size: 13px;
                text-transform: uppercase;
                letter-spacing: 0.5px;
                background: #fafafa;
            }
            .table td {
                vertical-align: middle;
                font-size: 14px;
            }
            .badge-status {
                padding: 4px 10px;
                border-radius: 20px;
                font-size: 11px;
                font-weight: 600;
            }
            .status-draft {
                background: #f0f0f0;
                color: #666;
            }
            .status-pending {
                background: #fff3cd;
                color: #856404;
            }
            .status-active {
                background: #d4edda;
                color: #155724;
            }
            .status-expired {
                background: #f8d7da;
                color: #721c24;
            }
            .status-cancelled {
                background: #e2e3e5;
                color: #383d41;
            }
            .empty-state {
                text-align: center;
                padding: 60px 20px;
                color: #999;
            }
            .empty-state i {
                font-size: 48px;
                margin-bottom: 16px;
                opacity: 0.5;
            }
        </style>
    </head>
    <body>
        <div class="content-wrapper">
            <jsp:include page="/public/components/managerSideBar.jsp" />
            <div class="page-header">
                <h1 class="page-title"><i class="fas fa-history me-2"></i>Lịch Sử Hợp Đồng Lao Động</h1>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <div class="card mb-4">
                <div class="card-body filter-box">
                    <form action="${pageContext.request.contextPath}/v1/manager/contract/history" method="get" class="row g-3 align-items-end">
                        <div class="col-md-3">
                            <label class="form-label">Nhân viên</label>
                            <select name="employeeId" class="form-select form-select-sm">
                                <option value="">Tất cả</option>
                                <c:if test="${employees != null}">
                                    <c:forEach items="${employees}" var="emp">
                                        <option value="${emp.employeeId}" ${param.employeeId == emp.employeeId ? 'selected' : ''}>${emp.fullName}</option>
                                    </c:forEach>
                                </c:if>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Mã hợp đồng</label>
                            <input type="text" name="contractCode" class="form-control form-control-sm" value="${param.contractCode}" placeholder="Nhập mã HĐ">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Trạng thái</label>
                            <select name="status" class="form-select form-select-sm">
                                <option value="">Tất cả</option>
                                <option value="DRAFT" ${param.status == 'DRAFT' ? 'selected' : ''}>Draft</option>
                                <option value="PENDING_APPROVAL" ${param.status == 'PENDING_APPROVAL' ? 'selected' : ''}>Chờ duyệt</option>
                                <option value="PENDING_ACTIVATION" ${param.status == 'PENDING_ACTIVATION' ? 'selected' : ''}>Chờ hiệu lực</option>
                                <option value="ACTIVE" ${param.status == 'ACTIVE' ? 'selected' : ''}>Đang hiệu lực</option>
                                <option value="EXPIRED" ${param.status == 'EXPIRED' ? 'selected' : ''}>Hết hạn</option>
                                <option value="TERMINATED" ${param.status == 'TERMINATED' ? 'selected' : ''}>Chấm dứt</option>
                                <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>Hủy</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Từ ngày</label>
                            <input type="date" name="filterStartDate" class="form-control form-control-sm" value="${param.filterStartDate}">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Đến ngày</label>
                            <input type="date" name="endDate" class="form-control form-control-sm" value="${param.endDate}">
                        </div>
                        <div class="col-md-1">
                            <button type="submit" class="btn btn-primary btn-sm w-100"><i class="fas fa-search"></i></button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <h5><i class="fas fa-list me-2"></i>Danh sách lịch sử hợp đồng</h5>
                </div>
                <div class="card-body p-0">
                    <c:if test="${history != null and not history.isEmpty()}">
                        <div class="table-responsive">

                            <thead>
                                <tr>
                                    <th>ID Log</th>
                                    <th>Mã HĐ</th>
                                    <th>Nhân viên</th>
                                    <th>Thao tác</th>
                                    <th>Trạng thái cũ</th>
                                    <th>Trạng thái mới</th>
                                    <th>Người thực hiện</th>
                                    <th>Thời gian</th>
                                    <th>Lý do</th>
                                </tr>
                            </thead>

                            <c:forEach items="${history}" var="log">

                                <td>${log.logId}</td>
                                <td>${log.contractId}</td>
                                <td>${log.employeeId != 0 ? log.employeeId : 'N/A'}</td>
                                <td>${log.actionReason != null ? log.actionReason : 'Cập nhật trạng thái'}</td>
                                <td><span class="badge badge-status status-${fn:toLowerCase(log.oldStatus)}">${log.oldStatus}</span></td>
                                <td><span class="badge badge-status status-${fn:toLowerCase(log.newStatus)}">${log.newStatus}</span></td>
                                <td>${log.changedByName != null ? log.changedByName : log.changedBy}</td>
                                <td><fmt:formatDate value="${log.changeDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                <td>${log.actionReason}</td>

                            </c:forEach>


                        </div>
                    </c:if>
                    <c:if test="${history == null or history.isEmpty()}">
                        <div class="empty-state">
                            <i class="fas fa-file-contract"></i>
                            <p>Không tìm thấy lịch sử hợp đồng nào.</p>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
