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
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            }
            .content-wrapper {
                margin-left: 260px;
                padding: 32px;
                min-height: 100vh;
            }
            .page-header {
                margin-bottom: 24px;
                display: flex;
                align-items: center;
                justify-content: space-between;
            }
            .page-title {
                font-size: 24px;
                font-weight: 700;
                color: #1a1a2e;
                margin: 0;
            }
            .page-title i {
                color: #4361ee;
            }
            .record-count {
                font-size: 13px;
                font-weight: 600;
                color: #666;
                background: #e8ecf4;
                padding: 6px 14px;
                border-radius: 20px;
            }
            .card {
                border: none;
                border-radius: 12px;
                box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            }
            .card-header {
                background: #fff;
                border-bottom: 1px solid #eee;
                padding: 16px 24px;
                border-radius: 12px 12px 0 0 !important;
            }
            .card-header h5 {
                margin: 0;
                font-weight: 600;
                color: #1a1a2e;
                font-size: 16px;
            }
            .card-header h5 i {
                color: #4361ee;
            }
            .card-body {
                padding: 0;
            }
            .filter-box {
                background: #f8f9fa;
                border-radius: 8px;
                padding: 16px;
            }
            .form-label {
                font-size: 13px;
                font-weight: 600;
                color: #555;
                margin-bottom: 4px;
            }
            .form-select-sm, .form-control-sm {
                font-size: 13px;
            }
            /* --- Table Styles --- */
            .table {
                margin-bottom: 0;
            }
            .table th {
                font-weight: 600;
                color: #666;
                font-size: 12px;
                text-transform: uppercase;
                letter-spacing: 0.5px;
                background: #fafafa;
                border-bottom: 2px solid #e9ecef;
                padding: 12px 12px;
                white-space: nowrap;
            }
            .table td {
                vertical-align: middle;
                font-size: 13px;
                padding: 10px 12px;
                color: #333;
            }
            .table-hover tbody tr:hover {
                background-color: #f0f4ff;
            }
            /* --- Badge Styles --- */
            .badge-status {
                padding: 4px 10px;
                border-radius: 20px;
                font-size: 11px;
                font-weight: 600;
                display: inline-block;
                white-space: nowrap;
            }
            .action-badge {
                font-size: 11px;
                font-weight: 600;
                padding: 4px 10px;
                border-radius: 4px;
            }
            .action-create {
                background: #e3f2fd;
                color: #1565c0;
            }
            .action-submit {
                background: #fff3e0;
                color: #e65100;
            }
            .action-approve {
                background: #e8f5e9;
                color: #2e7d32;
            }
            .action-reject {
                background: #fce4ec;
                color: #c62828;
            }
            .action-activate {
                background: #e0f2f1;
                color: #00695c;
            }
            .action-expire {
                background: #f3e5f5;
                color: #6a1b9a;
            }
            .action-terminate {
                background: #efebe9;
                color: #4e342e;
            }
            .action-cancel {
                background: #eceff1;
                color: #37474f;
            }
            .action-update {
                background: #f3e5f5;
                color: #7b1fa2;
            }
            .action-unknown {
                background: #f5f5f5;
                color: #757575;
            }
            /* --- Reason column --- */
            .reason-cell {
                max-width: 250px;
                white-space: normal;
                word-break: break-word;
                line-height: 1.4;
            }
            /* --- Empty State --- */
            .empty-state {
                text-align: center;
                padding: 64px 20px;
                color: #999;
            }
            .empty-state i {
                font-size: 56px;
                margin-bottom: 16px;
                opacity: 0.4;
                color: #aaa;
            }
            .empty-state h5 {
                font-weight: 600;
                color: #666;
                margin-bottom: 8px;
            }
            .empty-state p {
                color: #999;
                margin-bottom: 20px;
                font-size: 14px;
            }
            .empty-state .btn {
                border-radius: 8px;
                padding: 8px 24px;
                font-weight: 600;
                font-size: 14px;
            }
            /* --- Responsive --- */
            @media (max-width: 768px) {
                .content-wrapper {
                    margin-left: 0;
                    padding: 16px;
                }
                .page-header {
                    flex-direction: column;
                    align-items: flex-start;
                    gap: 8px;
                }
                .table th, .table td {
                    font-size: 12px;
                    padding: 8px;
                }
                .reason-cell {
                    max-width: 160px;
                }
            }

            /* Fix filter form on small screens */
            @media (max-width: 576px) {
                .filter-box .row > div {
                    margin-bottom: 8px;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/managerSideBar.jsp" />
        <div class="content-wrapper">

            <!-- Page Header -->
            <div class="page-header">
                <h1 class="page-title">
                    <i class="fas fa-history me-2"></i>Lịch Sử Hợp Đồng
                </h1>
                <c:if test="${history != null}">
                    <span class="record-count">
                        <i class="fas fa-file-contract me-1"></i>${history.size()} bản ghi
                    </span>
                </c:if>
            </div>

            <!-- Error / Success Flash Messages -->
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="fas fa-exclamation-circle me-2"></i>${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="fas fa-check-circle me-2"></i>${success}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <!-- Filter Card -->
            <div class="card mb-4">
                <div class="card-body filter-box">
                    <form action="${pageContext.request.contextPath}/v1/manager/contract/history" method="get" class="row g-3 align-items-end">
                        <div class="col-md-2">
                            <label class="form-label">Mã HĐ</label>
                            <input type="text" name="contractCode" class="form-control form-control-sm" value="${param.contractCode}" placeholder="Nhập mã HĐ">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Phòng ban</label>
                            <select name="deptId" class="form-select form-select-sm">
                                <option value="">Tất cả</option>
                                <c:if test="${departments != null}">
                                    <c:forEach items="${departments}" var="dept">
                                        <option value="${dept.departmentId}" ${param.deptId == dept.departmentId ? 'selected' : ''}>${dept.departmentName}</option>
                                    </c:forEach>
                                </c:if>
                            </select>
                        </div>

                        <div class="col-md-2">
                            <label class="form-label">Trạng thái</label>
                            <select name="status" class="form-select form-select-sm">
                                <option value="">Tất cả</option>
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
                            <button type="submit" class="btn btn-primary btn-sm w-100" title="Tìm kiếm">
                                <i class="fas fa-search me-1"></i>
                            </button>
                        </div>
                        <div class="col-md-1">
                            <a href="${pageContext.request.contextPath}/v1/manager/contract/history" class="btn btn-outline-secondary btn-sm w-100" title="Đặt lại">
                                <i class="fas fa-undo me-1"></i>
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- History Table Card -->
            <div class="card">
                <div class="card-header">
                    <h5><i class="fas fa-list me-2"></i>Danh sách lịch sử hợp đồng</h5>
                </div>
                <div class="card-body">

                    <c:if test="${history != null and not empty history}">

                        <div class="table-responsive">
                            <table class="table table-hover table-striped align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Mã HĐ</th>
                                        <th>Nhân viên</th>
                                        <th>Thao tác</th>
                                        <th>Trạng thái cũ</th>
                                        <th>Trạng thái mới</th>
                                        <th>Người thực hiện</th>
                                        <th>Thời gian</th>
                                        <th style="min-width:140px;">Lý do</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${history}" var="log" varStatus="loop">
                                        <tr>
                                            <td class="fw-semibold text-muted">${log.logId}</td>
                                            <td class="fw-semibold">#${log.contractId}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty log.employeeName}">
                                                        <c:out value="${log.employeeName}" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted">N/A</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <%-- Infer action label from status transition --%>
                                                <c:choose>
                                                    <c:when test="${log.oldStatus == null && log.newStatus == 'DRAFT'}">
                                                        <span class="action-badge action-create">Tạo hợp đồng</span>
                                                    </c:when>
                                                    <c:when test="${log.oldStatus == 'DRAFT' && log.newStatus == 'PENDING_APPROVAL'}">
                                                        <span class="action-badge action-submit">Gửi duyệt</span>
                                                    </c:when>
                                                    <c:when test="${(log.oldStatus == 'DRAFT' || log.oldStatus == 'PENDING_APPROVAL') && log.newStatus == 'PENDING_ACTIVATION'}">
                                                        <span class="action-badge action-approve">Phê duyệt</span>
                                                    </c:when>
                                                    <c:when test="${log.oldStatus == 'PENDING_APPROVAL' && log.newStatus == 'CANCELLED'}">
                                                        <span class="action-badge action-reject">Từ chối</span>
                                                    </c:when>
                                                    <c:when test="${log.oldStatus == 'PENDING_ACTIVATION' && log.newStatus == 'ACTIVE'}">
                                                        <span class="action-badge action-activate">Kích hoạt</span>
                                                    </c:when>
                                                    <c:when test="${log.oldStatus == 'ACTIVE' && log.newStatus == 'EXPIRED'}">
                                                        <span class="action-badge action-expire">Hết hạn</span>
                                                    </c:when>
                                                    <c:when test="${log.newStatus == 'TERMINATED'}">
                                                        <span class="action-badge action-terminate">Chấm dứt</span>
                                                    </c:when>
                                                    <c:when test="${log.newStatus == 'CANCELLED' && log.oldStatus != 'PENDING_APPROVAL'}">
                                                        <span class="action-badge action-cancel">Hủy</span>
                                                    </c:when>
                                                    <c:when test="${log.oldStatus == 'DRAFT' && log.newStatus == 'DRAFT'}">
                                                        <span class="action-badge action-update">Cập nhật</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="action-badge action-update">
                                                            <c:out value="${log.actionReason != null ? fn:substring(log.actionReason, 0, 20) : 'Cập nhật'}" />
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${log.oldStatus == null}">
                                                        <span class="text-muted" style="font-size:12px;">—</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:set var="oldClass" value="bg-secondary" />
                                                        <c:if test="${log.oldStatus == 'ACTIVE'}"><c:set var="oldClass" value="bg-success" /></c:if>
                                                        <c:if test="${log.oldStatus == 'DRAFT'}"><c:set var="oldClass" value="bg-secondary" /></c:if>
                                                        <c:if test="${log.oldStatus == 'PENDING_APPROVAL'}"><c:set var="oldClass" value="bg-warning text-dark" /></c:if>
                                                        <c:if test="${log.oldStatus == 'PENDING_ACTIVATION'}"><c:set var="oldClass" value="bg-info text-dark" /></c:if>
                                                        <c:if test="${log.oldStatus == 'EXPIRED'}"><c:set var="oldClass" value="bg-danger" /></c:if>
                                                        <c:if test="${log.oldStatus == 'CANCELLED'}"><c:set var="oldClass" value="bg-danger" /></c:if>
                                                        <c:if test="${log.oldStatus == 'TERMINATED'}"><c:set var="oldClass" value="bg-dark" /></c:if>
                                                        <c:if test="${log.oldStatus == 'REJECTED'}"><c:set var="oldClass" value="bg-danger" /></c:if>
                                                        <span class="badge badge-status ${oldClass}">${log.oldStatus}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:set var="newClass" value="bg-secondary" />
                                                <c:if test="${log.newStatus == 'ACTIVE'}"><c:set var="newClass" value="bg-success" /></c:if>
                                                <c:if test="${log.newStatus == 'DRAFT'}"><c:set var="newClass" value="bg-secondary" /></c:if>
                                                <c:if test="${log.newStatus == 'PENDING_APPROVAL'}"><c:set var="newClass" value="bg-warning text-dark" /></c:if>
                                                <c:if test="${log.newStatus == 'PENDING_ACTIVATION'}"><c:set var="newClass" value="bg-info text-dark" /></c:if>
                                                <c:if test="${log.newStatus == 'EXPIRED'}"><c:set var="newClass" value="bg-danger" /></c:if>
                                                <c:if test="${log.newStatus == 'CANCELLED'}"><c:set var="newClass" value="bg-danger" /></c:if>
                                                <c:if test="${log.newStatus == 'TERMINATED'}"><c:set var="newClass" value="bg-dark" /></c:if>
                                                <c:if test="${log.newStatus == 'REJECTED'}"><c:set var="newClass" value="bg-danger" /></c:if>
                                                <span class="badge badge-status ${newClass}">${log.newStatus}</span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty log.changedByName}">
                                                        <span><c:out value="${log.changedByName}" /></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted">UID-${log.changedBy}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-nowrap" style="font-size:12px;">
                                                <fmt:formatDate value="${log.changeDate}" pattern="dd/MM/yyyy HH:mm" />
                                            </td>
                                            <td class="reason-cell">
                                                <c:choose>
                                                    <c:when test="${not empty log.actionReason}">
                                                        <c:out value="${log.actionReason}" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted fst-italic">—</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                    </c:if>

                    <c:if test="${history == null or empty history}">
                        <div class="empty-state">
                            <i class="fas fa-file-contract"></i>
                            <h5>Chưa có lịch sử hợp đồng</h5>
                            <p>Không tìm thấy bản ghi nào. Hãy thử thay đổi bộ lọc tìm kiếm.</p>
                            <a href="${pageContext.request.contextPath}/v1/manager/dashboard" class="btn btn-outline-primary">
                                <i class="fas fa-arrow-left me-2"></i>Quay lại Dashboard
                            </a>
                        </div>
                    </c:if>

                </div>
            </div>

        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            // Auto-dismiss alerts after 5 seconds
            document.addEventListener('DOMContentLoaded', function() {
                setTimeout(function() {
                    var alerts = document.querySelectorAll('.alert-dismissible');
                    alerts.forEach(function(alert) {
                        var bsAlert = new bootstrap.Alert(alert);
                        bsAlert.close();
                    });
                }, 5000);
            });
        </script>
    </body>
</html>
