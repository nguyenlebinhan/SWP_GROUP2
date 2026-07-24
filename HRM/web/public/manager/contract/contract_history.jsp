<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Lịch sử hợp đồng - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background: #f5f6fa;
                font-family: "Segoe UI", sans-serif;
            }
            .main {
                margin-left: 250px;
                padding: 24px;
            }
            .page-card {
                background: #fff;
                border-radius: 16px;
                box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
                padding: 24px;
            }
            .contract-item {
                border: 1px solid #e2e8f0;
                border-radius: 14px;
                padding: 18px;
                background: #fff;
            }
            .contract-item + .contract-item {
                margin-top: 16px;
            }
            .meta-chip {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                padding: 6px 10px;
                border-radius: 999px;
                background: #f8fafc;
                border: 1px solid #e2e8f0;
                font-size: 12px;
            }
            .hidden-item {
                display: none !important;
            }
            @media (max-width: 768px) {
                .main {
                    margin-left: 0;
                    padding: 16px;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/managerSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Lịch sử hợp đồng" />
                <jsp:param name="backUrl" value="/v1/manager/dashboard" />
            </jsp:include>

            <div class="page-card">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-3">
                    <div>
                        <h5 class="fw-bold mb-1">
                            <c:choose>
                                <c:when test="${param.scope == 'own'}">Lịch sử hợp đồng của bản thân</c:when>
                                <c:otherwise>Lịch sử hợp đồng nhân viên</c:otherwise>
                            </c:choose>
                        </h5>
                        <div class="text-muted small"><span id="historyVisibleCount">${auditLogs.size()}</span> / ${auditLogs.size()} thay đổi</div>
                    </div>

                    <c:if test="${isHrStaffRole and param.scope != 'own'}">
                        <button type="button" class="btn btn-outline-warning" onclick="runSchedulerNow()" id="btnRunScheduler">
                            <i class="fa-solid fa-play me-1"></i>Chạy Scheduler
                        </button>
                    </c:if>
                </div>

                <div class="d-flex gap-2 flex-wrap mb-4">
                    <input type="text" id="historySearchInput" class="form-control" style="width:280px;" placeholder="Tìm theo nội dung hoặc người thay đổi...">

                </div>


                <c:if test="${param.scope == 'own' and not empty employee}">
                    <div class="alert alert-secondary">
                        <strong>${employee.fullName}</strong> (${employee.employeeCode})
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${empty auditLogs}">
                        <div class="alert alert-info mb-0">Chưa có dữ liệu lịch sử thay đổi hợp đồng.</div>
                    </c:when>
                    <c:otherwise>
                        <div id="historyContractList">
                            <c:forEach var="log" items="${auditLogs}">
                                <div class="contract-item history-contract-item" data-search="${log.actionReason} ${log.changedByName} ${log.fieldName} ${log.newStatus} ${log.oldStatus}">
                                    <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                                        <div class="flex-grow-1">
                                            <div class="d-flex justify-content-between align-items-start mb-2">
                                                <div>
                                                    <span class="badge bg-secondary me-2">#${log.contractId}</span>
                                                    <span class="text-muted small"><fmt:formatDate value="${log.changeDate}" pattern="dd/MM/yyyy HH:mm"/></span>
                                                </div>
                                                <span class="badge bg-info">${log.changedByName}</span>
                                            </div>

                                            <c:if test="${log.oldStatus == null and log.fieldName == null}">
                                                <div class="mb-2">
                                                    <i class="fa-solid fa-plus-circle text-success me-1"></i>
                                                    <strong>Tạo hợp đồng</strong> (trạng thái: ${log.newStatus})
                                                </div>
                                            </c:if>

                                            <c:if test="${log.fieldName == 'status' or (log.fieldName == null and log.oldStatus != null and log.oldStatus != 'CREATED' and log.oldStatus != '')}">
                                                <div class="mb-2">
                                                    <i class="fa-solid fa-arrow-right-arrow-left text-primary me-1"></i>
                                                    <strong>Thay đổi trạng thái:</strong>
                                                    <c:choose>
                                                        <c:when test="${not empty log.oldStatus}"><span class="meta-chip">${log.oldStatus}</span></c:when>
                                                        <c:otherwise><span class="meta-chip text-muted">(không có)</span></c:otherwise>
                                                    </c:choose>
                                                    <i class="fa-solid fa-arrow-right mx-1 text-muted"></i>
                                                    <span class="meta-chip">${log.newStatus}</span>
                                                    <c:if test="${not empty log.actionReason}">
                                                        <div class="text-muted small mt-1">Lý do: ${log.actionReason}</div>
                                                    </c:if>
                                                </div>
                                            </c:if>

                                            <c:if test="${log.fieldName != null and log.fieldName != 'status'}">
                                                <div class="mb-2">
                                                    <i class="fa-solid fa-pen text-warning me-1"></i>
                                                    <strong>Sửa ${log.fieldName}:</strong>
                                                    <span class="text-danger text-decoration-line-through">${log.oldValue}</span>
                                                    <i class="fa-solid fa-arrow-right mx-1 text-muted"></i>
                                                    <span class="text-success fw-bold">${log.newValue}</span>
                                                    <c:if test="${not empty log.actionReason}">
                                                        <div class="text-muted small mt-1">Lý do: ${log.actionReason}</div>
                                                    </c:if>
                                                </div>
                                            </c:if>

                                            <c:if test="${not empty log.employeeName}">
                                                <div class="text-muted small mt-1">
                                                    <i class="fa-regular fa-user me-1"></i>Nhân viên: ${log.employeeName}
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <div id="historyNoMatch" class="alert alert-warning mt-3 hidden-item mb-0">
                            Không tìm thấy kết quả phù hợp với bộ lọc hiện tại.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        <script>
                            (function () {
                                const input = document.getElementById("historySearchInput");
                                const items = Array.from(document.querySelectorAll(".history-contract-item"));
                                const emptyState = document.getElementById("historyNoMatch");
                                const countNode = document.getElementById("historyVisibleCount");
                                if (!input || !items.length || !emptyState || !countNode)
                                    return;

                                const normalize = (value) => (value || "").toLowerCase().trim();
                                const applyFilter = () => {
                                    const keyword = normalize(input.value);
                                    let visible = 0;
                                    items.forEach((item) => {
                                        const show = !keyword || normalize(item.dataset.search).includes(keyword);
                                        item.classList.toggle("hidden-item", !show);
                                        if (show)
                                            visible++;
                                    });
                                    countNode.textContent = visible;
                                    emptyState.classList.toggle("hidden-item", visible !== 0);
                                };

                                input.addEventListener("input", applyFilter);
                            })();
        </script>
        <script>
            async function runSchedulerNow() {
                const btn = document.getElementById('btnRunScheduler');
                btn.disabled = true;
                btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-1"></i>Đang chạy...';

                try {
                    const response = await fetch('${pageContext.request.contextPath}/v1/manager/scheduler/run-now', {
                        method: 'POST',
                        headers: {'X-Requested-With': 'XMLHttpRequest'}
                    });
                    const data = await response.json();

                    if (data.success) {
                        btn.className = 'btn btn-outline-success';
                        btn.innerHTML = '<i class="fa-solid fa-circle-check me-1"></i>' + data.message;
                    } else {
                        btn.className = 'btn btn-outline-danger';
                        btn.innerHTML = '<i class="fa-solid fa-triangle-exclamation me-1"></i>' + data.message;
                    }
                } catch (err) {
                    btn.className = 'btn btn-outline-danger';
                    btn.innerHTML = '<i class="fa-solid fa-circle-xmark me-1"></i>Lỗi kết nối';
                } finally {
                    setTimeout(() => {
                        btn.disabled = false;
                        btn.className = 'btn btn-outline-warning';
                        btn.innerHTML = '<i class="fa-solid fa-play me-1"></i>Chạy Scheduler';
                    }, 5000);
                }
            }
        </script>
    </body>
</html>
