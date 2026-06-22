<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Đặt lại mật khẩu - HRM</title>
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
    .logo-icon {
      width: 52px; height: 52px;
      background: linear-gradient(135deg, #1565c0 0%, #0d47a1 100%);
      border-radius: 14px;
      display: flex; align-items: center; justify-content: center;
      margin: 0 auto .75rem;
      box-shadow: 0 4px 14px rgba(21,101,192,.3);
    }
    .logo-icon i { font-size: 1.5rem; color: #fff; }
    .logo-hrm { font-size: 1.7rem; font-weight: 800; letter-spacing: -.5px; line-height: 1; }
    .logo-hrm span { color: #ff6b00; }
    .logo-powered { font-size: .7rem; color: #aaa; margin-top: .2rem; }
    .logo-powered span { color: #ff6b00; font-weight: 600; }


    
    .form-heading {
      font-size: 1.15rem; font-weight: 700;
      color: #1a1a2e; text-align: center;
      margin-bottom: .4rem;
    }
    .form-sub {
      font-size: .78rem; color: #9aa0b5;
      text-align: center; margin-bottom: 1.6rem;
      line-height: 1.55;
    }

    
    .form-label {
      font-size: .78rem; font-weight: 600;
      color: #444; margin-bottom: .3rem; display: block;
    }

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
    }
    .pw-toggle:hover { color: #555; }

    input[type="password"]::-ms-reveal,
    input[type="password"]::-ms-clear { display: none; }
    input[type="password"]::-webkit-credentials-auto-fill-button { display: none; }

   
    .btn-submit {
      width: 100%; height: 46px;
      background: #1565c0; color: #fff;
      border: none; border-radius: 9px;
      font-size: .9rem; font-weight: 600;
      letter-spacing: .02em; margin-top: 1.2rem;
      cursor: pointer; font-family: inherit;
      display: flex; align-items: center; justify-content: center; gap: .4rem;
    }
    .btn-submit:hover {
      background: #0d47a1;
      transform: translateY(-1px);
    }
    .btn-submit:active { transform: translateY(0); }
    .btn-submit:disabled {
      background: #90a4ae; cursor: not-allowed;
      transform: none; box-shadow: none;
    }


    .back-link {
      display: flex; align-items: center; justify-content: center; gap: .35rem;
      font-size: .8rem; color: #9aa0b5; font-weight: 500;
      text-decoration: none; margin-top: 1.1rem;
      transition: color .2s;
    }
    .back-link:hover { color: #1565c0; }

    .alert {
      border-radius: 9px; font-size: .8rem;
      padding: .7rem .9rem; margin-bottom: 1rem;
      border: 1px solid transparent;
      display: flex; align-items: flex-start; gap: .5rem;
    }
    .alert-danger  { background: #fff0f0; color: #c62828; border-color: #ffcdd2; }
    .alert-success { background: #f0fff4; color: #2e7d32; border-color: #c8e6c9; }


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
      <div class="logo-icon"><i class="bi bi-people-fill"></i></div>
      <div class="logo-hrm"><span>H</span><span>R</span><span>M</span></div>
      <div class="logo-powered">Powered by <span>Group 2</span></div>
    </div>


    <p class="form-heading">Đặt lại mật khẩu</p>
    <p class="form-sub">Nhập mật khẩu hệ thống đã gửi<br>và mật khẩu mới của bạn.</p>

    <c:if test="${not empty error}">
      <div class="alert alert-danger">
        <i class="bi bi-exclamation-circle"></i>
        <span><c:out value="${error}"/></span>
      </div>
    </c:if>

    <c:if test="${not empty success}">
      <div class="alert alert-success">
        <i class="bi bi-check-circle"></i>
        <span><c:out value="${success}"/></span>
      </div>
    </c:if>

    <c:if test="${empty success}">
      <form method="POST" action="${pageContext.request.contextPath}/v1/auth/change-password">

        <div class="mb-3">
          <label class="form-label" for="sysPassword">
            Mật khẩu hệ thống gửi <span class="required">*</span>
          </label>
          <div class="pw-wrap">
            <input
              type="password"
              id="sysPassword"
              name="sysPassword"
              class="form-control"
              placeholder="Nhập mật khẩu hệ thống"
              value ="${sysPassword}"
              required
            />
            <button type="button" class="pw-toggle" onclick="togglePw('sysPassword', this)" aria-label="Hiện/ẩn mật khẩu">
              <i class="bi bi-eye"></i>
            </button>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label" for="yourPassword">
            Mật khẩu mới <span class="required">*</span>
          </label>
          <div class="pw-wrap">
            <input
              type="password"
              id="yourPassword"
              name="yourPassword"
              class="form-control"
              placeholder="Nhập mật khẩu mới"
              required
            />
            <button type="button" class="pw-toggle" onclick="togglePw('yourPassword', this)" aria-label="Hiện/ẩn mật khẩu">
              <i class="bi bi-eye"></i>
            </button>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label" for="yourPassword">
            Xác nhận mật khẩu <span class="required">*</span>
          </label>
          <div class="pw-wrap">
            <input
              type="password"
              id="confirmationPassword"
              name="confirmationPassword"
              class="form-control"
              placeholder="Xác nhận mật khẩu"
              required
            />
            <button type="button" class="pw-toggle" onclick="togglePw('confirmationPassword', this)" aria-label="Hiện/ẩn mật khẩu">
              <i class="bi bi-eye"></i>
            </button>
          </div>
        </div>
          
        <button type="submit" class="btn-submit">
          <i class="bi bi-shield-check"></i> Thay đổi mật khẩu
        </button>

      </form>
    </c:if>

    <a href="${pageContext.request.contextPath}/v1/auth/login" class="back-link">
      <i class="bi bi-arrow-left"></i> Quay lại đăng nhập
    </a>

  </div>


</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
<script>
  function togglePw(inputId, btn) {
    var inp = document.getElementById(inputId);
    var icon = btn.querySelector('i');
    if (inp.type === 'password') {
      inp.type = 'text';
      icon.className = 'bi bi-eye-slash';
    } else {
      inp.type = 'password';
      icon.className = 'bi bi-eye';
    }
  }


</script>
</body>
</html>

