<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Qu&#7843;n l&#253; ph&#226;n quy&#7873;n - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
    <style>
        :root {
            --brand: #1f70c8;
            --brand-soft: #dbeafe;
            --ink: #101828;
            --muted: #667085;
            --line: #e8ebf0;
            --panel: #ffffff;
            --page: #f2f4f7;
        }

        * { box-sizing: border-box; }
        body {
            margin: 0;
            background: var(--page);
            color: var(--ink);
            font-family: "Segoe UI", Arial, sans-serif;
        }

        .main-content {
            min-height: 100vh;
            margin-left: 250px;
            padding: 30px;
            background: var(--page);
        }

        .page-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 24px;
        }

        .back-btn {
            width: 36px;
            height: 36px;
            background: #fff;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            color: #6b7280;
            text-decoration: none;
        }

        .page-header h5 {
            margin: 0;
            color: #0B0E2A;
            font-size: 18px;
            font-weight: 700;
        }

        .alert-flash {
            border: 0;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(16, 24, 40, .06);
            font-size: 14px;
            margin-bottom: 18px;
        }

        .permission-layout {
            display: grid;
            grid-template-columns: 280px minmax(680px, 1fr);
            gap: 20px;
            align-items: start;
        }

        .role-panel,
        .permission-panel {
            background: var(--panel);
            border: 1px solid #edf0f5;
            border-radius: 10px;
            box-shadow: 0 8px 22px rgba(16, 24, 40, .06);
        }

        .role-panel {
            min-height: 700px;
            padding: 20px 18px;
        }

        .panel-title {
            margin: 0 0 8px;
            color: #111827;
            font-size: 14px;
            font-weight: 800;
        }

        .add-role-btn {
            width: 100%;
            height: 38px;
            margin-bottom: 12px;
            border: 1px solid #9fc8f7;
            border-radius: 7px;
            background: #dcecff;
            color: var(--brand);
            font-size: 13px;
            font-weight: 800;
            cursor: not-allowed;
        }

        .role-list {
            display: grid;
            gap: 8px;
        }

        .role-item {
            min-height: 74px;
            padding: 12px 14px;
            display: grid;
            gap: 5px;
            border: 1px solid transparent;
            border-radius: 8px;
            background: #f3f4f6;
            color: #111827;
            text-decoration: none;
        }

        .role-item:hover {
            color: #111827;
            border-color: #b8d6fb;
            background: #eef6ff;
        }

        .role-item.active {
            border-color: #9fc8f7;
            background: #eef6ff;
        }

        .role-item strong {
            font-size: 14px;
            line-height: 1.2;
        }

        .role-item span {
            color: #6b7280;
            font-size: 12px;
            line-height: 1.2;
        }

        .count-pill {
            width: max-content;
            min-width: 34px;
            padding: 4px 10px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-radius: 999px;
            background: #d7f8df;
            color: #159947;
            font-size: 12px;
            font-weight: 800;
        }

        .permission-panel {
            min-height: 540px;
            padding: 24px 22px 28px;
        }

        .permission-heading {
            margin-bottom: 18px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
        }

        .permission-heading h2 {
            margin: 0;
            color: #111827;
            font-size: 15px;
            font-weight: 800;
        }

        .btn-save {
            height: 38px;
            min-width: 126px;
            padding: 0 18px;
            border: 0;
            border-radius: 7px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            background: var(--brand);
            color: #fff;
            font-size: 13px;
            font-weight: 800;
        }

        .btn-save:disabled {
            opacity: .55;
            cursor: not-allowed;
        }

        .permission-table-wrap {
            overflow: hidden;
            border-radius: 8px;
            border: 1px solid var(--line);
        }

        .permission-table {
            width: 100%;
            margin: 0;
            border-collapse: collapse;
            table-layout: fixed;
        }

        .permission-table thead th {
            height: 46px;
            background: #f3f5f8;
            color: #42526e;
            border-bottom: 1px solid var(--line);
            font-size: 12px;
            font-weight: 700;
            text-align: center;
        }

        .permission-table thead th:first-child {
            width: 42%;
            padding-left: 18px;
            text-align: left;
        }

        .permission-table tbody td {
            height: 56px;
            padding: 0 18px;
            border-bottom: 1px solid #eef1f5;
            color: #263238;
            font-size: 13px;
            vertical-align: middle;
            text-align: center;
        }

        .permission-table tbody tr:nth-child(even) td {
            background: #fbfbfd;
        }

        .permission-table tbody tr:last-child td {
            border-bottom: 0;
        }

        .permission-table tbody td:first-child {
            text-align: left;
            font-weight: 500;
        }

        .matrix-check {
            width: 18px;
            height: 18px;
            margin: 0;
            border-radius: 5px;
            accent-color: var(--brand);
            cursor: pointer;
        }

        .permission-empty {
            width: 18px;
            height: 18px;
            display: inline-block;
            border-radius: 5px;
            background: #f0f2f5;
        }

        .empty-state {
            padding: 44px 16px;
            color: #98a2b3;
            font-size: 14px;
            font-weight: 700;
            text-align: center;
        }

        @media (max-width: 1100px) {
            .main-content {
                margin-left: 0;
            }

            .permission-layout {
                grid-template-columns: 1fr;
            }

            .role-panel {
                min-height: auto;
            }

            .permission-panel {
                overflow-x: auto;
            }

            .permission-table {
                min-width: 680px;
            }
        }

        @media (max-width: 640px) {
            .main-content {
                padding: 18px;
            }

            .permission-heading {
                align-items: flex-start;
                flex-direction: column;
            }

        }
    </style>
</head>
<body>

<c:set var="context" value="${pageContext.request.contextPath}" />
<jsp:include page="/public/components/adminSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/adminTopBar.jsp">
        <jsp:param name="title" value="Quản lý phân quyền" />
    </jsp:include>

    <div class="page-header">
        <a href="${context}/v1/admin/role-list" class="back-btn" title="Quay lại">
            <i class="fa fa-arrow-left"></i>
        </a>
        <h5><i class="fa fa-key me-2" style="color:#ff8c00"></i>Quản lý phân quyền</h5>
    </div>

    <c:if test="${not empty success}">
        <div class="alert alert-success alert-flash">
            <i class="fa fa-circle-check me-2"></i><c:out value="${success}"/>
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-flash">
            <i class="fa fa-circle-exclamation me-2"></i><c:out value="${error}"/>
        </div>
    </c:if>

    <div class="permission-layout">
        <aside class="role-panel">
            <h2 class="panel-title">Danh s&#225;ch vai tr&#242;</h2>
            <button class="add-role-btn" type="button" title="Ch&#7913;c n&#259;ng s&#7869; &#273;&#432;&#7907;c b&#7893; sung sau">
                <i class="fa fa-plus me-1"></i> Th&#234;m vai tr&#242;
            </button>

            <div class="role-list">
                <c:choose>
                    <c:when test="${empty roles}">
                        <div class="empty-state">Ch&#432;a c&#243; vai tr&#242; n&#224;o trong h&#7879; th&#7889;ng.</div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${roles}" var="role">
                            <a class="role-item ${not empty selectedRole and selectedRole.roleId == role.roleId ? 'active' : ''}"
                               href="${context}/v1/admin/role-permissions?roleId=${role.roleId}">
                                <strong><c:out value="${role.roleName}"/></strong>
                                <span>
                                    <c:out value="${empty roleDescriptions[role.roleId] ? role.roleCode : roleDescriptions[role.roleId]}"/>
                                </span>
                                <span class="count-pill"><c:out value="${permissionCounts[role.roleId]}"/></span>
                            </a>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </aside>

        <section class="permission-panel">
            <c:choose>
                <c:when test="${empty selectedRole}">
                    <div class="empty-state">Ch&#432;a c&#243; d&#7919; li&#7879;u ph&#226;n quy&#7873;n.</div>
                </c:when>
                <c:otherwise>
                    <form id="permissionForm" method="post" action="${context}/v1/admin/role-permissions">
                        <input type="hidden" name="roleId" value="${selectedRole.roleId}"/>

                        <div class="permission-heading">
                            <h2>Ph&#226;n quy&#7873;n: <c:out value="${selectedRole.roleName}"/></h2>
                            <button class="btn-save" type="submit"
                                    <c:if test="${empty permissionMatrixRows}">disabled</c:if>>
                                L&#432;u thay &#273;&#7893;i
                            </button>
                        </div>

                        <div class="permission-table-wrap">
                            <table class="permission-table">
                                <thead>
                                    <tr>
                                        <th>T&#237;nh n&#259;ng</th>
                                        <c:forEach items="${permissionActions}" var="action">
                                            <th><c:out value="${action.label}"/></th>
                                        </c:forEach>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty permissionMatrixRows}">
                                            <tr>
                                                <td colspan="5" class="empty-state">
                                                    Ch&#432;a c&#243; quy&#7873;n n&#224;o trong h&#7879; th&#7889;ng.
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${permissionMatrixRows}" var="row">
                                                <tr>
                                                    <td><c:out value="${row.featureName}"/></td>
                                                    <c:forEach items="${permissionActions}" var="action">
                                                        <c:set var="permission" value="${row.permissionsByAction[action.key]}"/>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${not empty permission}">
                                                                    <input class="matrix-check" type="checkbox"
                                                                           name="permissionIds"
                                                                           value="${permission.permissionId}"
                                                                           aria-label="${action.label} ${row.featureName}"
                                                                           <c:if test="${grantedPermissionMap[permission.permissionId]}">checked</c:if>/>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="permission-empty" aria-hidden="true"></span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </c:forEach>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </form>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
