<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page session="false" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>User Profile</title>
    <!-- Bootstrap stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
    <!-- Bootstrap Icons stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Custom stylesheet -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/main.css" />

    <!-- External scripts for jQuery, Bootstrap, and custom JavaScript files -->
    <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
            crossorigin="anonymous"></script>
    <!-- Custom JavaScript files -->
    <script src="${pageContext.request.contextPath}/JS/main.js"></script>
    <script src="${pageContext.request.contextPath}/JS/profilePage.js"></script>
    <script src="${pageContext.request.contextPath}/JS/modals.js"></script>
</head>
<body style="background-color: #f8f8fe;">
    <!-- Header section with navbar -->
    <%@ include file="components/header.txt" %>


    <!-- Overlay -->
    <div id="overlay-ov" class="overlay-ov"></div>

    <!-- Error modal -->
    <div id="error-modal" class="myAlert-sm" style="z-index: 9999"></div>

    <!-- Main container -->
    <div class="container my-5">
        <div class="row">
            <div class="col-md mt-2">
                <!-- User details -->
                <h2 class="mb-3">User Profile</h2>
                <div class="mb-3">
                    <label for="email" class="form-label"><strong>Email:</strong></label>
                    <input class="editable-field form-control" id="email" placeholder="${user.email}">
                </div>
                <div class="mb-3">
                    <label for="password" class="form-label"><strong>Password:</strong></label>
                    <!-- Input field for password -->
                    <div class="input-group">
                        <input type="password" id="password" class="editable-field form-control"
                            placeholder="Your password" value="">
                        <!-- Icon for showing/hiding password -->
                        <button class="btn btn-outline-secondary bi-eye" type="button" id="togglePassword"></button>
                    </div>
                </div>
                <!-- Additional details section -->
                <h3>Additional Details</h3>
                <div class="mb-3">
                    <label for="description" class="form-label"><strong>Description:</strong></label>
                    <textarea class="editable-field form-control" id="description"
                        rows="3">${user.description}</textarea>
                </div>
                <!-- Submit button -->
                <div class="d-flex justify-content-end gap-3">
                    <button id="submitBtn" class="btn btn-primary">Update Profile</button>
                    <button id="deleteBtn" onclick="deleteUser()" class="btn btn-danger">Delete Profile</button>
                </div>
            </div>
        </div>
    </div>


</body>
</html>