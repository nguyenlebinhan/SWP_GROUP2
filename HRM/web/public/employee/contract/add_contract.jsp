<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="contractDAO" class="dao.EmploymentContractDAO" scope="page" />
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Thêm hợp đồng lao động - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet">
        <style>
            body {
                background: #f5f6fa;
                font-family: "Segoe UI", sans-serif;
            }
            .main {
                margin-left: 250px;
                padding: 24px;
            }
            .page-card {
                background: #fff;
                border-radius: 16px;
                box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
                padding: 28px;
            }
            @media (max-width: 768px) {
                .main {
                    margin-left: 0;
                    padding: 16px;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/employeeSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/employeeTopBar.jsp">
                <jsp:param name="title" value="Thêm hợp đồng lao động" />
                <jsp:param name="backUrl" value="/v1/employee/employee_info/list" />
            </jsp:include>

            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                    ${sessionScope.success}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="success" scope="session"/>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                    ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <div class="page-card">
                <form id="contractForm" method="post" action="${pageContext.request.contextPath}/v1/employee/contract/add" enctype="multipart/form-data">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Mã hợp đồng</label>
                            <input type="text" name="contractCode" id="contractCode" class="form-control"
                                   value="${generatedCode}" readonly>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Nhân viên</label>
                            <select name="employeeId" id="employeeId" class="form-select" required>
                                <option value="">-- Chọn nhân viên --</option>
                                <c:forEach var="emp" items="${employees}">
                                    <option value="${emp.employeeId}" ${param.employeeId == emp.employeeId ? 'selected' : ''}>
                                        ${emp.fullName} (${emp.employeeCode})
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Loại hợp đồng</label>
                            <select name="contractType" id="contractType" class="form-select" required>
                                <option value="">-- Chọn loại hợp đồng --</option>
                                <option value="PROBATION" ${param.contractType == 'PROBATION' ? 'selected' : ''}>Thử việc</option>
                                <option value="INTERNSHIP" ${param.contractType == 'INTERNSHIP' ? 'selected' : ''}>Thực tập</option>
                                <option value="FIXED_TERM" ${param.contractType == 'FIXED_TERM' ? 'selected' : ''}>Có thời hạn</option>
                                <option value="INDEFINITE" ${param.contractType == 'INDEFINITE' ? 'selected' : ''}>Không xác định thời hạn</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Lương</label>
                            <input type="number" name="salary" id="salary" class="form-control" min="0" step="1000"
                                   value="${param.salary}" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Ngày hiệu lực</label>
                            <input type="date" name="effectiveDate" id="effectiveDate" class="form-control"
                                   value="${param.effectiveDate}" required>
                        </div>
                        <div class="col-md-6" id="durationGroup">
                            <label class="form-label">Thời hạn</label>
                            <select name="durationValue" id="durationValue" class="form-select">
                                <option value="">-- Chọn thời hạn --</option>
                            </select>
                            <input type="hidden" name="durationUnit" id="durationUnit" value="">
                        </div>
                        <div class="col-12">
                            <label class="form-label">Ghi chú</label>
                            <textarea name="note" id="note" class="form-control" rows="3">${param.note}</textarea>
                        </div>
                        <div class="col-12">
                            <div class="form-check mt-2">
                                <input class="form-check-input" type="checkbox"
                                       name="unionMember" id="unionMember"
                                       ${param.unionMember != null ? 'checked' : ''}>
                                <label class="form-check-label" for="unionMember">
                                    <i class="fa-solid fa-people-group me-1 text-primary"></i>
                                    Tham gia công đoàn
                                </label>
                                <div class="form-text text-muted">
                                    Tích nếu nhân viên đăng ký tham gia tổ chức công đoàn.
                                </div>
                            </div>
                        </div>
                        <div class="col-12">
                            <label class="form-label">File hợp đồng đã ký (PDF)</label>
                            <div class="input-group">
                                <input type="file" name="signedContract" id="signedContract" class="form-control" accept="application/pdf">
                                <button type="button" id="extractBtn" class="btn btn-outline-primary">
                                    <i class="fa-solid fa-file-import me-1"></i> Trích xuất thông tin
                                </button>
                            </div>
                            <div id="extractStatus" class="form-text">Chỉ chấp nhận file PDF, tối đa 10MB.</div>
                        </div>
                    </div>

                    <div class="d-flex gap-2 mt-4 flex-wrap">
                        <button type="submit" class="btn btn-primary" name="action" value="submit">
                            <i class="fa-solid fa-paper-plane me-1"></i>Gửi duyệt
                        </button>
                        <a href="${pageContext.request.contextPath}/v1/employee/employee_info/list" class="btn btn-outline-secondary"
                           onclick="return confirm('Bạn có chắc muốn hủy? Thông tin chưa lưu sẽ mất.');">
                            <i class="fa-solid fa-ban me-1"></i>Hủy
                        </a>
                        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/v1/employee/contract/blank-template?type=fixed_term">
                            <i class="fa-solid fa-download me-1"></i>Tải mẫu hợp đồng trống
                        </a>
                    </div>
                </form>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        <script>
                               document.getElementById('extractBtn').addEventListener('click', function () {
                                   const fileInput = document.getElementById('signedContract');
                                   const file = fileInput.files[0];
                                   const statusDiv = document.getElementById('extractStatus');

                                   if (!file) {
                                       statusDiv.textContent = 'Vui lòng chọn file PDF trước.';
                                       statusDiv.className = 'form-text text-danger';
                                       return;
                                   }

                                   const formData = new FormData();
                                   formData.append('file', file);

                                   statusDiv.textContent = 'Đang xử lý...';
                                   statusDiv.className = 'form-text text-info';

                                   fetch('${pageContext.request.contextPath}/v1/employee/contract/parse-pdf', {
                                       method: 'POST',
                                       body: formData
                                   })
                                           .then(response => response.json())
                                           .then(data => {
                                               if (data.contractType) {
                                                   document.getElementById('contractType').value = data.contractType;
                                                   document.getElementById('contractType').dispatchEvent(new Event('change'));
                                               }
                                               if (data.effectiveDate) {
                                                   document.getElementById('effectiveDate').value = data.effectiveDate;
                                               }
                                               if (data.salary)
                                                   document.getElementById('salary').value = data.salary;

                                               if (data.employeeCode) {
                                                   const empSelect = document.getElementById('employeeId');
                                                   for (let i = 0; i < empSelect.options.length; i++) {
                                                       if (empSelect.options[i].text.includes(data.employeeCode)) {
                                                           empSelect.value = empSelect.options[i].value;
                                                           break;
                                                       }
                                                   }
                                               }

                                               if (data.effectiveDate && data.endDate && data.contractType !== 'INDEFINITE') {
                                                   const start = new Date(data.effectiveDate);
                                                   const end = new Date(data.endDate);
                                                   const diffYears = end.getFullYear() - start.getFullYear();
                                                   const diffMonths = (diffYears * 12) + (end.getMonth() - start.getMonth());

                                                   const durSelect = document.getElementById('durationValue');
                                                   let found = false;
                                                   for (let i = 0; i < durSelect.options.length; i++) {
                                                       const opt = durSelect.options[i];
                                                       if (data.contractType === 'FIXED_TERM' && diffYears == opt.value) {
                                                           durSelect.value = opt.value;
                                                           document.getElementById('durationUnit').value = opt.dataset.unit || 'YEAR';
                                                           found = true;
                                                           break;
                                                       } else if (data.contractType === 'PROBATION' && diffMonths == opt.value) {
                                                           durSelect.value = opt.value;
                                                           document.getElementById('durationUnit').value = opt.dataset.unit || 'DAY';
                                                           found = true;
                                                           break;
                                                       } else if (data.contractType === 'INTERNSHIP' && diffMonths == opt.value) {
                                                           durSelect.value = opt.value;
                                                           document.getElementById('durationUnit').value = opt.dataset.unit || 'MONTH';
                                                           found = true;
                                                           break;
                                                       }
                                                   }
                                                   if (found) {
                                                       durSelect.dispatchEvent(new Event('change'));
                                                   }
                                               }

                                               if (data.employeeName) {
                                                   statusDiv.textContent = 'Tìm thấy: ' + data.employeeName + (data.employeeCode ? ' (' + data.employeeCode + ')' : '') + '. Vui lòng kiểm tra lại thông tin.';
                                                   statusDiv.className = 'form-text text-success';
                                               } else {
                                                   statusDiv.textContent = 'Đã điền thông tin. Vui lòng kiểm tra lại.';
                                                   statusDiv.className = 'form-text text-success';
                                               }
                                           })
                                           .catch(error => {
                                               statusDiv.textContent = 'Lỗi xử lý PDF: ' + error.message;
                                               statusDiv.className = 'form-text text-danger';
                                           });
                               });
        </script>
        <script>
            document.addEventListener('DOMContentLoaded', function () {
                const contractType = document.getElementById('contractType');
                const durationSelect = document.getElementById('durationValue');
                const durationUnit = document.getElementById('durationUnit');

                const durationOptions = {
                    'PROBATION': [
                        {value: 30, label: '30 ngày', unit: 'DAY'},
                        {value: 60, label: '60 ngày', unit: 'DAY'},
                        {value: 180, label: '180 ngày', unit: 'DAY'}
                    ],
                    'INTERNSHIP': [
                        {value: 2, label: '2 tháng', unit: 'MONTH'},
                        {value: 3, label: '3 tháng', unit: 'MONTH'},
                        {value: 4, label: '4 tháng', unit: 'MONTH'},
                        {value: 5, label: '5 tháng', unit: 'MONTH'},
                        {value: 6, label: '6 tháng', unit: 'MONTH'}
                    ],
                    'FIXED_TERM': [
                        {value: 1, label: '1 năm', unit: 'YEAR'},
                        {value: 2, label: '2 năm', unit: 'YEAR'},
                        {value: 3, label: '3 năm', unit: 'YEAR'}
                    ],
                    'INDEFINITE': []
                };

                function updateDuration() {
                    const type = contractType.value;
                    durationSelect.innerHTML = '';

                    if (type === 'INDEFINITE') {
                        const opt = document.createElement('option');
                        opt.value = '';
                        opt.textContent = 'Không xác định';
                        durationSelect.appendChild(opt);
                        durationSelect.disabled = true;
                        durationUnit.value = '';
                    } else if (durationOptions[type]) {
                        durationSelect.disabled = false;
                        const placeholder = document.createElement('option');
                        placeholder.value = '';
                        placeholder.textContent = '-- Chọn thời hạn --';
                        durationSelect.appendChild(placeholder);

                        durationOptions[type].forEach(function (opt) {
                            const option = document.createElement('option');
                            option.value = opt.value;
                            option.textContent = opt.label;
                            option.dataset.unit = opt.unit;
                            durationSelect.appendChild(option);
                        });
                    }
                }

                function updateUnit() {
                    const selected = durationSelect.options[durationSelect.selectedIndex];
                    if (selected && selected.dataset.unit) {
                        durationUnit.value = selected.dataset.unit;
                    } else {
                        durationUnit.value = '';
                    }
                }

                contractType.addEventListener('change', function () {
                    updateDuration();
                    updateUnit();
                });
                durationSelect.addEventListener('change', updateUnit);
                updateDuration();
                if (durationSelect.options.length > 0) {
                    const savedVal = '${param.durationValue}';
                    if (savedVal) {
                        durationSelect.value = savedVal;
                        updateUnit();
                    }
                }
            });
        </script>
    </body>
</html>