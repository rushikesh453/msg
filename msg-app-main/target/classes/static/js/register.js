document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const passwordInput = document.getElementById('passwordHash');
    const strengthBar = document.getElementById('passwordStrength');
    const strengthText = document.getElementById('strengthText');

    // Password strength checker
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            const password = passwordInput.value;
            let strength = 0;
            let message = '';

            // Length check
            if (password.length < 6) {
                strength = 0;
                message = 'Password must be at least 6 characters';
            } else {
                // Basic strength calculation
                strength += Math.min(password.length * 4, 25); // Length factor (max 25%)

                if (/[A-Z]/.test(password)) strength += 15; // Uppercase
                if (/[a-z]/.test(password)) strength += 10; // Lowercase
                if (/[0-9]/.test(password)) strength += 25; // Numbers
                if (/[^A-Za-z0-9]/.test(password)) strength += 25; // Special chars

                // Set message based on strength
                if (strength < 30) message = 'Very weak';
                else if (strength < 60) message = 'Weak';
                else if (strength < 80) message = 'Good';
                else message = 'Strong';
            }

            // Update UI
            strengthBar.style.width = `${Math.min(strength, 100)}%`;
            strengthText.textContent = message;

            // Set color based on strength
            if (strength < 30) strengthBar.style.backgroundColor = '#ff4d4d';
            else if (strength < 60) strengthBar.style.backgroundColor = '#ffa64d';
            else if (strength < 80) strengthBar.style.backgroundColor = '#99cc33';
            else strengthBar.style.backgroundColor = '#4dff4d';
        });
    }

    // Form submission handler
    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            // Client-side validation
            const username = document.getElementById('username').value;
            const email = document.getElementById('email').value;
            const passwordHash = document.getElementById('passwordHash').value;
            const msgDiv = document.getElementById('registerMessage');

            // Clear previous messages
            msgDiv.innerHTML = '';

            // Validate inputs
            let isValid = true;
            let errorMessages = [];

            if (username.length < 3) {
                isValid = false;
                errorMessages.push('Username must be at least 3 characters');
            }

            if (!validateEmail(email)) {
                isValid = false;
                errorMessages.push('Please enter a valid email address');
            }

            if (passwordHash.length < 6) {
                isValid = false;
                errorMessages.push('Password must be at least 6 characters');
            }

            if (!isValid) {
                displayErrors(errorMessages, msgDiv);
                return;
            }

            // Show loading state
            msgDiv.innerHTML = '<div class="alert alert-info">Processing registration...</div>';

            // Submit form if validation passes
            try {
                console.log('Sending registration request...');
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, email, passwordHash })
                });

                console.log('Response status:', response.status);
                console.log('Response ok:', response.ok);

                const result = await response.json();
                console.log('Response data:', result);

                // Always show response for debugging
                if (response.ok) {
                    console.log('Registration successful');
                    // Ensure message div exists and is visible
                    if (msgDiv) {
                        console.log('Updating message div with success message');
                        msgDiv.innerHTML = `
                            <div class="alert alert-success" role="alert">
                                <i class="bi bi-check-circle-fill me-2"></i>
                                <strong>Success!</strong> ${result.message || 'User registered successfully!'}
                                <div>Redirecting to login page in 2 seconds...</div>
                            </div>
                        `;
                        msgDiv.style.display = 'block';

                        // Force layout recalculation
                        void msgDiv.offsetWidth;

                        // Scroll to message
                        msgDiv.scrollIntoView({behavior: 'smooth', block: 'center'});
                    } else {
                        console.error('Message div not found!');
                        alert('Registration successful! Redirecting to login page...');
                    }

                    // Redirect to login page after successful registration
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 2000);
                } else {
                    console.log('Registration failed');
                    // Handle validation errors from server
                    if (result.errors) {
                        const errorMessages = [];
                        for (const field in result.errors) {
                            errorMessages.push(result.errors[field]);
                        }
                        displayErrors(errorMessages, msgDiv);
                    } else {
                        // Generic error message
                        msgDiv.innerHTML = `
                            <div class="alert alert-danger" role="alert">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                <strong>Error!</strong> ${result.message || 'Registration failed. Please try again.'}
                            </div>
                        `;
                        msgDiv.style.display = 'block';
                    }
                }
            } catch (error) {
                console.error('Error during registration:', error);
                msgDiv.innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>
                        <strong>Error!</strong> An unexpected error occurred. Please try again.
                        <div><small>(${error.message})</small></div>
                    </div>
                `;
                msgDiv.style.display = 'block';
            }
        });
    }

    // Helper functions
    function validateEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function displayErrors(errorMessages, container) {
        let errorHTML = '<div class="alert alert-danger"><ul class="mb-0">';
        errorMessages.forEach(msg => {
            errorHTML += `<li><i class="bi bi-exclamation-circle"></i> ${msg}</li>`;
        });
        errorHTML += '</ul></div>';
        container.innerHTML = errorHTML;
    }
});

// Function to toggle password visibility
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const button = input.nextElementSibling;
    const icon = button.querySelector('i');

    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.replace('bi-eye', 'bi-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.replace('bi-eye-slash', 'bi-eye');
    }
}
