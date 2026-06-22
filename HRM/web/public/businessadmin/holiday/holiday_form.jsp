<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${editMode ? 'Sửa' : 'Thêm'} Ngày lễ</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white; border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px; margin-bottom: 24px;
        }
        .btn-primary-x { background:#1565C0; border:none; border-radius:8px; font-weight:600; }
        .btn-primary-x:hover { background:#0d47a1; }
    </style>
</head>
<body>

<jsp:include page="/public/components/businessAdminSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/businessAdminTopBar.jsp">
        <jsp:param name="title" value="Quản lý Ngày lễ" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="section-card">
        <h5 class="mb-4 fw-bold" style="color:#0B0E2A;">
            <i class="fa-solid fa-calendar-day me-2"></i>${editMode ? 'Sửa ngày lễ' : 'Thêm ngày lễ mới'}
        </h5>

        <form method="post"
              action="${pageContext.request.contextPath}/v1/businessadmin/holiday/${editMode ? 'edit' : 'add'}">
            <c:if test="${editMode}">
                <input type="hidden" name="holidayId" value="${holiday.holidayId}">
            </c:if>

            <div class="mb-3">
                <label class="form-label">Tên ngày lễ <span class="text-danger">*</span></label>
                <input type="text" name="holidayName" class="form-control"
                       value="<c:out value='${holiday.holidayName}'/>" maxlength="255"
                       placeholder="VD: Quốc Khánh, Tết Nguyên Đán..." required>
            </div>

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Từ ngày <span class="text-danger">*</span></label>
                    <input type="date" name="startDate" class="form-control" value="${holiday.startDate}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Đến ngày <span class="text-danger">*</span></label>
                    <input type="date" name="endDate" class="form-control" value="${holiday.endDate}" required>
                </div>
            </div>

            <div class="form-check mt-3">
                <input type="checkbox" name="isActive" id="isActive" class="form-check-input"
                       ${empty holiday ? 'checked' : (holiday.active ? 'checked' : '')}>
                <label class="form-check-label" for="isActive">
                    Đang áp dụng (chỉ ngày lễ đang áp dụng mới được dùng khi import chấm công)
                </label>
            </div>
            <div class="form-text">Ngày lễ kéo dài nhiều ngày chỉ cần một dòng (chọn Từ ngày → Đến ngày).</div>

            <div class="mt-4 d-flex gap-2">
                <button type="submit" id="holidaySubmitBtn"
                        style="background:#1565C0;color:#fff;border:none;border-radius:8px;font-weight:600;padding:8px 24px;cursor:pointer;">
                    Lưu
                </button>
                <a href="${pageContext.request.contextPath}/v1/businessadmin/holiday"
                   style="background:#6c757d;color:#fff;border:none;border-radius:8px;font-weight:600;padding:8px 24px;text-decoration:none;">
                    Quay lại
                </a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
