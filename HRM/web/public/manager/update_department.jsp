<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Cáº­p nháº­t phÃ²ng ban - HRM</title>
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
        textarea.form-control { resize: vertical; min-height: 80px; }

        .required-dot {
            display: inline-block;
            width: 5px; height: 5px;
            background: #ef4444;
            border-radius: 50%;
            vertical-align: super;
            margin-left: 3px;
        }

        .hint { font-size: 11.5px; color: #94a3b8; }

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
    </style>
</head>
<body>

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Cáº­p nháº­t phÃ²ng ban" />
        <jsp:param name="backUrl" value="/v1/manager/department-detail?id=${department.departmentId}" />
    </jsp:include>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="fa-solid fa-circle-xmark me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session"/>
    </c:if>

    <div class="page-card">

        <div class="page-header">
            <div class="page-header-icon">
                <i class="fa-solid fa-pen-to-square"></i>
            </div>
            <div>
                <h5 class="fw-bold mb-1" style="color:#0f172a">Cáº­p nháº­t thÃ´ng tin phÃ²ng ban</h5>
                <p class="text-muted mb-0" style="font-size:13px">
                    Chá»‰nh sá»­a thÃ´ng tin phÃ²ng ban. CÃ¡c trÆ°á»ng cÃ³ <span class="required-dot"></span> lÃ  báº¯t buá»™c.
                </p>
            </div>
        </div>

        <form action="${pageContext.request.contextPath}/v1/manager/update-department"
              method="post" id="updateDeptForm" novalidate>

            <input type="hidden" name="departmentId" value="${department.departmentId}">

            <div class="section-title">ThÃ´ng tin báº¯t buá»™c</div>

            <div class="h-form-row cols-2">
                <div class="h-label">MÃ£ phÃ²ng ban</div>
                <div class="h-field">
                    <input type="text" class="form-control" id="departmentCode" name="departmentCode"
                           value="${department.departmentCode}" disabled>
                    <span class="hint">MÃ£ Ä‘á»‹nh danh khÃ´ng thá»ƒ thay Ä‘á»•i</span>
                </div>

                <div class="h-label">TÃªn phÃ²ng ban <span class="required-dot"></span></div>
                <div class="h-field">
                    <input type="text" class="form-control" id="departmentName" name="departmentName"
                           placeholder="VD: PhÃ²ng CÃ´ng nghá»‡ thÃ´ng tin"
                           value="${department.departmentName}" maxlength="100">
                </div>
            </div>

            <div class="h-form-row cols-2">
                <div class="h-label">Tráº¡ng thÃ¡i</div>
                <div class="h-field">
                    <select class="form-select" name="status">
                        <option value="1" ${department.status == 1 ? 'selected' : ''}>Hoáº¡t Ä‘á»™ng (Active)</option>
                        <option value="0" ${department.status == 0 ? 'selected' : ''}>Táº¡m dá»«ng (Inactive)</option>
                    </select>
                </div>
            </div>

            <div class="section-title">ThÃ´ng tin bá»• sung <span style="font-weight:400;text-transform:none;letter-spacing:0">(tÃ¹y chá»n)</span></div>

            <div class="h-form-row cols-1">
                <div class="h-label" style="padding-top:10px">MÃ´ táº£</div>
                <div class="h-field">
                    <textarea class="form-control" id="description" name="description"
                              rows="3" placeholder="MÃ´ táº£ chá»©c nÄƒng, nhiá»‡m vá»¥ cá»§a phÃ²ng ban...">${department.description}</textarea>
                </div>
            </div>

            <div class="section-title">Vai trÃ² Ä‘Æ°á»£c phÃ©p <span style="font-weight:400;text-transform:none;letter-spacing:0">(tÃ¹y chá»n)</span></div>

            <div class="h-form-row cols-1">
                <div class="h-label" style="padding-top:10px">Vai trÃ² há»£p lá»‡</div>
                <div class="h-field">
                    <c:choose>
                        <c:when test="${empty roles}">
                            <span class="hint">ChÆ°a cÃ³ vai trÃ² nÃ o trong há»‡ thá»‘ng.</span>
                        </c:when>
                        <c:otherwise>
                            <div style="display:flex; flex-direction:column; gap:10px;">
                                <c:forEach var="role" items="${roles}">
                                    <label style="display:flex; align-items:center; gap:8px; font-size:13.5px; font-weight:500; color:#374151; cursor:pointer;">
                                        <input type="checkbox" name="roleIds" value="${role.roleId}"
                                               <c:if test="${not empty selectedRoleIds and selectedRoleIds.contains(role.roleId)}">checked</c:if>>
                                        <span>${role.roleName}</span>
                                        <c:if test="${not empty role.description}">
                                            <span class="hint" style="font-weight:400;">â€” ${role.description}</span>
                                        </c:if>
                                    </label>
                                </c:forEach>
                            </div>
                            <span class="hint" style="margin-top:8px;">
                                Äá»ƒ trá»‘ng = phÃ²ng ban nháº­n má»i vai trÃ². Náº¿u tick, chá»‰ nhÃ¢n viÃªn cÃ³ vai trÃ² Ä‘Æ°á»£c chá»n má»›i Ä‘Æ°á»£c phÃ¢n cÃ´ng vÃ o phÃ²ng nÃ y.
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="action-bar">
                <button type="submit" class="btn btn-primary">
                    <i class="fa-solid fa-save"></i> LÆ°u
                </button>
                <a href="${pageContext.request.contextPath}/v1/manager/department-detail?id=${department.departmentId}" class="btn-cancel">
                    <i class="fa-solid fa-xmark"></i> Há»§y
                </a>
                <span class="text-muted ms-auto" style="font-size:12px">
                    <span class="required-dot"></span> Báº¯t buá»™c Ä‘iá»n
                </span>
            </div>

        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.getElementById('updateDeptForm').addEventListener('submit', function (e) {
        const missing = [];
        if (!document.getElementById('departmentName').value.trim()) missing.push('TÃªn phÃ²ng ban');
        if (missing.length) {
            e.preventDefault();
            alert('Vui lÃ²ng Ä‘iá»n: ' + missing.join(', ') + '.');
        }
    });
</script>
</body>
</html>
