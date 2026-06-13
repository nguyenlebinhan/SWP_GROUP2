<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Chuyển phòng ban - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }

        .page-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 32px 36px;
        }

        .page-header {
            display: flex;
            align-items: center;
            gap: 14px;
            padding-bottom: 20px;
            border-bottom: 1px solid #f1f5f9;
            margin-bottom: 28px;
        }

        .page-header-icon {
            width: 48px; height: 48px;
            border-radius: 12px;
            background: #fef3c7;
            color: #d97706;
            display: flex; align-items: center; justify-content: center;
            font-size: 20px;
            flex-shrink: 0;
        }

        .section-title {
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: .08em;
            color: #94a3b8;
            margin: 24px 0 16px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .section-title::after {
            content: '';
            flex: 1;
            height: 1px;
            background: #f1f5f9;
        }

        .h-form-row {
            display: grid;
            align-items: start;
            gap: 0 20px;
            margin-bottom: 18px;
        }

        .h-form-row.cols-1 { grid-template-columns: 160px 1fr; }
        .h-form-row.cols-2 { grid-template-columns: 160px 1fr 160px 1fr; }

        .h-label {
            font-size: 13px;
            font-weight: 600;
            color: #374151;
            padding-top: 10px;
            text-align: right;
            padding-right: 4px;
        }

        .h-field { display: flex; flex-direction: column; gap: 4px; }

        .form-control, .form-select {
            border-radius: 8px;
            border: 1px solid #e2e8f0;
            font-size: 13.5px;
            padding: 9px 12px;
            background: #fafafa;
            transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
        }
        .form-control:focus, .form-select:focus {
            border-color: #2563eb;
            box-shadow: 0 0 0 3px rgba(37,99,235,0.1);
            background: white;
            outline: none;
        }

        .required-dot {
            display: inline-block;
            width: 5px; height: 5px;
            background: #ef4444;
            border-radius: 50%;
            vertical-align: super;
            margin-left: 3px;
        }

        .user-info-box {
            display: none;
            background: #f0f7ff;
            border: 1px solid #bfdbfe;
            border-radius: 8px;
            padding: 10px 14px;
            font-size: 12.5px;
            color: #1e40af;
            gap: 16px;
        }
        .user-info-box.visible { display: flex; align-items: center; flex-wrap: wrap; }
        .user-info-item { display: flex; align-items: center; gap: 5px; }

        .action-bar {
            display: flex;
            align-items: center;
            gap: 12px;
            padding-top: 24px;
            margin-top: 8px;
            border-top: 1px solid #f1f5f9;
        }

        .btn-submit {
            background: #d97706;
            border: none;
            border-radius: 8px;
            padding: 10px 24px;
            font-weight: 600;
            font-size: 13.5px;
            color: white;
            display: flex; align-items: center; gap: 8px;
        }
        .btn-submit:hover { background: #b45309; color: white; }

        .btn-cancel {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 10px 20px;
            font-weight: 600;
            font-size: 13.5px;
            color: #64748b;
            text-decoration: none;
            display: flex; align-items: center; gap: 8px;
        }
        .btn-cancel:hover { background: #f8fafc; color: #374151; }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }
        .empty-state-icon {
            width: 72px; height: 72px;
            background: #fef3c7;
            border-radius: 50%;
            display: flex; align-items: center; justify-content: center;
            font-size: 28px; color: #d97706;
            margin: 0 auto 16px;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Chuyển phòng ban" />
        <jsp:param name="backUrl" value="/v1/manager/dashboard" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session" />
    </c:if>

    <div class="page-card">
        <c:choose>
            <c:when test="${empty assignedEmployees}">
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <i class="fa-solid fa-users-slash"></i>
                    </div>
                    <h6 class="fw-bold mb-2">Chưa có nhân viên nào để chuyển</h6>
                    <p class="text-muted small mb-3">Không có nhân viên nào đã được phân công phòng ban.</p>
                    <a href="${pageContext.request.contextPath}/v1/manager/employee-list"
                       class="btn btn-outline-primary btn-sm">
                        <i class="fa-solid fa-list me-1"></i>Xem danh sách nhân viên
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <form action="${pageContext.request.contextPath}/v1/manager/reassign-department"
                      method="post" id="reassignForm" novalidate>

                    <div class="section-title">Thông tin bắt buộc</div>

                    <div class="h-form-row cols-1">
                        <div class="h-label">
                            Nhân viên <span class="required-dot"></span>
                        </div>
                        <div class="h-field">
                            <select class="form-select" id="employeeId" name="employeeId"
                                    required onchange="onEmployeeChange(this)">
                                <option value="">— Chọn nhân viên —</option>
                                <c:forEach var="emp" items="${assignedEmployees}">
                                    <option value="${emp.employeeId}"
                                            data-email="${emp.email}"
                                            data-role="${emp.roleName}"
                                            data-dept="${emp.departmentName}"
                                            data-deptid="${emp.departmentId}"
                                            ${selectedEmployeeId == emp.employeeId ? 'selected' : ''}>
                                        ${emp.fullName} &nbsp;(${emp.employeeCode})
                                    </option>
                                </c:forEach>
                            </select>
                            <div id="empInfoBox" class="user-info-box">
                                <span class="user-info-item">
                                    <i class="fa-regular fa-envelope"></i>
                                    <span id="eEmail">—</span>
                                </span>
                                <span class="user-info-item">
                                    <i class="fa-solid fa-shield-halved"></i>
                                    Vai trò: <strong id="eRole">—</strong>
                                </span>
                                <span class="user-info-item">
                                    <i class="fa-solid fa-sitemap"></i>
                                    Phòng ban hiện tại: <strong id="eDept">—</strong>
                                </span>
                            </div>
                        </div>
                    </div>


                    <div class="h-form-row cols-2">
                        <div class="h-label">
                            Phòng ban mới <span class="required-dot"></span>
                        </div>
                        <div class="h-field">
                            <select class="form-select" id="departmentId" name="departmentId" required>
                                <option value="">— Chọn phòng ban —</option>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.departmentId}">${dept.departmentName}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="h-label">
                            Vị trí <span class="required-dot"></span>
                        </div>
                        <div class="h-field">
                            <select class="form-select" id="positionId" name="positionId" required>
                                <option value="">— Chọn vị trí —</option>
                                <c:forEach var="pos" items="${positions}">
                                    <option value="${pos.positionId}" data-level="${pos.level}">${pos.positionName}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="action-bar">
                        <button type="submit" class="btn btn-submit">
                            <i class="fa-solid fa-right-left"></i> Xác nhận chuyển phòng
                        </button>
                        <a href="${pageContext.request.contextPath}/v1/manager/employee-list"
                           class="btn-cancel">
                            <i class="fa-solid fa-xmark"></i> Hủy
                        </a>
                        <span class="text-muted ms-auto" style="font-size:12px">
                            <span class="required-dot"></span> Bắt buộc điền
                        </span>
                    </div>

                </form>
            </c:otherwise>
        </c:choose>

    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function onEmployeeChange(select) {
        const box  = document.getElementById('empInfoBox');
        const opt  = select.options[select.selectedIndex];
        const role = (select.value && opt.dataset.role) ? opt.dataset.role : '';
        if (select.value) {
            document.getElementById('eEmail').textContent = opt.dataset.email || '—';
            document.getElementById('eRole').textContent  = role || '—';
            document.getElementById('eDept').textContent  = opt.dataset.dept || '—';
            box.classList.add('visible');
            hideCurrentDepartment(opt.dataset.deptid);
        } else {
            box.classList.remove('visible');
            showAllDepartments();
        }
        filterPositionsByRole(role);
    }

    function hideCurrentDepartment(currentDeptId) {
        const deptSelect = document.getElementById('departmentId');
        for (const o of deptSelect.options) {
            if (!o.value) continue;
            const isCurrent = o.value === currentDeptId;
            o.hidden = isCurrent;
            o.disabled = isCurrent;
            if (isCurrent && o.selected) {
                deptSelect.value = '';
            }
        }
    }

    function showAllDepartments() {
        const deptSelect = document.getElementById('departmentId');
        for (const o of deptSelect.options) {
            o.hidden = false;
            o.disabled = false;
        }
    }

    function filterPositionsByRole(role) {
        const r = role.toLowerCase();
        const employeeOnly = r.includes('employee');
        const managerOnly = r.includes('manager');
        const posSelect = document.getElementById('positionId');
        for (const o of posSelect.options) {
            if (!o.value) continue;
            const level = parseInt(o.dataset.level, 10);
            let hide = false;
            if (employeeOnly) {
                hide = level >= 3;
            } else if (managerOnly) {
                hide = level < 3;
            }
            o.hidden = hide;
            o.disabled = hide;
            if (hide && o.selected) {
                posSelect.value = '';
            }
        }
    }

    document.getElementById('reassignForm').addEventListener('submit', function (e) {
        const missing = [];
        if (!document.getElementById('employeeId').value)   missing.push('Nhân viên');
        if (!document.getElementById('departmentId').value) missing.push('Phòng ban mới');
        if (!document.getElementById('positionId').value)   missing.push('Vị trí');
        if (missing.length) {
            e.preventDefault();
            alert('Vui lòng chọn: ' + missing.join(', ') + '.');
        }
    });


    document.addEventListener('DOMContentLoaded', function () {
        const sel = document.getElementById('employeeId');
        if (sel && sel.value) {
            onEmployeeChange(sel);
        }
    });
</script>
</body>
</html>
