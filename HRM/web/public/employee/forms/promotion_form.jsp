<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đề Xuất Thăng/Giáng Chức - HRM</title>
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
        .form-type-tabs .nav-link {
            color: #475569;
            font-weight: 600;
            border-radius: 10px;
            padding: 10px 20px;
        }
        .form-type-tabs .nav-link.active {
            background: #0d6efd;
            color: #fff;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/employeeTopBar.jsp">
        <jsp:param name="title" value="Tạo Đề Xuất Thăng / Giáng Chức" />
    </jsp:include>

    <jsp:include page="/public/employee/forms/form_tabs.jsp">
        <jsp:param name="active" value="promotion" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session" />
    </c:if>

    <div class="section-card">
        <div class="mb-4">
            <h5 class="mb-1">Đề Xuất Thăng / Giáng Chức</h5>
            <p class="text-muted">Đề xuất thay đổi chức vụ (Role) cho nhân viên. Đơn sẽ được gửi cho phòng Nhân sự (HR) để phê duyệt.</p>
        </div>

        <form action="${pageContext.request.contextPath}/v1/employee/forms/submit-promotion" method="POST" enctype="multipart/form-data">
            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label fw-bold">Phòng ban <span class="text-danger">*</span></label>
                    <c:choose>
                        <c:when test="${isSelf}">
                            <c:forEach var="dept" items="${departments}">
                                <input type="text" class="form-control" value="${dept.departmentName}" readonly>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <select id="deptFilter" class="form-select" onchange="onDepartmentChange()" required>
                                <c:if test="${isHr}">
                                    <option value="">-- Tất cả phòng ban --</option>
                                </c:if>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.departmentId}" data-roles="${deptRolesMap[dept.departmentId]}"
                                            <c:if test="${not empty myDepartmentId and myDepartmentId == dept.departmentId}">selected</c:if>>
                                        ${dept.departmentName}
                                    </option>
                                </c:forEach>
                            </select>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="col-md-4">
                    <label class="form-label fw-bold">Nhân viên <span class="text-danger">*</span></label>
                    <c:choose>
                        <c:when test="${isSelf}">
                            <c:forEach var="emp" items="${employees}">
                                <input type="text" class="form-control" value="${emp.employeeCode} - ${emp.fullName}" readonly>
                                <input type="hidden" name="employeeId" value="${emp.employeeId}">
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="input-group mb-1">
                                <span class="input-group-text"><i class="fas fa-search"></i></span>
                                <input type="text" id="empSearchInput" class="form-control form-control-sm" placeholder="Lọc nhanh tên/mã (1000+ nv)..." oninput="filterEmployees()">
                            </div>
                            <select name="employeeId" id="employeeSelect" class="form-select" onchange="onEmployeeSelect()" required>
                                <option value="">-- Chọn nhân viên --</option>
                                <c:forEach var="emp" items="${employees}">
                                    <option value="${emp.employeeId}" data-dept="${emp.departmentId}">${emp.employeeCode} - ${emp.fullName}</option>
                                </c:forEach>
                            </select>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="col-md-4">
                    <label class="form-label fw-bold">Vai trò (Role) mới <span class="text-danger">*</span></label>
                    <select name="targetRoleId" id="targetRoleId" class="form-select" required>
                        <option value="">-- Chọn vai trò mới --</option>
                        <c:forEach var="role" items="${roles}">
                            <option value="${role.roleId}" data-name="${role.roleName}">${role.roleName}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-12 mt-3">
                    <label class="form-label fw-bold">Lý do đề xuất <span class="text-danger">*</span></label>
                    <textarea name="reason" class="form-control" rows="4" 
                              placeholder="Trình bày lý do đề xuất thăng chức hoặc giáng chức..." required></textarea>
                </div>

                <div class="col-md-12">
                    <label class="form-label fw-semibold">File đính kèm <span class="text-muted">(không bắt buộc)</span></label>
                    <input type="file" name="attachment" class="form-control"
                           accept=".xlsx,.pdf,.docx,.doc,.xls,.jpg,.png,.zip">
                    <small class="text-muted">Cho phép: xlsx, pdf, docx, doc, xls, jpg, png, zip</small>
                </div>

                <div class="col-12 mt-4 text-end">
                    <a href="${pageContext.request.contextPath}/v1/employee/forms/all" 
                       class="btn btn-light me-2">Hủy</a>
                    <button type="submit" class="btn btn-primary" id="btn-submit-promotion">
                        <i class="fas fa-paper-plane me-1"></i> Gửi Đề Xuất
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
function onDepartmentChange() {
    var deptSelect = document.getElementById("deptFilter");
    if (!deptSelect) return;
    var selectedOption = deptSelect.options[deptSelect.selectedIndex];
    var deptId = deptSelect.value;
    var rolesStr = selectedOption ? selectedOption.getAttribute("data-roles") : null;

    var roleSelect = document.getElementById("targetRoleId");
    roleSelect.value = "";
    var allowedRoles = rolesStr ? rolesStr.split(",") : null;
    for (var i = 1; i < roleSelect.options.length; i++) {
        var option = roleSelect.options[i];
        var roleName = option.getAttribute("data-name");
        if (!deptId || !allowedRoles || (roleName && allowedRoles.includes(roleName))) {
            option.style.display = "";
        } else {
            option.style.display = "none";
        }
    }

    filterEmployees();
}

function filterEmployees() {
    var deptSelect = document.getElementById("deptFilter");
    var empSelect = document.getElementById("employeeSelect");
    var empSearchInput = document.getElementById("empSearchInput");
    if (!deptSelect || !empSelect) return;

    var deptId = deptSelect.value;
    var searchText = empSearchInput ? empSearchInput.value.toLowerCase().trim() : "";

    for (var i = 1; i < empSelect.options.length; i++) {
        var option = empSelect.options[i];
        var empDept = option.getAttribute("data-dept");
        var empText = option.text.toLowerCase();

        var matchDept = !deptId || empDept === deptId;
        var matchText = !searchText || empText.indexOf(searchText) > -1;

        if (matchDept && matchText) {
            option.style.display = "";
        } else {
            option.style.display = "none";
            if (option.selected) empSelect.value = "";
        }
    }
}

function onEmployeeSelect() {
    var empSelect = document.getElementById("employeeSelect");
    if (!empSelect) return;
    var selectedOption = empSelect.options[empSelect.selectedIndex];
    if (selectedOption && selectedOption.value) {
        var empDept = selectedOption.getAttribute("data-dept");
        var deptSelect = document.getElementById("deptFilter");
        if (deptSelect && deptSelect.value !== empDept && empDept) {
            deptSelect.value = empDept;
            onDepartmentChange();
            empSelect.value = selectedOption.value;
        }
    }
}

document.addEventListener("DOMContentLoaded", function() {
    if (document.getElementById("deptFilter")) {
        onDepartmentChange();
    }
});
</script>
</body>
</html>
