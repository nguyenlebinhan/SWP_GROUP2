<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Xem trước hợp đồng lao động - HRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" rel="stylesheet"/>
    <style>
        body { background: #eef1f5; font-family: Arial, sans-serif; color: #111; }
        .main { margin-left: 250px; padding: 25px; }
        .toolbar { max-width: 794px; margin: 0 auto 16px; display: flex; justify-content: space-between; gap: 12px; }
        .paper { width: 794px; min-height: 1123px; margin: 0 auto 24px; background: #fff; padding: 88px 90px; box-shadow: 0 6px 24px rgba(15,23,42,0.14); }
        .paper h1 { font-size: 28px; font-weight: 700; margin-bottom: 34px; text-align: center; }
        .paper h2 { font-size: 20px; font-weight: 500; margin: 30px 0 14px; }
        .paper p, .paper li { font-size: 13px; line-height: 1.75; margin-bottom: 14px; }
        .field { color: #d00; font-weight: 600; }
        .checkline { margin-left: 34px; }
        .signature-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 56px; margin-top: 36px; }
        .signature-box { height: 78px; border-bottom: 1px solid #111; margin-bottom: 10px; }
        .muted-line { color: #555; }
        .page-break { page-break-before: always; }

        @media print {
            @page { size: A4; margin: 18mm; }
            body { background: #fff; }
            .no-print, .employee-sidebar, .employee-topbar { display: none !important; }
            .main { margin: 0; padding: 0; }
            .paper { width: auto; min-height: auto; margin: 0; padding: 0; box-shadow: none; }
            .paper h1 { margin-top: 0; }
            .page-break { page-break-before: always; }
        }
    </style>
</head>
<body>
<div class="no-print">
    <jsp:include page="/public/components/managerSideBar.jsp" />
</div>

<div class="main">
    <div class="no-print">
        <jsp:include page="/public/components/managerTopBar.jsp">
            <jsp:param name="title" value="Xem trước hợp đồng" />
            <jsp:param name="backUrl" value="/v1/manager/employee/detail?id=${employee.employeeId}" />
        </jsp:include>
    </div>

    <div class="toolbar no-print">
        <a href="${pageContext.request.contextPath}/v1/manager/employee/detail?id=${employee.employeeId}" class="btn btn-outline-secondary btn-sm">
            <i class="fa-solid fa-arrow-left me-1"></i>Quay lại
        </a>
        <div class="d-flex gap-2">
            <button type="button" class="btn btn-outline-primary btn-sm" onclick="window.print()">
                <i class="fa-solid fa-print me-1"></i>In
            </button>
            <button type="button" class="btn btn-primary btn-sm" onclick="window.print()">
                <i class="fa-solid fa-file-pdf me-1"></i>Xuất PDF
            </button>
        </div>
    </div>

    <article class="paper">
        <h1>Hợp Đồng Lao Động</h1>

        <p>
            Hợp đồng lao động này ("Hợp đồng") được lập ngày
            <span class="field"><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></span>,
            giữa <span class="field">Công ty HRM</span>, có địa chỉ tại
            <span class="field">Địa chỉ công ty</span> ("Người sử dụng lao động"), và
            <span class="field"><c:out value="${employee.fullName}"/></span>,
            mã nhân viên <span class="field"><c:out value="${employee.employeeCode}"/></span>
            ("Người lao động").
        </p>

        <h2>1. Vị trí và nhiệm vụ.</h2>
        <p>
            Người lao động được tuyển dụng ở vị trí
            <span class="field"><c:out value="${empty employee.positionName ? 'Chức danh' : employee.positionName}"/></span>
            thuộc <span class="field"><c:out value="${empty employee.departmentName ? 'Phòng ban' : employee.departmentName}"/></span>.
            Người lao động thực hiện các nhiệm vụ phù hợp với vị trí này và các công việc khác do Người sử dụng lao động phân công hợp lý.
        </p>

        <h2>2. Thời hạn hợp đồng.</h2>
        <p>
            Hợp đồng bắt đầu có hiệu lực từ ngày
            <span class="field"><fmt:formatDate value="${contract.effectiveDate}" pattern="dd/MM/yyyy"/></span>.
            Loại hợp đồng:
            <span class="field"><c:out value="${contract.contractType}"/></span>.
        </p>
        <p class="checkline">
            [<c:out value="${empty contract.endDate ? 'x' : ' '}"/>] Không xác định thời hạn, có thể chấm dứt theo quy định pháp luật và quy chế công ty.
        </p>
        <p class="checkline">
            [<c:out value="${empty contract.endDate ? ' ' : 'x'}"/>] Có thời hạn đến ngày
            <span class="field"><c:out value="${empty contract.endDate ? 'Ngày kết thúc' : contract.endDate}"/></span>,
            trừ khi được chấm dứt sớm theo điều khoản của Hợp đồng.
        </p>

        <h2>3. Thời gian thử việc <em>(nếu có)</em>.</h2>
        <p>
            Thời gian thử việc là <span class="field">60</span> ngày kể từ ngày bắt đầu làm việc, trừ khi hai bên có thỏa thuận khác.
            Trong thời gian này, việc chấm dứt thử việc được thực hiện theo quy định pháp luật và chính sách công ty.
        </p>

        <h2>4. Tiền lương.</h2>
        <p>
            Người lao động được trả mức lương
            <span class="field"><fmt:formatNumber value="${contract.salary}" type="number" groupingUsed="true"/></span>
            đồng mỗi tháng, thanh toán theo kỳ lương tiêu chuẩn của Người sử dụng lao động.
            Các khoản thuế, bảo hiểm và khấu trừ bắt buộc sẽ được thực hiện theo quy định hiện hành.
        </p>

        <h2>5. Phúc lợi.</h2>
        <p>
            Người lao động được tham gia các chế độ phúc lợi tiêu chuẩn của công ty, bao gồm bảo hiểm, nghỉ phép có lương,
            nghỉ ốm và các quyền lợi khác theo chính sách của Người sử dụng lao động.
        </p>

        <div class="page-break"></div>

        <h2>6. Thời giờ làm việc.</h2>
        <p>
            Người lao động làm việc <span class="field">40</span> giờ mỗi tuần hoặc theo lịch làm việc do Người sử dụng lao động sắp xếp.
            Việc thay đổi thời giờ làm việc được thực hiện theo nhu cầu công việc và quy định pháp luật.
        </p>

        <h2>7. Bảo mật thông tin.</h2>
        <p>
            Người lao động cam kết bảo mật mọi thông tin không công khai mà mình biết được trong quá trình làm việc
            và không tiết lộ thông tin đó cho bên thứ ba, trừ trường hợp pháp luật yêu cầu hoặc được công ty cho phép.
        </p>

        <h2>8. Sở hữu trí tuệ.</h2>
        <p>
            Mọi sáng chế, sản phẩm, tài liệu, mã nguồn, thiết kế hoặc tài sản trí tuệ khác do Người lao động tạo ra trong phạm vi công việc
            thuộc quyền sở hữu của Người sử dụng lao động, trong phạm vi pháp luật cho phép.
        </p>

        <h2>9. Chấm dứt hợp đồng.</h2>
        <p>
            Mỗi bên có quyền chấm dứt Hợp đồng theo quy định pháp luật, nội quy lao động và các thỏa thuận trong Hợp đồng.
            Khi chấm dứt, Người lao động được thanh toán tiền lương và các quyền lợi còn lại theo quy định.
        </p>

        <h2>10. Không cạnh tranh và không lôi kéo <em>(nếu áp dụng)</em>.</h2>
        <p>
            Trong thời gian làm việc và một khoảng thời gian hợp lý sau khi nghỉ việc, Người lao động không được cạnh tranh không lành mạnh
            hoặc lôi kéo khách hàng, đối tác, nhân sự của Người sử dụng lao động, trong phạm vi pháp luật cho phép.
        </p>

        <h2>11. Nội quy và quy trình.</h2>
        <p>Người lao động đồng ý tuân thủ nội quy, quy trình và chính sách của công ty, bao gồm các sửa đổi được ban hành theo từng thời điểm.</p>

        <h2>12. Toàn bộ thỏa thuận.</h2>
        <p>Hợp đồng này là toàn bộ thỏa thuận giữa hai bên liên quan đến quan hệ lao động và thay thế các thỏa thuận trước đó về cùng nội dung.</p>

        <h2>13. Sửa đổi hợp đồng.</h2>
        <p>Mọi sửa đổi hoặc bổ sung Hợp đồng phải được lập bằng văn bản và có xác nhận của cả hai bên.</p>

        <h2>14. Luật áp dụng.</h2>
        <p>Hợp đồng này được điều chỉnh bởi pháp luật Việt Nam và các quy định có liên quan.</p>

        <h2>15. Hiệu lực từng phần.</h2>
        <p>Nếu bất kỳ điều khoản nào của Hợp đồng bị tuyên vô hiệu hoặc không thể thực thi, các điều khoản còn lại vẫn tiếp tục có hiệu lực.</p>

        <h2>16. Chữ ký.</h2>
        <p>Hai bên xác nhận đã đọc, hiểu và đồng ý ký kết Hợp đồng này vào ngày ghi ở phần đầu Hợp đồng.</p>

        <div class="signature-grid">
            <div>
                <p><strong>Người sử dụng lao động:</strong></p>
                <div class="signature-box"></div>
                <p>Chữ ký</p>
                <p>Họ tên/Chức danh: <span class="field">Đại diện Công ty HRM</span></p>
                <p>Ngày ký: <span class="muted-line">________________</span></p>
            </div>
            <div>
                <p><strong>Người lao động:</strong></p>
                <div class="signature-box"></div>
                <p>Chữ ký</p>
                <p>Họ tên: <span class="field"><c:out value="${employee.fullName}"/></span></p>
                <p>Ngày ký: <span class="muted-line">________________</span></p>
            </div>
        </div>

        <c:if test="${not empty contract.note}">
            <h2>Ghi chú.</h2>
            <p><c:out value="${contract.note}"/></p>
        </c:if>
    </article>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
