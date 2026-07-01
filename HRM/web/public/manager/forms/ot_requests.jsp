<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Danh sách đơn Overtime - HRM</title>
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
            .badge-status {
                padding: 4px 12px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
            }
            .status-0 { background: #fef3c7; color: #92400e; }
            .status-1 { background: #d1fae5; color: #065f46; }
            .status-2 { background: #fee2e2; color: #991b1b; }
            .status-3 { background: #e5e7eb; color: #374151; }
            .status-4 { background: #dbeafe; color: #1e40af; }
        </style>
    </head>
    <body>

        <jsp:include page="/public/components/managerSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Đơn OT đã tạo" />
            </jsp:include>

            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success alert-dismissible fade show">
                    <i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="success" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-danger alert-dismissible fade show">
                    <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="error" scope="session"/>
            </c:if>

            <div class="d-flex justify-content-between align-items-center mb-4">
                <h4 class="mb-0"><i class="fa-solid fa-clock me-2"></i>Danh sách đơn Overtime đã tạo</h4>
                <a href="${pageContext.request.contextPath}/v1/manager/forms/create-ot" class="btn btn-primary">
                    <i class="fa-solid fa-plus me-1"></i> Tạo đơn OT
                </a>
            </div>

            <!-- Filters -->
            <div class="section-card">
                <form action="${pageContext.request.contextPath}/v1/manager/forms/ot-requests" method="GET" class="row g-3 align-items-end">
                    <div class="col-md-3">
                        <label class="form-label">Trạng thái</label>
                        <select name="status" class="form-select">
                            <option value="">Tất cả</option>
                            <option value="0" ${statusFilter == '0' ? 'selected' : ''}>Chờ duyệt</option>
                            <option value="1" ${statusFilter == '1' ? 'selected' : ''}>Đã duyệt</option>
                            <option value="2" ${statusFilter == '2' ? 'selected' : ''}>Từ chối</option>
                            <option value="3" ${statusFilter == '3' ? 'selected' : ''}>Đã hủy</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label">Ngày tăng ca</label>
                        <input type="date" name="otDate" value="${dateFilter}" class="form-control">
                    </div>
                    <div class="col-md-4">
                        <button type="submit" class="btn btn-primary">
                            <i class="fa-solid fa-filter me-1"></i> Lọc
                        </button>
                        <a href="${pageContext.request.contextPath}/v1/manager/forms/ot-requests" class="btn btn-outline-secondary ms-2">Xóa bộ lọc</a>
                    </div>
                </form>
            </div>

            <div class="section-card">
                <div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>Mã đơn</th>
                                <th>Ngày tăng ca</th>
                                <th>Thời gian</th>
                                <th>Loại ngày</th>
                                <th class="text-center">Số lượng NV</th>
                                <th>Trạng thái</th>
                                <th>Người duyệt</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="req" items="${otRequests}">
                                <tr>
                                    <td>
                                        <div class="fw-bold">${req.formCode}</div>
                                        <small class="text-muted"><fmt:formatDate value="${req.createdAt}" pattern="dd/MM/yyyy HH:mm"/></small>
                                    </td>
                                    <td><fmt:formatDate value="${req.otDate}" pattern="dd/MM/yyyy"/></td>
                                    <td>${req.startTime} - ${req.endTime}</td>
                                    <td>${req.dayTypeLabel}</td>
                                    <td class="text-center">
                                        <span class="badge bg-primary rounded-pill">${req.totalAssignees}</span>
                                    </td>
                                    <td>
                                        <span class="badge-status status-${req.status}">${req.statusLabel}</span>
                                        <c:if test="${req.status == 2}">
                                            <div class="small text-danger mt-1">Lý do: ${req.approverNote}</div>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${req.status == 1 || req.status == 2 || req.status == 4}">
                                                Business Admin
                                            </c:when>
                                            <c:when test="${not empty req.approverName}">
                                                ${req.approverName}
                                            </c:when>
                                            <c:otherwise>
                                                -
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/v1/manager/forms/ot-detail?id=${req.formId}" class="btn btn-sm btn-outline-primary" title="Xem chi tiết">
                                            <i class="fa-solid fa-eye"></i>
                                        </a>
                                        <c:if test="${req.status == 0}">
                                            <form action="${pageContext.request.contextPath}/v1/manager/forms/cancel-ot" method="POST" class="d-inline" onsubmit="return confirm('Bạn có chắc chắn muốn hủy đơn này không?');">
                                                <input type="hidden" name="formId" value="${req.formId}">
                                                <button type="submit" class="btn btn-sm btn-outline-danger ms-1" title="Hủy đơn">
                                                    <i class="fa-solid fa-trash"></i>
                                                </button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty otRequests}">
                                <tr>
                                    <td colspan="8" class="text-center text-muted py-4">
                                        <i class="fa-solid fa-inbox me-2"></i>Không tìm thấy đơn tăng ca nào.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
