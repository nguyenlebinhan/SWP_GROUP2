<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Lịch Sử Hợp Đồng - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background: #f5f6fa;
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            }
            .main {
                margin-left: 250px;
                padding: 30px;
            }
            .timeline {
                position: relative;
                padding-left: 30px;
                border-left: 3px solid #e2e8f0;
            }
            .timeline::before {
                content: '';
                position: absolute;
                left: -8px;
                top: 0;
                bottom: 0;
                width: 16px;
                background: #fff;
                border: 3px solid #e2e8f0;
                border-radius: 50%;
                z-index: 1;
            }
            .timeline-item {
                position: relative;
                padding-bottom: 30px;
            }
            .timeline-item:last-child {
                padding-bottom: 0;
            }
            .timeline-marker {
                position: absolute;
                left: -38px;
                top: 4px;
                width: 22px;
                height: 22px;
                border-radius: 50%;
                border: 4px solid #fff;
                box-shadow: 0 0 0 3px #e2e8f0;
                z-index: 2;
            }
            .timeline-content {
                background: white;
                border-radius: 12px;
                box-shadow: 0 2px 12px rgba(0,0,0,0.06);
                padding: 20px 24px;
                transition: transform 0.2s, box-shadow 0.2s;
            }
            .timeline-content:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 24px rgba(0,0,0,0.1);
            }
            .badge-custom {
                font-size: 0.75rem;
                padding: 0.4em 0.8em;
            }
            .termination-alert {
                background: #fef2f2;
                border-left: 4px solid #ef4444;
                padding: 12px 16px;
                margin-top: 16px;
                border-radius: 0 8px 8px 0;
                font-style: italic;
                color: #991b1b;
                font-size: 0.9rem;
            }
            .meta-row {
                display: flex;
                gap: 24px;
                flex-wrap: wrap;
                margin-top: 12px;
                font-size: 0.85rem;
                color: #6b7280;
            }
            .meta-item {
                display: flex;
                align-items: center;
                gap: 6px;
            }
            .meta-item i {
                color: #9ca3af;
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/employeeSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/employeeTopBar.jsp">
                <jsp:param name="title" value="Lịch Sử Hợp Đồng" />
                <jsp:param name="backUrl" value="/v1/employee/dashboard" />
            </jsp:include>

            <div class="container-fluid mt-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="mb-0">Lịch Sử Hợp Đồng Lao Động</h2>
                    <a href="${pageContext.request.contextPath}/v1/employee/contract/current" class="btn btn-outline-primary btn-sm">
                        <i class="fa-solid fa-file-contract me-1"></i> Xem Hợp Đồng Hiện Tại
                    </a>
                </div>

                <c:choose>
                    <c:when test="${empty auditLogs}">
                        <div class="alert alert-info shadow-sm" role="alert">
                            <i class="fa-solid fa-circle-info me-2"></i>
                            Không tìm thấy dữ liệu lịch sử thay đổi hợp đồng.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="timeline">
                            <c:forEach var="log" items="${auditLogs}" varStatus="loop">
                                <div class="timeline-item">
                                    <c:choose>
                                        <c:when test="${log.fieldName == 'status' or (log.newStatus == 'ACTIVE')}">
                                            <span class="timeline-marker" style="background: #22c55e;"></span>
                                        </c:when>
                                        <c:when test="${log.newStatus == 'PENDING_APPROVAL' or log.newStatus == 'PENDING_ACTIVATION'}">
                                            <span class="timeline-marker" style="background: #facc15;"></span>
                                        </c:when>
                                        <c:when test="${log.newStatus == 'EXPIRED'}">
                                            <span class="timeline-marker" style="background: #9ca3af;"></span>
                                        </c:when>
                                        <c:when test="${log.newStatus == 'TERMINATED'}">
                                            <span class="timeline-marker" style="background: #ef4444;"></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="timeline-marker" style="background: #6b7280;"></span>
                                        </c:otherwise>
                                    </c:choose>

                                    <div class="timeline-content">
                                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
                                            <div>
                                                <h5 class="mb-1">
                                                    <span class="fw-bold">#${log.contractId}</span>
                                                    <span class="text-muted small ms-2">
                                                        <fmt:formatDate value="${log.changeDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                    </span>
                                                </h5>
                                                <div class="text-muted small">Người thực hiện: ${log.changedByName}</div>
                                            </div>
                                        </div>

                                        <c:if test="${log.oldStatus == null and log.fieldName == null}">
                                            <div class="mb-2">
                                                <i class="fa-solid fa-plus-circle text-success me-1"></i>
                                                <strong>Tạo hợp đồng</strong> (trạng thái: ${log.newStatus})
                                            </div>
                                        </c:if>


                                        <c:if test="${log.fieldName == 'status' or (log.fieldName == null and log.oldStatus != null)}">
                                            <div class="mb-2">
                                                <i class="fa-solid fa-arrow-right-arrow-left text-primary me-1"></i>
                                                <strong>Thay đổi trạng thái:</strong>
                                                <c:if test="${not empty log.oldStatus}">
                                                    <span class="badge bg-secondary badge-custom">${log.oldStatus}</span>
                                                </c:if>
                                                <i class="fa-solid fa-arrow-right mx-1 text-muted"></i>
                                                <span class="badge bg-primary badge-custom">${log.newStatus}</span>
                                                <c:if test="${not empty log.actionReason}">
                                                    <div class="text-muted small mt-1">Lý do: ${log.actionReason}</div>
                                                </c:if>
                                            </div>
                                        </c:if>

                                        <c:if test="${log.fieldName != null and log.fieldName != 'status'}">
                                            <div class="mb-2">
                                                <i class="fa-solid fa-pen text-warning me-1"></i>
                                                <strong>Sửa ${log.fieldName}:</strong>
                                                <span class="text-danger text-decoration-line-through">${log.oldValue}</span>
                                                <i class="fa-solid fa-arrow-right mx-1 text-muted"></i>
                                                <span class="text-success fw-bold">${log.newValue}</span>
                                                <c:if test="${not empty log.actionReason}">
                                                    <div class="text-muted small mt-1">Lý do: ${log.actionReason}</div>
                                                </c:if>
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
