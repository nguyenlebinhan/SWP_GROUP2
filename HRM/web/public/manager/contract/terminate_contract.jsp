<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Xác nhận chấm dứt hợp đồng - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
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
                max-width: 980px;
                margin: 0 auto;
                background: #fff;
                border-radius: 16px;
                box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
                padding: 28px;
            }
            .detail-grid {
                display: grid;
                grid-template-columns: repeat(2, minmax(0, 1fr));
                gap: 16px;
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
            .section-title {
                font-size: 18px;
                font-weight: 700;
                margin: 20px 0 12px;
            }
            .terminate-warning {
                background: #fff3cd;
                border: 1px solid #ffc107;
                border-radius: 12px;
                padding: 16px;
                color: #664d03;
            }
            @media (max-width: 768px) {
                .main {
                    margin-left: 0;
                    padding: 16px;
                }
                .detail-grid {
                    grid-template-columns: 1fr;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/managerSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Xác nhận chấm dứt hợp đồng" />
                <jsp:param name="backUrl" value="/v1/manager/contract/terminate" />
            </jsp:include>

            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show mx-auto mb-3" style="max-width:980px;" role="alert">
                    <i class="fa-solid fa-circle-exclamation me-1"></i>${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <div class="page-card">
                <div class="terminate-warning mb-4">
                    <i class="fa-solid fa-triangle-exclamation me-2"></i>
                    <strong>Cảnh báo:</strong> Hành động này sẽ chấm dứt hợp đồng ngay lập tức. Dữ liệu sẽ được ghi nhận trong lịch sử và không thể hoàn tác.
                    <c:if test="${contract.status == 'PENDING_ACTIVATION'}">
                        <br/><span class="mt-2 d-inline-block"><i class="fa-regular fa-clock me-1"></i>Hợp đồng đang chờ kích hoạt. Ngày chấm dứt phải <strong>trước hoặc bằng</strong> ngày hiệu lực.</span>
                    </c:if>
                    <c:if test="${contract.status == 'ACTIVE'}">
                        <br/><span class="mt-2 d-inline-block"><i class="fa-regular fa-clock me-1"></i>Hợp đồng đang hiệu lực. Ngày chấm dứt phải <strong>từ ngày hiệu lực trở đi</strong>${not empty contract.endDate ? ' và không sau ngày kết thúc.' : '.'}</span>
                    </c:if>
                </div>

                <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                    <div>
                        <h1 class="h3 mb-1">Hợp đồng ${empty contract.contractCode ? contract.contractId : contract.contractCode}</h1>
                        <div class="text-muted">${employee.fullName} (${employee.employeeCode})</div>
                    </div>
                    <span class="badge" style="font-size:15px;">
                        <c:choose>
                            <c:when test="${contract.status == 'PENDING_APPROVAL'}"><span style="background:#fef3c7;color:#92400e;border:1px solid #fbbf24;padding:4px 14px;border-radius:999px;">Chờ duyệt</span></c:when>
                            <c:when test="${contract.status == 'PENDING_ACTIVATION'}"><span style="background:#ede9fe;color:#5b21b6;border:1px solid #a78bfa;padding:4px 14px;border-radius:999px;">Chờ hiệu lực</span></c:when>
                            <c:when test="${contract.status == 'ACTIVE'}"><span style="background:#d1fae5;color:#065f46;border:1px solid #34d399;padding:4px 14px;border-radius:999px;">Đang hiệu lực</span></c:when>
                            <c:when test="${contract.status == 'EXPIRED'}"><span style="background:#f1f5f9;color:#475569;border:1px solid #94a3b8;padding:4px 14px;border-radius:999px;">Đã hết hạn</span></c:when>
                            <c:when test="${contract.status == 'TERMINATED'}"><span style="background:#ffe4e6;color:#9f1239;border:1px solid #fb7185;padding:4px 14px;border-radius:999px;">Đã chấm dứt</span></c:when>
                            <c:when test="${contract.status == 'CANCELLED'}"><span style="background:#f8fafc;color:#334155;border:1px solid #cbd5e1;padding:4px 14px;border-radius:999px;">Đã hủy</span></c:when>
                            <c:when test="${contract.status == 'REJECTED'}"><span style="background:#fef2f2;color:#991b1b;border:1px solid #fca5a5;padding:4px 14px;border-radius:999px;">Bị từ chối</span></c:when>
                            <c:otherwise><span class="badge text-bg-secondary">${contract.status}</span></c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <div class="section-title">Thông tin hợp đồng</div>
                <div class="detail-grid">
                    <div class="detail-item">
                        <span class="detail-label">Loại hợp đồng</span>
                        <div class="detail-value">${contract.contractType.displayName}</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Lương</span>
                        <div class="detail-value"><fmt:formatNumber value="${contract.salary}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Ngày hiệu lực</span>
                        <div class="detail-value"><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Ngày kết thúc</span>
                        <div class="detail-value">
                            <c:choose>
                                <c:when test="${empty contract.endDate}">Không xác định thời hạn</c:when>
                                <c:otherwise><fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy"/></c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <div class="section-title">Thông tin nhân viên</div>
                <div class="detail-grid">
                    <div class="detail-item">
                        <span class="detail-label">Họ tên</span>
                        <div class="detail-value">${employee.fullName}</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Mã nhân viên</span>
                        <div class="detail-value">${employee.employeeCode}</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Phòng ban</span>
                        <div class="detail-value">${empty employee.departmentName ? 'Chưa cập nhật' : employee.departmentName}</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Chức danh</span>
                        <div class="detail-value">${empty employee.positionName ? 'Chưa cập nhật' : employee.positionName}</div>
                    </div>
                </div>

                <div class="section-title">Thông tin chấm dứt</div>
                <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/terminate">
                    <input type="hidden" name="contractId" value="${contract.contractId}">

                    <div class="detail-grid">
                        <div class="detail-item">
                            <span class="detail-label">Ngày chấm dứt <span class="text-danger">*</span></span>
                            <input type="date" class="form-control" name="terminationDate"
                                   value="${not empty terminationDate ? terminationDate : ''}"
                                   max="<fmt:formatDate value="<%= new java.sql.Date(System.currentTimeMillis()) %>" pattern="yyyy-MM-dd"/>"
                                   required>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Ngày hiệu lực</span>
                            <div class="detail-value"><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></div>
                        </div>
                        <c:if test="${not empty contract.endDate}">
                            <div class="detail-item">
                                <span class="detail-label">Ngày kết thúc hợp đồng</span>
                                <div class="detail-value"><fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy"/></div>
                            </div>
                        </c:if>
                        <div class="detail-item" style="grid-column: 1 / -1;">
                            <span class="detail-label">Lý do chấm dứt <span class="text-danger">*</span></span>
                            <textarea class="form-control" name="terminationReason" rows="4"
                                      placeholder="Nhập lý do chấm dứt hợp đồng..." required>${not empty terminationReason ? terminationReason : ''}</textarea>
                        </div>
                    </div>

                    <div class="d-flex justify-content-between gap-3 mt-4">
                        <a href="${pageContext.request.contextPath}/v1/manager/contract/terminate" class="btn btn-outline-secondary">
                            <i class="fa-solid fa-arrow-left me-1"></i>Quay lại danh sách
                        </a>
                        <button type="submit" class="btn btn-danger" onclick="return confirm('Xác nhận chấm dứt hợp đồng này? Hành động này không thể hoàn tác.');">
                            <i class="fa-solid fa-stop me-1"></i>Chấm dứt hợp đồng
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
