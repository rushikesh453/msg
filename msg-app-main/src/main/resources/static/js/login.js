document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value.trim();
            const passwordHash = document.getElementById('passwordHash').value;
            const msgDiv = document.getElementById('loginMessage');

            // Clear previous messages
            msgDiv.innerHTML = '<div class="alert alert-info">Logging in...</div>';

            // Use the current window's origin to ensure correct port
            const currentOrigin = window.location.origin;

            // Create a form that will POST directly to the server
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = currentOrigin + '/api/auth/direct-login'; // Use current origin to ensure correct port
            form.style.display = 'none';

            // Add username field
            const usernameInput = document.createElement('input');
            usernameInput.type = 'text';
            usernameInput.name = 'username';
            usernameInput.value = username;
            form.appendChild(usernameInput);

            // Add password field
            const passwordInput = document.createElement('input');
            passwordInput.type = 'password';
            passwordInput.name = 'passwordHash';
            passwordInput.value = passwordHash;
            form.appendChild(passwordInput);

            // Add the form to the body and submit it
            document.body.appendChild(form);

            // Log that we're submitting the form with the correct origin
            console.log('Submitting form to ' + form.action + ' for server-side redirect...');

            // Submit the form (this will allow the server's redirect to take effect)
            form.submit();
        });
    }

    // Check for error parameter in URL query string
    window.addEventListener('load', function() {
        const urlParams = new URLSearchParams(window.location.search);
        const error = urlParams.get('error');
        if (error) {
            const msgDiv = document.getElementById('loginMessage');
            if (msgDiv) {
                msgDiv.innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>
                        <strong>Error!</strong> ${decodeURIComponent(error)}
                    </div>
                `;
            }
        }
    });
});
