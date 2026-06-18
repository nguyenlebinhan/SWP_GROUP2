<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Tuyển Dụng</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                margin: 0;
                background: #f4f6fb;
            }
            .main-content {
                margin-left: 250px;
                padding: 32px 36px;
            }
            .stage-tabs {
                display: flex;
                gap: 0;
                margin-bottom: 24px;
                border-bottom: 2px solid #e0e0e0;
            }
            .stage-tab {
                padding: 10px 28px;
                cursor: pointer;
                font-weight: 500;
                color: #666;
                border-bottom: 3px solid transparent;
                margin-bottom: -2px;
                text-decoration: none;
            }
            .stage-tab.active {
                color: #1565C0;
                border-bottom-color: #1565C0;
            }
            .badge-applied   {
                background: #e3f2fd;
                color: #1565C0;
            }
            .badge-interview {
                background: #fff8e1;
                color: #f57f17;
            }
            .badge-probation {
                background: #e8f5e9;
                color: #2e7d32;
            }
            .badge-rejected  {
                background: #fce4ec;
                color: #c62828;
            }
            .result-btn {
                min-width: 110px;
                font-size: 13px;
            }
        </style>
    </head>
    <body>

        <jsp:include page="/public/components/managerSideBar.jsp" />

        <div class="main-content">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Tuyển Dụng" />
            </jsp:include>

            <%-- Alert --%>
            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success alert-dismissible fade show">
                    ${sessionScope.success}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="success" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-danger alert-dismissible fade show">
                    ${sessionScope.error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="error" scope="session"/>
            </c:if>

            <%-- Toolbar --%>
            <div class="d-flex justify-content-between align-items-center mb-3">

                <c:if test="${sessionScope.userPermissions.contains('PROCESS_RECRUITMENT')}">
                    <a href="${pageContext.request.contextPath}/v1/manager/recruitment-import"
                       class="btn btn-primary">
                        <i class="fa-solid fa-file-arrow-up"></i> Upload hồ sơ từ Excel
                    </a>
                </c:if>
            </div>

            <%-- Stage Tabs --%>
            <div class="stage-tabs">
                <a class="stage-tab ${currentStage == 'APPLIED'   ? 'active' : ''}"
                   href="?stage=APPLIED">Hồ sơ ứng tuyển</a>
                <a class="stage-tab ${currentStage == 'INTERVIEW' ? 'active' : ''}"
                   href="?stage=INTERVIEW">Hồ sơ phỏng vấn</a>
                <a class="stage-tab ${currentStage == 'PROBATION' ? 'active' : ''}"
                   href="?stage=PROBATION">Hồ sơ thử việc</a>
            </div>

            <%-- Search --%>
            <form method="get" class="d-flex gap-2 mb-3">
                <input type="hidden" name="stage" value="${currentStage}"/>
                <input type="text" name="keyword" class="form-control" style="max-width:280px"
                       placeholder="Tìm tên ứng viên..." value="${keyword}"/>
                <button class="btn btn-outline-primary" type="submit">
                    <i class="fa fa-search"></i> Tìm
                </button>
                <c:if test="${not empty keyword}">
                    <a href="?stage=${currentStage}" class="btn btn-outline-secondary">Xóa</a>
                </c:if>
            </form>

            <%-- Table --%>
            <div class="card shadow-sm">
                <div class="card-body p-0">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>Phòng ban</th>
                                <th>Ứng viên</th>
                                <th>Thời gian nhận</th>
                                <th>Chức vụ</th>
                                <th class="text-end">Kết quả</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty candidates}">
                                    <tr>
                                        <td colspan="5" class="text-center text-muted py-4">
                                            Không có hồ sơ nào.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="c" items="${candidates}">
                                        <tr>
                                            <td>${c.departmentName}</td>
                                            <td>
                                                <div class="d-flex align-items-center gap-2">
                                                    <i class="fa-solid fa-circle-user fs-4 text-secondary"></i>
                                                    <span>${c.fullName}</span>
                                                </div>
                                            </td>
                                            <td>${c.createdAt}</td>
                                            <td>${c.positionName}</td>
                                            <td class="text-end">
                                                <c:choose>
                                                    <%-- Đã có kết quả --%>
                                                    <c:when test="${c.stage == 'INTERVIEW'}">
                                                        <span class="badge badge-interview px-3 py-2">Hồ sơ đậu</span>
                                                    </c:when>
                                                    <c:when test="${c.stage == 'REJECTED'}">
                                                        <span class="badge badge-rejected px-3 py-2">Hồ sơ rớt</span>
                                                    </c:when>
                                                    <%-- Chưa duyệt --%>
                                                    <c:otherwise>
                                                        <c:choose>
                                                            <%-- Có quyền duyệt → hiện nút xem để vào trang duyệt --%>
                                                            <c:when test="${sessionScope.userPermissions.contains('PROCESS_RECRUITMENT')}">
                                                                <a href="${pageContext.request.contextPath}/v1/employee/recruitment-detail?id=${c.candidateId}"
                                                                   class="btn btn-sm btn-outline-primary result-btn">
                                                                    Ấn vào để duyệt
                                                                </a>
                                                            </c:when>
                                                            <%-- Chỉ có quyền xem --%>
                                                            <c:otherwise>
                                                                <a href="${pageContext.request.contextPath}/v1/employee/recruitment-detail?id=${c.candidateId}"
                                                                   class="btn btn-sm btn-outline-secondary result-btn">
                                                                    Xem chi tiết
                                                                </a>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>