<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Gia hạn hợp đồng - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
        <style>
            body {
                background: #f5f6fa;
                font-family: "Segoe UI", sans-serif;
            }
            .main {
                margin-left: 250px;
                padding: 24px;
            }
            .page-card {
                background: #fff;
                border-radius: 16px;
                box-shadow: 0 8px 24px rgba(15,23,42,0.08);
                padding: 28px;
            }
            .detail-item {
                background: #f8fafc;
                border: 1px solid #e2e8f0;
                border-radius: 12px;
                padding: 14px 16px;
            }
            .detail-label {
                display: block;
                font-size: 12px;
                font-weight: 700;
                color: #64748b;
                text-transform: uppercase;
                margin-bottom: 6px;
            }
            .detail-value {
                font-size: 15px;
                color: #0f172a;
            }
            @media (max-width: 768px) {
                .main {
                    margin-left: 0;
                    padding: 16px;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/managerSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Gia hạn hợp đồng" />
                <jsp:param name="backUrl" value="/v1/manager/contract/detail?contractId=${oldContract.contractId}" />
            </jsp:include>

            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                    ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <div class="page-card">
                <h3>Thông tin hợp đồng cũ</h3>
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <div class="detail-item">
                            <span class="detail-label">Mã hợp đồng</span>
                            <div class="detail-value">${oldContract.contractCode}</div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="detail-item">
                            <span class="detail-label">Nhân viên</span>
                            <div class="detail-value">${employee.fullName} (${employee.employeeCode})</div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="detail-item">
                            <span class="detail-label">Loại hợp đồng</span>
                            <div class="detail-value">${oldContract.contractType.displayName}</div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="detail-item">
                            <span class="detail-label">Lương hiện tại</span>
                            <div class="detail-value"><fmt:formatNumber value="${oldContract.salary}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND</div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="detail-item">
                            <span class="detail-label">Ngày hết hạn</span>
                            <div class="detail-value"><fmt:formatDate value="${oldContract.endDate}" pattern="dd/MM/yyyy"/></div>
                        </div>
                    </div>
                </div>

                <h3>Thông tin gia hạn</h3>
                <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/renewal">
                    <input type="hidden" name="contractId" value="${oldContract.contractId}">

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Ngày hiệu lực mới</label>
                            <input type="text" class="form-control" value="<fmt:formatDate value="${newEffectiveDate}" pattern="dd/MM/yyyy"/>" readonly disabled>
                            <small class="text-muted">Tự động: ngày tiếp theo sau khi hợp đồng cũ hết hạn</small>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Ngày hết hạn mới <span class="text-danger">*</span></label>
                            <input type="date" name="newEndDate" class="form-control" required>
                            <small class="text-muted">Phải sau ngày hiệu lực mới</small>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Lương mới <span class="text-danger">*</span></label>
                            <input type="number" name="newSalary" class="form-control" min="0" step="1000"
                                   value="${oldContract.salary}" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label">Ghi chú</label>
                            <textarea name="note" class="form-control" rows="3">Gia hạn từ hợp đồng ${oldContract.contractCode}</textarea>
                        </div>
                    </div>

                    <div class="d-flex gap-2 mt-4">
                        <button type="submit" class="btn btn-primary">
                            <i class="fa-solid fa-file-contract me-1"></i>Gửi duyệt gia hạn
                        </button>
                        <a href="${pageContext.request.contextPath}/v1/manager/contract/detail?contractId=${oldContract.contractId}"
                           class="btn btn-outline-secondary">Hủy</a>
                    </div>
                </form>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>