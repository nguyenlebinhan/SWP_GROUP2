<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Chấm dứt hợp đồng - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: "Segoe UI", sans-serif; }
        .main { margin-left: 250px; padding: 24px; }
        .page-card { background: #fff; border-radius: 16px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08); padding: 24px; }
        .contract-item { border: 1px solid #e2e8f0; border-radius: 14px; padding: 18px; background: #fff; }
        .contract-item + .contract-item { margin-top: 16px; }
        .meta-chip { display: inline-flex; align-items: center; gap: 6px; padding: 6px 10px; border-radius: 999px; background: #f8fafc; border: 1px solid #e2e8f0; font-size: 12px; }
        .hidden-item { display: none !important; }
        @media (max-width: 768px) {
            .main { margin-left: 0; padding: 16px; }
        }
    </style>
</head>
<body>
<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Chấm dứt hợp đồng" />
        <jsp:param name="backUrl" value="/v1/manager/dashboard" />
    </jsp:include>

    <c:if test="${not empty sessionScope.success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${sessionScope.success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="success" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session"/>
    </c:if>

    <div class="page-card">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
            <div>
                <h5 class="fw-bold mb-1">Hợp đồng đang hiệu lực</h5>
                <div class="text-muted small"><span id="visibleCount">${contracts.size()}</span> / ${contracts.size()} hợp đồng</div>
            </div>
            <div class="d-flex gap-2 flex-wrap">
                <input type="text" id="searchInput" class="form-control" style="width:280px;" placeholder="Tìm theo mã hợp đồng hoặc nhân viên">
                <select id="typeFilter" class="form-select" style="width:220px;">
                    <option value="">Tất cả loại hợp đồng</option>
                    <option value="PROBATION">Probation</option>
                    <option value="INTERNSHIP">Internship</option>
                    <option value="FIXED_TERM">Fixed-term</option>
                    <option value="INDEFINITE">Indefinite</option>
                </select>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty contracts}">
                <div class="alert alert-info mb-0">Không có hợp đồng đang hiệu lực nào để chấm dứt.</div>
            </c:when>
            <c:otherwise>
                <div id="contractList">
                    <c:forEach var="contract" items="${contracts}">
                        <div class="contract-item terminate-contract-item"
                             data-type="${contract.contractType}"
                             data-search="${contract.contractCode} ${contract.employeeCode} ${contract.employeeFullName}">
                            <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                                <div>
                                    <h6 class="mb-1">${empty contract.contractCode ? contract.contractId : contract.contractCode}</h6>
                                    <div class="text-muted mb-2">${contract.employeeFullName} (${contract.employeeCode})</div>
                                    <div class="d-flex flex-wrap gap-2">
                                        <span class="meta-chip"><i class="fa-regular fa-building"></i>${empty contract.departmentName ? 'Chưa cập nhật' : contract.departmentName}</span>
                                        <span class="meta-chip"><i class="fa-regular fa-id-badge"></i>${empty contract.positionName ? 'Chưa cập nhật' : contract.positionName}</span>
                                        <span class="meta-chip"><i class="fa-regular fa-calendar"></i><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></span>
                                        <span class="meta-chip"><i class="fa-regular fa-calendar-check"></i>
                                            <c:choose>
                                                <c:when test="${empty contract.endDate}">Không xác định thời hạn</c:when>
                                                <c:otherwise><fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy"/></c:otherwise>
                                            </c:choose>
                                        </span>
                                        <span class="meta-chip"><i class="fa-regular fa-file-lines"></i>${contract.contractType}</span>
                                    </div>
                                </div>
                                <div class="d-flex gap-2 flex-wrap">
                                    <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/v1/manager/contract/terminate?id=${contract.contractId}">
                                        <i class="fa-solid fa-stop me-1"></i>Review
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
                <div id="noMatch" class="alert alert-warning mt-3 hidden-item mb-0">
                    Không tìm thấy hợp đồng phù hợp với bộ lọc hiện tại.
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    (function () {
        const input = document.getElementById("searchInput");
        const typeFilter = document.getElementById("typeFilter");
        const items = Array.from(document.querySelectorAll(".terminate-contract-item"));
        const emptyState = document.getElementById("noMatch");
        const countNode = document.getElementById("visibleCount");
        if (!input || !typeFilter || !items.length || !emptyState || !countNode) return;

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
                if (show) visible++;
            });

            countNode.textContent = visible;
            emptyState.classList.toggle("hidden-item", visible !== 0);
        };

        input.addEventListener("input", applyFilter);
        typeFilter.addEventListener("change", applyFilter);
    })();
</script>
</body>
</html>
