<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Thêm hợp đồng lao động - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: "Segoe UI", sans-serif; }
        .main { margin-left: 250px; padding: 24px; }
        .page-card { background: #fff; border-radius: 16px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08); padding: 28px; }
        .draft-banner { background: #e0f2fe; border: 1px solid #7dd3fc; border-radius: 12px; padding: 12px 16px; color: #0369a1; }
        @media (max-width: 768px) { .main { margin-left: 0; padding: 16px; } }
    </style>
</head>
<body>
<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Thêm hợp đồng lao động" />
        <jsp:param name="backUrl" value="/v1/manager/employee_info/list" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${not empty draft}">
        <div class="draft-banner mb-3 d-flex justify-content-between align-items-center">
            <span><i class="fa-solid fa-pen-to-square me-2"></i>${not empty draftInfo ? draftInfo : 'Bạn đang chỉnh sửa bản nháp.'}</span>
            <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/add" class="m-0">
                <input type="hidden" name="action" value="discardDraft"/>
                <input type="hidden" name="draftId" value="${draft.contractId}"/>
                <button type="submit" class="btn btn-sm btn-outline-danger" onclick="return confirm('Xóa bản nháp này? Các thay đổi chưa lưu sẽ mất.');">
                    <i class="fa-solid fa-trash-can me-1"></i>Hủy bản nháp
                </button>
            </form>
        </div>
    </c:if>

    <div class="page-card">
        <form id="contractForm" method="post" action="${pageContext.request.contextPath}/v1/manager/contract/add">
            <c:if test="${not empty draft}">
                <input type="hidden" name="draftId" value="${draft.contractId}"/>
            </c:if>

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Mã hợp đồng</label>
                    <input type="text" name="contractCode" id="contractCode" class="form-control"
                           value="${not empty draft ? draft.contractCode : param.contractCode}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Nhân viên</label>
                    <select name="employeeId" id="employeeId" class="form-select"
                            ${not empty draft ? 'disabled' : ''} required>
                        <option value="">-- Chọn nhân viên --</option>
                        <c:forEach var="emp" items="${employees}">
                            <option value="${emp.employeeId}"
                                ${(not empty draft && draft.employeeId == emp.employeeId) || param.employeeId == emp.employeeId ? 'selected' : ''}>
                                ${emp.fullName} (${emp.employeeCode})
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${not empty draft}">
                        <input type="hidden" name="employeeId" value="${draft.employeeId}"/>
                    </c:if>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Loại hợp đồng</label>
                    <select name="contractType" id="contractType" class="form-select" required>
                        <option value="">-- Chọn loại hợp đồng --</option>
                        <option value="PROBATION" ${(not empty draft && draft.contractType == 'PROBATION') || param.contractType == 'PROBATION' ? 'selected' : ''}>Thử việc</option>
                        <option value="INTERNSHIP" ${(not empty draft && draft.contractType == 'INTERNSHIP') || param.contractType == 'INTERNSHIP' ? 'selected' : ''}>Thực tập</option>
                        <option value="FIXED_TERM" ${(not empty draft && draft.contractType == 'FIXED_TERM') || param.contractType == 'FIXED_TERM' ? 'selected' : ''}>Có thời hạn</option>
                        <option value="INDEFINITE" ${(not empty draft && draft.contractType == 'INDEFINITE') || param.contractType == 'INDEFINITE' ? 'selected' : ''}>Không xác định thời hạn</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Lương</label>
                    <input type="number" name="salary" id="salary" class="form-control" min="0" step="1000"
                           value="${not empty draft ? draft.salary : param.salary}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Ngày hiệu lực</label>
                    <input type="date" name="effectiveDate" id="effectiveDate" class="form-control"
                           value="${not empty draft ? draft.effectiveDate : param.effectiveDate}" required>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Ngày kết thúc</label>
                    <input type="date" name="endDate" id="endDate" class="form-control"
                           value="${not empty draft ? draft.endDate : param.endDate}">
                </div>
                <div class="col-12">
                    <label class="form-label">Ghi chú</label>
                    <textarea name="note" id="note" class="form-control" rows="3">${not empty draft ? draft.note : param.note}</textarea>
                </div>
            </div>

            <div class="d-flex gap-2 mt-4 flex-wrap">
                <button type="button" class="btn btn-outline-primary" onclick="saveDraft()">
                    <i class="fa-regular fa-floppy-disk me-1"></i>Lưu nháp
                </button>
                <button type="submit" class="btn btn-primary" name="action" value="submit">
                    <i class="fa-solid fa-paper-plane me-1"></i>Gửi duyệt
                </button>
                <a href="${pageContext.request.contextPath}/v1/manager/employee_info/list" class="btn btn-outline-secondary" onclick="return confirmCancel();">
                    <i class="fa-solid fa-ban me-1"></i>Hủy
                </a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    var formDirty = false;
    var draftId = '${not empty draft ? draft.contractId : ""}';

    // Track form changes
    document.getElementById('contractForm').addEventListener('input', function () {
        formDirty = true;
    });

    function hasContent() {
        var code = document.getElementById('contractCode').value.trim();
        var emp = document.getElementById('employeeId');
        var type = document.getElementById('contractType').value;
        var salary = document.getElementById('salary').value.trim();
        var effDate = document.getElementById('effectiveDate').value;
        if (!emp || emp.value === '') return false;
        return code !== '' || type !== '' || salary !== '' || effDate !== '';
    }

    function saveDraft() {
        if (!hasContent()) {
            alert('Vui lòng chọn nhân viên và nhập ít nhất một thông tin trước khi lưu nháp.');
            return;
        }
        var form = document.getElementById('contractForm');
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'action';
        input.value = 'saveDraft';
        form.appendChild(input);
        form.submit();
    }

    function confirmCancel() {
        if (!formDirty) return true;
        return confirm('Bạn có muốn lưu thông tin hiện tại thành bản nháp trước khi rời đi không?');
    }

    // beforeunload: auto-save draft
    window.addEventListener('beforeunload', function (e) {
        if (!hasContent()) return;
        var form = document.getElementById('contractForm');
        var data = new FormData(form);
        data.append('action', 'autosaveDraft');
        if (draftId) data.append('draftId', draftId);
        // Use sendBeacon for reliability during page unload
        if (navigator.sendBeacon) {
            var payload = new URLSearchParams(data);
            navigator.sendBeacon('${pageContext.request.contextPath}/v1/manager/contract/add', payload);
        } else {
            fetch('${pageContext.request.contextPath}/v1/manager/contract/add', {
                method: 'POST',
                body: new URLSearchParams(data),
                keepalive: true
            });
        }
    });

    // Override form submit for submit action
    document.getElementById('contractForm').addEventListener('submit', function (e) {
        // If no action set or action is submit, ensure action=submit is set
        var actionInput = this.querySelector('input[name="action"]:not([value="saveDraft"]):not([value="discardDraft"])');
        if (!actionInput) {
            var inp = document.createElement('input');
            inp.type = 'hidden';
            inp.name = 'action';
            inp.value = 'submit';
            this.appendChild(inp);
        }
    });
</script>
</body>
</html>
