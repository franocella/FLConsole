<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page session="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>User Profile</title>
    <!-- External stylesheets for icons and fonts -->

    <!-- Bootstrap stylesheet -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous" />
    <!-- Custom stylesheet -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/CSS/main.css" />

</head>

<body style="background-color: #f8f8fe;">

<!-- Header section with navbar -->
<%@ include file="components/header.txt" %>

<!-- Main container -->
<div class="container my-5">
    <div class="row">
        <div class="col-md-8">
            <!-- User details -->
            <h2>User Profile</h2>
            <p><strong>Username:</strong> <span class="editable-field" id="username">JohnDoe</span></p>
            <p><strong>Email:</strong> <span class="editable-field" id="email">johndoe@example.com</span></p>
            <!-- Additional details section -->
            <h3>Additional Details</h3>
            <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla vitae ligula in nulla mattis
                fermentum. Duis rutrum, odio vitae viverra mattis, magna felis bibendum libero, non tempus sapien
                nisi id velit.</p>
        </div>
    </div>
    <!-- Submit button -->
    <div class="d-flex justify-content-end gap-3">
        <button id="submitBtn" class="btn btn-primary">Update Profile</button>
        <button id="deleteBtn" class="btn btn-danger">Delete Profile</button>
    </div>
</div>



<!-- External scripts for jQuery, Bootstrap, and custom JavaScript files -->
<script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL"
        crossorigin="anonymous"></script>

<script>
    $(document).ready(function() {
        // Function to enable field editing on click
        $('.editable-field').click(function() {
            $(this).prop('contenteditable', true).focus();
        });

        // Function to handle submit button click
        $('#submitBtn').click(function() {
            var username = $('#username').text(); // Get modified username
            var email = $('#email').text(); // Get modified email
            $.ajax({
                type: 'POST',
                url: '/admin/profile/update',
                data: { username: username, email: email }, // Send modified values as request data
                success: function(response) {
                    alert('Profile update successful!');
                    // Perform other actions upon success if needed
                },
                error: function(xhr, status, error) {
                    openErrorModal('Error', 'An error occurred while updating the profile.');
                    // Handle error as needed
                }
            });
        });
    });

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
