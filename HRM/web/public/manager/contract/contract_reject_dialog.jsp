<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Từ chối Hợp đồng - HRM</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <style>
        body { background-color: #f5f6fa; font-family: 'Be Vietnam Pro', sans-serif; }
        .modal-content { border-radius: 1rem; }
        .modal-header { border-bottom: none; }
        .modal-footer { border-top: none; }
    </style>
</head>
<body>
<div class="container">
    <div class="row justify-content-center align-items-center" style="min-height: 100vh;">
        <div class="col-md-6">
            <div class="card shadow-sm">
                <div class="card-header bg-danger text-white rounded-top">
                    <h5 class="mb-0"><i class="bi bi-x-circle"></i> Từ chối Hợp đồng</h5>
                </div>
                <div class="card-body p-4">
                    <c:if test="${not empty contract}">
                        <div class="alert alert-warning">
                            <strong>Mã HD:</strong> ${contract.contractCode} | <strong>Nhân viên:</strong> ${contract.employee.fullName}
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/v1/manager/contract/reject" method="post">
                        <input type="hidden" name="action" value="reject">
                        <input type="hidden" name="contractId" value="${contract.contractId}">

                        <div class="mb-4">
                            <label for="rejectionReason" class="form-label fw-medium">Lý do từ chối <span class="text-danger">*</span></label>
                            <textarea class="form-control" id="rejectionReason" name="rejectionReason" rows="5" required placeholder="Nhập lý do từ chối hợp đồng..."></textarea>
                            <div class="form-text">Lý do này sẽ được lưu vào nhật ký kiểm toán (Audit Log).</div>
                        </div>

                        <div class="d-flex justify-content-end gap-2">
                            <a href="${pageContext.request.contextPath}/v1/manager/contract/pending" class="btn btn-outline-secondary">
                                <i class="bi bi-arrow-left"></i> Quay lại
                            </a>
                            <button type="submit" class="btn btn-danger">
                                <i class="bi bi-x-circle"></i> Xác nhận Từ chối
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
</body>
</html>
