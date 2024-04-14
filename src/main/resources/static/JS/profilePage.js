// Flags to track if fields have been modified
let emailModified = false;
let passwordModified = false;
let descriptionModified = false;

$(document).ready(function () {
    // Function to enable field editing on input
    $('.editable-field').on('input', function () {
        if ($(this).attr('id') === 'email' && !emailModified) {
            emailModified = true;
        } else if ($(this).attr('id') === 'password' && !passwordModified) {
            passwordModified = true;
        } else if ($(this).attr('id') === 'description' && !descriptionModified) {
            descriptionModified = true;
        }
    });

    // Function to toggle password visibility
    $('#togglePassword').click(function () {
        const passwordInput = $('#password');
        const icon = $(this);

        // Toggle password visibility
        if (passwordInput.attr('type') === 'password') {
            passwordInput.attr('type', 'text');
            icon.removeClass('bi-eye').addClass('bi-eye-slash');
        } else {
            passwordInput.attr('type', 'password');
            icon.removeClass('bi-eye-slash').addClass('bi-eye');
        }
    });

    function setCloseToRedirect() {
        const closeBtn = document.getElementById('closeBtn');
        closeBtn.onclick = function () {
            window.location.href = '/login';
        };
    }

    $('#deleteBtn').click(function () {
        $.ajax({
            type: 'GET',
            url: '/profile/delete',
            success: function (response) {
                openModal('Delete', 'message','Profile deleted successful!')
                setCloseToRedirect();
                // Wait for 10 seconds before redirecting to /login
                setTimeout(function() {
                    window.location.href = '/login';
                }, 5000); // 5000 milliseconds
            },
            error: function (xhr, status, error) {
                openModal('Error', 'error','An error occurred while deleting the profile.')
            }
        });
    });

    // Function to handle submit button click
    $('#submitBtn').click(function () {
        // Check if fields have been modified
        if (!emailModified && !passwordModified && !descriptionModified) {
            openModal('Warning', 'message','No changes detected.')
            return;
        }

        let data = {};
        if (emailModified) {
            const newEmail = $('#email').val().trim();
            if (newEmail !== '') {
                data.email = newEmail;
                $('#email').attr('placeholder', newEmail);
            }
        }

        if (passwordModified) {
            const newPassword = $('#password').val().trim();
            if (newPassword !== '') {
                data.password = newPassword;
            }
        }

        if (descriptionModified) {
            const newDescription = $('#description').val().trim();
            if (newDescription !== '') {
                data.description = newDescription;
            }
        }

        // Reset modified flags
        emailModified = false;
        passwordModified = false;
        descriptionModified = false;

        // Reset password and email fields
        $('#password').val('');
        $('#email').val('');

        // Log updated values (for testing purposes)
        console.log(data)

        // Perform further actions (e.g., AJAX request)
        $.ajax({
            type: 'POST',
            url: '/profile/update',
            data: data,
            success: function (response) {
                openModal('Update', 'message','Profile update successful!')
            },
            error: function (xhr, status, error) {
                openModal('Error', 'error','An error occurred while updating the profile.')
            }
        });
    });

});