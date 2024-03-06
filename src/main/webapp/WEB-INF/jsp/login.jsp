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
    <link rel="stylesheet" href="CSS/main.css"/>

</head>

<body style="background-color: #f8f8fe;">

<!-- Header section with navbar -->
<%@ include file="components/header.txt" %>

<!-- Container for the login form -->
<div class="container" style="margin-top: 50px;">
    <div class="row justify-content-center align-items-center g-5">
        <div class="col-md-6">
            <img src="Images/FedLearningPic.png" class="img-fluid" alt="">
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
                        <a href="/signup" class="mb-2 d-block text-start">Not registered? Sign Up</a>
                        <div class="text-end">
                            <!-- Button to submit the form with a js function which checks all the fields-->
                            <button class="btn btn-primary" onclick="submitLogin()">Login</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- External scripts for jQuery, Bootstrap, and custom JavaScript files -->
<script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>

<script>
    function submitLogin(){
        // Get the values of the email and password fields
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        // Check if are empty and if the format of the email is correct
        if(email === "" || password === ""){
            alert("Please fill in all the fields");
        } else {
            // Send the data to the server
            $.ajax({
                type: "POST",
                url: "/login",
                data: {
                    email: email,
                    password: password
                },
                success: function(response){
                    window.location.href = "/";
                },
                error: function(){
                    openErrorModal("Error", "Invalid email or password");
                }
            });
        }
    }

    function openErrorModal(title, message) {
        // Check if overlay already exists
        let overlay = document.getElementById('overlay');

        if (!overlay) {
            // If overlay does not exist, create HTML element
            overlay = document.createElement('div');
            overlay.id = 'overlay';
            overlay.className = 'overlay';

            // Add overlay to the page
            document.body.appendChild(overlay);
        }

        // Check if modal already exists
        let modal = document.getElementById('error-modal');

        if (!modal) {
            // If modal does not exist, create HTML elements
            modal = document.createElement('div');
            modal.id = 'error-modal';
            modal.className = 'myAlert-sm';

            const modalBody = document.createElement('div');
            modalBody.className = 'myAlertBody';

            const titleElement = document.createElement('h3');
            titleElement.id = 'Err-Title';

            const messageElement = document.createElement('p');
            messageElement.className = 'mt-3';
            messageElement.id = 'Err-Message';

            const closeButton = document.createElement('button');
            closeButton.className = 'btn btn-primary';
            closeButton.innerText = 'Close';
            closeButton.onclick = closeErrorModal;

            // Add elements to the modal
            modalBody.appendChild(titleElement);
            modalBody.appendChild(messageElement);
            modalBody.appendChild(closeButton);
            modal.appendChild(modalBody);

            // Add modal to the page
            document.body.appendChild(modal);
        }

        // Set titles and messages dynamically
        document.getElementById('Err-Title').innerText = title;
        document.getElementById('Err-Message').innerText = message;

        // Show overlay and modal
        overlay.style.display = 'block';
        modal.style.display = 'block';
    }

    function closeErrorModal() {
        // Remove modal, hide overlay
        const modal = document.getElementById('error-modal');
        document.body.removeChild(modal);

        const overlay = document.getElementById('overlay');
        overlay.style.display = 'none';
    }

</script>

</body>
</html>
