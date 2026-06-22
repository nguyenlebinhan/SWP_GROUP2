<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách Hợp đồng Chờ Duyệt - HRM</title>
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
        .table th { font-size: 13px; color: #6b7280; font-weight: 600; background: #f9fafb; }
        .table td { font-size: 14px; vertical-align: middle; }
        .empty-icon { font-size: 48px; color: #cbd5e1; margin-bottom: 16px; }

        /* --- Search Bar --- */
        .search-bar {
            display: flex;
            gap: 10px;
            align-items: center;
            flex-wrap: wrap;
            background: white;
            padding: 16px 20px;
            border: 1px solid #eef2f7;
            border-radius: 10px;
            margin-bottom: 20px;
        }
        .search-bar input[type="text"] {
            flex: 1;
            min-width: 200px;
            padding: 8px 14px;
            border: 1px solid #e5e7eb;
            border-radius: 7px;
            font-size: 14px;
            outline: none;
        }
        .search-bar select {
            min-width: 160px;
            padding: 8px 14px;
            border: 1px solid #e5e7eb;
            border-radius: 7px;
            font-size: 14px;
            outline: none;
            background: white;
        }
        .search-bar input[type="text"]:focus { border-color: #2563eb; }
        .search-bar select:focus { border-color: #2563eb; }
        .btn-search {
            background: #2563eb;
            color: white;
            border: none;
            padding: 8px 18px;
            border-radius: 7px;
            font-size: 14px;
            font-weight: 600;
        }
        .btn-clear {
            background: white;
            color: #6b7280;
            border: 1px solid #d1d5db;
            padding: 8px 14px;
            border-radius: 7px;
            font-size: 13px;
            text-decoration: none;
            font-weight: 500;
        }
        .btn-clear:hover { background: #f3f4f6; color: #374151; }

        /* --- Contract Type Badges (Semantic Colors) --- */
        .badge-probation {
            background: #dbeafe; color: #1e40af;
            padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
        }
        .badge-internship {
            background: #ede9fe; color: #5b21b6;
            padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
        }
        .badge-fixed {
            background: #d1fae5; color: #065f46;
            padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
        }
        .badge-indefinite {
            background: #fef3c7; color: #92400e;
            padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
        }
        /* Fallback for any other type */
        .badge-other {
            background: #f3f4f6; color: #374151;
            padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
        }

        /* --- Status badge --- */
        .badge-pending {
            background: #fef3c7; color: #92400e;
            padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
        }

        /* --- Empty Filter Result --- */
        #emptyFilterMsg {
            display: none;
            text-align: center;
            padding: 32px 20px;
            color: #9ca3af;
        }
        #emptyFilterMsg i {
            font-size: 36px;
            margin-bottom: 12px;
            opacity: 0.5;
        }
        #emptyFilterMsg h6 {
            font-weight: 600;
            color: #6b7280;
        }
    </style>
</head>
<body>

<jsp:include page="/public/components/managerSideBar.jsp" />

<div class="main">
    <jsp:include page="/public/components/managerTopBar.jsp">
        <jsp:param name="title" value="Hợp đồng chờ duyệt" />
        <jsp:param name="backUrl" value="/v1/manager/dashboard" />
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

    <c:choose>
        <c:when test="${empty pendingContracts}">
            <div class="section-card text-center py-5">
                <div class="empty-icon"><i class="fa-regular fa-file-lines"></i></div>
                <h6 class="text-muted">Không có hợp đồng nào chờ duyệt.</h6>
            </div>
        </c:when>
        <c:otherwise>
            <div class="section-card">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h5 class="fw-bold mb-1">Hợp đồng chờ duyệt</h5>
                        <span class="text-muted small">${pendingContracts.size()} hợp đồng</span>
                    </div>
                </div>

                <!-- Search / Filter Bar -->
                <div class="search-bar" id="filterBar">
                    <input type="text" id="filterKeyword" placeholder="Tìm theo mã hợp đồng, tên hoặc mã nhân viên..." oninput="filterTable()" />
                    <select id="filterContractType" onchange="filterTable()">
                        <option value="">Tất cả loại</option>
                        <option value="PROBATION">Thử việc</option>
                        <option value="INTERNSHIP">Thực tập</option>
                        <option value="FIXED_TERM">Có thời hạn</option>
                        <option value="INDEFINITE">Vô thời hạn</option>
                    </select>
                    <button type="button" class="btn-search" onclick="filterTable()"><i class="fa-solid fa-magnifying-glass me-1"></i>Tìm kiếm</button>
                    <a href="javascript:void(0)" class="btn-clear" onclick="clearFilters()">Xóa lọc</a>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover table-striped align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>Mã HD</th>
                                <th>Nhân viên</th>
                                <th>Loại hợp đồng</th>
                                <th>Ngày hiệu lực</th>
                                <th>Lương</th>
                                <th>Trạng thái</th>
                                <th class="text-end pe-4">Hành động</th>
                            </tr>
                        </thead>
                        <tbody id="contractTableBody">
                            <c:forEach items="${pendingContracts}" var="contract">
                                <tr data-code="${fn:toLowerCase(contract.contractCode)}"
                                    data-name="${fn:toLowerCase(contract.employeeFullName)}"
                                    data-employee-code="${fn:toLowerCase(contract.employeeCode)}"
                                    data-type="${contract.contractType}">
                                    <td class="fw-medium">${contract.contractCode}</td>
                                    <td>
                                        <div class="fw-medium">${contract.employeeFullName}</div>
                                        <small class="text-muted">${contract.employeeCode}</small>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${contract.contractType == 'PROBATION'}">
                                                <span class="badge-probation">Thử việc</span>
                                            </c:when>
                                            <c:when test="${contract.contractType == 'INTERNSHIP'}">
                                                <span class="badge-internship">Thực tập</span>
                                            </c:when>
                                            <c:when test="${contract.contractType == 'FIXED_TERM'}">
                                                <span class="badge-fixed">Có thời hạn</span>
                                            </c:when>
                                            <c:when test="${contract.contractType == 'INDEFINITE'}">
                                                <span class="badge-indefinite">Vô thời hạn</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-other">${contract.contractType}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></td>
                                    <td class="fw-medium text-success">
                                        <fmt:formatNumber value="${contract.salary}" pattern="#,##0" /> VNĐ
                                    </td>
                                    <td><span class="badge-pending">Chờ duyệt</span></td>
                                    <td class="text-end pe-4">
                                        <a href="${pageContext.request.contextPath}/v1/manager/contract/approve-preview?contractId=${contract.contractId}"
                                           class="btn btn-sm btn-outline-primary me-1" title="Xem chi tiết hợp đồng">
                                            <i class="fa-solid fa-eye"></i> Xem
                                        </a>
                                        <form action="${pageContext.request.contextPath}/v1/manager/contract/approve" method="post" style="display:inline;">
                                            <input type="hidden" name="contractId" value="${contract.contractId}">
                                            <button type="submit" class="btn btn-sm btn-outline-success me-1" title="Duyệt hợp đồng"
                                                    onclick="return confirm('Bạn có chắc muốn duyệt hợp đồng này?')">
                                                <i class="fa-solid fa-check"></i> Duyệt
                                            </button>
                                        </form>
                                        <button type="button" class="btn btn-sm btn-outline-danger" 
                                                title="Từ chối hợp đồng" 
                                                onclick="openRejectModal('${contract.contractId}')">
                                            <i class="fa-solid fa-xmark"></i> Từ chối
                                        </button>
                                    </td>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <!-- Empty Filter Result Message -->
                    <div id="emptyFilterMsg">
                        <div><i class="fa-solid fa-filter-circle-xmark"></i></div>
                        <h6>Không tìm thấy hợp đồng phù hợp</h6>
                        <p class="small text-muted">Hãy thử thay đổi từ khóa tìm kiếm hoặc xóa bộ lọc.</p>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- Reject Modal -->
<div class="modal fade" id="rejectModal" tabindex="-1" aria-labelledby="rejectModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="rejectModalLabel"><i class="fa-solid fa-xmark-circle text-danger me-2"></i>Từ chối Hợp đồng</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="${pageContext.request.contextPath}/v1/manager/contract/reject" method="post">
                <input type="hidden" name="contractId" id="modalContractId">
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="rejectionReason" class="form-label">Lý do từ chối:</label>
                        <textarea class="form-control" id="rejectionReason" name="reason" rows="3" required placeholder="Vui lòng nhập lý do từ chối..."></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger px-4">
                        <i class="fa-solid fa-xmark me-1"></i> Xác nhận Từ chối
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function openRejectModal(contractId) {
        document.getElementById('modalContractId').value = contractId;
        var myModal = new bootstrap.Modal(document.getElementById('rejectModal'));
        myModal.show();
    }

    function filterTable() {
        var kw = document.getElementById('filterKeyword').value.toLowerCase().trim();
        var type = document.getElementById('filterContractType').value;
        var rows = document.querySelectorAll('#contractTableBody tr[data-code]');
        var visibleCount = 0;

        rows.forEach(function(row) {
            var code = row.dataset.code || '';
            var name = row.dataset.name || '';
            var empCode = row.dataset.employeeCode || '';
            var rowType = row.dataset.type || '';

            var matchKeyword = !kw || code.includes(kw) || name.includes(kw) || empCode.includes(kw);
            var matchType = !type || rowType === type;
            var match = matchKeyword && matchType;

            row.style.display = match ? '' : 'none';
            if (match) visibleCount++;
        });

        // Toggle empty filter result message
        var emptyMsg = document.getElementById('emptyFilterMsg');
        if (emptyMsg) {
            emptyMsg.style.display = visibleCount === 0 ? 'block' : 'none';
        }
    }

    function clearFilters() {
        document.getElementById('filterKeyword').value = '';
        document.getElementById('filterContractType').value = '';
        filterTable();
    }
</script>
</body>
</html>

