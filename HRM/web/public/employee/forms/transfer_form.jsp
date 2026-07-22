<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Đơn Thuyên Chuyển - HRM</title>
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
        <jsp:param name="title" value="Tạo đơn yêu cầu" />
    </jsp:include>

    <jsp:include page="/public/employee/forms/form_tabs.jsp">
        <jsp:param name="active" value="transfer" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="section-card">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h5 class="mb-0">Tạo Đơn Thuyên Chuyển</h5>
        </div>

        <form action="${pageContext.request.contextPath}/v1/employee/forms/transfer/submit" method="POST">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label fw-bold">Phòng ban mong muốn <span class="text-danger">*</span></label>
                    <select name="targetDepartmentId" id="targetDepartmentId" class="form-select" required onchange="filterRoles()">
                        <option value="">-- Chọn phòng ban --</option>
                        <c:forEach var="dept" items="${departments}">
                            <option value="${dept.departmentId}" data-roles="${deptRolesMap[dept.departmentId]}">${dept.departmentName}</option>
                        </c:forEach>
                    </select>
                    
                    <label class="form-label fw-bold">Vị trí mong muốn <span class="text-danger">*</span></label>
                    <select name="targetRoleId" id="targetRoleId" class="form-select" required>
                        <option value="">-- Chọn vị trí --</option>
                        <c:forEach var="role" items="${roles}">
                            <option value="${role.roleId}" data-name="${role.roleName}">${role.roleName}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-12 mt-4">
                    <label class="form-label fw-bold">Lý do thuyên chuyển <span class="text-danger">*</span></label>
                    <textarea name="reason" class="form-control" rows="4" 
                              placeholder="Trình bày lý do bạn muốn thuyên chuyển sang phòng ban này..." required></textarea>
                </div>

                <div class="col-12 mt-4 text-end">
                    <a href="${pageContext.request.contextPath}/v1/employee/forms/my-forms" 
                       class="btn btn-light me-2">Hủy</a>
                    <button type="submit" class="btn btn-primary" id="btn-submit-transfer">
                        <i class="fas fa-paper-plane me-1"></i> Gửi Đơn
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
<script>
function filterRoles() {
    var deptSelect = document.getElementById("targetDepartmentId");
    // Lấy Option Phòng ban đang được chọn
    var selectedOption = deptSelect.options[deptSelect.selectedIndex];
    var rolesStr = selectedOption.getAttribute("data-roles"); 
    
    var roleSelect = document.getElementById("targetRoleId");
    roleSelect.value = ""; // Reset chức vụ đang chọn
    
    if (!rolesStr) {
        // Nếu chưa chọn phòng ban, ẩn tất cả các Role
        for (var i = 1; i < roleSelect.options.length; i++) {
            roleSelect.options[i].style.display = "none";
        }
        return;
    }
    // Tách chuỗi thành mảng các Role Name (VD: ["ITManager", "ITEmployee"])
    var allowedRoles = rolesStr.split(",");
    
    // Lặp qua tất cả thẻ Option trong Dropdown Chức vụ
    for (var i = 1; i < roleSelect.options.length; i++) { 
        var option = roleSelect.options[i];
        var roleName = option.getAttribute("data-name");
        
        // Hiện Role nếu Tên Role nằm trong danh sách allowedRoles của phòng ban đó
        if (roleName && allowedRoles.includes(roleName)) {
            option.style.display = ""; // Hiện lên
        } else {
            option.style.display = "none"; // Ẩn đi
        }
    }
}
// Chạy filterRoles 1 lần lúc trang vừa load xong để reset danh sách Role
document.addEventListener("DOMContentLoaded", function() {
    filterRoles();
});
</script>