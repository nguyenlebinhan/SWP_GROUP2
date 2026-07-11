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
                <!-- Hàng 1: Tiêu đề (Trái) và Nút Run Now (Sát lề Phải) -->
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-3">
                    <div>
                        <h5 class="fw-bold mb-1">
                            <c:choose>
                                <c:when test="${param.scope == 'own'}">Lịch sử hợp đồng của bản thân</c:when>
                                <c:otherwise>Lịch sử hợp đồng nhân viên</c:otherwise>
                            </c:choose>
                        </h5>
                        <div class="text-muted small"><span id="historyVisibleCount">${contracts.size()}</span> / ${contracts.size()} hợp đồng</div>
                    </div>

                    <!-- Nút này sẽ được đẩy sát lề phải nhờ justify-content-between -->
                    <c:if test="${isHrStaffRole and param.scope != 'own'}">
                        <button type="button" class="btn btn-outline-warning" onclick="runSchedulerNow()" id="btnRunScheduler">
                            <i class="fa-solid fa-play me-1"></i>Run Scheduler Now
                        </button>
                    </c:if>
                </div>

                <!-- Hàng 2: Search và Phân loại hợp đồng (Nằm dưới hàng trên) -->
                <div class="d-flex gap-2 flex-wrap mb-4">
                    <input type="text" id="historySearchInput" class="form-control" style="width:280px;" placeholder="Tìm theo mã hợp đồng hoặc nhân viên">
                    <select id="historyTypeFilter" class="form-select" style="width:220px;">
                        <option value="">Tất cả loại hợp đồng</option>
                        <option value="INTERNSHIP">Internship</option>
                        <option value="PROBATION">Probation</option>
                        <option value="FIXED_TERM">Fixed-term</option>
                        <option value="INDEFINITE">Indefinite</option>
                    </select>
                </div>


                <c:if test="${param.scope == 'own' and not empty employee}">
                    <div class="alert alert-secondary">
                        <strong>${employee.fullName}</strong> (${employee.employeeCode})
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${empty contracts}">
                        <div class="alert alert-info mb-0">Chưa có dữ liệu hợp đồng để hiển thị.</div>
                    </c:when>
                    <c:otherwise>
                        <div id="historyContractList">
                            <c:forEach var="contract" items="${contracts}">
                                <c:set var="employeeInfo" value="${employeeMap[contract.employeeId]}" />
                                <div class="contract-item history-contract-item"
                                     data-type="${contract.contractType}"
                                     data-search="${contract.contractCode} ${employeeInfo.employeeCode} ${employeeInfo.fullName}">
                                    <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                                        <div>
                                            <h6 class="mb-1">${empty contract.contractCode ? contract.contractId : contract.contractCode}</h6>
                                            <div class="text-muted mb-2">${employeeInfo.fullName} (${employeeInfo.employeeCode})</div>
                                            <div class="d-flex flex-wrap gap-2">
                                                <span class="meta-chip"><i class="fa-regular fa-id-badge"></i>${contract.contractType}</span>
                                                <span class="meta-chip"><i class="fa-regular fa-calendar"></i><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></span>
                                                <span class="meta-chip">
                                                    <i class="fa-regular fa-calendar-check"></i>
                                                    <c:choose>
                                                        <c:when test="${empty contract.endDate}">Không xác định thời hạn</c:when>
                                                        <c:otherwise><fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy"/></c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <span class="meta-chip"><i class="fa-regular fa-clock"></i>
                                                    <c:choose>
                                                        <c:when test="${contract.status == 'PENDING_APPROVAL'}"><span style="background:#fef3c7;color:#92400e;border:1px solid #fbbf24;padding:2px 10px;border-radius:999px;font-size:12px;">Chờ duyệt</span></c:when>
                                                        <c:when test="${contract.status == 'PENDING_ACTIVATION'}"><span style="background:#ede9fe;color:#5b21b6;border:1px solid #a78bfa;padding:2px 10px;border-radius:999px;font-size:12px;">Chờ hiệu lực</span></c:when>
                                                        <c:when test="${contract.status == 'ACTIVE'}"><span style="background:#d1fae5;color:#065f46;border:1px solid #34d399;padding:2px 10px;border-radius:999px;font-size:12px;">Đang hiệu lực</span></c:when>
                                                        <c:when test="${contract.status == 'EXPIRED'}"><span style="background:#f1f5f9;color:#475569;border:1px solid #94a3b8;padding:2px 10px;border-radius:999px;font-size:12px;">Đã hết hạn</span></c:when>
                                                        <c:when test="${contract.status == 'TERMINATED'}"><span style="background:#ffe4e6;color:#9f1239;border:1px solid #fb7185;padding:2px 10px;border-radius:999px;font-size:12px;">Đã chấm dứt</span></c:when>
                                                        <c:when test="${contract.status == 'CANCELLED'}"><span style="background:#f8fafc;color:#334155;border:1px solid #cbd5e1;padding:2px 10px;border-radius:999px;font-size:12px;">Đã hủy</span></c:when>
                                                        <c:when test="${contract.status == 'REJECTED'}"><span style="background:#fef2f2;color:#991b1b;border:1px solid #fca5a5;padding:2px 10px;border-radius:999px;font-size:12px;">Bị từ chối</span></c:when>
                                                        <c:otherwise>${contract.status}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                        </div>
                                        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/v1/manager/contract/detail?contractId=${contract.contractId}">
                                            Chi tiết
                                        </a>
                                    </div>
                                    <c:if test="${not empty contract.note}">
                                        <div class="mt-3 text-muted small">${contract.note}</div>
                                    </c:if>
                                </div>
                            </c:forEach>
                        </div>
                        <div id="historyNoMatch" class="alert alert-warning mt-3 hidden-item mb-0">
                            Không tìm thấy hợp đồng phù hợp với bộ lọc hiện tại.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            (function () {
                const input = document.getElementById("historySearchInput");
                const typeFilter = document.getElementById("historyTypeFilter");
                const items = Array.from(document.querySelectorAll(".history-contract-item"));
                const emptyState = document.getElementById("historyNoMatch");
                const countNode = document.getElementById("historyVisibleCount");
                if (!input || !typeFilter || !items.length || !emptyState || !countNode) {
                    return;
                }

                const normalize = (value) => (value || "").toLowerCase().trim();
                const applyFilter = () => {
                    const keyword = normalize(input.value);
                    const typeValue = normalize(typeFilter.value);
                    let visible = 0;

                    items.forEach((item) => {
                        const matchesKeyword = !keyword || normalize(item.dataset.search).includes(keyword);
                        const matchesType = !typeValue || normalize(item.dataset.type) === typeValue;
                        const show = matchesKeyword && matchesType;
                        item.classList.toggle("hidden-item", !show);
                        if (show) {
                            visible++;
                        }
                    });

                    countNode.textContent = visible;
                    emptyState.classList.toggle("hidden-item", visible !== 0);
                };

                input.addEventListener("input", applyFilter);
                typeFilter.addEventListener("change", applyFilter);
            })();
        </script>
        <script>
            async function runSchedulerNow() {
                const btn = document.getElementById('btnRunScheduler');
                btn.disabled = true;
                btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-1"></i>Running...';

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
                        btn.innerHTML = '<i class="fa-solid fa-play me-1"></i>Run Scheduler Now';
                    }, 5000);
                }
            }
        </script>
    </body>
</html>
