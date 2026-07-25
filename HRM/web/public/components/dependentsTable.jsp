<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="mt-4">
    <h6 class="fw-bold mb-3">Danh sách người phụ thuộc</h6>
    <c:choose>
        <c:when test="${empty dependents}">
            <div class="text-muted small">Chưa có người phụ thuộc đã duyệt.</div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table table-sm table-bordered align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th>#</th>
                            <th>Tên người phụ thuộc</th>
                            <th>Quan hệ</th>
                            <th>Ngày sinh</th>
                            <th>Mã số thuế</th>
                            <th>Ngày duyệt</th>
                            <c:if test="${not empty dependentStatusAction}">
                                <th>Trạng thái</th>
                            </c:if>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="dependent" items="${dependents}" varStatus="dependentStatus">
                            <tr>
                                <td>#${dependentStatus.index + 1}</td>
                                <td><c:out value="${dependent.fullName}"/></td>
                                <td><c:out value="${dependent.relationship}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty dependent.dateOfBirth}">
                                            <fmt:formatDate value="${dependent.dateOfBirth}" pattern="dd/MM/yyyy"/>
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                                <td><c:out value="${empty dependent.taxCode ? '-' : dependent.taxCode}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty dependent.approvedAt}">
                                            <fmt:formatDate value="${dependent.approvedAt}" pattern="dd/MM/yyyy HH:mm"/>
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                                <c:if test="${not empty dependentStatusAction}">
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty dependent.pendingStatus}">
                                                <span class="badge bg-warning text-dark">Chờ HR duyệt</span>
                                            </c:when>
                                            <c:otherwise>
                                                <form method="post" action="${dependentStatusAction}" class="m-0">
                                                    <input type="hidden" name="dependentId" value="${dependent.dependentId}">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm">
                                                        Yêu cầu ngừng tính
                                                    </button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>
