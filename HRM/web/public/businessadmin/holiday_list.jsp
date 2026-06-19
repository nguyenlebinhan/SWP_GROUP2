<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Ngày lễ</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .dept-card { background:#fff; border-radius:14px; box-shadow:0 2px 12px rgba(11,14,42,0.07); border:none; }
        .dept-table th { background:#0B0E2A; color:#fff; font-size:13px; font-weight:600; letter-spacing:.4px; }
        .dept-table td { vertical-align:middle; font-size:14px; }
        .badge-active   { background:#d1fae5; color:#065f46; font-weight:600; border-radius:20px; padding:4px 12px; font-size:12px; }
        .badge-inactive { background:#fee2e2; color:#991b1b; font-weight:600; border-radius:20px; padding:4px 12px; font-size:12px; }
        .btn-assign { background:#1565C0; color:#fff; border:none; border-radius:8px; padding:5px 14px; font-size:13px; font-weight:600; }
        .btn-assign:hover { background:#0d47a1; color:#fff; }
        .btn-unassign { background:#fff; color:#475569; border:1.5px solid #cbd5e1; border-radius:8px; padding:5px 14px; font-size:13px; font-weight:600; }
        .btn-unassign:hover { background:#f1f5f9; }
        .alert-flash { border-radius:10px; font-weight:500; }
    </style>
</head>
<body>

<jsp:include page="/public/components/businessAdminSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/businessAdminTopBar.jsp">
        <jsp:param name="title" value="Quản lý Ngày lễ" />
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

    <div class="dept-card">
        <div class="p-4 border-bottom d-flex justify-content-between align-items-center">
            <h5 class="mb-0 fw-bold" style="color:#0B0E2A;">Danh sách Ngày lễ</h5>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/holiday/add" class="btn btn-assign">
                <i class="fa-solid fa-plus me-1"></i> Thêm ngày lễ
            </a>
        </div>
        <div class="p-0">
            <table class="table dept-table mb-0">
                <thead>
                    <tr>
                        <th class="ps-4" style="width:50px">#</th>
                        <th>Tên ngày lễ</th>
                        <th>Từ ngày</th>
                        <th>Đến ngày</th>
                        <th>Trạng thái</th>
                        <th class="text-center" style="width:160px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty holidays}">
                            <tr><td colspan="6" class="text-center py-5 text-muted">Chưa có ngày lễ nào</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="h" items="${holidays}" varStatus="st">
                                <tr>
                                    <td class="ps-4 text-muted">${st.index + 1}</td>
                                    <td class="fw-semibold">${h.holidayName}</td>
                                    <td>${h.startDate}</td>
                                    <td>${h.endDate}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${h.active}"><span class="badge-active">Đang áp dụng</span></c:when>
                                            <c:otherwise><span class="badge-inactive">Tạm tắt</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <a href="${pageContext.request.contextPath}/v1/businessadmin/holiday/update?id=${h.holidayId}"
                                           class="btn btn-unassign me-1">Sửa</a>
                                        <button type="button" class="btn btn-unassign" style="color:#dc2626;border-color:#dc2626;"
                                                onclick="confirmDelete(${h.holidayId}, '${h.holidayName}')">Xóa</button>
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

<form id="deleteForm" method="post" action="${pageContext.request.contextPath}/v1/businessadmin/holiday/delete">
    <input type="hidden" name="holidayId" id="deleteHolidayId" />
</form>

<div class="modal fade" id="deleteModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border-radius:16px;border:none;">
            <div class="modal-body p-4 text-center">
                <h5 class="fw-bold mb-2">Xác nhận xóa ngày lễ</h5>
                <p class="text-muted mb-0" id="deleteModalMsg">Bạn có chắc muốn xóa ngày lễ này?</p>
            </div>
            <div class="modal-footer border-0 justify-content-center pb-4 gap-2">
                <button type="button" class="btn btn-light px-4" data-bs-dismiss="modal">Hủy</button>
                <button type="button" class="btn btn-danger px-4 fw-semibold" id="confirmDeleteBtn">Xóa</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function confirmDelete(id, name) {
        document.getElementById('deleteHolidayId').value = id;
        document.getElementById('deleteModalMsg').textContent =
            'Bạn có chắc muốn xóa ngày lễ "' + name + '"?';
        document.getElementById('confirmDeleteBtn').onclick = function () {
            document.getElementById('deleteForm').submit();
        };
        new bootstrap.Modal(document.getElementById('deleteModal')).show();
    }
</script>
</body>
</html>
