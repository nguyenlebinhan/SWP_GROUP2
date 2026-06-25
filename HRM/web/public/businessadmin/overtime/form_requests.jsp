<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Đơn từ</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .dept-card {
            background: #fff;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(11,14,42,0.07);
            border: none;
            margin-bottom: 20px;
        }
        .dept-table th {
            background: #0B0E2A; color: #fff; font-size: 13px; font-weight: 600; letter-spacing: .4px;
        }
        .dept-table td { vertical-align: middle; font-size: 14px; }
        .btn-assign {
            background: #1565C0; color: #fff; border: none; border-radius: 8px;
            padding: 5px 14px; font-size: 13px; font-weight: 600;
        }
        .btn-assign:hover { background: #0d47a1; color: #fff; }
        .btn-success-soft {
            background: #fff; color: #059669; border: 1.5px solid #059669;
            border-radius: 8px; padding: 5px 14px; font-size: 13px; font-weight: 600;
        }
        .btn-success-soft:hover { background: #d1fae5; }
        .btn-danger-soft {
            background: #fff; color: #dc2626; border: 1.5px solid #dc2626;
            border-radius: 8px; padding: 5px 14px; font-size: 13px; font-weight: 600;
        }
        .btn-danger-soft:hover { background: #fee2e2; }
        .alert-flash { border-radius: 10px; font-weight: 500; }
        .badge-status {
            padding: 5px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }
        .status-0 { background-color: #fef3c7; color: #92400e; }
        .status-1 { background-color: #d1fae5; color: #065f46; }
        .status-2 { background-color: #fee2e2; color: #991b1b; }
        .status-3 { background-color: #f3f4f6; color: #374151; }
    </style>
</head>
<body>

<jsp:include page="/public/components/businessAdminSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/businessAdminTopBar.jsp">
        <jsp:param name="title" value="Quản lý Đơn từ" />
    </jsp:include>

    <c:if test="${not empty success}">
        <div class="alert alert-success alert-flash alert-dismissible fade show" role="alert">
            ${success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash alert-dismissible fade show" role="alert">
            ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Bộ lọc -->
    <div class="dept-card p-4">
        <form action="${pageContext.request.contextPath}/v1/businessadmin/forms" method="GET" class="row g-3 align-items-end">
            <div class="col-md-2">
                <label class="form-label">Ngày tạo</label>
                <input type="number" name="day" class="form-control" placeholder="DD" min="1" max="31" value="${filterDay}">
            </div>
            <div class="col-md-2">
                <label class="form-label">Tháng</label>
                <input type="number" name="month" class="form-control" placeholder="MM" min="1" max="12" value="${filterMonth}">
            </div>
            <div class="col-md-2">
                <label class="form-label">Năm</label>
                <input type="number" name="year" class="form-control" placeholder="YYYY" value="${filterYear}">
            </div>
            <div class="col-md-4">
                <label class="form-label">Tìm kiếm (Tên NV, Phòng ban)</label>
                <input type="text" name="keyword" class="form-control" placeholder="Nhập từ khóa..." value="${keyword}">
            </div>
            <div class="col-md-2 text-end">
                <button type="submit" class="btn btn-assign w-100">
                    <i class="fa-solid fa-filter me-1"></i> Lọc
                </button>
            </div>
        </form>
    </div>

    <!-- Danh sách Đơn -->
    <div class="dept-card">
        <div class="p-4 border-bottom">
            <h5 class="mb-0 fw-bold" style="color:#0B0E2A;">Danh sách Đơn từ</h5>
        </div>
        <div class="p-0">
            <div class="table-responsive">
                <table class="table dept-table mb-0 align-middle">
                    <thead>
                        <tr>
                            <th class="ps-4">Mã đơn</th>
                            <th>Loại đơn</th>
                            <th>Người tạo</th>
                            <th>Phòng ban</th>
                            <th>Ngày tạo</th>
                            <th>Trạng thái</th>
                            <th class="text-center">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty forms}">
                                <tr><td colspan="7" class="text-center py-5 text-muted">Không tìm thấy đơn nào.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="f" items="${forms}">
                                    <tr>
                                        <td class="ps-4 fw-bold">${f.formCode}</td>
                                        <td>
                                            <span class="badge bg-secondary">${f.formTypeName}</span>
                                        </td>
                                        <td>
                                            <div class="fw-semibold">${f.fullName}</div>
                                            <small class="text-muted">${f.employeeCode}</small>
                                        </td>
                                        <td>${f.departmentName != null ? f.departmentName : '-'}</td>
                                        <td><fmt:formatDate value="${f.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                                        <td>
                                            <span class="badge-status status-${f.status}">${f.statusLabel}</span>
                                            <c:if test="${f.formTypeCode == 'TRANSFER'}">
                                                <div class="small text-muted mt-1">
                                                    Đến: <strong>${f.targetDepartmentName}</strong>
                                                </div>
                                            </c:if>
                                            <c:if test="${f.formTypeCode == 'PROMOTION_DEMOTION'}">
                                                <div class="small text-muted mt-1">
                                                    Role: <strong>${f.targetRoleName}</strong>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td class="text-center">
                                            <c:if test="${f.formTypeCode == 'OVERTIME'}">
                                                <a href="${pageContext.request.contextPath}/v1/businessadmin/forms/ot-detail?id=${f.formId}" class="btn btn-primary btn-sm" title="Xem chi tiết">
                                                    <i class="fa-solid fa-eye"></i> Xem chi tiết
                                                </a>
                                            </c:if>
                                            <c:if test="${f.status == 0 && f.formTypeCode != 'OVERTIME'}">
                                                <button type="button" class="btn btn-success-soft btn-sm me-1" 
                                                        onclick="openProcessModal(${f.formId}, 'approve', '${f.formCode}')" title="Duyệt đơn">
                                                    <i class="fa-solid fa-check"></i> Duyệt
                                                </button>
                                                <button type="button" class="btn btn-danger-soft btn-sm" 
                                                        onclick="openProcessModal(${f.formId}, 'reject', '${f.formCode}')" title="Từ chối">
                                                    <i class="fa-solid fa-xmark"></i> Từ chối
                                                </button>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Modal Duyệt/Từ chối -->
<div class="modal fade" id="processModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border-radius:16px;border:none;">
            <form id="processForm" method="POST">
                <div class="modal-header border-0 pb-0">
                    <h5 class="modal-title fw-bold" id="processModalTitle">Xử lý đơn</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <input type="hidden" name="formId" id="modalFormId">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Ghi chú (Note):</label>
                        <textarea class="form-control" name="note" id="modalNote" rows="3" placeholder="Nhập ghi chú hoặc lý do..."></textarea>
                    </div>
                </div>
                <div class="modal-footer border-0 pt-0 gap-2">
                    <button type="button" class="btn btn-light px-4" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn px-4 fw-semibold" id="modalSubmitBtn">Xác nhận</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function openProcessModal(formId, action, formCode) {
        document.getElementById('modalFormId').value = formId;
        document.getElementById('modalNote').value = '';
        
        var form = document.getElementById('processForm');
        var title = document.getElementById('processModalTitle');
        var btn = document.getElementById('modalSubmitBtn');
        
        if (action === 'approve') {
            form.action = '${pageContext.request.contextPath}/v1/businessadmin/forms/approve';
            title.textContent = 'Duyệt đơn: ' + formCode;
            btn.textContent = 'Duyệt đơn';
            btn.className = 'btn btn-success px-4 fw-semibold';
        } else {
            form.action = '${pageContext.request.contextPath}/v1/businessadmin/forms/reject';
            title.textContent = 'Từ chối đơn: ' + formCode;
            btn.textContent = 'Từ chối đơn';
            btn.className = 'btn btn-danger px-4 fw-semibold';
        }
        
        new bootstrap.Modal(document.getElementById('processModal')).show();
    }
</script>
</body>
</html>
