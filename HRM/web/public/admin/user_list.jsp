<%-- 
    Document   : user_list
    Created on : May 19, 2026, 11:36:40 PM
    Author     : PC
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>

    <style>
        .listuser{
            margin-left:250px;
            padding:25px;
        }
        
        .listuser table{
            border: 1px;
        }
        
        .addUserbtn{
            margin-left:250px;
            padding:25px;
        }
        
        
    </style>
    <body>
        <jsp:include page="/public/components/adminSideBar.jsp" />
        
        <div class="addUserbtn">
            <a href="${pageContext.request.contextPath}/v1/admin/add-user">Add user</a>
        </div>
        
        <div class="listuser">
            <table>
                <tr>
                    <th>Mã NV</th>
                    <th>Họ và Tên</th>
                    <th>Email</th>
                    <th>Phòng Ban</th>
                    <th>Chức Vụ</th>
                    <th>Điện Thoại</th>
                    <th>Trạng Thái</th>
                    <th>Thao Tác</th>
                </tr>
                <c:forEach var="i" items="${list}">
                    <tr>
                        <td>${i.userId}</td>
                        <td>${i.fullName}</td>
                        <td>${i.email}</td>
                        <td></td>
                        <td>${i.roleName}</td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </body>
</html>
