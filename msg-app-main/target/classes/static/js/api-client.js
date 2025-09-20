/**
 * API Client - Centralized API communication module
 * Handles all HTTP requests to the backend services
 */

const ApiClient = {
    /**
     * Base API request function with error handling and authentication support
     * @param {string} url - API endpoint
     * @param {Object} options - Fetch options
     * @returns {Promise} - Response promise
     */
    async request(url, options = {}) {
        // Default options for all requests
        const defaultOptions = {
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json',
                ...options.headers
            }
        };

        // Merge default options with provided options
        const fetchOptions = { ...defaultOptions, ...options };

        try {
            console.log(`API request to: ${url}`);
            const response = await fetch(url, fetchOptions);

            // Handle unauthorized responses (session expired)
            if (response.status === 401 || response.status === 403) {
                console.log('Session expired, redirecting to login');
                this.redirectToLogin('Session expired. Please log in again.');
                return null;
            }

            // Handle other error responses
            if (!response.ok) {
                const errorText = await response.text();
                console.error(`API Error (${response.status}):`, errorText);
                throw new Error(`API Error: ${response.status} ${response.statusText}`);
            }

            // Parse JSON if the content type is JSON
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                return await response.json();
            }

            return response;
        } catch (error) {
            console.error('API request error:', error);
            throw error;
        }
    },

    // Authentication APIs
    auth: {
        /**
         * Get current logged in user info
         * @returns {Promise} User data
         */
        getCurrentUser() {
            return ApiClient.request('/api/auth/me');
        },

        /**
         * Log out the current user
         * @returns {Promise} Logout response
         */
        logout() {
            return ApiClient.request('/api/auth/logout', {
                method: 'POST'
            });
        }
    },

    // Friends and requests APIs
    friends: {
        /**
         * Get friends list for the current user
         * @returns {Promise} Friends list
         */
        getFriendsList() {
            return ApiClient.request('/api/friends/list');
        },

        /**
         * Get pending friend requests
         * @returns {Promise} Friend requests
         */
        getFriendRequests() {
            return ApiClient.request('/api/friends/requests');
        },

        /**
         * Send friend request to another user
         * @param {number} fromUserId - Current user ID
         * @param {number} toUserId - Target user ID
         * @returns {Promise} Request result
         */
        sendFriendRequest(fromUserId, toUserId) {
            return ApiClient.request('/api/friends/request', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `fromUserId=${fromUserId}&toUserId=${toUserId}`
            });
        },

        /**
         * Accept a friend request
         * @param {number} requestId - Friend request ID
         * @returns {Promise} Accept result
         */
        acceptFriendRequest(requestId) {
            return ApiClient.request(`/api/friends/request/${requestId}/accept`, {
                method: 'POST'
            });
        },

        /**
         * Reject a friend request
         * @param {number} requestId - Friend request ID
         * @returns {Promise} Reject result
         */
        rejectFriendRequest(requestId) {
            return ApiClient.request(`/api/friends/request/${requestId}/reject`, {
                method: 'POST'
            });
        },

        /**
         * Find a user by username or email
         * @param {string} query - Username or email to search for
         * @returns {Promise} User data
         */
        findUser(query) {
            return ApiClient.request(`/api/friends/find?query=${encodeURIComponent(query)}`);
        }
    },

    // Messages APIs
    messages: {
        /**
         * Get chat messages between current user and another user
         * @param {number} currentUserId - Current user ID
         * @param {number} otherUserId - Other user ID
         * @returns {Promise} Chat messages
         */
        getChatMessages(currentUserId, otherUserId) {
            return ApiClient.request(`/api/messages/${currentUserId}/${otherUserId}`);
        },

        /**
         * Send a message to another user
         * @param {number} fromUserId - Sender user ID
         * @param {number} toUserId - Recipient user ID
         * @param {string} content - Message content
         * @returns {Promise} Send result
         */
        sendMessage(fromUserId, toUserId, content) {
            return ApiClient.request('/api/messages/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: `fromUserId=${fromUserId}&toUserId=${toUserId}&content=${encodeURIComponent(content)}`
            });
        }
    },

    // User APIs
    user: {
        /**
         * Get current user status
         * @param {number} userId - User ID
         * @returns {Promise} User status
         */
        getStatus(userId) {
            return ApiClient.request(`/api/users/${userId}/status`);
        },

        /**
         * Update user status
         * @param {number} userId - User ID
         * @param {string} status - New status
         * @returns {Promise} Update result
         */
        updateStatus(userId, status) {
            return ApiClient.request(`/api/users/${userId}/status`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status })
            });
        },

        /**
         * Get all users statuses
         * @returns {Promise} List of user statuses
         */
        getAllUserStatuses() {
            return ApiClient.request('/api/users/statuses');
        }
    },

    /**
     * Redirect to login page with optional message
     * @param {string} message - Optional message to display after redirection
     */
    redirectToLogin(message = null) {
        if (message) {
            sessionStorage.setItem('loginMessage', message);
        }
        window.location.href = '/login';
    }
};

// Make ApiClient globally available
window.ApiClient = ApiClient;
