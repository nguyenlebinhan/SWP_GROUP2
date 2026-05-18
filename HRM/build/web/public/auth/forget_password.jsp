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

    .logo-wrap {
      text-align: center;
      margin-bottom: 2rem;
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

    .auth-form {
      width: 100%;
      max-width: 360px;
    }
    .auth-form h2 {
      font-size: 1.5rem;
      font-weight: 700;
      text-align: center;
      color: #1a1a2e;
      margin-bottom: .6rem;
    }
    .auth-form .subtitle {
      font-size: .82rem;
      color: #888;
      text-align: center;
      margin-bottom: 1.8rem;
      line-height: 1.5;
    }

    .form-label {
      font-size: .8rem;
      font-weight: 600;
      color: #333;
      margin-bottom: .35rem;
      display: block;
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
      width: 100%;
      outline: none;
    }
    .form-control:focus {
      border-color: #1565c0;
      box-shadow: 0 0 0 3px rgba(21,101,192,.1);
    }
    .form-control::placeholder { color: #bbb; }

    .btn-submit {
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
    .btn-submit:hover {
      background: #0d47a1;
      box-shadow: 0 4px 16px rgba(21,101,192,.35);
      transform: translateY(-1px);
    }
    .btn-submit:active { transform: translateY(0); }

    .back-link {
      display: block;
      text-align: center;
      font-size: .82rem;
      color: #1565c0;
      font-weight: 600;
      text-decoration: none;
      margin-top: 1.4rem;
      transition: color .2s;
    }
    .back-link:hover { color: #0d47a1; text-decoration: underline; }

    .alert {
      border-radius: 8px;
      font-size: .83rem;
      padding: .75rem 1rem;
      margin-bottom: 1.2rem;
      border: 1px solid transparent;
    }
    .alert-danger  { background: #fff0f0; color: #c62828; border-color: #ffcdd2; }
    .alert-success { background: #f0fff4; color: #2e7d32; border-color: #c8e6c9; }
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
    <p class="subtitle">Nhập email của bạn, chúng tôi sẽ gửi mật khẩu mới về hòm thư.</p>

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
    <c:if test="${redirect == true}">
        <script>
            let seconds = 5;
            const interval = setInterval(() => {
                seconds--;
                if (seconds <= 0) {
                    window.location.href = '/HRM/v1/auth/change-password'; 
                }
            }, 1000);
        </script>
    </c:if>
    <form method="POST" action="${pageContext.request.contextPath}/v1/auth/forget-password">
      <div class="mb-3">
        <label class="form-label">
          Email <span class="required">*</span>
        </label>
        <input
          type="email"
          name="email"
          class="form-control"
          placeholder="Nhập địa chỉ email"
          value="<c:out value='${param.email}'/>"
          required
        />
      </div>

      <button type="submit" class="btn-submit">
        Gửi mã xác thực
      </button>
    </form>

    <a href="${pageContext.request.contextPath}/v1/auth/login" class="back-link">
      ← Quay lại đăng nhập
    </a>
  </div>

</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
</body>
</html>
