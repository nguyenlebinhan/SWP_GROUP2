<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sử thay đổi cấu hình lương</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background:#f5f6fa; font-family:'Segoe UI', sans-serif; }
        .main { margin-left:250px; padding:25px; }
        .panel { background:#fff; border-radius:14px; box-shadow:0 2px 12px rgba(11,14,42,.07); padding:22px; margin-bottom:22px; }
        .table th { background:#0B0E2A; color:#fff; font-size:13px; white-space:nowrap; }
        .hint { color:#6b7280; font-size:13px; }
        .change-text { white-space:pre-wrap; min-width:260px; max-width:420px; }
        .who { min-width:160px; }
    </style>
</head>
<body>
<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Lịch sử thay đổi cấu hình lương" />
        <jsp:param name="backUrl" value="/v1/manager/dashboard" />
    </jsp:include>

    <div class="d-flex justify-content-between align-items-center mb-3">
        <a href="${payrollConfigBaseUrl}" class="btn btn-outline-secondary btn-sm">
            <i class="fa-solid fa-arrow-left me-1"></i> Cấu hình lương
        </a>
    </div>

    <div class="panel">
        <h5 class="fw-bold mb-2">Lịch sử thay đổi cấu hình lương</h5>
        <form method="get" action="${payrollConfigBaseUrl}/history" class="row g-2 align-items-end mb-3">
            <div class="col-md-3">
                <label class="form-label small text-muted">Trạng thái</label>
                <select name="status" class="form-select form-select-sm">
                    <option value="">Tất cả</option>
                    <option value="1" ${statusFilter == 1 ? 'selected' : ''}>Đã duyệt</option>
                    <option value="2" ${statusFilter == 2 ? 'selected' : ''}>Từ chối</option>
                </select>
            </div>
            <div class="col-md-5">
                <label class="form-label small text-muted">Từ khóa</label>
                <input name="q" value="${q}" class="form-control form-control-sm" placeholder="Thao tác, đối tượng, người dùng, ghi chú">
            </div>
            <div class="col-md-4 d-flex gap-2">
                <button class="btn btn-sm btn-primary">Lọc</button>
                <a href="${payrollConfigBaseUrl}/history" class="btn btn-sm btn-outline-secondary">Xóa lọc</a>
            </div>
        </form>
        <div class="hint mb-2">Tổng ${totalItems} bản ghi</div>
        <c:choose>
            <c:when test="${empty changeHistory}">
                <div class="hint">Chưa có lịch sử thay đổi cấu hình lương.</div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead>
                            <tr>
                                <th>Mã yêu cầu</th><th>Thao tác</th><th>Đối tượng</th><th>Trạng thái</th>
                                <th>Người gửi</th><th>Người duyệt</th>
                                <th>Trước</th><th>Sau</th><th>Ghi chú duyệt</th>
                            </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="h" items="${changeHistory}">
                            <tr>
                                <td>${h.requestId}</td>
                                <td><c:out value="${h.actionLabel}" /></td>
                                <td><c:out value="${empty h.targetKey ? h.targetId : h.targetKey}" /></td>
                                <td><span class="badge ${h.status == 1 ? 'text-bg-success' : 'text-bg-danger'}"><c:out value="${h.statusLabel}" /></span></td>
                                <td class="who"><c:out value="${h.requestedByName}" /><br><small class="text-muted"><fmt:formatDate value="${h.requestedAt}" pattern="dd/MM/yyyy HH:mm" /></small></td>
                                <td class="who"><c:out value="${h.reviewedByName}" /><br><small class="text-muted"><fmt:formatDate value="${h.reviewedAt}" pattern="dd/MM/yyyy HH:mm" /></small></td>
                                <td class="change-text"><c:out value="${h.oldValue}" /></td>
                                <td class="change-text"><c:out value="${h.newValue}" /></td>
                                <td class="change-text"><c:out value="${h.reviewNote}" /></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
                <c:if test="${totalPages > 1}">
                    <nav class="mt-3">
                        <ul class="pagination justify-content-center mb-0">
                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                <a class="page-link" href="${pageBase}&page=${currentPage - 1}">&laquo;</a>
                            </li>
                            <li class="page-item disabled"><span class="page-link">Trang ${currentPage} / ${totalPages}</span></li>
                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageBase}&page=${currentPage + 1}">&raquo;</a>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</div>
</body>
</html>
