<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Phân công phòng ban - HRM</title>
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
            background: #dbeafe;
            color: #2563eb;
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

        /* Horizontal form */
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
        textarea.form-control { resize: vertical; min-height: 72px; }

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
            background: #2563eb;
            border: none;
            border-radius: 8px;
            padding: 10px 24px;
            font-weight: 600;
            font-size: 13.5px;
            color: white;
            display: flex; align-items: center; gap: 8px;
        }
        .btn-submit:hover { background: #1d4ed8; color: white; }

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
            background: #d1fae5;
            border-radius: 50%;
            display: flex; align-items: center; justify-content: center;
            font-size: 28px; color: #059669;
            margin: 0 auto 16px;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/employeeSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/adminTopBar.jsp">
        <jsp:param name="title" value="Phân công phòng ban" />
        <jsp:param name="backUrl" value="/v1/employee/dashboard" />
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

        <div class="page-header">
            <div class="page-header-icon">
                <i class="fa-solid fa-arrow-right-to-bracket"></i>
            </div>
            <div>
                <h5 class="fw-bold mb-1" style="color:#0f172a">Phân công nhân viên vào phòng ban</h5>
                <p class="text-muted mb-0" style="font-size:13px">
                    Chọn người dùng chưa có hồ sơ nhân viên, gán vào phòng ban và vị trí công việc.
                </p>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty availableUsers}">
                <div class="empty-state">
                    <div class="empty-state-icon">
                        <i class="fa-solid fa-user-check"></i>
                    </div>
                    <h6 class="fw-bold mb-2">Tất cả người dùng đã được phân công</h6>
                    <p class="text-muted small mb-3">Không còn người dùng nào chưa có hồ sơ nhân viên.</p>
                    <a href="${pageContext.request.contextPath}/v1/employee/employee-list"
                       class="btn btn-outline-primary btn-sm">
                        <i class="fa-solid fa-list me-1"></i>Xem danh sách nhân viên
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <form action="${pageContext.request.contextPath}/v1/employee/assign-department"
                      method="post" id="assignForm" novalidate>

                    <%-- Section: Bắt buộc --%>
                    <div class="section-title">Thông tin bắt buộc</div>

                    <%-- Người dùng - full width --%>
                    <div class="h-form-row cols-1">
                        <div class="h-label">
                            Người dùng <span class="required-dot"></span>
                        </div>
                        <div class="h-field">
                            <select class="form-select" id="userId" name="userId"
                                    required onchange="onUserChange(this)">
                                <option value="">— Chọn người dùng —</option>
                                <c:forEach var="u" items="${availableUsers}">
                                    <option value="${u.userId}"
                                            data-email="${u.email}"
                                            data-role="${u.roleName}">
                                        ${u.fullName} &nbsp;(${u.username})
                                    </option>
                                </c:forEach>
                            </select>
                            <div id="userInfoBox" class="user-info-box">
                                <span class="user-info-item">
                                    <i class="fa-regular fa-envelope"></i>
                                    <span id="uEmail">—</span>
                                </span>
                                <span class="user-info-item">
                                    <i class="fa-solid fa-shield-halved"></i>
                                    Vai trò: <strong id="uRole">—</strong>
                                </span>
                            </div>
                        </div>
                    </div>

                    <%-- Phòng ban + Vị trí --%>
                    <div class="h-form-row cols-2">
                        <div class="h-label">
                            Phòng ban <span class="required-dot"></span>
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
                                    <option value="${pos.positionId}">${pos.positionName}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <%-- Section: Bổ sung --%>
                    <div class="section-title">Thông tin bổ sung <span style="font-weight:400;text-transform:none;letter-spacing:0">(tùy chọn)</span></div>

                    <%-- Điện thoại + Học vấn --%>
                    <div class="h-form-row cols-2">
                        <div class="h-label">Số điện thoại</div>
                        <div class="h-field">
                            <input type="text" class="form-control" id="phoneNumber"
                                   name="phoneNumber" placeholder="0912 345 678">
                        </div>
                        <div class="h-label">Học vấn</div>
                        <div class="h-field">
                            <input type="text" class="form-control" id="degree"
                                   name="degree" placeholder="Đại học, Thạc sĩ...">
                        </div>
                    </div>

                    <%-- Kỹ năng + Kinh nghiệm --%>
                    <div class="h-form-row cols-2">
                        <div class="h-label" style="padding-top:10px">Kỹ năng</div>
                        <div class="h-field">
                            <textarea class="form-control" id="skills" name="skills"
                                      rows="3" placeholder="Java, SQL, Excel..."></textarea>
                        </div>
                        <div class="h-label" style="padding-top:10px">Kinh nghiệm</div>
                        <div class="h-field">
                            <textarea class="form-control" id="experience" name="experience"
                                      rows="3" placeholder="2 năm kinh nghiệm lập trình..."></textarea>
                        </div>
                    </div>

                    <div class="action-bar">
                        <button type="submit" class="btn btn-submit">
                            <i class="fa-solid fa-check"></i> Xác nhận phân công
                        </button>
                        <a href="${pageContext.request.contextPath}/v1/employee/employee-list"
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
    function onUserChange(select) {
        const box  = document.getElementById('userInfoBox');
        const opt  = select.options[select.selectedIndex];
        if (select.value) {
            document.getElementById('uEmail').textContent = opt.dataset.email || '—';
            document.getElementById('uRole').textContent  = opt.dataset.role  || '—';
            box.classList.add('visible');
        } else {
            box.classList.remove('visible');
        }
    }

    document.getElementById('assignForm').addEventListener('submit', function (e) {
        const missing = [];
        if (!document.getElementById('userId').value)       missing.push('Người dùng');
        if (!document.getElementById('departmentId').value) missing.push('Phòng ban');
        if (!document.getElementById('positionId').value)   missing.push('Vị trí');
        if (missing.length) {
            e.preventDefault();
            alert('Vui lòng chọn: ' + missing.join(', ') + '.');
        }
    });
</script>
</body>
</html>
