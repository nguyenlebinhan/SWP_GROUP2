<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xem trước Hợp đồng - HRM</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        body { background-color: #f5f6fa; font-family: 'Be Vietnam Pro', sans-serif; }
        .sidebar { min-height: 100vh; background: #0d1240; }
        .main-content { padding: 2rem; }
        .card-header { background-color: #fff; border-bottom: 1px solid #e0e0e0; }
        .detail-row { display: flex; align-items: center; margin-bottom: 0.75rem; }
        .detail-row strong { width: 150px; flex-shrink: 0; color: #555; }
        .detail-row span { flex-grow: 1; color: #333; }
        .status-badge { font-size: 0.85rem; padding: 0.4rem 0.9rem; border-radius: 999px; }
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
                    <li class="nav-item"><a href="${pageContext.request.contextPath}/v1/manager/contracts/pending" class="nav-link text-warning active">Hợp đồng chờ duyệt</a></li>
                </ul>
            </div>
        </div>

        <!-- Main Content -->
        <div class="col py-3 main-content">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="mb-0 fw-bold text-dark">Xem trước Hợp đồng</h2>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>

            <c:if test="${contract != null}">
                <div class="card shadow-sm mb-4">
                    <div class="card-header">
                        <h5 class="mb-0">Chi tiết Hợp đồng #${contract.contractCode}</h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="detail-row"><strong>Mã hợp đồng:</strong> <span>${contract.contractCode}</span></div>
                                <div class="detail-row"><strong>Nhân viên:</strong> <span>${contract.employee.fullName} (${contract.employee.employeeCode})</span></div>
                                <div class="detail-row"><strong>Loại hợp đồng:</strong> <span><span class="status-badge bg-info text-dark">${contract.contractType}</span></span></div>
                                <div class="detail-row"><strong>Ngày ký:</strong> <span><fmt:formatDate value="${contract.signedDate}" pattern="dd/MM/yyyy"/></span></div>
                                <div class="detail-row"><strong>Ngày hiệu lực:</strong> <span><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></span></div>
                                <div class="detail-row"><strong>Ngày kết thúc:</strong> 
                                    <span>
                                        <c:choose>
                                            <c:when test="${contract.endDate != null}">
                                                <fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">Vô thời hạn</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="detail-row"><strong>Lương:</strong> <span class="text-success fw-medium"><fmt:formatNumber value="${contract.salary}" pattern="#,##0"/> VNĐ</span></div>
                                <div class="detail-row"><strong>Trạng thái:</strong> <span><span class="status-badge bg-warning text-dark">${contract.status}</span></span></div>
                                <div class="detail-row"><strong>Ghi chú:</strong> <span>${contract.note != null && contract.note != '' ? contract.note : 'Không có'}</span></div>
                                <div class="detail-row"><strong>Hợp đồng trước:</strong> 
                                    <span>
                                        <c:choose>
                                            <c:when test="${contract.previousContractId != null}">
                                                <a href="${pageContext.request.contextPath}/v1/manager/contracts/history?contractId=${contract.previousContractId}" class="link-primary">#${contract.previousContractId}</a>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">Không có</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="detail-row"><strong>Lý do chấm dứt:</strong> <span>${contract.terminationReason != null && contract.terminationReason != '' ? contract.terminationReason : 'N/A'}</span></div>
                                <div class="detail-row"><strong>Tạo bởi:</strong> <span>${contract.createdBy}</span></div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="text-end mt-4">
                    <a href="${pageContext.request.contextPath}/v1/manager/contracts/pending" class="btn btn-secondary me-2">Quay lại danh sách</a>
                    <c:if test="${contract.status.name() == 'PENDING_APPROVAL'}">
                        <a href="${pageContext.request.contextPath}/contracts/approve?contractId=${contract.contractId}&action=approve" class="btn btn-success me-2" onclick="return confirm('Bạn có chắc muốn duyệt hợp đồng này?')">
                            <i class="bi bi-check-circle"></i> Duyệt
                        </a>
                        <a href="#" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#rejectModal">
                            <i class="bi bi-x-circle"></i> Từ chối
                        </a>
                    </c:if>
                </div>
            </c:if>
            <c:if test="${contract == null}">
                <div class="alert alert-warning text-center">Không tìm thấy thông tin hợp đồng.</div>
                 <div class="text-end mt-4">
                    <a href="${pageContext.request.contextPath}/v1/manager/contracts/pending" class="btn btn-secondary me-2">Quay lại danh sách</a>
                 </div>
            </c:if>
        </div>
    </div>
</div>

<!-- Reject Modal (Placeholder for contract-reject-dialog.jsp functionality) -->
<div class="modal fade" id="rejectModal" tabindex="-1" aria-labelledby="rejectModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="rejectModalLabel">Từ chối Hợp đồng</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="${pageContext.request.contextPath}/contracts/approve" method="post">
                <input type="hidden" name="action" value="reject">
                <input type="hidden" name="contractId" value="${contract.contractId}">
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="rejectionReason" class="form-label">Lý do từ chối:</label>
                        <textarea class="form-control" id="rejectionReason" name="rejectionReason" rows="3" required></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger">Xác nhận Từ chối</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
</body>
</html>