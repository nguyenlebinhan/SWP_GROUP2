<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html>

        <head>
            <title>Chi tiết nhân viên - HRM</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
            <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
            <style>
                body {
                    background: #f5f6fa;
                    font-family: 'Segoe UI', sans-serif;
                }

                .main {
                    margin-left: 250px;
                    padding: 25px;
                }

                .page-card {
                    background: white;
                    border-radius: 14px;
                    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
                    padding: 30px;
                    max-width: 800px;
                    margin: 0 auto;
                }

                .avatar-lg {
                    width: 80px;
                    height: 80px;
                    border-radius: 50%;
                    background: #2563eb;
                    color: white;
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 32px;
                    font-weight: 700;
                    flex-shrink: 0;
                    margin-bottom: 20px;
                }

                .info-label {
                    font-size: 13px;
                    color: #64748b;
                    font-weight: 600;
                    margin-bottom: 4px;
                }

                .info-value {
                    font-size: 16px;
                    color: #0f172a;
                    font-weight: 500;
                    margin-bottom: 20px;
                }

                .section-title {
                    font-size: 18px;
                    font-weight: 700;
                    color: #0f172a;
                    border-bottom: 2px solid #e2e8f0;
                    padding-bottom: 10px;
                    margin-bottom: 20px;
                    margin-top: 30px;
                }
            </style>
        </head>

        <body>

            <jsp:include page="/public/components/employeeSideBar.jsp" />

            <div class="main">
                <jsp:include page="/public/components/employeeTopBar.jsp">
                    <jsp:param name="title" value="Hồ sơ nhân viên" />
                    <jsp:param name="backUrl"
                        value="/v1/employee/department-detail?id=${employeeDetail.departmentId}" />
                </jsp:include>

                <c:if test="${not empty sessionScope.success}">
                    <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                        <i class="fa-solid fa-circle-check me-2"></i>${sessionScope.success}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                    <c:remove var="success" scope="session" />
                </c:if>
                <c:if test="${not empty sessionScope.error}">
                    <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                        <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                    <c:remove var="error" scope="session" />
                </c:if>

                <div class="page-card">
                    <div class="text-center">
                        <div class="avatar-lg">
                            ${employeeDetail.fullName.substring(0,1).toUpperCase()}
                        </div>
                        <h4 class="fw-bold">${employeeDetail.fullName}</h4>
                        <p class="text-muted">${employeeDetail.email}</p>
                    </div>

                    <div class="section-title">Thông tin công việc</div>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="info-label">Mã nhân viên</div>
                            <div class="info-value"><code>${employeeDetail.employeeCode}</code></div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-label">Phòng ban</div>
                            <div class="info-value">
                                <c:choose>
                                    <c:when test="${not empty employeeDetail.departmentName}">
                                        ${employeeDetail.departmentName}</c:when>
                                    <c:otherwise><span class="text-muted">Chưa phân công</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-label">Vị trí</div>
                            <div class="info-value">
                                <c:choose>
                                    <c:when test="${not empty employeeDetail.positionName}">
                                        ${employeeDetail.positionName}</c:when>
                                    <c:otherwise><span class="text-muted">Chưa có</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="info-label">Vai trò hệ thống</div>
                            <div class="info-value">${employeeDetail.roleName}</div>
                        </div>
                    </div>

                    <form action="${pageContext.request.contextPath}/v1/employee/update-employee-detail" method="post">
                        <input type="hidden" name="employeeId" value="${employeeDetail.employeeId}">

                        <div class="section-title">Thông tin liên hệ & Chuyên môn</div>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="info-label">Số điện thoại</label>
                                <input type="text" class="form-control" name="phoneNumber"
                                    value="${employeeDetail.phoneNumber}">
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="info-label">Bằng cấp</label>
                                <input type="text" class="form-control" name="degree" value="${employeeDetail.degree}">
                            </div>
                            <div class="col-12 mb-3">
                                <label class="info-label">Kinh nghiệm</label>
                                <textarea class="form-control" name="experience"
                                    rows="2">${employeeDetail.experience}</textarea>
                            </div>
                            <div class="col-12 mb-3">
                                <label class="info-label">Kỹ năng</label>
                                <textarea class="form-control" name="skills"
                                    rows="2">${employeeDetail.skills}</textarea>
                            </div>
                        </div>

                        <div class="section-title">Cập nhật trạng thái</div>
                        <div class="row align-items-center mb-4">
                            <div class="col-md-8 mb-3 mb-md-0">
                                <select class="form-select" name="status">
                                    <option value="1" ${employeeDetail.status==1 ? 'selected' : '' }>Đang làm việc
                                        (Active)</option>
                                    <option value="2" ${employeeDetail.status==2 ? 'selected' : '' }>Đang nghỉ phép (On
                                        leave)</option>
                                    <option value="0" ${employeeDetail.status==0 ? 'selected' : '' }>Không hoạt động
                                        (Inactive)</option>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <button type="submit" class="btn btn-primary w-100">
                                    <i class="fa-solid fa-save me-2"></i> Lưu
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        </body>

        </html>