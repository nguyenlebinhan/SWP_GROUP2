<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Chi tiết hợp đồng - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
    <style>
        body { background: #f5f6fa; font-family: "Segoe UI", sans-serif; }
        .main { margin-left: 250px; padding: 24px; }
        .page-card { max-width: 980px; margin: 0 auto; background: #fff; border-radius: 16px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08); padding: 28px; }
        .detail-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
        .detail-item { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px 16px; }
        .detail-label { display: block; font-size: 12px; font-weight: 700; color: #64748b; text-transform: uppercase; margin-bottom: 6px; }
        .detail-value { font-size: 15px; color: #0f172a; }
        .note-box { min-height: 100px; white-space: pre-line; }
        .toolbar { max-width: 980px; margin: 0 auto 16px; display: flex; justify-content: space-between; gap: 12px; align-items: center; }
        .section-title { font-size: 18px; font-weight: 700; margin: 20px 0 12px; }
        @media (max-width: 768px) {
            .main { margin-left: 0; padding: 16px; }
            .detail-grid { grid-template-columns: 1fr; }
            .toolbar { flex-direction: column; align-items: stretch; }
        }
    </style>
</head>
<body>
<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Chi tiết hợp đồng" />
        <jsp:param name="backUrl" value="${backUrl}" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show mx-auto mb-3" style="max-width:980px;" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show mx-auto mb-3" style="max-width:980px;" role="alert">
            ${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session"/>
    </c:if>

    <div class="toolbar">
        <a href="${pageContext.request.contextPath}${backUrl}" class="btn btn-outline-secondary">
            <i class="fa-solid fa-arrow-left me-1"></i>Quay lại
        </a>
        <div class="d-flex flex-wrap gap-2">
            <c:if test="${canApproveContract and contract.status == 'PENDING_APPROVAL'}">
                <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/approve">
                    <input type="hidden" name="contractId" value="${contract.contractId}">
                    <button type="submit" class="btn btn-success">
                        <i class="fa-solid fa-check me-1"></i>Phê duyệt
                    </button>
                </form>
            </c:if>
            <c:if test="${canRejectContract and contract.status == 'PENDING_APPROVAL'}">
                <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#rejectModal">
                    <i class="fa-solid fa-xmark me-1"></i>Từ chối
                </button>
            </c:if>
            <c:if test="${canAddEmploymentContract and contract.status == 'PENDING_APPROVAL'}">
                <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/cancel">
                    <input type="hidden" name="contractId" value="${contract.contractId}">
                    <button type="submit" class="btn btn-outline-warning">
                        <i class="fa-solid fa-ban me-1"></i>Hủy
                    </button>
                </form>
            </c:if>
            <c:if test="${canTerminateContract and contract.status == 'ACTIVE'}">
                <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/terminate">
                    <input type="hidden" name="contractId" value="${contract.contractId}">
                    <input type="hidden" name="note" value="Terminated by HR Manager">
                    <button type="submit" class="btn btn-outline-dark">
                        <i class="fa-solid fa-stop me-1"></i>Chấm dứt
                    </button>
                </form>
            </c:if>
        </div>
    </div>

    <div class="page-card">
        <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
            <div>
                <h1 class="h3 mb-1">Hợp đồng ${empty contract.contractCode ? contract.contractId : contract.contractCode}</h1>
                <div class="text-muted">${employee.fullName} (${employee.employeeCode})</div>
            </div>
            <span class="badge text-bg-secondary fs-6">${contract.status}</span>
        </div>

        <div class="section-title">Thông tin hợp đồng</div>
        <div class="detail-grid">
            <div class="detail-item">
                <span class="detail-label">Loại hợp đồng</span>
                <div class="detail-value">${contract.contractType}</div>
            </div>
            <div class="detail-item">
                <span class="detail-label">Lương</span>
                <div class="detail-value"><fmt:formatNumber value="${contract.salary}" type="number" groupingUsed="true"/> VND</div>
            </div>
            <div class="detail-item">
                <span class="detail-label">Ngày ký</span>
                <div class="detail-value">
                    <c:choose>
                        <c:when test="${not empty contract.signedDate}">
                            <fmt:formatDate value="${contract.signedDate}" pattern="dd/MM/yyyy"/>
                        </c:when>
                        <c:otherwise>Chưa ký</c:otherwise>
                    </c:choose>
                </div>
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
            <div class="detail-item">
                <span class="detail-label">Hợp đồng trước đó</span>
                <div class="detail-value">
                    <c:choose>
                        <c:when test="${not empty contract.previousContractId}">
                            <a href="${pageContext.request.contextPath}/v1/manager/contract/detail?contractId=${contract.previousContractId}">
                                #${contract.previousContractId}
                            </a>
                        </c:when>
                        <c:otherwise>Không có</c:otherwise>
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

        <div class="section-title">Ghi chú</div>
        <div class="detail-item note-box">
            <c:out value="${empty contract.note ? 'Không có ghi chú.' : contract.note}"/>
        </div>
    </div>
</div>

<div class="modal fade" id="rejectModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/reject">
                <div class="modal-header">
                    <h5 class="modal-title">Từ chối hợp đồng</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="contractId" value="${contract.contractId}">
                    <label class="form-label">Lý do từ chối</label>
                    <textarea class="form-control" name="rejectReason" rows="4" required></textarea>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Đóng</button>
                    <button type="submit" class="btn btn-danger">Xác nhận từ chối</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
