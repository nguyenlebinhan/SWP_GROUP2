<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Lịch sử phụ lục - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
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
                box-shadow: 0 8px 24px rgba(15,23,42,0.08);
                padding: 24px;
            }
            .section-title {
                font-size: 18px;
                font-weight: 700;
                margin: 20px 0 12px;
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
                <jsp:param name="title" value="Lịch sử phụ lục" />
                <jsp:param name="backUrl" value="/v1/manager/contract/history" />
            </jsp:include>

            <div class="page-card">
                <c:if test="${not empty contract && not empty employee}">
                    <div class="alert alert-secondary mb-4">
                        <strong>Hợp đồng:</strong> ${contract.contractCode}
                        &nbsp;|&nbsp; <strong>Nhân viên:</strong> ${employee.fullName} (${employee.employeeCode})
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/v1/manager/contract/amendments" method="get" class="row g-3 mb-4">
                    <c:if test="${not empty contract}">
                        <input type="hidden" name="contractId" value="${contract.contractId}">
                    </c:if>

                    <div class="col-md-4">
                        <div class="input-group">
                            <span class="input-group-text"><i class="fa-solid fa-magnifying-glass"></i></span>
                            <input type="text" name="keyword" class="form-control" placeholder="Tìm mã phụ lục, lý do..." value="${currentKeyword}">
                        </div>
                    </div>

                    <div class="col-md-3">
                        <select name="type" class="form-select">
                            <option value="ALL" ${currentType == 'ALL' ? 'selected' : ''}>Tất cả loại</option>
                            <option value="TRANSFER" ${currentType == 'TRANSFER' ? 'selected' : ''}>Thuyên chuyển</option>
                            <option value="PROMOTION_DEMOTION" ${currentType == 'PROMOTION_DEMOTION' ? 'selected' : ''}>Thăng/Giáng chức</option>
                            <option value="SALARY_ADJUSTMENT" ${currentType == 'SALARY_ADJUSTMENT' ? 'selected' : ''}>Điều chỉnh lương</option>
                            <option value="COMBINED" ${currentType == 'COMBINED' ? 'selected' : ''}>Kết hợp</option>
                            <option value="POSITION_CHANGE" ${currentType == 'POSITION_CHANGE' ? 'selected' : ''}>Thăng/Giáng chức</option>
                        </select>
                    </div>

                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary w-100">
                            <i class="fa-solid fa-filter me-1"></i> Lọc
                        </button>
                    </div>

                    <div class="col-md-3 text-end">
                        <a href="${pageContext.request.contextPath}/v1/manager/contract/amendments" class="btn btn-outline-secondary">
                            Xóa lọc
                        </a>
                    </div>
                </form>

                <div class="section-title">Danh sách phụ lục</div>

                <c:choose>
                    <c:when test="${empty amendments}">
                        <div class="alert alert-info mb-0">Chưa có phụ lục nào.</div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-bordered table-hover">
                                <thead class="table-light">
                                    <tr>
                                        <th>Mã phụ lục</th>
                                        <th>Loại</th>
                                        <th>Ngày hiệu lực</th>
                                        <th>Phòng ban cũ → mới</th>
                                        <th>Vị trí cũ → mới</th>
                                        <th>Lý do</th>
                                        <th>Người tạo</th>
                                        <th>Ngày tạo</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="amd" items="${amendments}">
                                        <tr>
                                            <td>${amd.amendmentCode}</td>
                                            <td>
                                                <span class="badge bg-info">${amd.amendmentType}</span>
                                            </td>
                                            <td><fmt:formatDate value="${amd.effectiveDate}" pattern="dd/MM/yyyy"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty amd.oldDepartmentId && not empty amd.newDepartmentId}">
                                                        ${amd.oldDepartmentId} → ${amd.newDepartmentId}
                                                    </c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty amd.oldPositionId && not empty amd.newPositionId}">
                                                        ${amd.oldPositionId} → ${amd.newPositionId}
                                                    </c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${amd.reason}</td>
                                            <td>${amd.createdBy}</td>
                                            <td><fmt:formatDate value="${amd.createdAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>