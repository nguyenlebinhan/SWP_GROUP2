<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html>
<head>
    <title>Chi tiết đơn Overtime - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background: #f5f6fa;
            font-family: 'Segoe UI', sans-serif;
        }
        .main {
            margin-left: 250px;
            padding: 25px;
        }
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
            margin-bottom: 4px;
        }
        .info-value {
            font-size: 1.05rem;
            color: #212529;
            font-weight: 500;
        }
        .employee-list-box {
            border: 1px solid #dee2e6;
            border-radius: 8px;
            overflow: hidden;
        }
        .table-custom {
            margin-bottom: 0;
        }
        .table-custom th {
            background-color: #f8f9fa;
            border-bottom: 2px solid #dee2e6;
            font-weight: 600;
            color: #495057;
        }
    </style>
</head>
<body>

    <jsp:include page="/public/components/businessAdminSideBar.jsp" />

    <div class="main">
        <jsp:include page="/public/components/businessAdminTopBar.jsp">
            <jsp:param name="title" value="Chi tiết Đơn Overtime" />
            <jsp:param name="backUrl" value="/v1/businessadmin/overtime/list"/>
        </jsp:include>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <h4 class="mb-0"><i class="fa-solid fa-file-invoice me-2 text-primary"></i>Chi tiết đơn OT: <span class="fw-bold">${otRequest.formCode}</span></h4>
        </div>

        <div class="row">
            <div class="col-lg-8">
                <!-- Thông tin đơn -->
                <div class="section-card">
                    <div class="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
                        <h5 class="mb-0 text-primary"><i class="fa-solid fa-circle-info me-2"></i>Thông tin chung</h5>
                        <span class="badge-status status-${otRequest.status}">${otRequest.statusLabel}</span>
                    </div>

                    <div class="row g-4 mb-4">
                        <div class="col-md-6">
                            <div class="info-label">Lý do tăng ca</div>
                            <div class="info-value">${otRequest.reason}</div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-label">Ngày tạo đơn</div>
                            <div class="info-value"><fmt:formatDate value="${otRequest.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/></div>
                        </div>
                        <div class="col-md-4">
                            <div class="info-label">Ngày tăng ca</div>
                            <div class="info-value"><i class="fa-regular fa-calendar text-primary me-2"></i><fmt:formatDate value="${otRequest.otDate}" pattern="dd/MM/yyyy"/></div>
                        </div>
                        <div class="col-md-4">
                            <div class="info-label">Thời gian</div>
                            <div class="info-value"><i class="fa-regular fa-clock text-warning me-2"></i>${otRequest.startTime} - ${otRequest.endTime}</div>
                        </div>
                        <div class="col-md-4">
                            <div class="info-label">Loại ngày</div>
                            <div class="info-value">
                                <c:choose>
                                    <c:when test="${otRequest.dayType == 1}"><span class="badge bg-secondary">Ngày thường</span></c:when>
                                    <c:when test="${otRequest.dayType == 2}"><span class="badge bg-info text-dark">Cuối tuần</span></c:when>
                                    <c:when test="${otRequest.dayType == 3}"><span class="badge bg-danger">Ngày lễ</span></c:when>
                                    <c:otherwise><span class="badge bg-secondary">Không xác định</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Danh sách nhân viên -->
                <div class="section-card">
                    <h5 class="mb-3 text-primary"><i class="fa-solid fa-users me-2"></i>Nhân viên tham gia (${otRequest.totalAssignees})</h5>
                    
                    <div class="employee-list-box">
                        <table class="table table-hover table-custom align-middle">
                            <thead>
                                <tr>
                                    <th>Mã NV</th>
                                    <th>Họ tên</th>
                                    <th>Chức vụ</th>
                                    <th>Phòng ban</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="emp" items="${assignees}">
                                    <tr>
                                        <td class="fw-bold">${emp.employeeCode}</td>
                                        <td>
                                            <div class="d-flex align-items-center">
                                                <div class="bg-light rounded-circle d-flex align-items-center justify-content-center me-2" style="width: 32px; height: 32px;">
                                                    <i class="fa-solid fa-user text-secondary"></i>
                                                </div>
                                                ${emp.fullName}
                                            </div>
                                        </td>
                                        <td><span class="badge bg-light text-dark border">${emp.positionName}</span></td>
                                        <td>${emp.departmentName}</td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty assignees}">
                                    <tr>
                                        <td colspan="4" class="text-center text-muted py-4">Không có nhân viên nào được phân công.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Thông tin phê duyệt -->
            <div class="col-lg-4">
                <div class="section-card">
                    <h5 class="mb-3 text-primary border-bottom pb-2"><i class="fa-solid fa-clipboard-check me-2"></i>Thông tin phê duyệt</h5>
                    
                    <c:choose>
                        <c:when test="${otRequest.status == 0}">
                            <div class="py-2">
                                <h6 class="text-muted mb-3">Xử lý đơn</h6>
                                <form id="processForm" method="POST">
                                    <input type="hidden" name="formId" value="${otRequest.formId}">
                                    <div class="mb-3">
                                        <label class="form-label fw-semibold">Ghi chú (Note):</label>
                                        <textarea class="form-control" name="note" rows="3" placeholder="Nhập ghi chú hoặc lý do..."></textarea>
                                    </div>
                                    <div class="d-flex gap-2">
                                        <button type="submit" class="btn btn-success flex-fill fw-semibold" onclick="document.getElementById('processForm').action='${pageContext.request.contextPath}/v1/businessadmin/overtime/approve';">
                                            <i class="fa-solid fa-check me-1"></i> Duyệt
                                        </button>
                                        <button type="submit" class="btn btn-danger flex-fill fw-semibold" onclick="document.getElementById('processForm').action='${pageContext.request.contextPath}/v1/businessadmin/overtime/reject';">
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
                                         <c:when test="${otRequest.status == 1 || otRequest.status == 2 || otRequest.status == 4}">
                                             <i class="fa-solid fa-user-check text-success me-2"></i>Business Admin
                                         </c:when>
                                         <c:when test="${not empty otRequest.approverName}">
                                             <i class="fa-solid fa-user-check text-success me-2"></i>${otRequest.approverName}
                                         </c:when>
                                         <c:otherwise>
                                             <span class="text-muted italic">Hệ thống</span>
                                         </c:otherwise>
                                     </c:choose>
                                </div>
                            </div>
                            <div class="mb-3">
                                <div class="info-label">Thời gian duyệt</div>
                                <div class="info-value">
                                    <c:choose>
                                        <c:when test="${not empty otRequest.approvedAt}">
                                            <i class="fa-regular fa-clock me-2"></i><fmt:formatDate value="${otRequest.approvedAt}" pattern="dd/MM/yyyy HH:mm:ss"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">-</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="mb-3">
                                <div class="info-label">Ghi chú của người duyệt</div>
                                <div class="p-3 bg-light rounded mt-1 border">
                                    <c:choose>
                                        <c:when test="${not empty otRequest.approverNote}">
                                            ${otRequest.approverNote}
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted italic">Không có ghi chú</span>
                                        </c:otherwise>
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
