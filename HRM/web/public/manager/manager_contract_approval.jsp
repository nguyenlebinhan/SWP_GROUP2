<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="model.EmploymentContract" %>
<%@ page import="model.EmployeeDetailDTO" %>
<%@ page import="model.ContractType" %>
<%@ page import="model.ContractStatus" %>
<%@ page import="java.util.List" %>

<%
    List<EmploymentContract> pendingContracts = (List<EmploymentContract>) request.getAttribute("pendingContracts");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Duyệt Hợp Đồng Lao Động</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f7fa; font-family: 'Segoe UI', sans-serif; }
        .content-wrapper { margin-left: 260px; padding: 32px; min-height: 100vh; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
        .page-title { font-size: 24px; font-weight: 700; color: #1a1a2e; margin: 0; }
        .card { border: none; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
        .card-header { background: #fff; border-bottom: 1px solid #eee; padding: 16px 24px; }
        .card-header h5 { margin: 0; font-weight: 600; color: #1a1a2e; }
        .table th { font-weight: 600; color: #666; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; background: #fafafa; }
        .table td { vertical-align: middle; font-size: 14px; }
        .badge-status { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-pending { background: #fff3cd; color: #856404; }
        .btn-action { padding: 6px 14px; font-size: 13px; border-radius: 6px; }
        .empty-state { text-align: center; padding: 60px 20px; color: #999; }
        .empty-state i { font-size: 48px; margin-bottom: 16px; opacity: 0.5; }
    </style>
</head>
<body>
<div class="content-wrapper">
    <div class="page-header">
        <h1 class="page-title"><i class="fas fa-file-contract me-2"></i>Duyệt Hợp Đồng Lao Động</h1>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="card">
        <div class="card-header">
            <h5><i class="fas fa-clock me-2"></i>Danh sách hợp đồng chờ duyệt</h5>
        </div>
        <div class="card-body p-0">
            <c:if test="${pendingContracts != null and not pendingContracts.isEmpty()}">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                            <tr>
                                <th>Mã HĐ</th>
                                <th>Nhân viên</th>
                                <th>Loại HĐ</th>
                                <th>Ngày hiệu lực</th>
                                <th>Ngày kết thúc</th>
                                <th>Lương</th>
                                <th>Trạng thái</th>
                                <th>Ngày tạo</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${pendingContracts}" var="contract">
                                <tr>
                                    <td><strong>${contract.contractCode}</strong></td>
                                    <td>
                                        <c:if test="${contract.employee != null}">
                                            ${contract.employee.fullName} <small class="text-muted">(#${contract.employee.employeeCode})</small>
                                        </c:if>
                                        <c:if test="${contract.employee == null}">
                                            <span class="text-muted">ID: ${contract.employeeId}</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${contract.contractType == 'PROBATION'}">Thử việc</c:when>
                                            <c:when test="${contract.contractType == 'INTERNSHIP'}">Thực tập</c:when>
                                            <c:when test="${contract.contractType == 'FIXED_TERM'}">Có thời hạn</c:when>
                                            <c:when test="${contract.contractType == 'INDEFINITE'}">Vô thời hạn</c:when>
                                            <c:otherwise>${contract.contractType}</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></td>
                                    <td>
                                        <c:if test="${contract.endDate != null}">
                                            <fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy"/>
                                        </c:if>
                                        <c:if test="${contract.endDate == null}">Vô thời hạn</c:if>
                                    </td>
                                    <td>${contract.salary != null ? contract.salary : 0}</td>
                                    <td><span class="badge badge-status badge-pending">${contract.status}</span></td>
                                    <td><fmt:formatDate value="${contract.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    <td>
                                        <div class="d-flex gap-2">
                                            <a href="${pageContext.request.contextPath}/v1/manager/contracts/approve-preview?contractId=${contract.contractId}" 
                                               class="btn btn-primary btn-action" title="Xem trước & Duyệt">
                                                <i class="fas fa-check me-1"></i> Duyệt
                                            </a>
                                            <a href="${pageContext.request.contextPath}/v1/manager/contracts/reject-dialog?contractId=${contract.contractId}" 
                                               class="btn btn-outline-danger btn-action" title="Từ chối">
                                                <i class="fas fa-times me-1"></i> Từ chối
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
            <c:if test="${pendingContracts == null or pendingContracts.isEmpty()}">
                <div class="empty-state">
                    <i class="fas fa-clipboard-check"></i>
                    <p>Không có hợp đồng nào chờ duyệt.</p>
                </div>
            </c:if>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>