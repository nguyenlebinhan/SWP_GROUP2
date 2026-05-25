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
            .right-panel{
                display: flex;
                align-content: center;
                align-items: center;
                margin-left: 500px;
            }

        </style>
    </head>
    <body>

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

                    <table>
                        <tr>
                            <td>
                                <label for="username" class="form-label">Username <span class="required">*</span></label>
                            </td>
                            <td>
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
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label for="pwdInput" class="form-label">Mật khẩu <span class="required">*</span></label>
                            </td>
                            <td>
                                <input
                                    type="password"
                                    id="pwdInput"
                                    name="password"
                                    class="form-control"
                                    placeholder="Nhập mật khẩu"
                                    autocomplete="current-password"
                                    required
                                    />
                            </td>
                            <td>
                                <button type="button" class="pw-toggle" id="togglePwd" aria-label="Hiện/ẩn mật khẩu">
                                    <i class="bi bi-eye" id="eyeIcon"></i>
                                </button>
                            </td>
                        </tr>
                        <tr>
                            <td><a href="${pageContext.request.contextPath}/v1/auth/forget-password" class="forgot-link">Quên mật khẩu?</a></td>
                            <td><button type="submit" class="btn-submit"> Đăng nhập </button></td>
                        </tr>

                    </table>

                    

                    <div class="divider"><span>HOẶC</span></div>

                    <a href="${pageContext.request.contextPath}/v1/auth/google" class="btn-google">
                        <span class="g-logo" aria-hidden="true"></span>
                        Đăng nhập bằng Google
                    </a>

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


    .alert {
      border-radius: 9px; font-size: .8rem;
      padding: .7rem .9rem; margin-bottom: 1rem;
      border: 1px solid transparent;
      display: flex; align-items: flex-start; gap: .5rem;
    }
    .alert-danger { background: #fff0f0; color: #c62828; border-color: #ffcdd2; }


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
