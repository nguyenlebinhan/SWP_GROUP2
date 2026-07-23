<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Hợp Đồng Hiện Tại - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background: #f5f6fa;
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            }
            .main {
                margin-left: 250px;
                padding: 30px;
            }
            .contract-profile-card {
                background: white;
                border-radius: 16px;
                box-shadow: 0 4px 20px rgba(0,0,0,0.08);
                overflow: hidden;
                border: none;
            }
            .card-header-custom {
                background: linear-gradient(135deg, #4e73df 0%, #224abe 100%);
                color: white;
                padding: 20px 30px;
                border: none;
            }
            .info-label {
                font-weight: 600;
                color: #6c757d;
                font-size: 0.9rem;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }
            .info-value {
                font-size: 1.1rem;
                color: #2d3436;
                font-weight: 500;
            }
            .data-row {
                padding: 15px 0;
                border-bottom: 1px solid #edf2f7;
            }
            .data-row:last-child {
                border-bottom: none;
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/employeeSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/employeeTopBar.jsp">
                <jsp:param name="title" value="Hợp Đồng Hiện Tại" />
                <jsp:param name="backUrl" value="/v1/employee/dashboard" />
            </jsp:include>

            <div class="container-fluid mt-4">
                <c:choose>
                    <c:when test="${empty activeContract}">
                        <div class="alert alert-info d-flex align-items-center shadow-sm" role="alert">
                            <i class="fa-solid fa-circle-info me-3 fs-4"></i>
                            <div>
                                Bạn hiện tại không có hợp đồng lao động nào đang hiệu lực hoặc sắp có hiệu lực.
                            </div>
                        </div>
                    </c:when>

                    <c:when test="${activeContract.status == 'PENDING_ACTIVATION'}">
                        <div class="alert alert-warning d-flex align-items-center shadow-sm" role="alert">
                            <i class="fa-solid fa-clock me-3 fs-4"></i>
                            <div>
                                <strong>Hợp đồng sắp có hiệu lực.</strong> Hợp đồng này sẽ có hiệu lực từ ngày <strong><fmt:formatDate value="${activeContract.effectiveDate}" pattern="dd/MM/yyyy" /></strong>.
                            </div>
                        </div>
                        <div class="row justify-content-center mt-3">
                            <div class="col-lg-8">
                                <div class="card contract-profile-card">
                                    <div class="card-header-custom d-flex justify-content-between align-items-center">
                                        <h4 class="mb-0">Hợp Đồng Sắp Có Hiệu Lực</h4>
                                        <span class="badge bg-warning text-dark fs-6">
                                            <i class="fa-solid fa-clock me-1"></i>
                                            Sắp hiệu lực
                                        </span>
                                    </div>
                                    <div class="card-body p-4">
                                        <div class="row">
                                            <div class="col-md-6">
                                                <div class="data-row">
                                                    <div class="info-label">Số hợp đồng</div>
                                                    <div class="info-value">${activeContract.contractCode != null ? activeContract.contractCode : activeContract.contractId}</div>
                                                </div>
                                                <div class="data-row">
                                                    <div class="info-label">Loại hợp đồng</div>
                                                    <div class="info-value">
                                                        <span class="badge bg-info text-dark">${activeContract.contractType.displayName}</span>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="col-md-6">
                                                <div class="data-row">
                                                    <div class="info-label">Mức lương cơ bản</div>
                                                    <div class="info-value">
                                                        <fmt:formatNumber value="${activeContract.salary}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                                    </div>
                                                </div>
                                                <div class="data-row">
                                                    <div class="info-label">Ngày bắt đầu</div>
                                                    <div class="info-value">
                                                        <strong><fmt:formatDate value="${activeContract.effectiveDate}" pattern="dd/MM/yyyy" /></strong>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="row justify-content-center">
                            <div class="col-lg-8">
                                <div class="card contract-profile-card">
                                    <div class="card-header-custom d-flex justify-content-between align-items-center">
                                        <h4 class="mb-0">Thông Tin Hợp Đồng Hiện Tại</h4>
                                        <span class="badge bg-white text-primary fs-6">
                                            <i class="fa-solid fa-file-signature me-1"></i>
                                            ${activeContract.status}
                                        </span>
                                    </div>
                                    <div class="card-body p-4">
                                        <div class="row">
                                            <%-- Column 1 --%>
                                            <div class="col-md-6">
                                                <div class="data-row">
                                                    <div class="info-label">Số hợp đồng</div>
                                                    <div class="info-value">${activeContract.contractCode != null ? activeContract.contractCode : activeContract.contractId}</div>
                                                </div>
                                                <div class="data-row">
                                                    <div class="info-label">Loại hợp đồng</div>
                                                    <div class="info-value">
                                                        <span class="badge bg-info text-dark">${activeContract.contractType.displayName}</span>
                                                    </div>
                                                </div>
                                                <div class="data-row">
                                                    <div class="info-label">Vị trí công tác</div>
                                                    <div class="info-value">${employee.positionName != null ? employee.positionName : 'Đang cập nhật'}</div>
                                                </div>
                                            </div>

                                            <div class="col-md-6">
                                                <div class="data-row">
                                                    <div class="info-label">Mức lương cơ bản</div>
                                                    <div class="info-value">
                                                        <fmt:formatNumber value="${activeContract.salary}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND
                                                    </div>
                                                </div>
                                                <c:if test="${not empty activeContract.signedDate}">
                                                    <div class="data-row">
                                                        <div class="info-label">Ngày ký</div>
                                                        <div class="info-value">
                                                            <fmt:formatDate value="${activeContract.signedDate}" pattern="dd/MM/yyyy" />
                                                        </div>
                                                    </div>
                                                </c:if>
                                                <div class="data-row">
                                                    <div class="info-label">Ngày hiệu lực</div>
                                                    <div class="info-value">
                                                        <fmt:formatDate value="${activeContract.effectiveDate}" pattern="dd/MM/yyyy" />
                                                    </div>
                                                </div>
                                                <div class="data-row">
                                                    <div class="info-label">Ngày hết hạn</div>
                                                    <div class="info-value">
                                                        <c:choose>
                                                            <c:when test="${activeContract.contractType == 'INDEFINITE' or empty activeContract.endDate}">
                                                                <span class="badge bg-success">Vô thời hạn</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <fmt:formatDate value="${activeContract.endDate}" pattern="dd/MM/yyyy" />
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="card-footer bg-light text-center py-3">
                                        <small class="text-muted">
                                            <i class="fa-solid fa-clock-rotate-left me-1"></i>
                                            Cập nhật lần cuối: <fmt:formatDate value="${activeContract.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                                        </small>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
