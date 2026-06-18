<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Import Ứng Viên</title>
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
            .upload-zone {
                border: 2px dashed #1565C0;
                border-radius: 12px;
                padding: 48px;
                text-align: center;
                background: #f0f4ff;
                cursor: pointer;
                transition: background 0.2s;
            }
            .upload-zone:hover {
                background: #e3eaff;
            }
            .upload-zone i {
                font-size: 48px;
                color: #1565C0;
            }
            .result-success {
                border-left: 4px solid #2e7d32;
            }
            .result-partial  {
                border-left: 4px solid #f57f17;
            }
            .result-failed   {
                border-left: 4px solid #c62828;
            }
        </style>
    </head>
    <body>

        <%@ include file="/public/components/managerSideBar.jsp" %>

        <div class="main-content">

            <a href="${pageContext.request.contextPath}/v1/manager/recruitment-list"
               class="text-decoration-none text-secondary mb-3 d-inline-block">
                <i class="fa fa-arrow-left"></i> Quay lại danh sách
            </a>

            <h4 class="fw-bold mb-1">Import Ứng Viên từ Excel</h4>
            <p class="text-muted mb-4">Tải lên file Excel theo đúng định dạng mẫu để import hàng loạt ứng viên.</p>

            <%-- Kết quả import --%>
            <c:if test="${not empty importResult}">
                <c:choose>
                    <c:when test="${importResult.status == 1}">
                        <div class="card result-success mb-4 shadow-sm">
                            <div class="card-body">
                                <h6 class="text-success fw-bold">
                                    <i class="fa fa-check-circle"></i> Import thành công
                                </h6>
                                <p class="mb-1">${importResult.note}</p>
                                <small class="text-muted">File: ${importResult.fileName}</small>
                            </div>
                        </div>
                    </c:when>
                    <c:when test="${importResult.status == 3}">
                        <div class="card result-partial mb-4 shadow-sm">
                            <div class="card-body">
                                <h6 class="text-warning fw-bold">
                                    <i class="fa fa-triangle-exclamation"></i> Import một phần
                                </h6>
                                <p class="mb-2">${importResult.note}</p>
                                <c:if test="${not empty importResult.errors}">
                                    <div class="mt-2">
                                        <strong class="small">Chi tiết lỗi:</strong>
                                        <ul class="small text-danger mt-1 mb-0">
                                            <c:forEach var="err" items="${importResult.errors}">
                                                <li>${err}</li>
                                                </c:forEach>
                                        </ul>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="card result-failed mb-4 shadow-sm">
                            <div class="card-body">
                                <h6 class="text-danger fw-bold">
                                    <i class="fa fa-times-circle"></i> Import thất bại
                                </h6>
                                <p class="mb-2">${importResult.note}</p>
                                <c:if test="${not empty importResult.errors}">
                                    <ul class="small text-danger mt-1 mb-0">
                                        <c:forEach var="err" items="${importResult.errors}">
                                            <li>${err}</li>
                                            </c:forEach>
                                    </ul>
                                </c:if>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:if>

            <div class="row g-4">
                <%-- Form upload --%>
                <div class="col-md-7">
                    <div class="card shadow-sm">
                        <div class="card-body p-4">
                            <h6 class="fw-bold mb-3">Tải lên file Excel</h6>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/v1/manager/recruitment-import"
                                  enctype="multipart/form-data">

                                <div class="upload-zone mb-3" onclick="document.getElementById('fileInput').click()">
                                    <i class="fa-solid fa-file-excel mb-3 d-block"></i>
                                    <p class="mb-1 fw-semibold">Kéo thả hoặc click để chọn file</p>
                                    <p class="text-muted small mb-0">Chỉ chấp nhận file <strong>.xlsx</strong></p>
                                    <p id="fileName" class="text-primary small mt-2 mb-0"></p>
                                </div>
                                <input type="file" id="fileInput" name="file"
                                       accept=".xlsx" class="d-none"
                                       onchange="showFileName(this)"/>

                                <button type="submit" class="btn btn-primary w-100">
                                    <i class="fa fa-upload"></i> Import
                                </button>
                            </form>
                        </div>
                    </div>
                </div>

                <%-- Hướng dẫn --%>
                <div class="col-md-5">
                    <div class="card shadow-sm h-100">
                        <div class="card-body p-4">
                            <h6 class="fw-bold mb-3">Hướng dẫn</h6>
                            <ol class="small text-muted ps-3">
                                <li class="mb-2">Tải file Excel mẫu về máy</li>
                                <li class="mb-2">Điền thông tin ứng viên theo đúng định dạng</li>
                                <li class="mb-2">Cột <strong>departmentCode</strong> phải khớp mã phòng ban trong hệ thống (VD: HR, IT, FI)</li>
                                <li class="mb-2">Cột <strong>positionName</strong> phải khớp tên chức vụ trong hệ thống</li>
                                <li class="mb-2">Cột <strong>dateOfBirth</strong> định dạng <code>yyyy-MM-dd</code></li>
                                <li class="mb-2">Cột <strong>cvUrl</strong> là link Google Drive file CV của ứng viên</li>
                                <li>Cột bắt buộc: fullName, email, departmentCode, positionName</li>
                            </ol>
                            <hr/>
                            <a href="${pageContext.request.contextPath}/downloads/candidate_import_template.xlsx"
                               class="btn btn-outline-success w-100">
                                <i class="fa-solid fa-download"></i> Tải file mẫu
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <script>
            function showFileName(input) {
                const label = document.getElementById('fileName');
                label.textContent = input.files.length > 0
                        ? '📎 ' + input.files[0].name
                        : '';
            }

            // Drag & drop
            const zone = document.querySelector('.upload-zone');
            zone.addEventListener('dragover', e => {
                e.preventDefault();
                zone.style.background = '#d6e4ff';
            });
            zone.addEventListener('dragleave', () => {
                zone.style.background = '#f0f4ff';
            });
            zone.addEventListener('drop', e => {
                e.preventDefault();
                zone.style.background = '#f0f4ff';
                const file = e.dataTransfer.files[0];
                if (file && file.name.endsWith('.xlsx')) {
                    document.getElementById('fileInput').files = e.dataTransfer.files;
                    document.getElementById('fileName').textContent = '📎 ' + file.name;
                } else {
                    alert('Vui lòng chọn file .xlsx');
                }
            });
        </script>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>