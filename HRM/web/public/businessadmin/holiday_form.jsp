<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isEdit" value="${not empty holiday and holiday.holidayId > 0}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${isEdit ? 'Sửa ngày lễ' : 'Thêm ngày lễ'}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background:#f5f6fa; font-family:'Segoe UI', sans-serif; }
        .main { margin-left:250px; padding:25px; }
        .form-card { background:#fff; border-radius:14px; box-shadow:0 2px 12px rgba(11,14,42,0.07); padding:28px; width:100%; }
        .btn-assign { background:#1565C0; color:#fff; border:none; border-radius:8px; padding:8px 20px; font-weight:600; }
        .btn-assign:hover { background:#0d47a1; color:#fff; }
    </style>
</head>
<body>

<jsp:include page="/public/components/businessAdminSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/businessAdminTopBar.jsp">
        <jsp:param name="title" value="${isEdit ? 'Sửa ngày lễ' : 'Thêm ngày lễ'}" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger" role="alert">${error}</div>
    </c:if>

    <div class="form-card">
        <form method="post" action="${pageContext.request.contextPath}/v1/businessadmin/holiday/${isEdit ? 'update' : 'add'}">
            <c:if test="${isEdit}">
                <input type="hidden" name="holidayId" value="${holiday.holidayId}" />
            </c:if>

            <div class="row g-3 align-items-end">
                <div class="col-md-6">
                    <label class="form-label">Tên ngày lễ <span class="text-danger">*</span></label>
                    <input type="text" name="holidayName" class="form-control" required
                           value="${holiday.holidayName}" placeholder="VD: Quốc Khánh, Tết Nguyên Đán">
                </div>
                <div class="col-md-3">
                    <label class="form-label">Từ ngày <span class="text-danger">*</span></label>
                    <input type="date" name="startDate" class="form-control" required value="${holiday.startDate}">
                </div>
                <div class="col-md-3">
                    <label class="form-label">Đến ngày <span class="text-danger">*</span></label>
                    <input type="date" name="endDate" class="form-control" required value="${holiday.endDate}">
                </div>
            </div>

            <div class="form-check mt-3 mb-4">
                <input type="checkbox" name="isActive" class="form-check-input" id="isActive"
                       ${empty holiday or holiday.active ? 'checked' : ''}>
                <label class="form-check-label" for="isActive">Áp dụng ngày lễ này khi tính chấm công</label>
            </div>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-assign">${isEdit ? 'Lưu thay đổi' : 'Thêm ngày lễ'}</button>
                <a href="${pageContext.request.contextPath}/v1/businessadmin/holiday" class="btn btn-light">Hủy</a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
