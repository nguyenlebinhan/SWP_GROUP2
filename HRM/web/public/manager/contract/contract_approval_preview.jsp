<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xem trước Hợp đồng - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background: #f5f6fa;
            font-family: 'Segoe UI', sans-serif;
        }
        .main {
            margin-left: 250px;
            padding: 24px;
        }

        /* --- Summary Banner --- */
        .summary-banner {
            background: linear-gradient(135deg, #4361ee 0%, #3730a3 100%);
            color: #fff;
            border-radius: 1rem;
            padding: 1.5rem 2rem;
        }
        .summary-banner .banner-label {
            font-size: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            color: rgba(255,255,255,0.7);
            font-weight: 500;
        }
        .summary-banner .banner-value {
            font-size: 1.25rem;
            font-weight: 700;
        }
        .summary-banner .badge {
            font-size: 0.85rem;
            padding: 0.45em 0.85em;
            border-radius: 0.5rem;
        }

        /* --- Cards --- */
        .card {
            border: none;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            border-radius: 1rem;
        }
        .card-header {
            background: #fff;
            border-bottom: 1px solid #eef2f7;
            border-radius: 1rem 1rem 0 0 !important;
            padding: 1rem 1.25rem;
        }
        .card-header h5 {
            margin: 0;
            font-size: 1rem;
            font-weight: 600;
            color: #1a1a2e;
        }
        .card-header i {
            color: #4361ee;
        }
        .card-body {
            padding: 1.25rem;
        }

        .info-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.55rem 0;
            border-bottom: 1px solid #f1f3f5;
        }
        .info-row:last-of-type {
            border-bottom: none;
        }
        .info-label {
            color: #6b7280;
            font-size: 0.85rem;
            font-weight: 500;
        }
        .info-value {
            color: #1a1a2e;
            font-size: 0.9rem;
            text-align: right;
            max-width: 55%;
            word-break: break-word;
        }

        /* --- Semantic Badges --- */
        .badge-ct {
            padding: 0.35em 0.7em;
            border-radius: 0.5rem;
            font-size: 0.8rem;
            font-weight: 600;
        }
        .badge-ct-probation   { background: #dbeafe; color: #1e40af; }
        .badge-ct-internship  { background: #ede9fe; color: #5b21b6; }
        .badge-ct-fixed       { background: #d1fae5; color: #065f46; }
        .badge-ct-indefinite  { background: #fef3c7; color: #92400e; }
        .badge-ct-other       { background: #f3f4f6; color: #374151; }

        .badge-st {
            padding: 0.35em 0.7em;
            border-radius: 0.5rem;
            font-size: 0.8rem;
            font-weight: 600;
        }
        .badge-st-default            { background: #f3f4f6; color: #4b5563; }
        .badge-st-PENDING_APPROVAL   { background: #fef3c7; color: #92400e; }
        .badge-st-PENDING_ACTIVATION { background: #dbeafe; color: #1e40af; }
        .badge-st-ACTIVE             { background: #d1fae5; color: #065f46; }
        .badge-st-EXPIRED            { background: #fee2e2; color: #991b1b; }
        .badge-st-TERMINATED         { background: #e5e7eb; color: #374151; }
        .badge-st-CANCELLED          { background: #fee2e2; color: #991b1b; }
        .badge-st-DRAFT              { background: #f3f4f6; color: #4b5563; }

        /* --- Action Panel --- */
        .action-panel {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            gap: 0.75rem;
            padding: 1rem 1.25rem;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Xem trước Hợp đồng" />
        <jsp:param name="backUrl" value="/v1/manager/contract/pending"/>
    </jsp:include>

    <!-- Flash Messages -->
    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session" />
    </c:if>

<c:choose>
    <c:when test="${contract != null}">

    <div class="container-fluid px-0">

        <div class="row mb-4">
            <div class="col-12">
                <div class="summary-banner">
                    <div class="row align-items-center g-3">
                        <div class="col-md-4">
                            <div class="banner-label">Mã hợp đồng</div>
                            <div class="banner-value">
                                <c:choose>
                                    <c:when test="${not empty contract.contractCode}">${contract.contractCode}</c:when>
                                    <c:otherwise>HD-${contract.contractId}</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="banner-label">Loại hợp đồng</div>
                            <div class="banner-value">
                                <c:choose>
                                    <c:when test="${contract.contractType == 'PROBATION'}"><span class="badge badge-ct-probation">Probation</span></c:when>
                                    <c:when test="${contract.contractType == 'INTERNSHIP'}"><span class="badge badge-ct-internship">Internship</span></c:when>
                                    <c:when test="${contract.contractType == 'FIXED_TERM'}"><span class="badge badge-ct-fixed">Fixed-term</span></c:when>
                                    <c:when test="${contract.contractType == 'INDEFINITE'}"><span class="badge badge-ct-indefinite">Indefinite</span></c:when>
                                    <c:otherwise><span class="badge badge-ct-other">${contract.contractType}</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="col-md-4 text-md-end">
                            <div class="banner-label">Trạng thái</div>
                            <div class="banner-value">
                                <c:set var="sCls" value="badge-st-default" />
                                <c:if test="${contract.status == 'PENDING_APPROVAL'}"><c:set var="sCls" value="badge-st-PENDING_APPROVAL" /></c:if>
                                <c:if test="${contract.status == 'PENDING_ACTIVATION'}"><c:set var="sCls" value="badge-st-PENDING_ACTIVATION" /></c:if>
                                <c:if test="${contract.status == 'ACTIVE'}"><c:set var="sCls" value="badge-st-ACTIVE" /></c:if>
                                <c:if test="${contract.status == 'EXPIRED'}"><c:set var="sCls" value="badge-st-EXPIRED" /></c:if>
                                <c:if test="${contract.status == 'TERMINATED'}"><c:set var="sCls" value="badge-st-TERMINATED" /></c:if>
                                <c:if test="${contract.status == 'CANCELLED'}"><c:set var="sCls" value="badge-st-CANCELLED" /></c:if>
                                <c:if test="${contract.status == 'DRAFT'}"><c:set var="sCls" value="badge-st-DRAFT" /></c:if>
                                <span class="badge-st ${sCls}">${contract.status}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ============================================================ -->
        <!-- ROW 1: EMPLOYEE INFO   +   CONTRACT INFO                     -->
        <!-- ============================================================ -->
        <div class="row g-4 mb-4">

            <!-- Card 1: Employee Information -->
            <div class="col-xl-6 col-12">
                <div class="card h-100">
                    <div class="card-header">
                        <h5><i class="fa-solid fa-user me-2"></i>Thông tin Nhân viên</h5>
                    </div>
                    <div class="card-body">
                        <div class="info-row">
                            <span class="info-label">Họ và tên</span>
                            <span class="info-value fw-medium">${contract.employeeFullName != null ? contract.employeeFullName : 'N/A'}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Mã nhân viên</span>
                            <span class="info-value">${contract.employeeCode != null ? contract.employeeCode : 'N/A'}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Mã hợp đồng</span>
                            <span class="info-value fw-medium">
                                <c:choose>
                                    <c:when test="${not empty contract.contractCode}">${contract.contractCode}</c:when>
                                    <c:otherwise>HD-${contract.contractId}</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Loại hợp đồng</span>
                            <span class="info-value">
                                <c:choose>
                                    <c:when test="${contract.contractType == 'PROBATION'}"><span class="badge-ct badge-ct-probation">Probation</span></c:when>
                                    <c:when test="${contract.contractType == 'INTERNSHIP'}"><span class="badge-ct badge-ct-internship">Internship</span></c:when>
                                    <c:when test="${contract.contractType == 'FIXED_TERM'}"><span class="badge-ct badge-ct-fixed">Fixed-term</span></c:when>
                                    <c:when test="${contract.contractType == 'INDEFINITE'}"><span class="badge-ct badge-ct-indefinite">Indefinite</span></c:when>
                                    <c:otherwise><span class="badge-ct badge-ct-other">${contract.contractType}</span></c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Người tạo</span>
                            <span class="info-value">
                                <c:choose>
                                    <c:when test="${not empty contract.createdByName}">
                                        <c:out value="${contract.createdByName}" />
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted fst-italic">Unknown User</span>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Card 2: Contract Information -->
            <div class="col-xl-6 col-12">
                <div class="card h-100">
                    <div class="card-header">
                        <h5><i class="fa-solid fa-file-contract me-2"></i>Thông tin Hợp đồng</h5>
                    </div>
                    <div class="card-body">
                        <div class="info-row">
                            <span class="info-label">Lương</span>
                            <span class="info-value text-success fw-medium"><fmt:formatNumber value="${contract.salary}" pattern="#,##0"/> VND</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Ngày ký</span>
                            <span class="info-value">
                                <c:choose>
                                    <c:when test="${contract.signedDate != null}"><fmt:formatDate value="${contract.signedDate}" pattern="dd/MM/yyyy"/></c:when>
                                    <c:otherwise><span class="text-muted fst-italic">Chưa ký</span></c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Ngày hiệu lực</span>
                            <span class="info-value"><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Ngày kết thúc</span>
                            <span class="info-value">
                                <c:choose>
                                    <c:when test="${contract.endDate != null}"><fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy"/></c:when>
                                    <c:otherwise><span class="text-muted">Vô thời hạn</span></c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Trạng thái</span>
                            <span class="info-value">
                                <c:set var="sCls" value="badge-st-default" />
                                <c:if test="${contract.status == 'PENDING_APPROVAL'}"><c:set var="sCls" value="badge-st-PENDING_APPROVAL" /></c:if>
                                <c:if test="${contract.status == 'PENDING_ACTIVATION'}"><c:set var="sCls" value="badge-st-PENDING_ACTIVATION" /></c:if>
                                <c:if test="${contract.status == 'ACTIVE'}"><c:set var="sCls" value="badge-st-ACTIVE" /></c:if>
                                <c:if test="${contract.status == 'EXPIRED'}"><c:set var="sCls" value="badge-st-EXPIRED" /></c:if>
                                <c:if test="${contract.status == 'TERMINATED'}"><c:set var="sCls" value="badge-st-TERMINATED" /></c:if>
                                <c:if test="${contract.status == 'CANCELLED'}"><c:set var="sCls" value="badge-st-CANCELLED" /></c:if>
                                <c:if test="${contract.status == 'DRAFT'}"><c:set var="sCls" value="badge-st-DRAFT" /></c:if>
                                <span class="badge-st ${sCls}">${contract.status}</span>
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Ghi chú</span>
                            <span class="info-value" style="max-width:55%;word-break:break-word">${contract.note != null && contract.note != '' ? contract.note : 'Không có'}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ============================================================ -->
        <!-- ROW 2: CONTRACT METADATA   +   ACTION PANEL                  -->
        <!-- ============================================================ -->
        <div class="row g-4 mb-4">

            <!-- Card 3: Contract Metadata -->
            <div class="col-xl-6 col-12">
                <div class="card h-100">
                    <div class="card-header">
                        <h5><i class="fa-solid fa-clock-rotate-left me-2"></i>Thông tin bổ sung</h5>
                    </div>
                    <div class="card-body">
                        <div class="info-row">
                            <span class="info-label">Hợp đồng trước</span>
                            <span class="info-value">
                                <c:choose>
                                    <c:when test="${contract.previousContractId != null}">
                                        <a href="${pageContext.request.contextPath}/v1/manager/contract/history?contractId=${contract.previousContractId}" class="link-primary">#${contract.previousContractId}</a>
                                    </c:when>
                                    <c:otherwise><span class="text-muted">Không có</span></c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Lý do chấm dứt</span>
                            <span class="info-value">${contract.terminationReason != null && contract.terminationReason != '' ? contract.terminationReason : 'N/A'}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Ngày tạo</span>
                            <span class="info-value"><fmt:formatDate value="${contract.createdAt}" pattern="dd/MM/yyyy HH:mm"/></span>
                        </div>
                        <c:if test="${contract.updatedAt != null}">
                        <div class="info-row">
                            <span class="info-label">Cập nhật lần cuối</span>
                            <span class="info-value"><fmt:formatDate value="${contract.updatedAt}" pattern="dd/MM/yyyy HH:mm"/></span>
                        </div>
                        </c:if>
                    </div>
                </div>
            </div>

            <!-- Card 4: Action Panel -->
            <div class="col-xl-6 col-12 d-flex">
                <div class="card w-100" style="align-self:flex-end;">
                    <div class="card-header">
                        <h5><i class="fa-solid fa-gavel me-2"></i>Thao tác</h5>
                    </div>
                    <div class="action-panel">
                        <a href="${pageContext.request.contextPath}/v1/manager/contract/pending"
                           class="btn btn-outline-secondary" title="Quay lại danh sách chờ duyệt">
                            <i class="fa-solid fa-arrow-left me-1"></i> Quay lại
                        </a>
                        <c:if test="${contract.status.name() == 'PENDING_APPROVAL'}">
                            <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/approve" style="display:inline">
                                <input type="hidden" name="contractId" value="${contract.contractId}">
                                <button type="submit" class="btn btn-success px-4" title="Duyệt hợp đồng"
                                        onclick="return confirm('Bạn có chắc muốn duyệt hợp đồng này?')">
                                    <i class="fa-solid fa-check me-1"></i> Duyệt
                                </button>
                            </form>
                            <button type="button" class="btn btn-danger px-4" title="Từ chối hợp đồng"
                                    data-bs-toggle="modal" data-bs-target="#rejectModal">
                                <i class="fa-solid fa-xmark me-1"></i> Từ chối
                            </button>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

    </div> <!-- /container-fluid -->

    </c:when>
    <c:otherwise>
        <!-- Empty State -->
        <div class="card text-center py-5">
            <div class="card-body py-5">
                <i class="fa-regular fa-file-lines" style="font-size:48px;color:#cbd5e1;margin-bottom:16px"></i>
                <h6 class="text-muted">Không tìm thấy thông tin hợp đồng.</h6>
                <a href="${pageContext.request.contextPath}/v1/manager/contract/pending" class="btn btn-outline-secondary mt-3">
                    <i class="fa-solid fa-arrow-left me-1"></i> Quay lại danh sách
                </a>
            </div>
        </div>
    </c:otherwise>
</c:choose>

</div> <!-- /main -->

<!-- Reject Modal -->
<div class="modal fade" id="rejectModal" tabindex="-1" aria-labelledby="rejectModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="rejectModalLabel"><i class="fa-solid fa-xmark-circle text-danger me-2"></i>Từ chối Hợp đồng</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="${pageContext.request.contextPath}/v1/manager/contract/reject" method="post">
                <input type="hidden" name="action" value="reject">
                <input type="hidden" name="contractId" value="${contract.contractId}">
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="rejectionReason" class="form-label">Lý do từ chối:</label>
                        <textarea class="form-control" id="rejectionReason" name="reason" rows="3" required></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger px-4">
                        <i class="fa-solid fa-xmark me-1"></i> Xác nhận Từ chối
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>