<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Chi tiết nhân viên - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .page-card { background: white; border-radius: 14px; box-shadow: 0 2px 12px rgba(0,0,0,0.07); padding: 28px; }
        .avatar-circle { width: 80px; height: 80px; border-radius: 50%; background: #dbeafe; color: #2563eb; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: 700; }
        .info-label { display: block; font-size: 12px; color: #64748b; font-weight: 600; margin-bottom: 4px; }
        .info-value { font-size: 14px; color: #0f172a; font-weight: 600; }
        .status-pill { display: inline-flex; padding: 5px 12px; border-radius: 20px; font-size: 12px; font-weight: 700; }
        .status-1 { background: #d1fae5; color: #065f46; }
        .status-2 { background: #fef3c7; color: #92400e; }
        .status-0 { background: #fee2e2; color: #991b1b; }
    </style>
</head>
<body>
<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Chi tiết nhân viên" />
        <jsp:param name="backUrl" value="/v1/manager/employee-list" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session" />
    </c:if>

    <div class="page-card">
        <div class="d-flex align-items-center justify-content-between mb-4">
            <div class="d-flex align-items-center gap-3">
                <div class="avatar-circle">${employee.fullName.substring(0,1).toUpperCase()}</div>
                <div>
                    <h5 class="fw-bold mb-1"><c:out value="${employee.fullName}"/></h5>
                    <div class="text-muted small"><c:out value="${employee.email}"/></div>
                    <div class="mt-2">
                        <span class="status-pill status-${employee.status}"><c:out value="${employee.statusLabel}"/></span>
                    </div>
                    <c:if test="${canReassignDept and employee.departmentId > 0}">
                        <div class="mt-2 d-flex gap-2 flex-wrap">
                            <a href="${pageContext.request.contextPath}/v1/manager/reassign-department?id=${employee.employeeId}"
                               class="btn btn-warning btn-sm">
                                <i class="fa-solid fa-right-left me-1"></i>Chuyển phòng ban
                            </a>
                            <form action="${pageContext.request.contextPath}/v1/manager/unassign-department"
                                  method="post" class="d-inline"
                                  onsubmit="return confirm('Gỡ phân công sẽ đưa nhân viên về trạng thái chưa có phòng ban. Tiếp tục?');">
                                <input type="hidden" name="employeeId" value="${employee.employeeId}">
                                <button type="submit" class="btn btn-outline-danger btn-sm">
                                    <i class="fa-solid fa-link-slash me-1"></i>Gỡ phân công
                                </button>
                            </form>
                        </div>
                    </c:if>
                </div>
            </div>
            <div class="d-flex gap-2">
                <c:if test="${canEditEmployee}">
                    <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/v1/manager/update-employee?id=${employee.employeeId}">
                        <i class="fa-solid fa-pen me-1"></i>Chỉnh sửa
                    </a>
                </c:if>
                <c:if test="${canAddEmploymentContract}">
                    <a class="btn btn-outline-success btn-sm" href="${pageContext.request.contextPath}/v1/manager/contract-preview?employeeId=${employee.employeeId}">
                        <i class="fa-solid fa-print me-1"></i>In hợp đồng
                    </a>
                    <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/v1/manager/add-contract?employeeId=${employee.employeeId}">
                        <i class="fa-solid fa-file-contract me-1"></i>Thêm hợp đồng
                    </a>
                </c:if>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-md-4">
                <span class="info-label">Mã nhân viên</span>
                <div class="info-value"><c:out value="${employee.employeeCode}"/></div>
            </div>
            <div class="col-md-4">
                <span class="info-label">Tên đăng nhập</span>
                <div class="info-value"><c:out value="${employee.username}"/></div>
            </div>
            <div class="col-md-4">
                <span class="info-label">Vai trò</span>
                <div class="info-value"><c:out value="${employee.roleName}"/></div>
            </div>
            <div class="col-md-4">
                <span class="info-label">Phòng ban</span>
                <div class="info-value"><c:out value="${empty employee.departmentName ? '-' : employee.departmentName}"/></div>
            </div>
            <div class="col-md-4">
                <span class="info-label">Vị trí</span>
                <div class="info-value"><c:out value="${empty employee.positionName ? '-' : employee.positionName}"/></div>
            </div>
            <div class="col-md-4">
                <span class="info-label">Số điện thoại</span>
                <div class="info-value"><c:out value="${empty employee.phoneNumber ? '-' : employee.phoneNumber}"/></div>
            </div>
            <div class="col-md-6">
                <span class="info-label">Kỹ năng</span>
                <div class="info-value"><c:out value="${empty employee.skills ? '-' : employee.skills}"/></div>
            </div>
            <div class="col-md-6">
                <span class="info-label">Kinh nghiệm</span>
                <div class="info-value"><c:out value="${empty employee.experience ? '-' : employee.experience}"/></div>
            </div>
            <div class="col-md-6">
                <span class="info-label">Bằng cấp</span>
                <div class="info-value"><c:out value="${empty employee.degree ? '-' : employee.degree}"/></div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
