<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
    .manager-topbar {
        min-height: 70px;
        background: white;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 18px;
        padding: 0 24px;
        border-radius: 12px;
        box-shadow: 0 2px 12px rgba(0,0,0,0.07);
        margin-bottom: 24px;
    }

    .manager-topbar-title {
        font-size: 20px;
        font-weight: 700;
        color: #0B0E2A;
        margin: 0;
    }

    .manager-topbar-actions {
        display: flex;
        align-items: center;
        gap: 18px;
    }

    .manager-user-menu {
        position: relative;
    }

    .manager-user-trigger {
        border: 0;
        background: transparent;
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 6px 0;
        color: #111827;
        cursor: pointer;
    }

    .manager-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #ff8c00;
        color: #fff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        font-weight: 700;
    }

    .manager-user-name {
        max-width: 180px;
        font-size: 14px;
        font-weight: 600;
        color: #111827;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .manager-user-role {
        font-size: 11px;
        color: #9ca3af;
        font-weight: 500;
    }

    .manager-user-dropdown {
        display: none;
        position: absolute;
        right: 0;
        top: calc(100% + 8px);
        min-width: 200px;
        background: #fff;
        border: 1px solid #e5e7eb;
        border-radius: 10px;
        box-shadow: 0 12px 28px rgba(15,23,42,0.14);
        padding: 8px;
        z-index: 1000;
    }

    .manager-user-menu:hover .manager-user-dropdown,
    .manager-user-menu:focus-within .manager-user-dropdown {
        display: block;
    }

    .manager-dropdown-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 12px;
        border-radius: 6px;
        color: #374151;
        text-decoration: none;
        font-size: 14px;
        font-weight: 600;
        transition: background .15s;
    }

    .manager-dropdown-item:hover {
        background: #f3f4f6;
        color: #111827;
    }

    .manager-dropdown-item.logout {
        color: #b91c1c;
    }

    .manager-dropdown-item.logout:hover {
        background: #fef2f2;
        color: #b91c1c;
    }

    .manager-dropdown-divider {
        height: 1px;
        background: #f1f3f5;
        margin: 6px 0;
    }
</style>

<c:set var="topbarUser" value="${sessionScope.user}" />
<c:set var="topbarDisplayName" value="${empty topbarUser.fullName ? topbarUser.username : topbarUser.fullName}" />

<div class="manager-topbar">
    <div style="display:flex;align-items:center;gap:12px">
        <c:if test="${not empty param.backUrl}">
            <a href="${pageContext.request.contextPath}${param.backUrl}"
               style="width:34px;height:34px;background:#f3f4f6;border:1px solid #e5e7eb;border-radius:8px;
                      display:inline-flex;align-items:center;justify-content:center;color:#6b7280;text-decoration:none;
                      flex-shrink:0;transition:background .2s"
               title="Quay lại">
                <i class="fa fa-arrow-left" style="font-size:13px"></i>
            </a>
        </c:if>
        <h4 class="manager-topbar-title">
            <c:out value="${empty param.title ? 'HRM Manager' : param.title}" />
        </h4>
    </div>

    <div class="manager-topbar-actions">
        <div class="manager-user-menu">
            <button type="button" class="manager-user-trigger" aria-label="Tài khoản">
                <span class="manager-avatar">
                    <i class="fa-solid fa-user-tie"></i>
                </span>
                <span>
                    <span class="manager-user-name">
                        <c:out value="${empty topbarDisplayName ? 'Manager' : topbarDisplayName}" />
                    </span>
                    <span class="manager-user-role d-block">Manager</span>
                </span>
                <i class="fa-solid fa-chevron-down" style="font-size:11px;color:#6b7280;margin-left:4px"></i>
            </button>
            <div class="manager-user-dropdown">
                <a href="${pageContext.request.contextPath}/v1/manager/my-profile" class="manager-dropdown-item">
                    <i class="fa-solid fa-user-gear"></i> Hồ sơ của tôi
                </a>
                <a href="${pageContext.request.contextPath}/v1/manager/salary/own" class="manager-dropdown-item">
                    <i class="fa-solid fa-wallet"></i> Lương của tôi
                </a>
                <div class="manager-dropdown-divider"></div>
                <a href="${pageContext.request.contextPath}/v1/auth/logout" class="manager-dropdown-item logout">
                    <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
                </a>
            </div>
        </div>
    </div>
</div>



