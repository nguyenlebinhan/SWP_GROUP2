<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Sửa dữ liệu chấm công - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
    <style>
        body { background: #f5f6fa; font-family: 'Segoe UI', sans-serif; }
        .main { margin-left: 250px; padding: 25px; }
        .section-card {
            background: white;
            border-radius: 14px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.07);
            padding: 24px;
            margin-bottom: 24px;
        }
        .badge-s0 { background:#d1fae5; color:#065f46; }
        .badge-s1 { background:#fef3c7; color:#92400e; }
        .badge-s2 { background:#fee2e2; color:#991b1b; }
        .badge-s3 { background:#e5e7eb; color:#374151; }
        .badge-s4 { background:#dbeafe; color:#1e40af; }
        .badge-s5 { background:#ede9fe; color:#5b21b6; }
        .badge-s6 { background:#f3f4f6; color:#4b5563; }
        .badge-st { padding:4px 10px; border-radius:20px; font-size:12px; font-weight:600; }
    </style>
</head>
<body>

<jsp:include page="${empty sidebarPage ? '/public/components/managerSideBar.jsp' : sidebarPage}" />

<div class="main">
    <jsp:include page="${empty topbarPage ? '/public/components/managerTopBar.jsp' : topbarPage}">
        <jsp:param name="title" value="Sửa dữ liệu chấm công" />
    </jsp:include>

    <c:if test="${not empty sessionScope.error}">
        <div class="alert alert-danger alert-dismissible fade show">
            <i class="fa-solid fa-circle-xmark me-2"></i>${sessionScope.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        <c:remove var="error" scope="session"/>
    </c:if>

    <div class="section-card">
        <div class="mb-4">
            <h5 class="mb-1">
                <i class="fa-solid fa-user me-2"></i>${attendance.employeeCode} - <c:out value="${attendance.fullName}"/>
            </h5>
            <div class="text-muted">
                Phòng ban: <c:out value="${attendance.departmentName}"/> &nbsp;|&nbsp;
                Ngày chấm công: <strong>${attendance.workDate}</strong>
            </div>
            <div class="mt-2">
                Trạng thái hiện tại:
                <span class="badge-st badge-s${attendance.attendanceStatus}">${attendance.statusLabel}</span>
            </div>
        </div>

        <c:if test="${editLocked}">
            <div class="alert alert-warning">
                <i class="fa-solid fa-lock me-2"></i>
                Đã quá hạn chỉnh sửa. Chấm công chỉ được sửa đến hết ngày 5 của tháng kế tiếp tháng chấm công.
            </div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/v1/employee/attendance/update">
            <input type="hidden" name="attendanceId" value="${attendance.attendanceId}">
            <%-- giữ lại bộ lọc để quay về đúng trang danh sách --%>
            <input type="hidden" name="month" value="${filterMonth}">
            <input type="hidden" name="year" value="${filterYear}">
            <input type="hidden" name="departmentId" value="${filterDepartmentId}">
            <input type="hidden" name="employeeCode" value="<c:out value='${filterEmployeeCode}'/>">

            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Giờ vào</label>
                    <input type="time" name="timeIn" class="form-control" value="${attendance.timeIn}" ${editLocked ? 'disabled' : ''}>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Giờ ra</label>
                    <input type="time" name="timeOut" class="form-control" value="${attendance.timeOut}" ${editLocked ? 'disabled' : ''}>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Số giờ làm</label>
                    <input type="text" class="form-control" value="${attendance.hoursWorkedLabel}" disabled>
                    <div class="form-text">
                        Trạng thái được hệ thống tự xác định lại từ giờ vào / giờ ra khi lưu
                        (đúng giờ, đi muộn, vắng mặt, nghỉ phép, nghỉ lễ, cuối tuần). Số giờ làm cũng tự tính lại.
                    </div>
                </div>
                <div class="col-md-12">
                    <label class="form-label">Lý do chỉnh sửa <span class="text-danger">*</span></label>
                    <textarea name="reason" class="form-control" rows="2" ${editLocked ? 'disabled' : 'required'}
                              placeholder="VD: máy chấm công lỗi..."></textarea>
                </div>
            </div>

            <div class="mt-4 d-flex gap-2">
                <c:if test="${not editLocked}">
                    <button type="submit" class="btn btn-primary">
                        <i class="fa-solid fa-floppy-disk me-1"></i> Lưu thay đổi
                    </button>
                </c:if>
                <a href="${backUrl}" class="btn btn-secondary">
                    <i class="fa-solid fa-arrow-left me-1"></i> Quay lại danh sách
                </a>
            </div>
        </form>
    </div>

    <div class="section-card">
        <h6 class="mb-3"><i class="fa-solid fa-clock-rotate-left me-2"></i>Lịch sử chỉnh sửa</h6>
        <c:choose>
            <c:when test="${empty adjustmentHistory}">
                <div class="text-muted">Bản ghi này chưa từng được chỉnh sửa.</div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table table-sm align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>Thời điểm</th>
                                <th>Người sửa</th>
                                <th>Giá trị cũ</th>
                                <th>Giá trị mới</th>
                                <th>Lý do</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="h" items="${adjustmentHistory}">
                                <tr>
                                    <td>${h.updatedAt}</td>
                                    <td><c:out value="${h.updatedByName}" /></td>
                                    <td class="small text-muted"><c:out value="${h.oldValue}" /></td>
                                    <td class="small"><c:out value="${h.newValue}" /></td>
                                    <td><c:out value="${h.reason}" /></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
