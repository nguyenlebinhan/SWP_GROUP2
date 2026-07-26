<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Hợp đồng của tôi - HRM</title>
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
            @media (max-width: 768px) {
                .main {
                    margin-left: 0;
                    padding: 16px;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/employeeSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/employeeTopBar.jsp">
                <jsp:param name="title" value="Hợp đồng của tôi" />
                <jsp:param name="backUrl" value="/v1/employee/dashboard" />
            </jsp:include>

            <div class="page-card">
                <h5 class="fw-bold mb-3">Hợp đồng của tôi</h5>

                <c:if test="${not empty employee}">
                    <div class="alert alert-secondary mb-4">
                        <strong>${employee.fullName}</strong> (${employee.employeeCode})
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${empty contracts}">
                        <div class="alert alert-info mb-0">Bạn chưa có hợp đồng lao động nào.</div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="contract" items="${contracts}">
                            <div class="contract-item">
                                <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                                    <div>
                                        <h6 class="mb-1">${empty contract.contractCode ? contract.contractId : contract.contractCode}</h6>
                                        <div class="d-flex flex-wrap gap-2 mt-2">
                                            <span class="meta-chip"><i class="fa-regular fa-id-badge"></i>${contract.contractType.displayName}</span>
                                            <span class="meta-chip"><i class="fa-regular fa-calendar"></i><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></span>
                                            <c:if test="${not empty contract.endDate}">
                                                <span class="meta-chip"><i class="fa-regular fa-calendar-check"></i><fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy"/></span>
                                            </c:if>
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
                                    <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/v1/employee/contract/detail?contractId=${contract.contractId}">
                                        Chi tiết
                                    </a>
                                </div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>