<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
    .admin-topbar {
        min-height: 70px;
        background: white;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 18px;
        padding: 0 24px;
        border-radius: 10px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        margin-bottom: 24px;
    }

    .admin-topbar-title {
        font-size: 20px;
        font-weight: 700;
        color: #0B0E2A;
        margin: 0;
    }

    .admin-topbar-actions {
        display: flex;
        align-items: center;
        gap: 18px;
    }

    .admin-user-menu {
        position: relative;
    }

    .admin-user-trigger {
        border: 0;
        background: transparent;
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 6px 0;
        color: #111827;
        cursor: pointer;
    }

    .admin-avatar-img {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #2b6cb0;
        color: #fff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        flex: 0 0 auto;
    }

    .admin-user-name {
        max-width: 180px;
        font-size: 14px;
        font-weight: 700;
        color: #111827;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .admin-user-dropdown {
        display: none;
        position: absolute;
        right: 0;
        top: calc(100% + 8px);
        min-width: 190px;
        background: #fff;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        box-shadow: 0 12px 28px rgba(15,23,42,0.14);
        padding: 8px;
        z-index: 1000;
    }

    .admin-user-menu:hover .admin-user-dropdown,
    .admin-user-menu:focus-within .admin-user-dropdown {
        display: block;
    }

    .admin-dropdown-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 12px;
        border-radius: 6px;
        color: #374151;
        text-decoration: none;
        font-size: 14px;
        font-weight: 600;
    }

    .admin-dropdown-item:hover {
        background: #f3f4f6;
        color: #111827;
    }

    .admin-dropdown-item.logout {
        color: #b91c1c;
    }

    @media (max-width: 768px) {
        .admin-topbar {
            align-items: flex-start;
            flex-direction: column;
            padding: 16px;
        }

        .admin-topbar-actions {
            width: 100%;
            justify-content: flex-end;
        }
    }
</style>

<c:set var="topbarUser" value="${sessionScope.user}" />
<c:set var="topbarDisplayName" value="${empty topbarUser.fullName ? topbarUser.username : topbarUser.fullName}" />

<div class="admin-topbar">
    <div style="display:flex;align-items:center;gap:12px">
        <c:if test="${not empty param.backUrl}">
            <a href="${pageContext.request.contextPath}${param.backUrl}"
               style="width:34px;height:34px;background:#f3f4f6;border:1px solid #e5e7eb;border-radius:8px;display:inline-flex;align-items:center;justify-content:center;color:#6b7280;text-decoration:none;flex-shrink:0"
               title="Quay lại">
                <i class="fa fa-arrow-left" style="font-size:13px"></i>
            </a>
        </c:if>
        <h4 class="admin-topbar-title">
            <c:out value="${empty param.title ? 'HRM Admin' : param.title}" />
        </h4>
    </div>

    <div class="admin-topbar-actions">

        <div class="admin-user-menu">
            <button type="button" class="admin-user-trigger" aria-label="Tài khoản">
                <span class="admin-avatar-img">
                    <i class="fa-solid fa-user-tie"></i>
                </span>
                <span class="admin-user-name">
                    <c:out value="${empty topbarDisplayName ? 'SystemAdmin' : topbarDisplayName}" />
                </span>
                <i class="fa-solid fa-chevron-down" style="font-size:12px;color:#6b7280"></i>
            </button>
            <div class="admin-user-dropdown">
                <a href="${pageContext.request.contextPath}/v1/systemadmin/my-profile" class="admin-dropdown-item">
                    <i class="fa-solid fa-user-gear"></i> Hồ sơ của tôi
                </a>
                <a href="${pageContext.request.contextPath}/v1/auth/logout" class="admin-dropdown-item logout">
                    <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
                </a>
            </div>
        </div>
    </div>
</div>