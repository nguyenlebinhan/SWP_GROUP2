<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách Hợp đồng Chờ Duyệt - HRM</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        body { background-color: #f5f6fa; font-family: 'Be Vietnam Pro', sans-serif; }
        .sidebar { min-height: 100vh; background: #0d1240; }
        .main-content { padding: 2rem; }
        .table th, .table td { vertical-align: middle; }
        .status-badge { font-size: 0.75rem; padding: 0.35rem 0.75rem; border-radius: 999px; }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <!-- Sidebar Placeholder -->
        <div class="col-auto col-md-3 col-xl-2 px-0 sidebar">
            <div class="d-flex flex-column align-items-center align-items-sm-start px-3 pt-2 text-white min-vh-100">
                <a href="#" class="d-flex align-items-center pb-3 mb-3 link-white text-decoration-none border-bottom">
                    <span class="fs-4 fw-bold">HRM <span class="text-warning">Group 2</span></span>
                </a>
                <ul class="nav nav-pills flex-column mb-sm-auto mb-0 w-100">
                    <li class="nav-item"><a href="#" class="nav-link text-white">Dashboard</a></li>
                    <li class="nav-item"><a href="#" class="nav-link text-warning active">Hợp đồng chờ duyệt</a></li>
                </ul>
            </div>
        </div>

        <!-- Main Content -->
        <div class="col py-3 main-content">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="mb-0 fw-bold text-dark">Danh sách Hợp đồng Chờ Duyệt</h2>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>

            <div class="card shadow-sm">
                <div class="card-header bg-white border-0">
                    <h5 class="mb-0">Hợp đồng đang chờ phê duyệt (PENDING_APPROVAL)</h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th>Mã HD</th>
                                    <th>Nhân viên</th>
                                    <th>Loại hợp đồng</th>
                                    <th>Ngày hiệu lực</th>
                                    <th>Lương</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end pe-4">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${pendingContracts}" var="contract">
                                    <tr>
                                        <td class="fw-medium">${contract.contractCode}</td>
                                        <td>
                                            <div class="fw-medium">${contract.employeeFullName}</div>
                                            <small class="text-muted">${contract.employeeCode}</small>
                                        </td>
                                        <td>
                                            <span class="badge bg-info text-dark">${contract.contractType}</span>
                                        </td>
                                        <td><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></td>
                                        <td class="fw-medium text-success">
                                            <fmt:formatNumber value="${contract.salary}" pattern="#,##0" /> VNĐ
                                        </td>
                                        <td>
                                            <span class="status-badge bg-warning text-dark">${contract.status}</span>
                                        </td>
                                        <td class="text-end pe-4">
                                            <a href="${pageContext.request.contextPath}/v1/manager/contract/approve-preview?contractId=${contract.contractId}" class="btn btn-sm btn-outline-primary me-1">
                                                <i class="bi bi-eye"></i> Xem
                                            </a>
                                            <form action="${pageContext.request.contextPath}/v1/contracts/approve" method="post" style="display:inline;">
                                                <input type="hidden" name="action" value="approve">
                                                <input type="hidden" name="contractId" value="${contract.contractId}">
                                                <button type="submit" class="btn btn-sm btn-outline-success me-1" onclick="return confirm('Bạn có chắc muốn duyệt hợp đồng này?')">
                                                    <i class="bi bi-check-circle"></i> Duyệt
                                                </button>
                                            </form>
                                            <form action="${pageContext.request.contextPath}/v1/contracts/approve" method="post" style="display:inline;">
                                                <input type="hidden" name="action" value="reject">
                                                <input type="hidden" name="contractId" value="${contract.contractId}">
                                                <button type="submit" class="btn btn-sm btn-outline-danger" onclick="return confirm('Bạn có chắc muốn từ chối hợp đồng này?')">
                                                    <i class="bi bi-x-circle"></i> Từ chối
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty pendingContracts}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">Không có hợp đồng nào chờ duyệt.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
</body>
</html>
