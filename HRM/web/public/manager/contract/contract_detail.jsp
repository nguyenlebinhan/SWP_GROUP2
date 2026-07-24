<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8"/>
        <title>Chi tiết hợp đồng - HRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
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
                max-width: 980px;
                margin: 0 auto;
                background: #fff;
                border-radius: 16px;
                box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
                padding: 28px;
            }
            .detail-grid {
                display: grid;
                grid-template-columns: repeat(2, minmax(0, 1fr));
                gap: 16px;
            }
            .detail-item {
                background: #f8fafc;
                border: 1px solid #e2e8f0;
                border-radius: 12px;
                padding: 14px 16px;
            }
            .detail-label {
                display: block;
                font-size: 12px;
                font-weight: 700;
                color: #64748b;
                text-transform: uppercase;
                margin-bottom: 6px;
            }
            .detail-value {
                font-size: 15px;
                color: #0f172a;
            }
            .note-box {
                min-height: 100px;
                white-space: pre-line;
            }
            .toolbar {
                max-width: 980px;
                margin: 0 auto 16px;
                display: flex;
                justify-content: space-between;
                gap: 12px;
                align-items: center;
            }
            .section-title {
                font-size: 18px;
                font-weight: 700;
                margin: 20px 0 12px;
            }
            @media (max-width: 768px) {
                .main {
                    margin-left: 0;
                    padding: 16px;
                }
                .detail-grid {
                    grid-template-columns: 1fr;
                }
                .toolbar {
                    flex-direction: column;
                    align-items: stretch;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="/public/components/managerSideBar.jsp" />

        <div class="main">
            <jsp:include page="/public/components/managerTopBar.jsp">
                <jsp:param name="title" value="Chi tiết hợp đồng" />
                <jsp:param name="backUrl" value="${backUrl}" />
            </jsp:include>

            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success alert-dismissible fade show mx-auto mb-3" style="max-width:980px;" role="alert">
                    ${sessionScope.success}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="success" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-danger alert-dismissible fade show mx-auto mb-3" style="max-width:980px;" role="alert">
                    ${sessionScope.error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
                <c:remove var="error" scope="session"/>
            </c:if>

            <div class="toolbar">
                <div class="d-flex flex-wrap gap-2">
                    <c:if test="${canApproveContract and contract.status == 'PENDING_APPROVAL'}">
                        <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/approve">
                            <input type="hidden" name="contractId" value="${contract.contractId}">
                            <button type="submit" class="btn btn-success">
                                <i class="fa-solid fa-check me-1"></i>Phê duyệt
                            </button>
                        </form>
                    </c:if>
                    <c:if test="${canRejectContract and contract.status == 'PENDING_APPROVAL'}">
                        <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#rejectModal">
                            <i class="fa-solid fa-xmark me-1"></i>Từ chối
                        </button>
                    </c:if>
                    <c:if test="${canAddEmploymentContract and contract.status == 'PENDING_APPROVAL'}">
                        <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/cancel">
                            <input type="hidden" name="contractId" value="${contract.contractId}">
                            <button type="submit" class="btn btn-outline-warning">
                                <i class="fa-solid fa-ban me-1"></i>Hủy
                            </button>
                        </form>
                    </c:if>
                    <c:if test="${canTerminateContract and (contract.status == 'ACTIVE' or contract.status == 'PENDING_ACTIVATION')}">
                        <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/terminate">
                            <input type="hidden" name="contractId" value="${contract.contractId}">
                            <input type="hidden" name="note" value="Terminated by HR Manager">
                            <button type="submit" class="btn btn-outline-dark">
                                <a href="${pageContext.request.contextPath}/v1/manager/contract/terminate?id=${contract.contractId} class="btn btn-outliner-dark"></a><i class="fa-solid fa-stop me-1"></i>Chấm dứt
                            </button>
                        </form>
                    </c:if>
                    <c:if test="${not contract.status.isFinalStatus()}">
                        <button type="button" class="btn btn-outline-primary" onclick="toggleEditForm()">
                            <i class="fa-solid fa-pen me-1"></i>Sửa hợp đồng
                        </button>
                    </c:if>
                </div>
            </div>

            <div class="page-card">
                <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                    <div>
                        <h1 class="h3 mb-1">Hợp đồng ${empty contract.contractCode ? contract.contractId : contract.contractCode}</h1>
                        <div class="text-muted">${employee.fullName} (${employee.employeeCode})</div>
                    </div>
                    <span class="badge" style="font-size:15px;">
                        <c:choose>
                            <c:when test="${contract.status == 'PENDING_APPROVAL'}"><span style="background:#fef3c7;color:#92400e;border:1px solid #fbbf24;padding:4px 14px;border-radius:999px;">Chờ duyệt</span></c:when>
                            <c:when test="${contract.status == 'PENDING_ACTIVATION'}"><span style="background:#ede9fe;color:#5b21b6;border:1px solid #a78bfa;padding:4px 14px;border-radius:999px;">Chờ hiệu lực</span></c:when>
                            <c:when test="${contract.status == 'ACTIVE'}"><span style="background:#d1fae5;color:#065f46;border:1px solid #34d399;padding:4px 14px;border-radius:999px;">Đang hiệu lực</span></c:when>
                            <c:when test="${contract.status == 'EXPIRED'}"><span style="background:#f1f5f9;color:#475569;border:1px solid #94a3b8;padding:4px 14px;border-radius:999px;">Đã hết hạn</span></c:when>
                            <c:when test="${contract.status == 'TERMINATED'}"><span style="background:#ffe4e6;color:#9f1239;border:1px solid #fb7185;padding:4px 14px;border-radius:999px;">Đã chấm dứt</span></c:when>
                            <c:when test="${contract.status == 'CANCELLED'}"><span style="background:#f8fafc;color:#334155;border:1px solid #cbd5e1;padding:4px 14px;border-radius:999px;">Đã hủy</span></c:when>
                            <c:when test="${contract.status == 'REJECTED'}"><span style="background:#fef2f2;color:#991b1b;border:1px solid #fca5a5;padding:4px 14px;border-radius:999px;">Bị từ chối</span></c:when>
                            <c:otherwise><span class="badge text-bg-secondary">${contract.status}</span></c:otherwise>
                        </c:choose>
                    </span>
                </div>

                <div class="section-title">Thông tin hợp đồng</div>
                <div class="detail-grid">
                    <div class="detail-item">
                        <span class="detail-label">Loại hợp đồng</span>
                        <div class="detail-value">${contract.contractType.displayName}</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Lương</span>
                        <div class="detail-value"><fmt:formatNumber value="${contract.salary}" type="number" groupingUsed="true" maxFractionDigits="0"/> VND</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Ngày ký</span>
                        <div class="detail-value">
                            <c:choose>
                                <c:when test="${not empty contract.signedDate}">
                                    <fmt:formatDate value="${contract.signedDate}" pattern="dd/MM/yyyy"/>
                                </c:when>
                                <c:otherwise>Chưa ký</c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Ngày hiệu lực</span>
                        <div class="detail-value"><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Ngày kết thúc</span>
                        <div class="detail-value">
                            <c:choose>
                                <c:when test="${empty contract.endDate}">Không xác định thời hạn</c:when>
                                <c:otherwise><fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy"/></c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Hợp đồng trước đó</span>
                        <div class="detail-value">
                            <c:choose>
                                <c:when test="${not empty contract.previousContractId}">
                                    <a href="${pageContext.request.contextPath}/v1/manager/contract/detail?contractId=${contract.previousContractId}">
                                        #${contract.previousContractId}
                                    </a>
                                </c:when>
                                <c:otherwise>Không có</c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <div class="section-title">Thông tin nhân viên</div>
                <div class="detail-grid">
                    <div class="detail-item">
                        <span class="detail-label">Họ tên</span>
                        <div class="detail-value">${employee.fullName}</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Mã nhân viên</span>
                        <div class="detail-value">${employee.employeeCode}</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Phòng ban</span>
                        <div class="detail-value">${empty contract.departmentName ? 'Chưa cập nhật' : contract.departmentName}</div>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Chức danh</span>
                        <div class="detail-value">${empty contract.positionName ? 'Chưa cập nhật' : contract.positionName}</div>
                    </div>
                </div>

                <div class="section-title">Ghi chú</div>
                <div class="detail-item note-box">
                    <c:out value="${empty contract.note ? 'Không có ghi chú.' : contract.note}"/>
                </div>

                <div class="d-flex gap-2 mt-3">
                    <c:choose>
                        <c:when test="${not empty contract.contractFilePath}">
                            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/v1/manager/contract/download-signed?id=${contract.contractId}">
                                <i class="fa-solid fa-file-pdf me-1"></i> Xem hợp đồng đã ký
                            </a>
                        </c:when>
                    </c:choose>
                </div>
                <div id="editFormSection" style="display:none;">
                    <div class="section-title">Chỉnh sửa hợp đồng</div>
                    <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/detail">
                        <input type="hidden" name="contractId" value="${contract.contractId}" />

                        <div class="detail-grid">
                            <div class="detail-item">
                                <span class="detail-label">Mã hợp đồng</span>
                                <input type="text" class="form-control" name="contractCode" value="${contract.contractCode}" />
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Loại hợp đồng</span>
                                <select class="form-select" name="contractType">
                                    <option value="">-- Chọn --</option>
                                    <option value="INTERNSHIP" ${contract.contractType == 'INTERNSHIP' ? 'selected' : ''}>Thực tập</option>
                                    <option value="PROBATION" ${contract.contractType == 'PROBATION' ? 'selected' : ''}>Thử việc</option>
                                    <option value="FIXED_TERM" ${contract.contractType == 'FIXED_TERM' ? 'selected' : ''}>Có thời hạn</option>
                                    <option value="INDEFINITE" ${contract.contractType == 'INDEFINITE' ? 'selected' : ''}>Không xác định thời hạn</option>
                                </select>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Lương</span>
                                <input type="number" class="form-control" name="salary" value="${contract.salary}" />
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Ngày hiệu lực</span>
                                <input type="date" class="form-control" name="effectiveDate" value="${contract.effectiveDate}" />
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Ngày kết thúc</span>
                                <input type="date" class="form-control" name="endDate" value="${contract.endDate}" />
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Thời hạn</span>
                                <div class="d-flex gap-2">
                                    <input type="number" class="form-control" name="durationValue" value="${contract.durationValue}" placeholder="Số" style="width:50%;" />
                                    <select class="form-select" name="durationUnit" style="width:50%;">
                                        <option value="">--</option>
                                        <option value="MONTH" ${contract.durationUnit == 'MONTH' ? 'selected' : ''}>Tháng</option>
                                        <option value="YEAR" ${contract.durationUnit == 'YEAR' ? 'selected' : ''}>Năm</option>
                                        <option value="DAY" ${contract.durationUnit == 'DAY' ? 'selected' : ''}>Ngày</option>
                                    </select>
                                </div>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Phòng ban</span>
                                <input type="text" class="form-control" name="departmentName" value="${contract.departmentName}" />
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">Chức danh</span>
                                <input type="text" class="form-control" name="positionName" value="${contract.positionName}" />
                            </div>
                        </div>

                        <div class="detail-item mt-3">
                            <span class="detail-label">Ghi chú</span>
                            <textarea class="form-control" name="note" rows="3">${contract.note}</textarea>
                        </div>

                        <div class="detail-item mt-3">
                            <span class="detail-label">Lý do chỉnh sửa <span class="text-danger">*</span></span>
                            <input type="text" class="form-control" name="reason" required placeholder="Nhập lý do chỉnh sửa..." />
                        </div>

                        <div class="d-flex gap-2 mt-3">
                            <button type="submit" class="btn btn-primary"><i class="fa-solid fa-save me-1"></i>Lưu thay đổi</button>
                            <button type="button" class="btn btn-secondary" onclick="toggleEditForm()">Hủy</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="modal fade" id="rejectModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <form method="post" action="${pageContext.request.contextPath}/v1/manager/contract/reject">
                        <div class="modal-header">
                            <h5 class="modal-title">Từ chối hợp đồng</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <input type="hidden" name="contractId" value="${contract.contractId}">
                            <label class="form-label">Lý do từ chối</label>
                            <textarea class="form-control" name="rejectReason" rows="4" required></textarea>
                        </div>
                        <div class="modal-footer">
                             <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Đóng</button>
                            <button type="submit" class="btn btn-danger">Xác nhận từ chối</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        <script>
                                function toggleEditForm() {
                                    const form = document.getElementById("editFormSection");
                                    const btn = document.querySelector('[onclick="toggleEditForm()"]');
                                    if (form.style.display === "none") {
                                        form.style.display = "block";
                                        if (btn)
                                            btn.textContent = "Hủy sửa";
                                    } else {
                                        form.style.display = "none";
                                        if (btn)
                                            btn.innerHTML = '<i class="fa-solid fa-pen me-1"></i>Sửa hợp đồng';
                                    }
                                }
        </script>
    </body>
</html>
