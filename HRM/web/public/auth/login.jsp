<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Đăng nhập - HRM</title>
  <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet"/>
  <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-icons/1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
  <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@300;400;500;600;700;800&display=swap" rel="stylesheet"/>
  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    html, body { height: 100%; }

    body {
      font-family: 'Be Vietnam Pro', sans-serif;
      display: flex;
      height: 100vh;
      overflow: hidden;
      background: #0d1240;
    }

    .left-panel {
      flex: 0 0 58%;
      position: relative;
      overflow: hidden;
    }
    .left-panel .panel-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      object-position: center;
      display: block;
    }

    .right-panel {
      flex: 0 0 42%;
      background: #f5f6fa;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 2rem 3rem;
      position: relative;
      border-left: 1px solid rgba(21,101,192,.12);
      overflow-y: auto;
    }

    .auth-card {
      width: 100%;
      max-width: 360px;
      background: #fff;
      border-radius: 20px;
      padding: 2.4rem 2.2rem 2rem;
      box-shadow: 0 2px 8px rgba(13,18,64,.06), 0 12px 40px rgba(13,18,64,.10);
    }

    .logo-wrap { text-align: center; margin-bottom: 1.6rem; }
    .logo-hrm { font-size: 1.7rem; font-weight: 800; letter-spacing: -.5px; line-height: 1; }
    .logo-hrm span { color: #ff6b00; }
    .logo-powered { font-size: .7rem; color: #aaa; margin-top: .2rem; }
    .logo-powered span { color: #ff6b00; font-weight: 600; }

    .form-heading {
      font-size: 1.15rem; font-weight: 700;
      color: #1a1a2e; text-align: center;
      margin-bottom: 1.4rem;
    }

    .form-label {
      font-size: .78rem; font-weight: 600;
      color: #444; margin-bottom: .3rem; display: block;
    }
    .required { color: #e53935; margin-left: 2px; }

    .form-control {
      border: 1.5px solid #e4e6ef;
      border-radius: 9px; height: 44px;
      font-size: .875rem; color: #333;
      transition: border-color .2s, box-shadow .2s;
      padding: 0 .9rem; width: 100%; outline: none;
      font-family: inherit;
    }
    .form-control:focus {
      border-color: #1565c0;
      box-shadow: 0 0 0 3px rgba(21,101,192,.1);
    }
    .form-control::placeholder { color: #c0c4d0; }

    .pw-wrap { position: relative; }
    .pw-wrap .form-control { padding-right: 2.8rem; }
    .pw-toggle {
      position: absolute; right: .75rem; top: 50%;
      transform: translateY(-50%);
      background: none; border: none; padding: 0;
      color: #b0b6c8; cursor: pointer;
      font-size: 1rem; line-height: 1;
      transition: color .2s;
    }
    .pw-toggle:hover { color: #555; }

    .forgot-link {
      display: block; text-align: right;
      font-size: .75rem; color: #9aa0b5;
      text-decoration: none; margin-top: .35rem;
      transition: color .2s;
    }
    .forgot-link:hover { color: #1565c0; }

    .btn-submit {
      width: 100%; height: 46px;
      background: #1565c0; color: #fff;
      border: none; border-radius: 9px;
      font-size: .9rem; font-weight: 600;
      letter-spacing: .02em; margin-top: 1.2rem;
      transition: background .2s, transform .1s, box-shadow .2s;
      cursor: pointer; font-family: inherit;
      display: flex; align-items: center; justify-content: center; gap: .4rem;
    }
    .btn-submit:hover {
      background: #0d47a1;
      box-shadow: 0 4px 16px rgba(21,101,192,.35);
      transform: translateY(-1px);
    }
    .btn-submit:active { transform: translateY(0); }

    .divider {
      display: flex; align-items: center; gap: .7rem;
      margin: 1.1rem 0;
    }
    .divider::before, .divider::after {
      content: ''; flex: 1; height: 1px; background: #eef0f5;
    }
    .divider span { font-size: .7rem; color: #c5c9d8; letter-spacing: .04em; }

    .btn-google {
      width: 100%; height: 44px;
      background: #f8f9fa; border: 1.5px solid #e4e6ef;
      border-radius: 9px; font-size: .85rem; font-weight: 500;
      color: #444; display: flex; align-items: center;
      justify-content: center; gap: .55rem;
      text-decoration: none; cursor: pointer;
      transition: background .2s, border-color .2s;
      font-family: inherit;
    }
    .btn-google:hover { background: #fff; border-color: #bbb; color: #222; }
    .g-logo {
      width: 17px; height: 17px; flex-shrink: 0;
      background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 48 48'%3E%3Cpath fill='%234285F4' d='M45.12 24.5c0-1.56-.14-3.06-.4-4.5H24v8.51h11.84c-.51 2.75-2.06 5.08-4.39 6.64v5.52h7.11c4.16-3.83 6.56-9.47 6.56-16.17z'/%3E%3Cpath fill='%2334A853' d='M24 46c5.94 0 10.92-1.97 14.56-5.33l-7.11-5.52c-1.97 1.32-4.49 2.1-7.45 2.1-5.73 0-10.58-3.87-12.31-9.07H4.34v5.7C7.96 41.07 15.4 46 24 46z'/%3E%3Cpath fill='%23FBBC05' d='M11.69 28.18C11.25 26.86 11 25.45 11 24s.25-2.86.69-4.18v-5.7H4.34C2.85 17.09 2 20.45 2 24c0 3.55.85 6.91 2.34 9.88l7.35-5.7z'/%3E%3Cpath fill='%23EA4335' d='M24 10.75c3.23 0 6.13 1.11 8.41 3.29l6.31-6.31C34.91 4.18 29.93 2 24 2 15.4 2 7.96 6.93 4.34 14.12l7.35 5.7z'/%3E%3C/svg%3E") center/contain no-repeat;
    }

    .signup-text {
      text-align: center; font-size: .78rem;
      color: #9aa0b5; margin-top: 1rem;
    }
    .signup-text a { color: #1565c0; font-weight: 600; text-decoration: none; }
    .signup-text a:hover { text-decoration: underline; }

    .alert {
      border-radius: 9px; font-size: .8rem;
      padding: .7rem .9rem; margin-bottom: 1rem;
      border: 1px solid transparent;
      display: flex; align-items: flex-start; gap: .5rem;
    }
    .alert-danger { background: #fff0f0; color: #c62828; border-color: #ffcdd2; }

    .brand-strip {
      margin-top: 1.4rem;
      display: flex; align-items: center; gap: .5rem;
      font-size: .7rem; color: #c0c5d4;
    }
    .brand-strip .dot {
      width: 3px; height: 3px;
      background: #d0d4e0; border-radius: 50%;
    }

    @media (max-width: 768px) {
      .left-panel { display: none; }
      .right-panel { flex: 1; padding: 2rem 1.5rem; }
    }
  </style>
</head>
<body>

<div class="left-panel">
  <img src="${pageContext.request.contextPath}/public/asset/Left Panel.jpg" alt="HRM System" class="panel-img"/>
</div>

<div class="right-panel">
  <div class="auth-card">

    <div class="logo-wrap">
      <div class="logo-hrm"><span>H</span><span>R</span><span>M</span></div>
      <div class="logo-powered">Powered by <span>Group 2</span></div>
    </div>

    <p class="form-heading">Đăng nhập hệ thống</p>

    <form method="POST" action="${pageContext.request.contextPath}/v1/auth/login">

      <c:if test="${not empty error}">
        <div class="alert alert-danger">
          <i class="bi bi-exclamation-circle"></i>
          <span><c:out value="${error}"/></span>
        </div>
      </c:if>
      <c:if test="${param.required == '1'}">
        <div class="alert alert-danger">
          <i class="bi bi-exclamation-circle"></i>
          <span>bạn chưa đăng nhập</span>
        </div>
      </c:if>

      <div class="mb-3">
        <label for="username" class="form-label">Username <span class="required">*</span></label>
        <input
          type="text"
          id="username"
          name="username"
          class="form-control"
          placeholder="Nhập username"
          value="<c:out value='${param.username}'/>"
          autocomplete="username"
          required
        />
      </div>

      <div class="mb-1">
        <label for="pwdInput" class="form-label">Mật khẩu <span class="required">*</span></label>
        <div class="pw-wrap">
          <input
            type="password"
            id="pwdInput"
            name="password"
            class="form-control"
            placeholder="Nhập mật khẩu"
            autocomplete="current-password"
            required
          />
          <button type="button" class="pw-toggle" id="togglePwd" aria-label="Hiện/ẩn mật khẩu">
            <i class="bi bi-eye" id="eyeIcon"></i>
          </button>
        </div>
        <a href="${pageContext.request.contextPath}/v1/auth/forget-password" class="forgot-link">Quên mật khẩu?</a>
      </div>

      <button type="submit" class="btn-submit">
        <i class="bi bi-box-arrow-in-right"></i> Đăng nhập
      </button>

      <div class="divider"><span>HOẶC</span></div>

      <a href="${pageContext.request.contextPath}/v1/auth/google" class="btn-google">
        <span class="g-logo" aria-hidden="true"></span>
        Đăng nhập bằng Google
      </a>

    </form>
  </div>

  <div class="brand-strip">
    <span>HRM System</span>
    <span class="dot"></span>
    <span>Phần mềm Quản trị Nhân sự</span>
    <span class="dot"></span>
    <span>v2.0</span>
  </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
<script>
  document.getElementById('togglePwd').addEventListener('click', function () {
    var inp = document.getElementById('pwdInput');
    var icon = document.getElementById('eyeIcon');
    if (inp.type === 'password') {
      inp.type = 'text';
      icon.className = 'bi bi-eye-slash';
    } else {
      inp.type = 'password';
      icon.className = 'bi bi-eye';
    }
  });
</script>
</body>
</html>
