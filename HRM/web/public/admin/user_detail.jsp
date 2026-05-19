<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>User Detail - HRM</title>
  <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    * { box-sizing: border-box; }
    body {
      margin: 0;
      min-height: 100vh;
      font-family: "Be Vietnam Pro", Arial, sans-serif;
      color: #171923;
      background: #f4f6f8;
    }
    .shell {
      min-height: 100vh;
    }
    .sidebar {
      width: 250px;
      height: 100vh;
      background: #0B0E2A;
      position: fixed;
      top: 0;
      left: 0;
      color: white;
      z-index: 100;
    }
    .sidebar h4 {
      padding: 20px;
      color: #ff8c00;
      margin: 0;
      font-size: 20px;
      font-weight: 700;
    }
    .sidebar a {
      display: block;
      padding: 14px 20px;
      color: #cbd5e1;
      text-decoration: none;
      transition: background .2s;
      font-size: 15px;
      font-weight: 500;
    }
    .sidebar a:hover {
      background: #1565C0;
      color: white;
      border-radius: 5px;
    }
    .sidebar a.active-menu {
      background: #2b6cb0;
      color: white;
      border-radius: 5px;
    }
    .sidebar i {
      width: 20px;
      margin-right: 8px;
      text-align: center;
    }
    .main {
      margin-left: 250px;
      min-width: 0;
      background: #f5f7fb;
    }
    .topbar {
      min-height: 76px;
      background: #fff;
      border-bottom: 1px solid #edf0f5;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 18px;
      padding: 0 36px;
    }
    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      color: #9aa1ad;
      font-size: 13px;
      font-weight: 600;
    }
    .breadcrumb a {
      color: #9aa1ad;
      text-decoration: none;
    }
    .breadcrumb strong {
      color: #111827;
      font-weight: 800;
    }
    .top-actions {
      display: flex;
      align-items: center;
      gap: 12px;
      color: #374151;
      font-size: 14px;
      font-weight: 700;
    }
    .edit-link {
      height: 40px;
      min-width: 96px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 0 18px;
      border-radius: 8px;
      background: #1267c5;
      color: #fff;
      text-decoration: none;
      font-weight: 700;
      font-size: 13px;
    }
    .admin-chip {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      color: #111827;
      font-weight: 700;
    }
    .admin-dot {
      width: 42px;
      height: 42px;
      border-radius: 50%;
      background: #1267c5;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }
    .content {
      padding: 28px 36px;
      max-width: 1240px;
    }
    .alert {
      padding: 12px 14px;
      border-radius: 8px;
      color: #991b1b;
      background: #fee2e2;
      border: 1px solid #fecaca;
      margin-bottom: 18px;
    }
    .detail-layout {
      display: grid;
      grid-template-columns: 290px minmax(0, 1fr);
      gap: 24px;
      align-items: stretch;
    }
    .employee-card,
    .info-panel,
    .empty-state {
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 10px;
      box-shadow: 0 10px 24px rgba(15, 23, 42, .04);
    }
    .employee-card {
      min-height: 640px;
      padding: 32px 24px;
      display: grid;
      align-content: start;
      justify-items: center;
      text-align: center;
    }
    .profile-avatar {
      width: 106px;
      height: 106px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #dbe7f6;
      color: #1267c5;
      font-size: 42px;
      margin-bottom: 18px;
    }
    .employee-card h2 {
      margin: 0;
      color: #111827;
      font-size: 18px;
      line-height: 1.3;
      font-weight: 800;
    }
    .role-pill,
    .state-pill {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-height: 26px;
      padding: 0 16px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 800;
      margin-top: 10px;
    }
    .role-pill {
      min-width: 112px;
      color: #1267c5;
      background: #e7f0fb;
    }
    .state-pill {
      gap: 7px;
      color: #167044;
      background: #e6f6ea;
    }
    .state-pill.warn {
      color: #9a3412;
      background: #ffedd5;
    }
    .state-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: currentColor;
    }
    .employee-meta {
      width: 100%;
      margin-top: 34px;
      padding-top: 20px;
      border-top: 1px solid #edf0f4;
      display: grid;
      gap: 18px;
    }
    .meta-label,
    .info-label {
      display: block;
      color: #9aa1ad;
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0;
    }
    .meta-value,
    .info-value {
      display: block;
      color: #111827;
      font-size: 14px;
      font-weight: 800;
      line-height: 1.45;
      overflow-wrap: anywhere;
      margin-top: 5px;
    }
    .info-panel {
      min-height: 640px;
      padding: 32px 34px;
    }
    .info-section + .info-section {
      margin-top: 36px;
      padding-top: 28px;
      border-top: 1px solid #edf0f4;
    }
    .section-title {
      margin: 0 0 18px;
      color: #111827;
      font-size: 17px;
      font-weight: 800;
    }
    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      column-gap: 48px;
      row-gap: 0;
    }
    .info-item {
      min-height: 58px;
      padding: 11px 0 12px;
      border-bottom: 1px solid #edf0f4;
    }
    .info-item.wide {
      grid-column: 1 / -1;
    }
    .badge {
      display: inline-flex;
      align-items: center;
      min-height: 28px;
      padding: 0 10px;
      border-radius: 999px;
      background: #eef2ff;
      color: #3730a3;
      font-size: 12px;
      font-weight: 800;
      white-space: nowrap;
    }
    .status {
      background: #dcfce7;
      color: #166534;
    }
    .status.warn {
      background: #ffedd5;
      color: #9a3412;
    }
    .empty-state {
      padding: 36px;
      color: #6b7280;
      text-align: center;
      font-weight: 700;
    }
    @media (max-width: 760px) {
      .shell { grid-template-columns: 1fr; }
      .sidebar { display: none; }
      .main { margin-left: 0; }
      .topbar {
        height: auto;
        align-items: flex-start;
        gap: 12px;
        padding: 18px;
        flex-direction: column;
      }
      .content { padding: 18px; }
      .detail-layout,
      .info-grid {
        grid-template-columns: 1fr;
      }
      .employee-card,
      .info-panel {
        min-height: auto;
      }
      .info-item.wide { grid-column: auto; }
    }
    @media (min-width: 761px) and (max-width: 1180px) {
      .detail-layout { grid-template-columns: 260px minmax(0, 1fr); }
      .info-grid { column-gap: 28px; }
    }
  </style>
</head>
<body>
  <div class="shell">
    <div class="sidebar">
      <h4><i class="fa fa-building"></i>HRM System</h4>
        <a href="${pageContext.request.contextPath}/">
          <i class="fa fa-chart-line"></i>
          <span>Tổng quan</span>
        </a>
        <a href="#"><i class="fa fa-briefcase"></i><span>Công việc</span></a>
        <a href="${pageContext.request.contextPath}/v1/admin/users" class="active-menu">
          <i class="fa fa-users"></i>
          <span>Người dùng</span>
        </a>
        <a href="#"><i class="fa fa-clock"></i><span>Chấm công</span></a>
        <a href="#"><i class="fa fa-user-plus"></i><span>Tuyển dụng</span></a>
        <a href="#"><i class="fa fa-money-bill"></i><span>Bảng lương</span></a>
        <a href="#"><i class="fa fa-building"></i><span>Phòng ban</span></a>
        <a href="#"><i class="fa fa-key"></i><span>Phân quyền</span></a>
        <a href="#"><i class="fa fa-chart-bar"></i><span>Báo cáo</span></a>
    </div>

    <div class="main">
      <c:set var="employeeName" value="${empty selectedUser.fullName ? selectedUser.username : selectedUser.fullName}"/>

      <header class="topbar">
        <div class="breadcrumb">
          <a href="${pageContext.request.contextPath}/v1/admin/users">Nhân viên</a>
          <i class="bi bi-chevron-right"></i>
          <strong>
            <c:choose>
              <c:when test="${not empty selectedUser}">
                <c:out value="${employeeName}"/>
              </c:when>
              <c:otherwise>Chi tiết nhân viên</c:otherwise>
            </c:choose>
          </strong>
        </div>

        <div class="top-actions">
          <c:if test="${not empty selectedUser}">
            <a class="edit-link" href="#">
              <i class="bi bi-pencil-fill"></i>
              <span>Sửa</span>
            </a>
          </c:if>
          <div class="admin-chip">
            <span class="admin-dot"><i class="bi bi-person-fill"></i></span>
            <span><c:out value="${empty sessionScope.user.roleName ? 'Admin' : sessionScope.user.roleName}"/></span>
          </div>
        </div>
      </header>

      <main class="content">
        <c:if test="${not empty error}">
          <div class="alert"><c:out value="${error}"/></div>
        </c:if>

        <c:choose>
          <c:when test="${empty selectedUser}">
            <div class="empty-state">Không tìm thấy thông tin nhân viên.</div>
          </c:when>
          <c:otherwise>
            <section class="detail-layout">
              <aside class="employee-card">
                <div class="profile-avatar"><i class="bi bi-person-fill"></i></div>
                <h2><c:out value="${employeeName}"/></h2>
                <span class="role-pill"><c:out value="${empty selectedUser.roleName ? '-' : selectedUser.roleName}"/></span>

                <c:choose>
                  <c:when test="${selectedUser.isTemporaryPassword}">
                    <span class="state-pill warn">
                      <span class="state-dot"></span>
                      <span>Mật khẩu tạm</span>
                    </span>
                  </c:when>
                  <c:otherwise>
                    <span class="state-pill">
                      <span class="state-dot"></span>
                      <span>Đang hoạt động</span>
                    </span>
                  </c:otherwise>
                </c:choose>

                <div class="employee-meta">
                  <div>
                    <span class="meta-label">Mã nhân viên</span>
                    <span class="meta-value">NV<c:out value="${selectedUser.userId}"/></span>
                  </div>
                  <div>
                    <span class="meta-label">Vai trò</span>
                    <span class="meta-value"><c:out value="${empty selectedUser.roleName ? '-' : selectedUser.roleName}"/></span>
                  </div>
                  <div>
                    <span class="meta-label">Tên đăng nhập</span>
                    <span class="meta-value"><c:out value="${empty selectedUser.username ? '-' : selectedUser.username}"/></span>
                  </div>
                  <div>
                    <span class="meta-label">Email</span>
                    <span class="meta-value"><c:out value="${empty selectedUser.email ? '-' : selectedUser.email}"/></span>
                  </div>
                </div>
              </aside>

              <section class="info-panel">
                <section class="info-section">
                  <h3 class="section-title">Thông tin cơ bản</h3>
                  <div class="info-grid">
                    <div class="info-item">
                      <span class="info-label">Họ và tên</span>
                      <span class="info-value"><c:out value="${empty selectedUser.fullName ? '-' : selectedUser.fullName}"/></span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">Ngày sinh</span>
                      <span class="info-value"><c:out value="${empty selectedUser.dob ? '-' : selectedUser.dob}"/></span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">Tên đăng nhập</span>
                      <span class="info-value"><c:out value="${empty selectedUser.username ? '-' : selectedUser.username}"/></span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">Email</span>
                      <span class="info-value"><c:out value="${empty selectedUser.email ? '-' : selectedUser.email}"/></span>
                    </div>
                    <div class="info-item wide">
                      <span class="info-label">Địa chỉ</span>
                      <span class="info-value"><c:out value="${empty selectedUser.address ? '-' : selectedUser.address}"/></span>
                    </div>
                  </div>
                </section>

                <section class="info-section">
                  <h3 class="section-title">Thông tin công việc</h3>
                  <div class="info-grid">
                    <div class="info-item">
                      <span class="info-label">Mã nhân viên</span>
                      <span class="info-value">NV<c:out value="${selectedUser.userId}"/></span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">Vai trò</span>
                      <span class="info-value"><c:out value="${empty selectedUser.roleName ? '-' : selectedUser.roleName}"/></span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">Trạng thái tài khoản</span>
                      <span class="info-value">Đang hoạt động</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">Trạng thái mật khẩu</span>
                      <span class="info-value">
                        <c:choose>
                          <c:when test="${selectedUser.isTemporaryPassword}">Mật khẩu tạm</c:when>
                          <c:otherwise>Đã thiết lập</c:otherwise>
                        </c:choose>
                      </span>
                    </div>
                  </div>
                </section>
              </section>
            </section>
          </c:otherwise>
        </c:choose>
      </main>
    </div>
  </div>
</body>
</html>
