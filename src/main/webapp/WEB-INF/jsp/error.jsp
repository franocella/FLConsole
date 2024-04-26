<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Error</title>
    <!-- External stylesheets for icons and fonts -->

    <!-- Bootstrap stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
    <!-- Custom stylesheet -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/main.css" />

    <!-- External scripts for jQuery, Bootstrap, and custom JavaScript files -->
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>

    <style>
        body {
            background-color: #f8f8fe;
        }

        .container {
            text-align: center;
        }

        .access-denied-box {
            margin-top: 50px;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            background-color: #ffffff;
            width: auto;
        }
    </style>
</head>

<body>



<!-- Container -->
<div class="container">
    <div class="access-denied-box">
        <img src="${pageContext.request.contextPath}/Images/error-page.png" alt="Page Not Found" style="width: 500px; height: auto; margin-top: 20px;" />

    </div>
    <button type="button" class="btn btn-lg btn-danger mt-5" onclick="goHomePage()">Go to User Dashboard</button>
</div>
</body>


<script>
    let isAdmin = false;
    <c:if test="${isAdmin}">
    isAdmin = true;
    </c:if>

    function goHomePage() {
        if (isAdmin) {
            window.location.href = "/FLConsole/admin/dashboard";
        } else {
            window.location.href = "/FLConsole/";
        }
    }
</script>
</html>