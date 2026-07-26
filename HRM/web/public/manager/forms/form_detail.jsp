<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Chi tiết Đơn yêu cầu - HRM</title>
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
        .status-0 { background: #fef3c7; color: #92400e; }
        .status-1 { background: #d1fae5; color: #065f46; }
        .status-2 { background: #fee2e2; color: #991b1b; }
        .status-3 { background: #d1fae5; color: #065f46; }
        .status-4 { background: #d1fae5; color: #065f46; }
        .info-label {
            font-size: 0.85rem;
            color: #6c757d;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 4px;
        }
        .info-value {
            font-size: 1rem;
            color: #212529;
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
        .approver-box {
            background: #f8f9fa;
            border-left: 4px solid #198754;
            padding: 16px;
            border-radius: 4px;
            white-space: pre-wrap;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Chi tiết Đơn yêu cầu" />
        <jsp:param name="backUrl" value="/v1/manager/forms/dept-forms" />
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
        <div>
            <h4 class="mb-1"><i class="fa-solid fa-file-invoice me-2"></i>Chi tiết Đơn #${form.formCode}</h4>
            <p class="text-muted mb-0">Loại đơn: <strong>${form.formTypeName}</strong></p>
        </div>
    </div>

    <div class="row">
        <div class="col-md-4">
            <div class="section-card h-100">
                <h5 class="border-bottom pb-2 mb-3">Thông tin người gửi</h5>

                <div class="info-label">Nhân viên</div>
                <div class="info-value">
                    <strong>${form.fullName}</strong> (${form.employeeCode})
                </div>

                <div class="info-label">Phòng ban</div>
                <div class="info-value">
                    <c:choose>
                        <c:when test="${not empty form.departmentName}">${form.departmentName}</c:when>
                        <c:otherwise><span class="text-muted">Chưa phân bổ</span></c:otherwise>
                    </c:choose>
                </div>

                <div class="info-label">Ngày gửi</div>
                <div class="info-value">
                    <fmt:formatDate value="${form.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                </div>

                <div class="info-label">Trạng thái</div>
                <div class="info-value mt-2">
                    <span class="badge-status status-${form.status}">${form.statusLabel}</span>
                </div>
            </div>
        </div>

        <!-- Nội dung đơn -->
        <div class="col-md-8">
            <div class="section-card h-100">
                <h5 class="border-bottom pb-2 mb-3">Nội dung chi tiết</h5>

                <div class="info-label">Lý do / Nội dung đơn</div>
                <div class="info-value reason-box mt-2">${form.reason}</div>

                <c:if test="${form.formTypeCode eq 'LEAVE'}">
                    <div class="row mt-3">
                        <div class="col-md-4">
                            <div class="info-label">Ngày bắt đầu</div>
                            <div class="info-value"><fmt:formatDate value="${form.startDate}" pattern="dd/MM/yyyy" /></div>
                        </div>
                        <div class="col-md-4">
                            <div class="info-label">Ngày kết thúc</div>
                            <div class="info-value"><fmt:formatDate value="${form.endDate}" pattern="dd/MM/yyyy" /></div>
                        </div>
                        <div class="col-md-4">
                            <div class="info-label">Số ngày muốn nghỉ</div>
                            <div class="info-value">${form.totalDays} ngày</div>
                        </div>
                    </div>
                </c:if>
                <c:if test="${form.formTypeCode eq 'TRANSFER'}">
                    <div class="row mt-3">
                        <div class="col-md-6">
                            <div class="info-label">Phòng ban muốn chuyển đến</div>
                            <div class="info-value text-primary fw-bold">${form.targetDepartmentName}</div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-label">Vai trò (Role) mới</div>
                            <div class="info-value text-primary fw-bold">${not empty form.targetRoleName ? form.targetRoleName : 'Chưa xác định'}</div>
                        </div>
                    </div>
                </c:if>
                <c:if test="${form.formTypeCode eq 'PROMOTION_DEMOTION'}">
                    <div class="row mt-3">
                        <div class="col-md-6">
                            <div class="info-label">Vai trò (Role) đề xuất</div>
                            <div class="info-value text-primary fw-bold">${form.targetRoleName}</div>
                        </div>
                    </div>
                </c:if>
                <c:if test="${form.formTypeCode eq 'COMPLAINT'}">
                    <div class="row mt-3">
                        <div class="col-md-4">
                            <div class="info-label">Ngày làm việc thực tế</div>
                            <div class="info-value"><fmt:formatDate value="${form.startDate}" pattern="dd/MM/yyyy" /></div>
                        </div>
                        <div class="col-md-4">
                            <div class="info-label">Giờ bắt đầu thực tế</div>
                            <div class="info-value"><fmt:formatDate value="${form.startTime}" pattern="HH:mm" /></div>
                        </div>
                        <div class="col-md-4">
                            <div class="info-label">Giờ kết thúc thực tế</div>
                            <div class="info-value"><fmt:formatDate value="${form.endTime}" pattern="HH:mm" /></div>
                        </div>
                    </div>
                </c:if>

                <div class="info-label mt-4">Tài liệu đính kèm</div>
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

                <c:if test="${form.status == 1 || form.status == 2 || form.status == 3 || form.status == 4}">
                    <hr class="my-4">
                    <h5 class="text-success"><i class="fa-solid fa-clipboard-check me-2"></i>Kết quả xử lý</h5>

                    <div class="row mt-3">
                        <div class="col-md-6">
                            <div class="info-label">Người duyệt</div>
                            <div class="info-value">
                                <c:choose>
                                    <c:when test="${not empty form.approverName}">${form.approverName}</c:when>
                                    <c:otherwise>Hệ thống</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-label">Ngày duyệt</div>
                            <div class="info-value">
                                <c:choose>
                                    <c:when test="${not empty form.approvedAt}">
                                        <fmt:formatDate value="${form.approvedAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </c:when>
                                    <c:otherwise>-</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <div class="info-label">Ghi chú của người duyệt</div>
                    <div class="info-value approver-box mt-2">
                        <c:choose>
                            <c:when test="${not empty form.approverNote}">${form.approverNote}</c:when>
                            <c:otherwise><span class="text-muted">Không có lời nhắn</span></c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <%-- Khu vực duyệt/từ chối: chỉ trưởng phòng của phòng ban này, đơn đang chờ duyệt --%>
                <c:if test="${form.status == 0 and canApprove and form.formTypeCode ne 'DEPENDENT'}">
                    <hr class="my-4">
                    <h5 class="mb-3"><i class="fa-solid fa-gavel me-2"></i>Xử lý đơn</h5>
                    <form method="post">
                        <input type="hidden" name="formId" value="${form.formId}">
                        <div class="mb-3 mt-3">
                            <label for="note" class="form-label">Ghi chú (tuỳ chọn)</label>
                            <textarea id="note" name="note" class="form-control" rows="3"
                                      placeholder="Nhập lý do duyệt hoặc từ chối..."></textarea>
                        </div>
                        <div class="d-flex justify-content-end gap-2">
                            <button type="submit" formaction="${pageContext.request.contextPath}/v1/manager/forms/reject" class="btn btn-danger px-4">
                                <i class="fa-solid fa-xmark me-1"></i> Từ chối
                            </button>
                            <button type="submit" formaction="${pageContext.request.contextPath}/v1/manager/forms/approve" class="btn btn-success px-4">
                                <i class="fa-solid fa-check me-1"></i> Duyệt
                            </button>
                        </div>
                    </form>
                </c:if>

                <%-- Khu vực duyệt lần 2 (HR): chỉ áp dụng cho đơn Khiếu nại đã được Manager duyệt (status = 1) --%>
                <c:if test="${((form.status == 1 and form.formTypeCode eq 'COMPLAINT') or (form.status == 0 and form.formTypeCode eq 'DEPENDENT' and not isMyForm)) and isHrStaff}">
                    <hr class="my-4">
                    <h5 class="mb-3 text-primary"><i class="fa-solid fa-user-tie me-2"></i>Duyệt (HR)</h5>
                    <form method="post">
                        <input type="hidden" name="formId" value="${form.formId}">
                        <div class="mb-3 mt-3">
                            <label for="hrNote" class="form-label">Ghi chú của HR (tuỳ chọn)</label>
                            <textarea id="hrNote" name="note" class="form-control" rows="3"
                                      placeholder="Nhập lý do duyệt hoặc từ chối..."></textarea>
                        </div>
                        <div class="d-flex justify-content-end gap-2">
                            <button type="submit" formaction="${pageContext.request.contextPath}/v1/manager/forms/hr-reject" class="btn btn-danger px-4">
                                <i class="fa-solid fa-xmark me-1"></i> Từ chối (HR)
                            </button>
                            <button type="submit" formaction="${pageContext.request.contextPath}/v1/manager/forms/hr-approve" class="btn btn-primary px-4">
                                <i class="fa-solid fa-check-double me-1"></i> Hoàn tất Duyệt (HR)
                            </button>
                        </div>
                    </form>
                </c:if>
                <c:if test="${form.status == 0 and form.formTypeCode eq 'DEPENDENT' and isMyForm and isHrStaff}">
                    <hr class="my-4">
                    <div class="alert alert-warning mb-0">
                        <i class="fa-solid fa-circle-info me-2"></i>
                        Đây là đơn của chính bạn nên bạn không thể tự duyệt. Cần một nhân viên HR khác xử lý đơn này.
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
