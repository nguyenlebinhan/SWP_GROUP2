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
    :root {
      --bs-body-font-family: 'Be Vietnam Pro', sans-serif;
      --bs-font-sans-serif: 'Be Vietnam Pro', sans-serif;
    }
    html, body { height: 100%; }
    body {
      font-family: 'Be Vietnam Pro', sans-serif;
      display: flex;
      height: 100vh;
      overflow: hidden;
    }
    .right-panel {
      flex: 0 0 42%;
      background: #fff;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 2.5rem 3.5rem;
      position: relative;
    }
    .flag-btn {
      position: absolute;
      top: 1.2rem; right: 1.2rem;
      width: 34px; height: 24px;
      border-radius: 3px;
      overflow: hidden;
      border: 1px solid #e0e0e0;
      cursor: pointer;
    }
    .flag-btn svg { width: 100%; height: 100%; }
    .logo-wrap {
      text-align: center;
      margin-bottom: 2.2rem;
    }
    .logo-hrm {
      font-size: 2rem;
      font-weight: 800;
      letter-spacing: -.5px;
    }
    .logo-hrm span { color: #ff6b00; }
    .logo-powered {
      font-size: .72rem;
      color: #999;
      letter-spacing: .02em;
      margin-top: .1rem;
    }
    .logo-powered span { color: #ff6b00; font-weight: 600; }
    .login-form { width: 100%; max-width: 360px; }
    .login-form h2 {
      font-size: 1.5rem;
      font-weight: 700;
      text-align: center;
      color: #1a1a2e;
      margin-bottom: 1.8rem;
    }
    .form-label {
      font-size: .8rem;
      font-weight: 600;
      color: #333;
      margin-bottom: .35rem;
    }
    .required { color: #e53935; margin-left: 2px; }
    .form-control {
      border: 1.5px solid #e0e0e0;
      border-radius: 8px;
      height: 46px;
      font-size: .875rem;
      color: #333;
      transition: border-color .2s, box-shadow .2s;
      padding: 0 .9rem;
    }
    .form-control:focus {
      border-color: #1565c0;
      box-shadow: 0 0 0 3px rgba(21,101,192,.1);
      outline: none;
    }
    .form-control::placeholder { color: #bbb; }
    .input-group .form-control { border-right: none; border-radius: 8px 0 0 8px; }
    .input-group .btn-eye {
      border: 1.5px solid #e0e0e0;
      border-left: none;
      background: #fff;
      border-radius: 0 8px 8px 0;
      color: #bbb;
      padding: 0 .8rem;
      transition: color .2s;
    }
    .input-group .btn-eye:hover { color: #555; }
    .forgot-link {
      display: block;
      text-align: right;
      font-size: .78rem;
      color: #888;
      text-decoration: none;
      margin-top: .4rem;
      transition: color .2s;
    }
    .forgot-link:hover { color: #1565c0; }
    .btn-login {
      width: 100%;
      height: 48px;
      background: #1565c0;
      color: #fff;
      border: none;
      border-radius: 8px;
      font-size: .95rem;
      font-weight: 600;
      letter-spacing: .02em;
      margin-top: 1.4rem;
      transition: background .2s, transform .1s, box-shadow .2s;
      cursor: pointer;
    }
    .btn-login:hover {
      background: #0d47a1;
      box-shadow: 0 4px 16px rgba(21,101,192,.35);
      transform: translateY(-1px);
    }
    .btn-login:active { transform: translateY(0); }
    .divider {
      display: flex; align-items: center; gap: .8rem;
      margin: 1.2rem 0;
    }
    .divider::before, .divider::after {
      content: ''; flex: 1;
      height: 1px; background: #ebebeb;
    }
    .divider span { font-size: .78rem; color: #bbb; }
    .btn-google {
      width: 100%;
      height: 46px;
      background: #f8f9fa;
      border: 1.5px solid #e0e0e0;
      border-radius: 8px;
      font-size: .875rem;
      font-weight: 500;
      color: #444;
      display: flex; align-items: center; justify-content: center; gap: .6rem;
      text-decoration: none;
      cursor: pointer;
      transition: background .2s, border-color .2s;
    }
    .btn-google:hover { background: #fff; border-color: #bbb; }
    .g-logo {
      width: 18px; height: 18px;
      background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 48 48'%3E%3Cpath fill='%234285F4' d='M45.12 24.5c0-1.56-.14-3.06-.4-4.5H24v8.51h11.84c-.51 2.75-2.06 5.08-4.39 6.64v5.52h7.11c4.16-3.83 6.56-9.47 6.56-16.17z'/%3E%3Cpath fill='%2334A853' d='M24 46c5.94 0 10.92-1.97 14.56-5.33l-7.11-5.52c-1.97 1.32-4.49 2.1-7.45 2.1-5.73 0-10.58-3.87-12.31-9.07H4.34v5.7C7.96 41.07 15.4 46 24 46z'/%3E%3Cpath fill='%23FBBC05' d='M11.69 28.18C11.25 26.86 11 25.45 11 24s.25-2.86.69-4.18v-5.7H4.34C2.85 17.09 2 20.45 2 24c0 3.55.85 6.91 2.34 9.88l7.35-5.7z'/%3E%3Cpath fill='%23EA4335' d='M24 10.75c3.23 0 6.13 1.11 8.41 3.29l6.31-6.31C34.91 4.18 29.93 2 24 2 15.4 2 7.96 6.93 4.34 14.12l7.35 5.7c1.73-5.2 6.58-9.07 12.31-9.07z'/%3E%3C/svg%3E") center/contain no-repeat;
    }
    .signup-text {
      text-align: center;
      font-size: .82rem;
      color: #888;
      margin-top: 1.4rem;
    }
    .signup-text a {
      color: #1565c0;
      font-weight: 600;
      text-decoration: none;
    }
    .signup-text a:hover { text-decoration: underline; }
    .alert {
      border-radius: 8px;
      font-size: .83rem;
      padding: .75rem 1rem;
      margin-bottom: 1.2rem;
      border: 1px solid transparent;
    }
    .alert-danger { background: #fff0f0; color: #c62828; border-color: #ffcdd2; }
    #togglePwd { cursor: pointer; }
  </style>
</head>
<body>

<jsp:include page="/public/components/sidebar.jsp" />

<div class="right-panel">
  <div class="flag-btn" title="Tiếng Việt">
    <svg viewBox="0 0 30 20" xmlns="http://www.w3.org/2000/svg">
      <rect width="30" height="20" fill="#DA251D"/>
      <polygon points="15,4 16.8,9.5 22.5,9.5 17.9,12.8 19.7,18.3 15,15 10.3,18.3 12.1,12.8 7.5,9.5 13.2,9.5" fill="#FFFF00"/>
    </svg>
  </div>

  <div class="logo-wrap">
    <div class="logo-hrm"><span>H</span><span>R</span><span>M</span></div>
    <div class="logo-powered">Powered by <span>Group 2</span></div>
  </div>

  <form class="login-form" method="POST" action="${pageContext.request.contextPath}/v1/auth/login">
    <h2>Đăng nhập hệ thống</h2>

    <c:if test="${not empty error}">
      <div class="alert alert-danger">
        <i class="bi bi-exclamation-circle me-1"></i>
        <c:out value="${error}"/>
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

    <div class="mb-2">
      <label for="pwdInput" class="form-label">Mật khẩu <span class="required">*</span></label>
      <div class="input-group">
        <input
          type="password"
          id="pwdInput"
          name="password"
          class="form-control"
          placeholder="Nhập mật khẩu"
          autocomplete="current-password"
          required
        />
        <button type="button" class="btn btn-eye" id="togglePwd" aria-label="Hiện/ẩn mật khẩu">
          <i class="bi bi-eye" id="eyeIcon"></i>
        </button>
      </div>
      <a href="${pageContext.request.contextPath}/v1/auth/forget-password" class="forgot-link">Quên mật khẩu?</a>
    </div>

    <button type="submit" class="btn-login">Đăng nhập</button>

    <div class="divider"><span>HOẶC</span></div>

    <a href="${pageContext.request.contextPath}/v1/auth/google" class="btn-google">
      <span class="g-logo" aria-hidden="true"></span>
      Đăng nhập bằng Google
    </a>

    <div class="signup-text">
      Chưa có tài khoản?
      <a href="${pageContext.request.contextPath}/v1/auth/register">Đăng ký</a>
    </div>
  </form>
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
