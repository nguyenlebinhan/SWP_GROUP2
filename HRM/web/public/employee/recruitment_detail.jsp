<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Chi tiết ứng viên</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                margin: 0;
                background: #f4f6fb;
            }
            .main-content {
                margin-left: 250px;
                padding: 32px 36px;
            }
            .info-label {
                color: #888;
                font-size: 13px;
            }
            .info-value {
                font-size: 15px;
                font-weight: 500;
            }
            .cv-card {
                border: 1px solid #e0e0e0;
                border-radius: 10px;
                padding: 24px;
                background: #fff;
            }
        </style>
    </head>
    <body>

        <jsp:include page="/public/components/employeeSideBar.jsp" />

        <div class="main-content">
            <jsp:include page="/public/components/employeeTopBar.jsp">
                <jsp:param name="title" value="Tuyển Dụng" />
            </jsp:include>
            
            <%-- Back --%>
            <a href="${pageContext.request.contextPath}/v1/employee/recruitment-list?stage=${candidate.stage == 'APPLIED' ? 'APPLIED' : candidate.stage}"
               class="text-decoration-none text-secondary mb-3 d-inline-block">
                <i class="fa fa-arrow-left"></i> Quay lại
            </a>

            <%-- Alert --%>
            <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-danger alert-dismissible fade show">
                    ${sessionScope.error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="error" scope="session"/>
            </c:if>

            <h4 class="fw-bold mb-0">${candidate.fullName}</h4><br>

            <div class="row g-4">
                <%-- Cột trái: thông tin --%>
                <div class="col-md-7">
                    <div class="card shadow-sm p-4">
                        <table class="table table-borderless mb-0">
                            <tr>
                                <td class="info-label" style="width:160px">Họ và tên</td>
                                <td class="info-value">${candidate.fullName}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Vị trí ứng tuyển</td>
                                <td class="info-value">${candidate.positionName} của ${candidate.departmentName}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Ngày sinh</td>
                                <td class="info-value">${candidate.dateOfBirth}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Giới tính</td>
                                <td class="info-value">${candidate.gender}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Địa chỉ</td>
                                <td class="info-value">${candidate.address}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Số điện thoại</td>
                                <td class="info-value">${candidate.phoneNumber}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Email</td>
                                <td class="info-value">${candidate.email}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Kinh nghiệm</td>
                                <td class="info-value">${candidate.experience}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Kỹ năng</td>
                                <td class="info-value">${candidate.skills}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Chứng chỉ</td>
                                <td class="info-value">${candidate.certificates}</td>
                            </tr>
                            <tr>
                                <td class="info-label">Bằng cấp</td>
                                <td class="info-value">${candidate.degree}</td>
                            </tr>
                        </table>

                        <%-- Nút Đậu / Loại — chỉ hiện khi có PROCESS_RECRUITMENT và stage là APPLIED --%>
                        <c:if test="${sessionScope.userPermissions.contains('PROCESS_RECRUITMENT') && candidate.stage == 'APPLIED'}">
                            <div class="d-flex gap-3 mt-4">
                                <button class="btn btn-success px-4"
                                        onclick="openEmailModal('PASSED')">
                                    Đậu
                                </button>
                                <button class="btn btn-secondary px-4"
                                        onclick="openEmailModal('REJECTED')">
                                    Loại
                                </button>
                            </div>
                        </c:if>

                        <%-- Hiển thị trạng thái nếu đã duyệt --%>
                        <c:if test="${candidate.stage == 'INTERVIEW'}">
                            <div class="alert alert-success mt-4 mb-0">
                                <i class="fa fa-check-circle"></i> Hồ sơ đã được duyệt đậu — chuyển sang phỏng vấn.
                            </div>
                        </c:if>
                        <c:if test="${candidate.stage == 'REJECTED'}">
                            <div class="alert alert-danger mt-4 mb-0">
                                <i class="fa fa-times-circle"></i> Hồ sơ đã bị loại.
                            </div>
                        </c:if>
                    </div>
                </div>

                <%-- Cột phải: CV --%>
                <div class="col-md-5">
                    <div class="cv-card h-100 d-flex flex-column">
                        <h6 class="fw-bold mb-3">CV ứng viên</h6>
                        <c:choose>
                            <c:when test="${not empty candidate.cvFileUrl}">
                                <a href="${candidate.cvFileUrl}" target="_blank"
                                   class="btn btn-outline-primary mb-3">
                                    <i class="fa fa-file"></i> Xem toàn bộ hồ sơ
                                </a>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted">Không có file CV.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <%-- Modal gửi email — chỉ render khi có quyền PROCESS_RECRUITMENT --%>
        <c:if test="${sessionScope.userPermissions.contains('PROCESS_RECRUITMENT') && candidate.stage == 'APPLIED'}">
            <div class="modal fade" id="emailModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="emailModalTitle">Gửi thông báo cho ứng viên</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <form method="post"
                              action="${pageContext.request.contextPath}/v1/employee/recruitment-review">
                            <div class="modal-body">
                                <input type="hidden" name="candidateId" value="${candidate.candidateId}"/>
                                <input type="hidden" name="result" id="resultInput"/>
                                <input type="hidden" name="toEmail" value="${candidate.email}"/>

                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Gửi đến</label>
                                    <input type="text" class="form-control" value="${candidate.email}" readonly/>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Tiêu đề <span class="text-danger">*</span></label>
                                    <input type="text" name="emailSubject" id="emailSubject" class="form-control" required/>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Nội dung <span class="text-danger">*</span></label>
                                    <textarea name="emailBody" id="emailBody" class="form-control" rows="7" required></textarea>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Ghi chú (nội bộ)</label>
                                    <input type="text" name="note" class="form-control"
                                           placeholder="Ghi chú không gửi đến ứng viên"/>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <div id="confirmBox" class="d-none w-100">
                                    <div class="alert alert-warning mb-2">
                                        <i class="fa fa-triangle-exclamation"></i>
                                        Email sẽ được gửi đến <strong>${candidate.email}</strong>.
                                        Hồ sơ sẽ được cập nhật sau khi gửi thành công. Xác nhận?
                                    </div>
                                    <div class="d-flex justify-content-end gap-2">
                                        <button type="button" class="btn btn-secondary"
                                                onclick="document.getElementById('confirmBox').classList.add('d-none')">
                                            Hủy bỏ
                                        </button>
                                        <button type="submit" class="btn btn-primary">
                                            <i class="fa fa-paper-plane"></i> Gửi
                                        </button>
                                    </div>
                                </div>
                                <div id="sendBtnBox" class="d-flex gap-2">
                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                                    <button type="button" class="btn btn-primary" onclick="showConfirm()">
                                        Tiếp tục
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <script>
                // Template mặc định theo result
                const templates = {
                    PASSED: {
                        subject: "Thư mời phỏng vấn - ${candidate.positionName}",
                        body: "Chào ${candidate.fullName},\n\nChúng tôi trân trọng mời bạn tham gia phỏng vấn cho vị trí ${candidate.positionName}.\n\nThời gian: [thời gian]\nĐịa điểm: [địa điểm]\n\nTrân trọng."
                    },
                    REJECTED: {
                        subject: "Thông báo kết quả hồ sơ - ${candidate.positionName}",
                        body: "Chào ${candidate.fullName},\n\nCảm ơn bạn đã quan tâm đến vị trí ${candidate.positionName}.\nSau xem xét, chúng tôi nhận thấy bạn chưa phù hợp với vị trí này.\n\nChúc bạn thành công."
                    }
                };

                function openEmailModal(result) {
                    document.getElementById('resultInput').value = result;
                    document.getElementById('emailSubject').value = templates[result].subject;
                    document.getElementById('emailBody').value = templates[result].body;
                    document.getElementById('confirmBox').classList.add('d-none');
                    document.getElementById('sendBtnBox').classList.remove('d-none');
                    document.getElementById('emailModalTitle').innerText =
                            result === 'PASSED' ? 'Gửi thư mời phỏng vấn' : 'Gửi thông báo từ chối';
                    new bootstrap.Modal(document.getElementById('emailModal')).show();
                }

                function showConfirm() {
                    const subject = document.getElementById('emailSubject').value.trim();
                    const body = document.getElementById('emailBody').value.trim();
                    if (!subject || !body) {
                        alert('Vui lòng nhập tiêu đề và nội dung email.');
                        return;
                    }
                    document.getElementById('sendBtnBox').classList.add('d-none');
                    document.getElementById('confirmBox').classList.remove('d-none');
                }
            </script>
        </c:if>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>F