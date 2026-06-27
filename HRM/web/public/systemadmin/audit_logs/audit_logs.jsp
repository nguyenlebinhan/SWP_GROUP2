<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Nhật ký hệ thống – HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
            body {
                background: #f5f6fa;
                font-family: 'Segoe UI', sans-serif;
            }
            .main-content {
                margin-left: 250px;
                padding: 30px;
            }
            .search-bar {
                display: flex;
                gap: 10px;
                align-items: center;
                flex-wrap: wrap;
                background: white;
                padding: 16px 20px;
                border-radius: 10px;
                box-shadow: 0 1px 4px rgba(0,0,0,.06);
                margin-bottom: 20px;
            }
            .search-bar select {
                padding: 8px 12px;
                border: 1px solid #e5e7eb;
                border-radius: 7px;
                font-size: 14px;
                background: white;
                outline: none;
            }
            .btn-search {
                background: #6366f1;
                color: white;
                border: none;
                padding: 8px 18px;
                border-radius: 7px;
                font-size: 14px;
                font-weight: 600;
                cursor: pointer;
            }
            .btn-search:hover { background: #4f46e5; }

            .table thead th {
                font-weight: 600;
                font-size: 13px;
                letter-spacing: .4px;
                padding: 14px 16px;
                border: none;
            }
            .table tbody tr { vertical-align: middle; }
            .table tbody td {
                padding: 10px 16px;
                font-size: 13px;
                border-bottom: 1px solid #f3f4f6;
            }
            .table tbody tr:hover { background: #fafafa; }

            .badge-success {
                background: #d1fae5;
                color: #065f46;
                padding: 3px 10px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
            }
            .badge-failed {
                background: #fee2e2;
                color: #991b1b;
                padding: 3px 10px;
                border-radius: 20px;
                font-size: 12px;
                font-weight: 600;
            }
            .mono {
                font-family: 'Consolas', monospace;
                color: #6b7280;
                font-size: 12px;
            }
            .cell-value {
                max-width: 260px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .empty-state {
                text-align: center;
                padding: 60px 0;
                color: #9ca3af;
            }
            .total-label {
                font-size: 13px;
                color: #6b7280;
            }
        </style>
    </head>
    <body>

        <jsp:include page="/public/components/systemAdminSideBar.jsp" />

        <div class="main-content">
            <jsp:include page="/public/components/systemAdminTopBar.jsp">
                <jsp:param name="title" value="Nhật ký hệ thống" />
            </jsp:include>

            <%-- Bộ lọc số dòng hiển thị --%>
            <form method="get"
                  action="${pageContext.request.contextPath}/v1/systemadmin/audit_logs/list"
                  class="search-bar">
                <span class="total-label">Hiển thị</span>
                <select name="limit" onchange="this.form.submit()">
                    <c:forEach var="opt" items="50,100,200,500,1000">
                        <option value="${opt}" <c:if test="${limit == opt}">selected</c:if>>${opt} dòng gần nhất</option>
                    </c:forEach>
                </select>
                <button type="submit" class="btn-search">Lọc</button>
            </form>

            <div class="card">
                <div class="card-body p-0">
                    <table class="table mb-0">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Thời gian</th>
                                <th>User ID</th>
                                <th>Hành động</th>
                                <th>Bảng</th>
                                <th>Record</th>
                                <th>Giá trị cũ</th>
                                <th>Giá trị mới</th>
                                <th>IP</th>
                                <th>Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty logs}">
                                    <tr>
                                        <td colspan="10"><div class="empty-state">Chưa có nhật ký nào</div></td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="log" items="${logs}">
                                        <tr>
                                            <td class="mono">${log.logId}</td>
                                            <td class="mono">
                                                <fmt:formatDate value="${log.createdAt}" pattern="dd/MM/yyyy HH:mm:ss"/>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty log.userId}"><span class="mono">—</span></c:when>
                                                    <c:otherwise>${log.userId}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><strong>${log.action}</strong></td>
                                            <td>${log.tableName}</td>
                                            <td class="mono">
                                                <c:choose>
                                                    <c:when test="${empty log.recordId}">—</c:when>
                                                    <c:otherwise>${log.recordId}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="cell-value" title="${log.oldValue}">${log.oldValue}</td>
                                            <td class="cell-value" title="${log.newValue}">${log.newValue}</td>
                                            <td class="mono">${log.ipAddress}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${log.status eq 'FAILED'}">
                                                        <span class="badge-failed">${log.status}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge-success">${log.status}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                    <div class="pagination-wrap" style="padding:14px 20px;border-top:1px solid #f3f4f6;">
                        <span class="total-label">Tổng <strong>${logs.size()}</strong> bản ghi gần nhất</span>
                    </div>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
