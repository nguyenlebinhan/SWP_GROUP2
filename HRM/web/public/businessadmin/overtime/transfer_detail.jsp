<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết đơn Chuyển phòng ban - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
            margin-bottom: 24px;
        }
        .badge-status {
            padding: 6px 16px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 600;
        }
        .status-0 { background: #fef3c7; color: #92400e; border: 1px solid #fcd34d; }
        .status-1 { background: #d1fae5; color: #065f46; border: 1px solid #6ee7b7; }
        .status-2 { background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; }
        .status-3 { background: #e5e7eb; color: #374151; border: 1px solid #d1d5db; }
        .info-label {
            font-size: 0.85rem;
            color: #6c757d;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 4px;
        }
        .info-value {
            font-size: 1.05rem;
            color: #212529;
            font-weight: 500;
            margin-bottom: 16px;
            word-wrap: break-word;
        }
        .reason-box {
            background: #f8f9fa;
            border-left: 4px solid #0d6efd;
            padding: 16px;
            border-radius: 4px;
            white-space: pre-wrap;
        }
        .transfer-arrow { font-size: 1.4rem; color: #1565C0; }
    </style>
</head>
<body>

    <jsp:include page="/public/components/businessAdminSideBar.jsp" />

    <div class="main">
        <jsp:include page="/public/components/businessAdminTopBar.jsp">
            <jsp:param name="title" value="Chi tiết Đơn Chuyển phòng ban" />
            <jsp:param name="backUrl" value="/v1/businessadmin/forms"/>
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
            <h4 class="mb-0"><i class="fa-solid fa-right-left me-2 text-primary"></i>Chi tiết đơn chuyển phòng ban: <span class="fw-bold">${form.formCode}</span></h4>

        </div>

        <div class="row">
            <div class="col-lg-8">
                <!-- Thông tin chung -->
                <div class="section-card">
                    <div class="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
                        <h5 class="mb-0 text-primary"><i class="fa-solid fa-circle-info me-2"></i>Thông tin chung</h5>
                        <span class="badge-status status-${form.status}">${form.statusLabel}</span>
                    </div>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <div class="info-label">Người tạo đơn</div>
                            <div class="info-value"><strong>${form.fullName}</strong> (${form.employeeCode})</div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-label">Ngày tạo đơn</div>
                            <div class="info-value"><fmt:formatDate value="${form.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/></div>
                        </div>
                    </div>

                    <div class="row g-3 align-items-center mt-1">
                        <div class="col-md-5">
                            <div class="info-label">Phòng ban hiện tại</div>
                            <div class="info-value mb-0">
                                <c:choose>
                                    <c:when test="${not empty form.departmentName}">${form.departmentName}</c:when>
                                    <c:otherwise><span class="text-muted">Chưa phân bổ</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="col-md-2 text-center">
                            <i class="fa-solid fa-arrow-right-long transfer-arrow"></i>
                        </div>
                        <div class="col-md-5">
                            <div class="info-label">Phòng ban muốn chuyển đến</div>
                            <div class="info-value mb-0 text-primary fw-bold">
                                <c:choose>
                                    <c:when test="${not empty form.targetDepartmentName}">${form.targetDepartmentName}</c:when>
                                    <c:otherwise><span class="text-muted">Chưa xác định</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <div class="row g-3 mt-1">
                        <div class="col-md-6">
                            <div class="info-label">Vai trò (Role) mới</div>
                            <div class="info-value text-primary fw-bold">
                                <c:choose>
                                    <c:when test="${not empty form.targetRoleName}">${form.targetRoleName}</c:when>
                                    <c:otherwise><span class="text-muted">Giữ nguyên</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <div class="info-label mt-2">Lý do / Nội dung đơn</div>
                    <div class="info-value reason-box mt-1">
                        <c:choose>
                            <c:when test="${not empty form.reason}">${form.reason}</c:when>
                            <c:otherwise><span class="text-muted">Không có nội dung</span></c:otherwise>
                        </c:choose>
                    </div>

                    <div class="info-label mt-3">Tài liệu đính kèm</div>
                    <div class="info-value">
                        <c:choose>
                            <c:when test="${not empty form.attachmentName}">
                                <a href="${pageContext.request.contextPath}/${form.attachmentUrl}" target="_blank" class="btn btn-sm btn-outline-primary mt-1">
                                    <i class="fa-solid fa-paperclip me-1"></i> Tải về ${form.attachmentName}
                                </a>
                            </c:when>
                            <c:otherwise>
                                <span class="text-muted">Không có tài liệu đính kèm</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Thông tin phê duyệt -->
            <div class="col-lg-4">
                <div class="section-card">
                    <h5 class="mb-3 text-primary border-bottom pb-2"><i class="fa-solid fa-clipboard-check me-2"></i>Thông tin phê duyệt</h5>

                    <c:choose>
                        <c:when test="${form.status == 0}">
                            <div class="py-2">
                                <h6 class="text-muted mb-3">Xử lý đơn</h6>
                                <form id="processForm" method="POST">
                                    <input type="hidden" name="formId" value="${form.formId}">
                                    <div class="mb-3">
                                        <label class="form-label fw-semibold">Ghi chú (Note):</label>
                                        <textarea class="form-control" name="note" rows="3" placeholder="Nhập ghi chú hoặc lý do..."></textarea>
                                    </div>
                                    <div class="d-flex gap-2">
                                        <button type="submit" class="btn btn-success flex-fill fw-semibold" onclick="document.getElementById('processForm').action='${pageContext.request.contextPath}/v1/businessadmin/forms/approve';">
                                            <i class="fa-solid fa-check me-1"></i> Duyệt
                                        </button>
                                        <button type="submit" class="btn btn-danger flex-fill fw-semibold" onclick="document.getElementById('processForm').action='${pageContext.request.contextPath}/v1/businessadmin/forms/reject';">
                                            <i class="fa-solid fa-xmark me-1"></i> Từ chối
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="mb-3">
                                <div class="info-label">Người duyệt</div>
                                <div class="info-value">
                                    <c:choose>
                                        <c:when test="${not empty form.approverName}"><i class="fa-solid fa-user-check text-success me-2"></i>${form.approverName}</c:when>
                                        <c:otherwise><span class="text-muted">Business Admin</span></c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="mb-3">
                                <div class="info-label">Thời gian duyệt</div>
                                <div class="info-value">
                                    <c:choose>
                                        <c:when test="${not empty form.approvedAt}">
                                            <i class="fa-regular fa-clock me-2"></i><fmt:formatDate value="${form.approvedAt}" pattern="dd/MM/yyyy HH:mm:ss"/>
                                        </c:when>
                                        <c:otherwise><span class="text-muted">-</span></c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="mb-1">
                                <div class="info-label">Ghi chú của người duyệt</div>
                                <div class="p-3 bg-light rounded mt-1 border">
                                    <c:choose>
                                        <c:when test="${not empty form.approverNote}">${form.approverNote}</c:when>
                                        <c:otherwise><span class="text-muted fst-italic">Không có ghi chú</span></c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
