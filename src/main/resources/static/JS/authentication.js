function submitSignup() {
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();
    const confirmPassword = document.getElementById("confirmPassword").value.trim();

    if (email === "" || password === "" || confirmPassword === "") {
        openModal("Error", "error", "Please fill in all the fields")
    } else if (password !== confirmPassword) {
        openModal("Error", "error", "Passwords do not match. Please enter the same password in both fields.")
    } else {
        // Send the data to the server for signup
        $.ajax({
            type: "POST",
            url: "/FLConsole/signup",
            data: {
                email: email,
                password: password,
            },
            success: function(response) {
                window.location.href = "/FLConsole/";
            },
            error: function(error) {
                let message;

                if (error.responseJSON && error.responseJSON.error) {
                    message = error.responseJSON.error;
                } else {
                    try {
                        const parsedError = JSON.parse(error.responseText);
                        message = parsedError.error || "An error occurred during signup";
                    } catch (e) {
                        message = "An error occurred during signup";
                    }
                }
                openModal("Error", "error", message)
            }
        });
    }
}

function submitLogin(){
    // Get the values of the email and password fields
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    // Check if are empty and if the format of the email is correct
    if(email === "" || password === ""){
        openModal("Error", "error", "Please fill in all the fields")
    } else {
        // Send the data to the server
        $.ajax({
            type: "POST",
            url: "/FLConsole/login",
            data: {
                email: email,
                password: password
            },
            success: function (response) {
                const roleCookie = getCookie("role");

                if (roleCookie) {
                    window.location.href = "/FLConsole/admin/dashboard";
                } else {
                    window.location.href = "/FLConsole/";
                }
            },
            error: function(){
                openModal("Error", "error", "Invalid email or password")
            }
        });
    }
}

function getCookie(name) {
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
        const cookie = cookies[i].trim();
        if (cookie.startsWith(name + '=')) {
            return cookie.substring(name.length + 1);
        }
    }
    return null;
}