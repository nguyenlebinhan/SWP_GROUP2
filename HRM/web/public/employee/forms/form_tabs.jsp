<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<ul class="nav nav-pills form-type-tabs mb-4">
    <li class="nav-item">
        <a class="nav-link ${param.active == 'leave' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/v1/employee/forms/leave/new">
            Đơn Nghỉ Phép
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link ${param.active == 'complaint' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/v1/employee/forms/complaint/new">
            Đơn Khiếu Nại
        </a>
    </li>
</ul>
