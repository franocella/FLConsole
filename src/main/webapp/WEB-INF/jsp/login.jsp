<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Login</title>
    <!-- External stylesheets for icons and fonts -->
    
    <!-- Bootstrap stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN"  crossorigin="anonymous"/>
    <!-- Custom stylesheet -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/main.css"/>

    <!-- External scripts for jQuery, Bootstrap, and custom JavaScript files -->
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
    <script src="${pageContext.request.contextPath}/JS/modals.js"></script>
    <script src="${pageContext.request.contextPath}/JS/main.js"></script>
    <script src="${pageContext.request.contextPath}/JS/authentication.js"></script>
</head>

<body style="background-color: #f8f8fe;">

<!-- Header section with navbar -->
<%@ include file="components/header.txt" %>

<!-- Overlay -->
<div id="overlay-ov" class="overlay-ov"></div>

<!-- Error modal -->
<div id="error-modal" class="myAlert-sm" style="z-index: 9999"></div>

<!-- Container for the login form -->
<div class="container" style="margin-top: 50px;">
    <div class="row justify-content-center align-items-center g-5">
        <div class="col-md-6">
            <img src="${pageContext.request.contextPath}/Images/FedLearningPic.png" class="img-fluid" alt="">
        </div>
        <div class="col-md-6">
            <div class="card">
                <div class="card-body">
                    <h2 class="card-title text-center pb-3">Login</h2>
                    <!-- Form for user login -->
                    <div id="LoginForm">
                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" required/>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Password</label>
                            <input type="password" class="form-control" id="password" required title="Required length: 8 characters and at least one number, one uppercase letter, and one special character"/>
                        </div>
                        <a href="/FLConsole/signup" class="mb-2 d-block text-start">Not registered? Sign Up</a>
                        <div class="text-end">
                            <a class="btn btn-primary" onclick="submitLogin()">Login</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
