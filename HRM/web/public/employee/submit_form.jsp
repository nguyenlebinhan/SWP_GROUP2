<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html>

        <head>
            <title>Gửi đơn yêu cầu - HRM</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
            <style>
                body {
                    background: #f5f6fa;
                    font-family: 'Segoe UI', sans-serif;
                }

                .main {
                    margin-left: 250px;
                    padding: 25px;
                }

                .section-card {
                    background: white;
                    border-radius: 14px;
                    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
                    padding: 28px;
                    margin-bottom: 24px;
                }

                .section-card h5 {
                    font-weight: 700;
                    color: #0f172a;
                }

                #dateFields {
                    display: none;
                }
            </style>
        </head>

        <body>

            <jsp:include page="/public/components/employeeSideBar.jsp" />

            <div class="main">
                <jsp:include page="/public/components/employeeTopBar.jsp">
                    <jsp:param name="title" value="Gửi đơn yêu cầu" />
                </jsp:include>

                <c:if test="${not empty sessionScope.success}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        ${sessionScope.success}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                    <c:remove var="success" scope="session" />
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        ${error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <div class="section-card">
                    <h5 class="mb-4">Tạo đơn yêu cầu mới</h5>
                    <form method="post" action="${pageContext.request.contextPath}/v1/employee/submit-form"
                        enctype="multipart/form-data">
                        <div class="row g-3">

                            <div class="col-md-5">
                                <label class="form-label fw-semibold">Loại đơn <span
                                        class="text-danger">*</span></label>
                                <select id="formTypeId" name="formTypeId" class="form-select" required
                                    onchange="toggleDateFields(this)">
                                    <option value="">-- Chọn loại đơn --</option>
                                    <c:forEach var="ft" items="${formTypes}">
                                        <option value="${ft.formTypeId}" data-code="${ft.formTypeCode}">
                                            <c:out value="${ft.formTypeName}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-12" id="dateFields">
                                <div class="row g-3">
                                    <div class="col-md-4">
                                        <label class="form-label fw-semibold">Ngày bắt đầu <span
                                                class="text-danger">*</span></label>
                                        <input type="date" id="startDate" name="startDate" class="form-control">
                                        <small class="text-muted">Không được chọn ngày trong quá khứ</small>
                                    </div>
                                    <div class="col-md-4">
                                        <label class="form-label fw-semibold">Ngày kết thúc <span
                                                class="text-danger">*</span></label>
                                        <input type="date" id="endDate" name="endDate" class="form-control">

                                    </div>
                                </div>
                            </div>

                            <div class="col-md-12">
                                <label class="form-label fw-semibold">Lý do / Nội dung</label>
                                <textarea id="reason" name="reason" class="form-control" rows="4"
                                    placeholder="Mô tả chi tiết lý do gửi đơn..."><c:out value="${param.reason}"/></textarea>
                            </div>

                            <div class="col-md-12">
                                <label class="form-label fw-semibold">File đính kèm <span class="text-muted">(không bắt
                                        buộc)</span></label>
                                <input type="file" name="attachment" class="form-control"
                                    accept=".xlsx,.pdf,.docx,.doc,.xls,.jpg,.png,.zip">
                                <small class="text-muted">Cho phép: xlsx, pdf, docx, doc, xls, jpg, png, zip</small>
                            </div>

                        </div>
                        <div class="mt-4 d-flex gap-2">
                            <button type="submit" class="btn btn-primary" id="submitBtn">Gửi đơn</button>
                            <a href="${pageContext.request.contextPath}/v1/employee/my-forms"
                                class="btn btn-outline-secondary">Xem đơn của tôi</a>
                        </div>
                    </form>
                </div>
            </div>

            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
            <script>
                const DATE_TYPES = ['LEAVE', 'OVERTIME'];

                function toggleDateFields(select) {
                    const option = select.options[select.selectedIndex];
                    const code = option ? option.getAttribute('data-code') : '';
                    const show = DATE_TYPES.includes(code);
                    const dateFields = document.getElementById('dateFields');
                    dateFields.style.display = show ? 'block' : 'none';

                    const startInput = document.getElementById('startDate');
                    const endInput = document.getElementById('endDate');
                    startInput.required = show;
                    endInput.required = show;

                    if (show) {
                        // Đặt min date = hôm nay
                        const today = new Date().toISOString().split('T')[0];
                        startInput.min = today;
                        endInput.min = today;
                    } else {
                        startInput.value = '';
                        endInput.value = '';
                    }
                }

                document.getElementById('startDate').addEventListener('change', function () {
                    const start = this.value;
                    if (!start) return;
                    const endInput = document.getElementById('endDate');
                    endInput.min = start;
                    if (endInput.value && endInput.value < start) {
                        endInput.value = start;
                    }
                });
            </script>
        </body>

        </html>