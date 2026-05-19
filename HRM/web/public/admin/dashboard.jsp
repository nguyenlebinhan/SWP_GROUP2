<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="model.User" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    User currentUser = (User) session.getAttribute("user");
    String displayName = currentUser != null ? currentUser.getFullName() : "Admin";
    if (displayName == null || displayName.trim().isEmpty()) {
        displayName = currentUser != null ? currentUser.getUsername() : "Admin";
    }
    String roleName = currentUser != null ? currentUser.getRoleName() : "Nhân viên";
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Dashboard - HRM</title>
  <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    * { box-sizing: border-box; }
    body {
      margin: 0;
      min-height: 100vh;
      font-family: "Be Vietnam Pro", Arial, sans-serif;
      color: #15172f;
      background: #f4f5fa;
      display: flex;
    }
    .sidebar {
      width: 260px;
      min-height: 100vh;
      background: #101331;
      color: #d7d9e8;
      display: flex;
      flex-direction: column;
      position: fixed;
      inset: 0 auto 0 0;
    }
    .brand {
      height: 72px;
      display: flex;
      align-items: center;
      padding: 0 28px;
      gap: 8px;
      color: #aeb3c9;
      border-bottom: 1px solid rgba(255,255,255,.04);
    }
    .brand strong {
      color: #ff7a00;
      font-size: 24px;
      line-height: 1;
      letter-spacing: 0;
    }
    .nav {
      padding: 12px;
      display: grid;
      gap: 8px;
    }
    .nav a {
      height: 44px;
      padding: 0 14px;
      display: flex;
      align-items: center;
      gap: 12px;
      color: #d9dbea;
      text-decoration: none;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 500;
    }
    .nav a.active {
      color: #fff;
      background: #1f70c8;
      box-shadow: inset 0 0 0 1px rgba(255,255,255,.08);
    }
    .nav a:hover { background: rgba(255,255,255,.07); }
    .nav a.active:hover { background: #1f70c8; }
    .sidebar-user {
      margin-top: auto;
      padding: 18px 16px;
      background: rgba(31,112,200,.12);
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .avatar {
      width: 38px;
      height: 38px;
      border-radius: 50%;
      display: grid;
      place-items: center;
      background: #1f70c8;
      color: #fff;
      font-weight: 700;
      flex: 0 0 auto;
    }
    .sidebar-user strong {
      display: block;
      color: #fff;
      font-size: 13px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 150px;
    }
    .sidebar-user span {
      display: block;
      color: #9fa7ca;
      font-size: 11px;
      margin-top: 2px;
    }
    .layout {
      width: calc(100% - 260px);
      margin-left: 260px;
      min-height: 100vh;
    }
    .topbar {
      height: 72px;
      background: #fff;
      border-bottom: 1px solid #dddfe8;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 28px 0 32px;
    }
    .topbar h1 {
      margin: 0;
      font-size: 20px;
      font-weight: 800;
      letter-spacing: 0;
    }
    .top-actions {
      display: flex;
      align-items: center;
      gap: 16px;
      font-size: 14px;
      font-weight: 600;
      color: #141622;
    }
    .icon-btn {
      width: 36px;
      height: 36px;
      border: 0;
      background: #f6f7fb;
      color: #15172f;
      border-radius: 50%;
      display: grid;
      place-items: center;
      text-decoration: none;
    }
    .logout-link {
      height: 36px;
      padding: 0 12px;
      border-radius: 8px;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      color: #b42318;
      background: #fff1f0;
      text-decoration: none;
      font-size: 13px;
      font-weight: 700;
    }
    main {
      padding: 28px 32px;
    }
    .hello {
      margin: 0 0 22px;
      color: #777;
      font-size: 16px;
      font-weight: 700;
    }
    .stats {
      display: grid;
      grid-template-columns: repeat(4, minmax(150px, 1fr));
      gap: 20px;
      margin-bottom: 24px;
    }
    .stat-card,
    .panel {
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 8px 22px rgba(31, 35, 58, .06);
      border: 1px solid rgba(220,224,235,.7);
    }
    .stat-card {
      height: 110px;
      padding: 22px 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .stat-card strong {
      display: block;
      font-size: 32px;
      line-height: 1;
      margin-bottom: 10px;
    }
    .stat-card span {
      color: #777;
      font-size: 13px;
    }
    .stat-dot {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      opacity: .85;
    }
    .content-grid {
      display: grid;
      grid-template-columns: minmax(420px, 1.7fr) minmax(300px, 1fr);
      gap: 20px;
    }
    .panel {
      padding: 22px 24px 24px;
      min-height: 300px;
    }
    .panel h2 {
      margin: 0 0 16px;
      font-size: 16px;
      font-weight: 800;
    }
    .chart-box {
      height: 220px;
      border-radius: 8px;
      background: #f8f8fc;
      display: flex;
      align-items: end;
      justify-content: center;
      gap: 20px;
      padding: 34px 28px 16px;
    }
    .bar-item {
      width: 52px;
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: end;
      align-items: center;
      gap: 10px;
      color: #777;
      font-size: 11px;
    }
    .bar {
      width: 52px;
      min-height: 38px;
      border-radius: 6px 6px 2px 2px;
      box-shadow: inset 0 0 0 1px rgba(0,0,0,.08);
    }
    .employee-list {
      display: grid;
      gap: 18px;
    }
    .employee {
      display: grid;
      grid-template-columns: 36px 1fr auto;
      align-items: center;
      gap: 12px;
    }
    .employee .avatar {
      width: 32px;
      height: 32px;
      background: #dbe9f8;
      color: #2464a5;
      font-size: 12px;
    }
    .employee strong {
      display: block;
      font-size: 13px;
      margin-bottom: 2px;
    }
    .employee span {
      display: block;
      color: #777;
      font-size: 11px;
    }
    .badge {
      border-radius: 999px;
      padding: 5px 10px;
      font-size: 11px;
      font-weight: 700;
      white-space: nowrap;
    }
    .badge.green { color: #1a7f37; background: #eaf6ec; }
    .badge.orange { color: #e85d04; background: #fff0e4; }
    @media (max-width: 960px) {
      .sidebar {
        width: 80px;
      }
      .brand span,
      .nav a span,
      .sidebar-user div {
        display: none;
      }
      .layout {
        width: calc(100% - 80px);
        margin-left: 80px;
      }
      .stats {
        grid-template-columns: repeat(2, minmax(150px, 1fr));
      }
      .content-grid {
        grid-template-columns: 1fr;
      }
    }
    @media (max-width: 640px) {
      .topbar {
        padding: 0 16px;
      }
      main {
        padding: 20px 16px;
      }
      .stats {
        grid-template-columns: 1fr;
      }
      .chart-box {
        overflow-x: auto;
        justify-content: start;
      }
      .logout-label {
        display: none;
      }
    }
  </style>
</head>
<body>
  <aside class="sidebar">
    <div class="brand">
      <strong>HRM</strong>
      <span>System</span>
    </div>
    <nav class="nav">
      <a class="active" href="${pageContext.request.contextPath}/v1/auth/dashboard"><i class="bi bi-grid-1x2-fill"></i><span>Tổng quan</span></a>
      <a href="#"><i class="bi bi-clipboard-check"></i><span>Công việc</span></a>
      <a href="#"><i class="bi bi-people-fill"></i><span>Người dùng</span></a>
      <a href="#"><i class="bi bi-clock-fill"></i><span>Chấm công</span></a>
      <a href="#"><i class="bi bi-person-plus-fill"></i><span>Tuyển dụng</span></a>
      <a href="#"><i class="bi bi-cash-coin"></i><span>Bảng lương</span></a>
      <a href="#"><i class="bi bi-building"></i><span>Phòng ban</span></a>
      <a href="#"><i class="bi bi-key-fill"></i><span>Phân quyền</span></a>
      <a href="#"><i class="bi bi-bar-chart-fill"></i><span>Báo cáo</span></a>
    </nav>
    <div class="sidebar-user">
      <div class="avatar"><%= displayName.substring(0, 1).toUpperCase() %></div>
      <div>
        <strong><%= displayName %></strong>
        <span><%= roleName %></span>
      </div>
    </div>
  </aside>

  <div class="layout">
    <header class="topbar">
      <h1>Dashboard</h1>
      <div class="top-actions">
        <span class="icon-btn" title="Thông báo"><i class="bi bi-bell-fill"></i></span>
        <span class="avatar"><%= displayName.substring(0, 1).toUpperCase() %></span>
        <span><%= currentUser != null ? currentUser.getUsername() : "Admin" %></span>
        <a class="logout-link" href="${pageContext.request.contextPath}/v1/auth/logout">
          <i class="bi bi-box-arrow-right"></i>
          <span class="logout-label">Đăng xuất</span>
        </a>
      </div>
    </header>

    <main>
      <p class="hello">Xin chào, <%= displayName %>! Hôm nay: <%= today %></p>

      <section class="stats" aria-label="Thống kê nhanh">
        <div class="stat-card">
          <div><strong>128</strong><span>Tổng nhân viên</span></div>
          <div class="stat-dot" style="background:#e4eef9"></div>
        </div>
        <div class="stat-card">
          <div><strong>115</strong><span>Đang làm việc</span></div>
          <div class="stat-dot" style="background:#e8f2e9"></div>
        </div>
        <div class="stat-card">
          <div><strong>8</strong><span>Nghỉ phép hôm nay</span></div>
          <div class="stat-dot" style="background:#fff0e4"></div>
        </div>
        <div class="stat-card">
          <div><strong>12</strong><span>Phòng ban</span></div>
          <div class="stat-dot" style="background:#f1e4fb"></div>
        </div>
      </section>

      <section class="content-grid">
        <div class="panel">
          <h2>Biểu đồ nhân viên theo phòng ban</h2>
          <div class="chart-box" aria-label="Biểu đồ cột nhân viên theo phòng ban">
            <div class="bar-item"><div class="bar" style="height:140px;background:#236fc2"></div><span>IT</span></div>
            <div class="bar-item"><div class="bar" style="height:90px;background:#912ee0"></div><span>HR</span></div>
            <div class="bar-item"><div class="bar" style="height:120px;background:#2e7d32"></div><span>Sales</span></div>
            <div class="bar-item"><div class="bar" style="height:70px;background:#ff6d00"></div><span>Finance</span></div>
            <div class="bar-item"><div class="bar" style="height:100px;background:#ef7900"></div><span>Ops</span></div>
            <div class="bar-item"><div class="bar" style="height:60px;background:#27b8d0"></div><span>Marketing</span></div>
          </div>
        </div>

        <div class="panel">
          <h2>Nhân viên mới nhất</h2>
          <div class="employee-list">
            <div class="employee">
              <div class="avatar">A</div>
              <div><strong>Nguyễn Văn A</strong><span>IT Department</span></div>
              <span class="badge green">Đang làm</span>
            </div>
            <div class="employee">
              <div class="avatar">B</div>
              <div><strong>Trần Thị B</strong><span>HR Department</span></div>
              <span class="badge orange">Nghỉ phép</span>
            </div>
            <div class="employee">
              <div class="avatar">C</div>
              <div><strong>Lê Văn C</strong><span>Sales</span></div>
              <span class="badge green">Đang làm</span>
            </div>
            <div class="employee">
              <div class="avatar">D</div>
              <div><strong>Phạm Thị D</strong><span>Finance</span></div>
              <span class="badge green">Đang làm</span>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</body>
</html>
