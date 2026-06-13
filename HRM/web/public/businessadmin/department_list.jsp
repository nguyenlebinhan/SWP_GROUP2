<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Phòng ban</title>
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
            transition: box-shadow 0.2s;
        }
        .dept-card:hover { box-shadow: 0 6px 24px rgba(11,14,42,0.13); }

        .badge-active   { background: #d1fae5; color: #065f46; font-weight: 600; border-radius: 20px; padding: 4px 12px; font-size: 12px; }
        .badge-inactive { background: #fee2e2; color: #991b1b; font-weight: 600; border-radius: 20px; padding: 4px 12px; font-size: 12px; }
        .badge-manager  { background: #ede9fe; color: #5b21b6; font-weight: 600; border-radius: 20px; padding: 4px 12px; font-size: 12px; }
        .badge-no-mgr   { background: #fef3c7; color: #92400e; font-weight: 600; border-radius: 20px; padding: 4px 12px; font-size: 12px; }

        .dept-table th {
            background: #0B0E2A;
            color: #fff;
            font-size: 13px;
            font-weight: 600;
            letter-spacing: .4px;
        }
        .dept-table td { vertical-align: middle; font-size: 14px; }

        .btn-assign {
            background: #1565C0;
            color: #fff;
            border: none;
            border-radius: 8px;
            padding: 5px 14px;
            font-size: 13px;
            font-weight: 600;
        }
        .btn-assign:hover { background: #0d47a1; color: #fff; }

        .btn-unassign {
            background: #fff;
            color: #dc2626;
            border: 1.5px solid #dc2626;
            border-radius: 8px;
            padding: 5px 14px;
            font-size: 13px;
            font-weight: 600;
        }
        .btn-unassign:hover { background: #fee2e2; }

        .dept-code { font-family: monospace; background: #f3f4f6; padding: 2px 8px; border-radius: 6px; font-size: 13px; color: #374151; }

        .alert-flash {
            border-radius: 10px;
            font-weight: 500;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/businessAdminSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/businessAdminTopBar.jsp">
        <jsp:param name="title" value="Quản lý Phòng ban" />
    </jsp:include>


    <c:if test="${not empty success}">
        <div class="alert alert-success alert-flash alert-dismissible fade show" role="alert">
            <i class="fa fa-circle-check me-2"></i>${success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash alert-dismissible fade show" role="alert">
            <i class="fa fa-circle-exclamation me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>


    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="dept-card p-4 d-flex align-items-center gap-3">
                <div style="background:#ede9fe;border-radius:12px;width:48px;height:48px;display:flex;align-items:center;justify-content:center;">
                    <i class="fa fa-building" style="color:#7c3aed;font-size:20px;"></i>
                </div>
                <div>
                    <div style="font-size:22px;font-weight:700;color:#0B0E2A;">${departments.size()}</div>
                    <div style="font-size:13px;color:#6b7280;">Tổng phòng ban</div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="dept-card p-4 d-flex align-items-center gap-3">
                <div style="background:#d1fae5;border-radius:12px;width:48px;height:48px;display:flex;align-items:center;justify-content:center;">
                    <i class="fa fa-circle-check" style="color:#059669;font-size:20px;"></i>
                </div>
                <div>

                    <c:set var="activeCount" value="0" />
                    <c:forEach var="d" items="${departments}">
                        <c:if test="${d.status == 1}"><c:set var="activeCount" value="${activeCount + 1}" /></c:if>
                    </c:forEach>
                    <div style="font-size:22px;font-weight:700;color:#0B0E2A;">${activeCount}</div>
                    <div style="font-size:13px;color:#6b7280;">Đang hoạt động</div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="dept-card p-4 d-flex align-items-center gap-3">
                <div style="background:#fef3c7;border-radius:12px;width:48px;height:48px;display:flex;align-items:center;justify-content:center;">
                    <i class="fa fa-user-tie" style="color:#d97706;font-size:20px;"></i>
                </div>
                <div>
                    <c:set var="hasManagerCount" value="0" />
                    <c:forEach var="d" items="${departments}">
                        <c:if test="${not empty managerMap[d.departmentId]}"><c:set var="hasManagerCount" value="${hasManagerCount + 1}" /></c:if>
                    </c:forEach>
                    <div style="font-size:22px;font-weight:700;color:#0B0E2A;">${hasManagerCount}</div>
                    <div style="font-size:13px;color:#6b7280;">Đã có Manager</div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="dept-card p-4 d-flex align-items-center gap-3">
                <div style="background:#fee2e2;border-radius:12px;width:48px;height:48px;display:flex;align-items:center;justify-content:center;">
                    <i class="fa fa-user-xmark" style="color:#dc2626;font-size:20px;"></i>
                </div>
                <div>
                    <div style="font-size:22px;font-weight:700;color:#0B0E2A;">${activeCount - hasManagerCount}</div>
                    <div style="font-size:13px;color:#6b7280;">Chưa có Manager</div>
                </div>
            </div>
        </div>
    </div>


    <div class="dept-card">
        <div class="p-4 border-bottom d-flex justify-content-between align-items-center">
            <h5 class="mb-0 fw-bold" style="color:#0B0E2A;">
                <i class="fa fa-building me-2" style="color:#1565C0;"></i>Danh sách Phòng ban
            </h5>
            <a href="${pageContext.request.contextPath}/v1/businessadmin/add-department" class="btn btn-assign">
                <i class="fa fa-plus me-1"></i>Thêm phòng ban
            </a>
        </div>
        <div class="p-0">
            <table class="table dept-table mb-0">
                <thead>
                    <tr>
                        <th class="ps-4" style="width:50px">#</th>
                        <th>Mã phòng ban</th>
                        <th>Tên phòng ban</th>
                        <th>Manager hiện tại</th>
                        <th>Trạng thái</th>
                        <th class="text-center" style="width:180px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty departments}">
                            <tr>
                                <td colspan="6" class="text-center py-5 text-muted">
                                    <i class="fa fa-inbox fa-2x mb-2 d-block"></i>Chưa có phòng ban nào
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="dept" items="${departments}" varStatus="st">
                                <tr>
                                    <td class="ps-4 text-muted">${st.index + 1}</td>
                                    <td><span class="dept-code">${dept.departmentCode}</span></td>
                                    <td>
                                        <span class="fw-semibold">${dept.departmentName}</span>
                                        <c:if test="${not empty dept.description}">
                                            <div style="font-size:12px;color:#9ca3af;">${dept.description}</div>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty managerMap[dept.departmentId]}">
                                                <c:set var="mgr" value="${managerMap[dept.departmentId]}" />
                                                <div class="d-flex align-items-center gap-2">
                                                    <div style="width:32px;height:32px;border-radius:50%;background:#ede9fe;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                                                        <i class="fa fa-user-tie" style="color:#7c3aed;font-size:13px;"></i>
                                                    </div>
                                                    <div>
                                                        <div class="fw-semibold" style="font-size:13px;">${mgr.fullName}</div>
                                                        <span class="badge-manager">${mgr.roleName}</span>
                                                    </div>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-no-mgr"><i class="fa fa-triangle-exclamation me-1"></i>Chưa có manager</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${dept.status == 1}">
                                                <span class="badge-active"><i class="fa fa-circle-dot me-1"></i>Hoạt động</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-inactive"><i class="fa fa-circle-dot me-1"></i>Tạm dừng</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <a href="${pageContext.request.contextPath}/v1/businessadmin/department/employees?id=${dept.departmentId}"
                                           class="btn btn-unassign me-1" style="color:#1565C0;border-color:#1565C0;">
                                            <i class="fa fa-users me-1"></i>Nhân viên
                                        </a>
                                        <a href="${pageContext.request.contextPath}/v1/businessadmin/update-department?id=${dept.departmentId}"
                                           class="btn btn-unassign me-1" style="color:#475569;border-color:#cbd5e1;">
                                            <i class="fa fa-pen me-1"></i>Sửa
                                        </a>
                                        <c:if test="${dept.status == 1}">
                                            <a href="${pageContext.request.contextPath}/v1/businessadmin/department/assign?id=${dept.departmentId}"
                                               class="btn btn-assign me-1">
                                                <i class="fa fa-user-plus me-1"></i>
                                                <c:choose>
                                                    <c:when test="${not empty managerMap[dept.departmentId]}">Đổi</c:when>
                                                    <c:otherwise>Assign</c:otherwise>
                                                </c:choose>
                                            </a>
                                            <c:if test="${not empty managerMap[dept.departmentId]}">
                                                <button type="button"
                                                        class="btn btn-unassign"
                                                        onclick="confirmUnassign(${dept.departmentId}, '${dept.departmentName}')">
                                                    <i class="fa fa-user-minus me-1"></i>Gỡ
                                                </button>
                                            </c:if>
                                        </c:if>
                                        <c:if test="${dept.status != 1}">
                                            <span class="text-muted" style="font-size:12px;">—</span>
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


<form id="unassignForm" method="post" action="${pageContext.request.contextPath}/v1/businessadmin/department/unassign">
    <input type="hidden" name="departmentId" id="unassignDeptId" />
</form>


<div class="modal fade" id="unassignModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content" style="border-radius:16px;border:none;">
            <div class="modal-body p-4 text-center">
                <div style="width:60px;height:60px;background:#fee2e2;border-radius:50%;margin:0 auto 16px;display:flex;align-items:center;justify-content:center;">
                    <i class="fa fa-user-minus" style="color:#dc2626;font-size:24px;"></i>
                </div>
                <h5 class="fw-bold mb-2">Xác nhận gỡ Manager</h5>
                <p class="text-muted mb-0" id="unassignModalMsg">Bạn có chắc muốn gỡ manager khỏi phòng ban này?</p>
            </div>
            <div class="modal-footer border-0 justify-content-center pb-4 gap-2">
                <button type="button" class="btn btn-light px-4" data-bs-dismiss="modal">Hủy</button>
                <button type="button" class="btn btn-danger px-4 fw-semibold" id="confirmUnassignBtn">
                    <i class="fa fa-user-minus me-1"></i>Gỡ Manager
                </button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function confirmUnassign(deptId, deptName) {
        document.getElementById('unassignDeptId').value = deptId;
        document.getElementById('unassignModalMsg').textContent =
            'Bạn có chắc muốn gỡ manager khỏi phòng ban "' + deptName + '"?';
        document.getElementById('confirmUnassignBtn').onclick = function () {
            document.getElementById('unassignForm').submit();
        };
        new bootstrap.Modal(document.getElementById('unassignModal')).show();
    }
</script>
</body>
</html>
