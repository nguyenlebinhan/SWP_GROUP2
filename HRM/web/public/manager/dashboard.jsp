<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Dashboard - HRM Manager</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main-content { margin-left: 250px; padding: 30px; }
        .page-header { margin-bottom: 24px; }
        .page-header h4 { font-weight: 700; color: #0B0E2A; margin: 0; }
        .page-header p { color: #6b7280; font-size: 14px; margin: 4px 0 0; }
        .stat-card {
            background: #fff;
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
            height: 100%;
        }
        .stat-icon {
            width: 52px;
            height: 52px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
            margin-bottom: 16px;
        }
        .stat-label {
            color: #6b7280;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 4px;
        }
        .stat-value {
            color: #111827;
            font-size: 32px;
            font-weight: 800;
            margin: 0;
        }
        .stat-sub { color: #9ca3af; font-size: 12px; margin-top: 4px; }
        .section-title {
            font-size: 16px;
            font-weight: 700;
            color: #0B0E2A;
            margin: 0 0 16px;
        }
        .section-card {
            background: #fff;
            border: none;
            border-radius: 12px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main-content">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Tổng quan" />
    </jsp:include>

    <div class="page-header">
        <h4>Chào mừng bạn quay trở lại!</h4>
        <p>Tổng quan về hệ thống quản lý nhân sự</p>
    </div>

    <div class="row g-4 mb-4">
        <div class="col-md-4">
            <div class="stat-card">
                <div class="stat-icon" style="background:#e0f2fe;color:#0369a1">
                    <i class="fa fa-users"></i>
                </div>
                <div class="stat-label">Tổng nhân viên</div>
                <p class="stat-value">${totalEmployees}</p>
                <div class="stat-sub">Nhân viên &amp; Quản lý</div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="stat-card">
                <div class="stat-icon" style="background:#d1fae5;color:#065f46">
                    <i class="fa fa-circle-check"></i>
                </div>
                <div class="stat-label">Đang hoạt động</div>
                <p class="stat-value">${activeEmployees}</p>
                <div class="stat-sub">Nhân viên đang làm việc</div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="stat-card">
                <div class="stat-icon" style="background:#fee2e2;color:#991b1b">
                    <i class="fa fa-user-slash"></i>
                </div>
                <div class="stat-label">Không hoạt động</div>
                <p class="stat-value">${inactiveEmployees}</p>
                <div class="stat-sub">Nhân viên bị vô hiệu</div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-md-6">
            <div class="section-card">
                <div class="section-title"><i class="fa fa-bolt me-2" style="color:#ff8c00"></i>Thao tác nhanh</div>
                <div class="d-flex flex-column gap-2">
                    <a href="${pageContext.request.contextPath}/v1/manager/employee-list"
                       class="quick-action-btn">
                        <i class="fa fa-list"></i> Danh sách nhân viên
                    </a>
                    <a href="${pageContext.request.contextPath}/v1/manager/leave-requests"
                       class="quick-action-btn">
                        <i class="fa fa-calendar-check"></i> Duyệt đơn nghỉ phép
                    </a>
                    <a href="${pageContext.request.contextPath}/v1/manager/leave-balances"
                       class="quick-action-btn">
                        <i class="fa fa-chart-pie"></i> Quản lý ngày phép
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="section-card">
                <div class="section-title"><i class="fa fa-bullhorn me-2" style="color:#ff8c00"></i>Thông báo</div>
                <div class="text-center text-muted py-4">
                    <i class="fa fa-bell fa-2x mb-2" style="color:#d1d5db"></i><br/>
                    Chưa có thông báo mới
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    .quick-action-btn {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 12px 16px;
        background: #f9fafb;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        color: #374151;
        text-decoration: none;
        font-size: 14px;
        font-weight: 600;
        transition: all .2s;
    }
    .quick-action-btn:hover {
        background: #ff8c00;
        color: white;
        border-color: #ff8c00;
    }
    .quick-action-btn i { width: 18px; text-align: center; }
</style>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
