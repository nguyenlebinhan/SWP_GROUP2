<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Gửi đơn yêu cầu - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 28px;
            margin-bottom: 24px;
        }
        .section-card h5 { font-weight: 700; color: #0f172a; }
        .upload-area {
            border: 2px dashed #cbd5e1;
            border-radius: 10px;
            padding: 22px;
            text-align: center;
            background: #f8fafc;
            cursor: pointer;
            transition: border-color .2s, background .2s;
        }
        .upload-area:hover { border-color: #6366f1; background: #eef2ff; }
        .upload-area .fa-cloud-arrow-up { font-size: 2rem; color: #94a3b8; }
        .file-ext-badge {
            display: inline-block;
            background: #f1f5f9;
            color: #475569;
            border-radius: 6px;
            padding: 2px 8px;
            font-size: 11px;
            margin: 2px;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
        <jsp:param name="title" value="Gửi đơn yêu cầu" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="section-card">
        <h5 class="mb-4"><i class="fa-solid fa-file-pen me-2"></i>Tạo đơn yêu cầu mới</h5>
        <form method="post" action="${pageContext.request.contextPath}/v1/employee/submit-form"
              enctype="multipart/form-data">
            <div class="row g-3">

                <div class="col-md-5">
                    <label class="form-label fw-semibold">Loại đơn <span class="text-danger">*</span></label>
                    <select id="formTypeId" name="formTypeId" class="form-select" required>
                        <option value="">-- Chọn loại đơn --</option>
                        <c:forEach var="ft" items="${formTypes}">
                            <option value="${ft.formTypeId}"><c:out value="${ft.formTypeName}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-12">
                    <label class="form-label fw-semibold">Lý do / Nội dung</label>
                    <textarea id="reason" name="reason" class="form-control" rows="4"
                              placeholder="Mô tả chi tiết lý do gửi đơn..."><c:out value="${param.reason}"/></textarea>
                </div>

                <div class="col-md-12">
                    <label class="form-label fw-semibold">File đính kèm <span class="text-muted">(không bắt buộc)</span></label>
                    <div class="upload-area" id="uploadArea" onclick="document.getElementById('attachmentInput').click()">
                        <i class="fa-solid fa-cloud-arrow-up d-block mb-2"></i>
                        <div id="uploadLabel">Nhấn để chọn file hoặc kéo thả vào đây</div>
                        <div class="mt-2">
                            <span class="file-ext-badge">xlsx</span>
                            <span class="file-ext-badge">pdf</span>
                            <span class="file-ext-badge">docx</span>
                            <span class="file-ext-badge">doc</span>
                            <span class="file-ext-badge">xls</span>
                            <span class="file-ext-badge">jpg</span>
                            <span class="file-ext-badge">png</span>
                            <span class="file-ext-badge">zip</span>
                        </div>
                    </div>
                    <input type="file" id="attachmentInput" name="attachment" class="d-none"
                           accept=".xlsx,.pdf,.docx,.doc,.xls,.jpg,.png,.zip">
                </div>

            </div>
            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-primary" id="submitBtn">
                    <i class="fa-solid fa-paper-plane me-1"></i> Gửi đơn
                </button>
                <a href="${pageContext.request.contextPath}/v1/employee/my-forms"
                   class="btn btn-outline-secondary">
                    <i class="fa-solid fa-list me-1"></i> Xem đơn của tôi
                </a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const input = document.getElementById('attachmentInput');
    const label = document.getElementById('uploadLabel');
    const area  = document.getElementById('uploadArea');

    input.addEventListener('change', () => {
        if (input.files.length > 0) {
            label.textContent = input.files[0].name;
            area.style.borderColor = '#6366f1';
            area.style.background  = '#eef2ff';
        }
    });

    // Drag & drop
    area.addEventListener('dragover', e => { e.preventDefault(); area.style.borderColor = '#6366f1'; });
    area.addEventListener('dragleave', ()=> { area.style.borderColor = '#cbd5e1'; area.style.background = '#f8fafc'; });
    area.addEventListener('drop', e => {
        e.preventDefault();
        if (e.dataTransfer.files.length > 0) {
            input.files = e.dataTransfer.files;
            label.textContent = e.dataTransfer.files[0].name;
            area.style.borderColor = '#6366f1';
            area.style.background  = '#eef2ff';
        }
    });
</script>
</body>
</html>
