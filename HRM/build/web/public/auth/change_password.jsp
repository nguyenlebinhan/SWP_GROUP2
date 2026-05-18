<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
    :root {
      --bs-body-font-family: 'Be Vietnam Pro', sans-serif;
      --bs-font-sans-serif: 'Be Vietnam Pro', sans-serif;
    }
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

    .logo-wrap { text-align: center; margin-bottom: 2rem; }
    .logo-hrm { font-size: 2rem; font-weight: 800; letter-spacing: -.5px; }
    .logo-hrm span { color: #ff6b00; }
    .logo-powered { font-size: .72rem; color: #999; letter-spacing: .02em; margin-top: .1rem; }
    .logo-powered span { color: #ff6b00; font-weight: 600; }

    .auth-form { width: 100%; max-width: 360px; }
    .auth-form h2 {
      font-size: 1.5rem; font-weight: 700;
      text-align: center; color: #1a1a2e; margin-bottom: .6rem;
    }
    .auth-form .subtitle {
      font-size: .82rem; color: #888;
      text-align: center; margin-bottom: 1.8rem; line-height: 1.5;
    }

    .form-label {
      font-size: .8rem; font-weight: 600;
      color: #333; margin-bottom: .35rem; display: block;
    }
    .required { color: #e53935; margin-left: 2px; }

    /* Password field wrapper để chứa icon toggle */
    .input-password-wrap {
      position: relative;
    }
    .input-password-wrap .form-control {
      padding-right: 2.8rem; /* chừa chỗ cho icon */
    }
    .toggle-pw {
      position: absolute;
      right: .75rem;
      top: 50%;
      transform: translateY(-50%);
      background: none;
      border: none;
      padding: 0;
      color: #aaa;
      cursor: pointer;
      font-size: 1.05rem;
      line-height: 1;
      transition: color .2s;
    }
    .toggle-pw:hover { color: #555; }

    .form-control {
      border: 1.5px solid #e0e0e0;
      border-radius: 8px;
      height: 46px;
      font-size: .875rem;
      color: #333;
      transition: border-color .2s, box-shadow .2s;
      padding: 0 .9rem;
      width: 100%;
      outline: none;
    }
    .form-control:focus {
      border-color: #1565c0;
      box-shadow: 0 0 0 3px rgba(21,101,192,.1);
    }
    .form-control::placeholder { color: #bbb; }

    .btn-submit {
      width: 100%; height: 48px;
      background: #1565c0; color: #fff;
      border: none; border-radius: 8px;
      font-size: .95rem; font-weight: 600;
      letter-spacing: .02em; margin-top: 1.4rem;
      transition: background .2s, transform .1s, box-shadow .2s;
      cursor: pointer;
    }
    .btn-submit:hover {
      background: #0d47a1;
      box-shadow: 0 4px 16px rgba(21,101,192,.35);
      transform: translateY(-1px);
    }
    .btn-submit:active { transform: translateY(0); }
    .btn-submit:disabled {
      background: #90a4ae;
      cursor: not-allowed;
      transform: none;
      box-shadow: none;
    }

    .back-link {
      display: block; text-align: center;
      font-size: .82rem; color: #1565c0;
      font-weight: 600; text-decoration: none;
      margin-top: 1.4rem; transition: color .2s;
    }
    .back-link:hover { color: #0d47a1; text-decoration: underline; }

    .alert {
      border-radius: 8px; font-size: .83rem;
      padding: .75rem 1rem; margin-bottom: 1.2rem;
      border: 1px solid transparent;
    }
    .alert-danger  { background: #fff0f0; color: #c62828; border-color: #ffcdd2; }
    .alert-success { background: #f0fff4; color: #2e7d32; border-color: #c8e6c9; }

    /* Countdown badge */
    .redirect-notice {
      margin-top: .6rem;
      font-size: .8rem;
      color: #2e7d32;
      text-align: center;
    }
    .redirect-notice span {
      font-weight: 700;
    }
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
    <div class="logo-hrm">
      <span>H</span><span>R</span><span>M</span>
    </div>
    <div class="logo-powered">Powered by <span>Group 2</span></div>
  </div>

  <div class="auth-form">
    <h2>Đặt lại mật khẩu</h2>
    <p class="subtitle">Nhập mật khẩu hệ thống đã gửi và mật khẩu mới của bạn.</p>

    <%-- Thông báo lỗi --%>
    <c:if test="${not empty error}">
      <div class="alert alert-danger">
        <i class="bi bi-exclamation-circle me-1"></i>
        <c:out value="${error}"/>
      </div>
    </c:if>

    <c:if test="${not empty success}">
      <div class="alert alert-success">
        <i class="bi bi-check-circle me-1"></i>
        <c:out value="${success}"/>
      </div>
    </c:if>

    <c:if test="${empty success}">
      <form method="POST" action="${pageContext.request.contextPath}/v1/auth/change-password">

        <div class="mb-3">
          <label class="form-label" for="sysPassword">
            Mật khẩu hệ thống gửi<span class="required">*</span>
          </label>
          <div class="input-password-wrap">
            <input
              type="password"
              id="sysPassword"
              name="sysPassword"
              class="form-control"
              placeholder="Nhập mật khẩu hệ thống"
              autocomplete="current-password"
              required
            />
            <button type="button" class="toggle-pw" onclick="togglePw('sysPassword', this)" aria-label="Hiện/ẩn mật khẩu">
              <i class="bi bi-eye"></i>
            </button>
          </div>
        </div>

        <div class="mb-3">
          <label class="form-label" for="yourPassword">
            Mật khẩu mới<span class="required">*</span>
          </label>
          <div class="input-password-wrap">
            <input
              type="password"
              id="yourPassword"
              name="yourPassword"
              class="form-control"
              placeholder="Nhập mật khẩu mới"
              autocomplete="new-password"
              required
            />
            <button type="button" class="toggle-pw" onclick="togglePw('yourPassword', this)" aria-label="Hiện/ẩn mật khẩu">
              <i class="bi bi-eye"></i>
            </button>
          </div>
        </div>

        <button type="submit" class="btn-submit">
          <i class="bi bi-shield-lock me-1"></i> Thay đổi mật khẩu
        </button>
      </form>
    </c:if>

    <a href="${pageContext.request.contextPath}/v1/auth/login" class="back-link">
      ← Quay lại đăng nhập
    </a>
  </div>

</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
<script>
  // Toggle show/hide password
  function togglePw(inputId, btn) {
    const input = document.getElementById(inputId);
    const icon  = btn.querySelector('i');
    if (input.type === 'password') {
      input.type = 'text';
      icon.classList.replace('bi-eye', 'bi-eye-slash');
    } else {
      input.type = 'password';
      icon.classList.replace('bi-eye-slash', 'bi-eye');
    }
  }

  // Countdown redirect sau khi đổi mật khẩu thành công
  const countdownEl = document.getElementById('countdown');
  if (countdownEl) {
    let seconds = 5;
    const timer = setInterval(() => {
      seconds--;
      countdownEl.textContent = seconds;
      if (seconds <= 0) {
        clearInterval(timer);
        window.location.href = '${pageContext.request.contextPath}/v1/auth/login';
      }
    }, 1000);
  }
</script>
</body>
</html>