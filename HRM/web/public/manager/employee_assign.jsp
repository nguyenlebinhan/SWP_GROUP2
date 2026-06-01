<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Assign Employee – ${dept.departmentName}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .assign-card { background: #fff; border-radius: 16px; box-shadow: 0 2px 14px rgba(11,14,42,0.08); border: none; }
        .dept-banner { background: linear-gradient(135deg, #0B0E2A 0%, #1565C0 100%); border-radius: 14px; padding: 24px 28px; color: #fff; margin-bottom: 28px; }
        .dept-banner .dept-code-badge { background: rgba(255,255,255,0.15); border-radius: 8px; padding: 3px 12px; font-family: monospace; font-size: 13px; display: inline-block; margin-bottom: 8px; }
        .candidate-item { border: 1.5px solid #e5e7eb; border-radius: 12px; padding: 14px 18px; cursor: pointer; transition: all 0.15s; display: flex; align-items: center; gap: 14px; margin-bottom: 10px; }
        .candidate-item:hover { border-color: #1565C0; background: #eff6ff; }
        .candidate-item.selected { border-color: #1565C0; background: #eff6ff; box-shadow: 0 0 0 3px rgba(21,101,192,0.12); }
        .candidate-item input[type=radio] { accent-color: #1565C0; width: 18px; height: 18px; flex-shrink: 0; }
        .cand-avatar { width: 40px; height: 40px; border-radius: 50%; background: #ede9fe; display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-weight: bold; color: #5b21b6; }
        .role-badge { background: #ede9fe; color: #5b21b6; font-size: 11px; font-weight: 700; border-radius: 20px; padding: 2px 10px; }
        .dept-badge { background: #e0f2fe; color: #0369a1; font-size: 11px; font-weight: 600; border-radius: 20px; padding: 2px 10px; }
        .search-box { border-radius: 10px; border: 1.5px solid #e5e7eb; padding: 10px 16px; font-size: 14px; width: 100%; outline: none; transition: border-color 0.15s; }
        .search-box:focus { border-color: #1565C0; }
        .btn-search { background: #6366f1; color: white; border: none; padding: 8px 18px; border-radius: 7px; font-size: 14px; font-weight: 600; }
        .btn-search:hover { background: #4f46e5; }
        .btn-clear { background: white; color: #6b7280; border: 1px solid #d1d5db; padding: 8px 14px; border-radius: 7px; font-size: 13px; text-decoration: none; font-weight: 500; }
        .btn-clear:hover { background: #f3f4f6; }
        .btn-submit { background: #1565C0; color: #fff; border: none; border-radius: 10px; padding: 12px 32px; font-size: 15px; font-weight: 700; }
        .btn-submit:hover { background: #0d47a1; color: #fff; }
        .btn-submit:disabled { background: #93c5fd; cursor: not-allowed; }
        .empty-cand { text-align: center; padding: 40px 20px; color: #9ca3af; }
        .candidate-list-wrap { max-height: 440px; overflow-y: auto; padding-right: 4px; }
        .candidate-list-wrap::-webkit-scrollbar { width: 6px; }
        .candidate-list-wrap::-webkit-scrollbar-track { background: #f1f5f9; border-radius: 6px; }
        .candidate-list-wrap::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 6px; }
    </style>
</head>
<body>
    <jsp:include page="/public/components/managerSideBar.jsp" />

    <div class="main">
        <jsp:include page="/public/components/managerTopBar.jsp">
            <jsp:param name="title" value="Assign Employee" />
            <jsp:param name="backUrl" value="/v1/manager/department" />
        </jsp:include>

        <div class="dept-banner">
            <div class="dept-code-badge">${dept.departmentCode}</div>
            <h4 class="mb-1 fw-bold">${dept.departmentName}</h4>
            <c:if test="${not empty dept.description}"><div style="opacity:.8;font-size:14px;">${dept.description}</div></c:if>
        </div>

        <div class="assign-card p-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h6 class="fw-bold mb-0" style="color:#0B0E2A;">
                    Chọn nhân viên thêm vào phòng ban
                    <span class="text-muted fw-normal" style="font-size:13px;">(${candidates.size()} nhân viên)</span>
                </h6>
                <button type="submit" form="assignForm" class="btn btn-submit" id="submitBtn" disabled>Xác nhận Assign</button>
            </div>

            <form method="get" action="${pageContext.request.contextPath}/v1/manager/department/assign-employee">
                <input type="hidden" name="id" value="${dept.departmentId}" />
                <div class="mb-3 d-flex gap-2">
                    <input type="text" name="keyword" class="search-box" placeholder="Tìm theo tên, email, role..." value="${param.keyword}" />
                    <button type="submit" class="btn btn-search">Tìm</button>
                    <a href="${pageContext.request.contextPath}/v1/manager/department/assign-employee?id=${dept.departmentId}" class="btn btn-clear">Xóa</a>
                </div>
            </form>

            <form id="assignForm" method="post" action="${pageContext.request.contextPath}/v1/manager/department/assign-employee">
                <input type="hidden" name="departmentId" value="${dept.departmentId}" />

                <div class="candidate-list-wrap">
                    <c:choose>
                        <c:when test="${empty candidates}">
                            <div class="empty-cand">Không có nhân viên nào đủ điều kiện.</div>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="cand" items="${candidates}">
                                <label class="candidate-item">
                                    <input type="radio" name="employeeId" value="${cand.employeeId}" onchange="document.getElementById('submitBtn').disabled=false" />
                                    <div class="cand-avatar">${cand.fullName.substring(0,1).toUpperCase()}</div>
                                    <div class="flex-grow-1">
                                        <div class="fw-semibold" style="font-size:14px;">${cand.fullName}</div>
                                        <div style="font-size:12px;color:#6b7280;">${cand.email}</div>
                                        <div class="mt-1 d-flex gap-1 flex-wrap">
                                            <span class="role-badge">${cand.roleName}</span>
                                            <c:if test="${not empty cand.departmentName && cand.departmentName != 'Chưa phân công'}"><span class="dept-badge">${cand.departmentName}</span></c:if>
                                            <c:if test="${cand.departmentName == 'Chưa phân công' || empty cand.departmentName}"><span style="background:#f3f4f6;color:#9ca3af;font-size:11px;font-weight:600;border-radius:20px;padding:2px 10px;">Chưa có phòng</span></c:if>
                                        </div>
                                    </div>
                                </label>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
            </form>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
