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
        .status-3 { background: #e5e7eb; color: #374151; }
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

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
        <jsp:param name="title" value="Chi tiết Đơn yêu cầu" />
        <jsp:param name="backUrl" value="${backUrl}" />
    </jsp:include>

    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="mb-1">Chi tiết Đơn #${form.formCode}</h4>
            <p class="text-muted mb-0">Loại đơn: <strong>${form.formTypeName}</strong></p>
        </div>
    </div>

    <div class="row">
        <!-- Thông tin nhân viên & trạng thái -->
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
                                Tải về ${form.attachmentName}
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted">Không có tài liệu đính kèm</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <c:if test="${form.status == 1 || form.status == 2 || form.status == 3 || form.status == 4}">
                    <hr class="my-4">
                    <h5 class="text-success">Kết quả xử lý</h5>
                    
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
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
