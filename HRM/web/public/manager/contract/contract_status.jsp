<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Tổng quan hợp đồng - HRM</title>
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
                <jsp:param name="title" value="Tổng quan hợp đồng" />
                <jsp:param name="backUrl" value="/v1/manager/dashboard" />
            </jsp:include>

            <div class="page-card">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
                    <div>
                        <h5 class="fw-bold mb-1">Tổng quan hợp đồng lao động</h5>
                        <div class="text-muted small">
                            <span id="contractVisibleCount">${contracts.size()}</span> / ${contracts.size()} hợp đồng
                        </div>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <input type="text" id="searchInput" class="form-control" style="width:220px;" placeholder="Tìm mã hợp đồng / nhân viên">
                        <select id="typeFilter" class="form-select" style="width:180px;">
                            <option value="">Tất cả loại</option>
                            <option value="INTERNSHIP">Thực tập</option>
                            <option value="PROBATION">Thử việc</option>
                            <option value="FIXED_TERM">Có thời hạn</option>
                            <option value="INDEFINITE">Không xác định</option>
                        </select>
                        <select id="statusFilter" class="form-select" style="width:180px;">
                            <option value="">Tất cả trạng thái</option>
                            <option value="PENDING_APPROVAL">Chờ duyệt</option>
                            <option value="PENDING_ACTIVATION">Chờ hiệu lực</option>
                            <option value="ACTIVE">Đang hiệu lực</option>
                            <option value="EXPIRED">Đã hết hạn</option>
                            <option value="TERMINATED">Đã chấm dứt</option>
                            <option value="CANCELLED">Đã hủy</option>
                            <option value="REJECTED">Bị từ chối</option>
                        </select>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${empty contracts}">
                        <div class="alert alert-info mb-0">Không có hợp đồng nào.</div>
                    </c:when>
                    <c:otherwise>
                        <div id="contractList">
                            <c:forEach var="contract" items="${contracts}">
                                <c:set var="emp" value="${employeeMap[contract.employeeId]}" />
                                <div class="contract-item contract-card"
                                     data-type="${contract.contractType}"
                                     data-status="${contract.status}"
                                     data-search="${contract.contractCode} ${emp.employeeCode} ${emp.fullName}">
                                    <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                                        <div>
                                            <h6 class="mb-1">${empty contract.contractCode ? contract.contractId : contract.contractCode}</h6>
                                            <div class="text-muted mb-2">${emp.fullName} (${emp.employeeCode})</div>
                                            <div class="d-flex flex-wrap gap-2">
                                                <span class="meta-chip"><i class="fa-regular fa-id-badge"></i>${contract.contractType.displayName}</span>
                                                <span class="meta-chip"><i class="fa-regular fa-calendar"></i><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></span>
                                                <span class="meta-chip"><i class="fa-solid fa-money-bill-wave"></i><fmt:formatNumber value="${contract.salary}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND</span>
                                                <span class="meta-chip">
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
                                        <div class="d-flex gap-2 flex-wrap">
                                            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/v1/manager/contract/detail?contractId=${contract.contractId}">
                                                Chi tiết
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            document.addEventListener('DOMContentLoaded', function () {
                const searchInput = document.getElementById('searchInput');
                const typeFilter = document.getElementById('typeFilter');
                const statusFilter = document.getElementById('statusFilter');

                function filterContracts() {
                    const kw = searchInput.value.toLowerCase().trim();
                    const type = typeFilter.value;
                    const status = statusFilter.value;
                    const cards = document.querySelectorAll('.contract-card');
                    let visible = 0;

                    cards.forEach(function (card) {
                        const searchText = card.dataset.search ? card.dataset.search.toLowerCase() : '';
                        const cardType = card.dataset.type || '';
                        const cardStatus = card.dataset.status || '';

                        const matchSearch = !kw || searchText.includes(kw);
                        const matchType = !type || cardType === type;
                        const matchStatus = !status || cardStatus === status;

                        if (matchSearch && matchType && matchStatus) {
                            card.classList.remove('hidden-item');
                            visible++;
                        } else {
                            card.classList.add('hidden-item');
                        }
                    });

                    document.getElementById('contractVisibleCount').textContent = visible;
                }

                searchInput.addEventListener('input', filterContracts);
                typeFilter.addEventListener('change', filterContracts);
                statusFilter.addEventListener('change', filterContracts);
            });
        </script>
    </body>
</html>