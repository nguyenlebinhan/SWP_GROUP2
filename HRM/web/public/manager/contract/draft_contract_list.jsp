<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Danh sách hợp đồng nháp - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    </head>
    <body>
        <jsp:include page="/public/components/managerSideBar.jsp" />
        <div class="main" style="margin-left: 250px; padding: 24px;">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Danh sách hợp đồng nháp" />
                <jsp:param name="backUrl" value="/v1/manager/contract/add" />
            </jsp:include>

            <div class="page-card">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h4 class="mb-0">Hợp đồng nháp</h4>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/v1/manager/contract/add">
                        <i class="fa-solid fa-plus me-1"></i> Tạo nháp mới
                    </a>
                </div>

                <c:if test="${empty drafts}">
                    <div class="text-center text-muted py-5">
                        <i class="fa-solid fa-file-circle-plus fa-2x mb-2"></i>
                        <p>Chưa có hợp đồng nháp nào.</p>
                    </div>
                </c:if>

                <c:if test="${not empty drafts}">
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead>
                                <tr>
                                    <th>Mã HĐ</th>
                                    <th>Nhân viên</th>
                                    <th>Loại HĐ</th>
                                    <th>Lần cập nhật</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="d" items="${drafts}">
                                <tr>
                                    <td>${d.contractCode}</td>
                                    <td>${d.employeeFullName} (${d.employeeCode})</td>
                                    <td>${d.contractType != null ? d.contractType.displayName : ''}</td>
                                    <td><fmt:formatDate value="${d.updatedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                <td>
                                    <div class="d-flex gap-1">
                                        <a class="btn btn-sm btn-outline-primary" 
                                           href="${pageContext.request.contextPath}/v1/manager/contract/add?draftId=${d.contractId}">
                                            <i class="fa-solid fa-pen"></i> Sửa
                                        </a>
                                        <a class="btn btn-sm btn-outline-danger" 
                                           href="${pageContext.request.contextPath}/v1/manager/contract/delete-draft?id=${d.contractId}"
                                           onclick="return confirm('Xóa nháp này?');">
                                            <i class="fa-solid fa-trash"></i> Xóa
                                        </a>
                                    </div>
                                </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:if>
            </div>
        </div>
    </body>
</html>