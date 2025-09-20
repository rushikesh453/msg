/**
 * Dashboard.js - Main JavaScript file for the messaging application dashboard
 * Handles UI interactions and application state management
 */

// Make logout function globally available right away
window.logout = function() {
    console.log('Logout function called');
    if (ApiClient && ApiClient.auth) {
        ApiClient.auth.logout()
            .then(() => {
                // Clear local storage
                localStorage.removeItem('user');
                localStorage.removeItem('userId');
                localStorage.removeItem('username');

                // Redirect to login page
                window.location.href = '/login';
            })
            .catch(error => {
                console.error('Logout error:', error);
                // If API call fails, still redirect to login
                window.location.href = '/login';
            });
    } else {
        console.error('ApiClient not available');
        window.location.href = '/login';
    }
};

document.addEventListener('DOMContentLoaded', function() {
    // UI Elements
    const dashboardContent = document.querySelector('.container');
    const loadingSpinner = document.getElementById('loading-spinner') || createLoadingSpinner();
    const messageContainer = document.getElementById('messages-container');
    const userInfo = document.getElementById('user-info');
    
    // Application State
    const appState = {
        currentUser: null,
        selectedFriend: null,
        friends: [],
        messages: [],
        isLoading: true,
        userStatus: 'online',
        contactStatuses: {} // Store contacts' status
    };
    
    // Shared variables for chat functionality
    let selectedContactId = null;
    let chatPollingInterval = null;
    let contactsRefreshInterval = null; // New variable for contacts auto-refresh
    const CONTACTS_REFRESH_INTERVAL = 5000; // Refresh contacts every 15 seconds

    // Initialize the dashboard
    initDashboard();
    
    /**
     * Initializes the dashboard
     */
    async function initDashboard() {
        showLoading();
        
        try {
            // Get current user details
            await getCurrentUser();
            
            // Load friends list
            await loadFriends();
            
            // Load chat contacts
            loadChatContacts();

            // Load friend requests
            loadFriendRequests();

            // Set up event listeners
            setupEventListeners();
            
            hideLoading();
            dashboardContent.style.display = 'block';
        } catch (error) {
            console.error('Dashboard initialization error:', error);
            showNotification('Failed to load dashboard. Please refresh the page.', 'error');
            hideLoading();
        }
    }
    
    // Utility functions for UI
    
    /**
     * Shows loading spinner
     */
    function showLoading() {
        appState.isLoading = true;
        loadingSpinner.style.display = 'flex';
    }
    
    /**
     * Hides loading spinner
     */
    function hideLoading() {
        appState.isLoading = false;
        loadingSpinner.style.display = 'none';
    }
    
    /**
     * Creates loading spinner element if it doesn't exist
     */
    function createLoadingSpinner() {
        const spinner = document.createElement('div');
        spinner.id = 'loading-spinner';
        spinner.className = 'loading-spinner';
        spinner.innerHTML = '<div class="spinner"></div><p>Loading...</p>';
        spinner.style.cssText = 'position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(255,255,255,0.8); display:flex; flex-direction:column; justify-content:center; align-items:center; z-index:1000;';
        document.body.appendChild(spinner);
        return spinner;
    }
    
    /**
     * Shows notification message to user
     */
    function showNotification(message, type = 'info') {
        // Create notification element if it doesn't exist
        let notification = document.getElementById('notification');
        if (!notification) {
            notification = document.createElement('div');
            notification.id = 'notification';
            notification.style.cssText = 'position:fixed; top:20px; right:20px; padding:15px; border-radius:5px; color:white; z-index:1000; transition:opacity 0.3s;';
            document.body.appendChild(notification);
        }
        
        // Set notification style based on type
        const bgColor = type === 'error' ? '#ff3860' : type === 'success' ? '#23d160' : '#3298dc';
        notification.style.backgroundColor = bgColor;
        
        // Set message and show notification
        notification.textContent = message;
        notification.style.opacity = 1;
        
        // Auto-hide after 5 seconds
        setTimeout(() => {
            notification.style.opacity = 0;
        }, 5000);
    }

    // --- User Info and Friends List ---
    /**
     * Fetches and displays the current user's information
     */
    async function getCurrentUser() {
        try {
            const data = await ApiClient.auth.getCurrentUser();

            // Extract the user object from the response
            const user = data.user;

            if (!user || !user.userId) throw new Error('Invalid user data');
            
            appState.currentUser = user;
            updateUserInfoUI(user);
            console.log('Current user info:', user);
        } catch (error) {
            console.error('Error fetching user info:', error);
            showNotification('Failed to load user information. Please try again.', 'error');
        }
    }
    
    /**
     * Updates the user information displayed in the UI
     * @param {object} user - User data object
     */
    function updateUserInfoUI(user) {
        if (userInfo) {
            userInfo.textContent = `Logged in as: ${user.username || user.email}`;
        }

        // Set current user ID globally for use in other functions
        window.currentUserId = user.userId;
        console.log('Set current user ID:', window.currentUserId);

        // Update user name in header if element exists
        const currentUserName = document.getElementById('current-user-name');
        if (currentUserName) {
            currentUserName.textContent = user.username || user.email;
        }
    }
    
    /**
     * Loads the friends list for the current user
     */
    async function loadFriends() {
        try {
            const friends = await ApiClient.friends.getFriendsList();
            appState.friends = friends;
            updateFriendsListUI(friends);
            console.log('Friends list:', friends);
        } catch (error) {
            console.error('Error fetching friends list:', error);
            showNotification('Failed to load friends list. Please try again.', 'error');
        }
    }
    
    /**
     * Updates the friends list displayed in the UI
     * @param {Array} friends - Array of friend objects
     */
    function updateFriendsListUI(friends) {
        const friendsList = document.getElementById('friends-list');
        if (!friendsList) return;
        
        friendsList.innerHTML = '';
        friends.forEach(friend => {
            const li = document.createElement('li');
            li.className = 'list-group-item';
            li.textContent = friend.username || friend.email || friend.userId;
            friendsList.appendChild(li);
        });
    }
    
    /**
     * Sets up event listeners for the dashboard
     */
    function setupEventListeners() {
        // Set up status selector
        setupStatusSelector();

        // Initialize Bootstrap tabs properly
        initializeTabs();

        // Initialize the status system
        initStatusSystem();

        // Set up chat form
        setupChatForm();

        // Start the auto-refresh for contacts
        startContactsAutoRefresh();
    }

    /**
     * Initializes the Bootstrap tabs
     */
    function initializeTabs() {
        try {
            console.log("Initializing tabs...");
            // Create tab instances programmatically
            const tabElements = document.querySelectorAll('[data-bs-toggle="tab"]');
            console.log("Found tab elements:", tabElements.length);

            tabElements.forEach(tabEl => {
                // Initialize each tab with Bootstrap's tab API
                try {
                    new bootstrap.Tab(tabEl);

                    // Add event listener to handle tab switch
                    tabEl.addEventListener('shown.bs.tab', function(event) {
                        const targetId = event.target.getAttribute('data-bs-target');
                        console.log(`Tab switched to: ${targetId}`);

                        // Special handling for chat tab
                        if (targetId === '#chat') {
                            console.log("Chat tab activated");
                            // Refresh chat contacts and messages when the chat tab is shown
                            loadChatContacts();

                            // Reset the chat area
                            document.getElementById('chat-form').style.display = 'none';
                            document.getElementById('chat-with-label').textContent = 'Select a contact to chat';
                            document.getElementById('chat-messages').innerHTML = '<div class="text-center p-5 text-muted">Select a contact to start chatting</div>';
                            stopChatPolling();

                            // Start auto-refresh for contacts when chat tab is active
                            startContactsAutoRefresh();
                        } else {
                            // Stop auto-refresh for contacts when leaving chat tab
                            stopContactsAutoRefresh();
                        }
                    });
                } catch (e) {
                    console.error("Error initializing tab:", e);
                }
            });
        } catch (e) {
            console.error("Error in initializeTabs:", e);
        }
    }

    /**
     * Sets up the user status selector
     */
    function setupStatusSelector() {
        const statusOptions = document.querySelectorAll('.status-option');

        // Set the initial status
        updateStatusDisplay(appState.userStatus);

        // Add click event listeners to status options
        statusOptions.forEach(option => {
            option.addEventListener('click', function() {
                const status = this.getAttribute('data-status');
                changeUserStatus(status);
            });
        });
    }

    /**
     * Changes the user's status
     * @param {string} status - The new status (online, away, offline)
     */
    function changeUserStatus(status) {
        if (!['online', 'away', 'offline'].includes(status)) return;

        appState.userStatus = status;

        // Update UI to reflect the new status
        updateStatusDisplay(status);

        // Update status in the footer
        updateFooterStatus(status);

        // Send status update to server (if API endpoint exists)
        updateUserStatusOnServer(status);

        // Show notification
        const statusMessages = {
            'online': 'You are now online',
            'away': 'You are now set to away',
            'offline': 'You appear offline to others'
        };
        showNotification(statusMessages[status], 'info');
    }

    /**
     * Updates the status display in the UI
     * @param {string} status - The status to display
     */
    function updateStatusDisplay(status) {
        // Remove active class from all options
        document.querySelectorAll('.status-option').forEach(option => {
            option.classList.remove('active');
        });

        // Add active class to selected status
        const selectedOption = document.querySelector(`.status-option[data-status="${status}"]`);
        if (selectedOption) {
            selectedOption.classList.add('active');
        }
    }

    /**
     * Updates the status in the footer
     * @param {string} status - The status to display
     */
    function updateFooterStatus(status) {
        const footerStatus = document.querySelector('.footer-section .status-indicator');
        const statusText = document.querySelector('.footer-section .status-text');

        if (footerStatus) {
            // Remove all status classes
            footerStatus.classList.remove('status-online', 'status-away', 'status-offline');
            // Add the current status class
            footerStatus.classList.add(`status-${status}`);
        }

        if (statusText) {
            const statusMessages = {
                'online': 'You\'re Online',
                'away': 'You\'re Away',
                'offline': 'You\'re Offline'
            };
            statusText.textContent = statusMessages[status] || 'You\'re Online';
        }
    }

    /**
     * Updates the user's status on the server (if API endpoint exists)
     * @param {string} status - The status to update
     */
    function updateUserStatusOnServer(status) {
        // This is a placeholder. In a real application, you'd send this to your server.
        // Example:
        // ApiClient.user.updateStatus(window.currentUserId, status)
        //     .then(response => console.log('Status updated on server'))
        //     .catch(error => console.error('Error updating status:', error));

        // For now, just store it in localStorage
        try {
            localStorage.setItem('userStatus', status);
            console.log('User status saved locally:', status);
        } catch (e) {
            console.error('Error saving status to localStorage:', e);
        }
    }
    
    // --- Friend Requests Dynamic Loading ---
    /**
     * Loads friend requests for the current user
     */
    function loadFriendRequests() {
        console.log("Loading friend requests...");

        // Clear the table and show loading message
        const tbody = document.getElementById('friend-requests-list');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center">Loading friend requests...</td></tr>';
        } else {
            console.error('Friend requests list element not found');
            return;
        }

        ApiClient.friends.getFriendRequests()
            .then(response => {
                console.log('Friend requests raw response:', response);

                // Extract requests from the response structure
                let requests = [];
                if (response && response.success && response.requests) {
                    requests = response.requests;
                } else if (response && Array.isArray(response)) {
                    requests = response;
                }

                console.log('Processed friend requests:', requests);

                // Clear the table before adding new content
                tbody.innerHTML = '';

                // If no requests, show message
                if (!requests || requests.length === 0) {
                    console.log('No friend requests found, showing empty message');
                    tbody.innerHTML = '<tr><td colspan="3" class="text-center">No friend requests yet.</td></tr>';
                    return;
                }

                // Loop through requests and add rows
                requests.forEach(req => {
                    console.log('Processing request:', req);

                    const senderName = req.senderUsername || (req.sender && (req.sender.username || req.sender.email || req.sender.userId)) || req.senderId || 'Unknown';
                    let isPending = true; // Default to pending for action buttons

                    if (req.status) {
                        if (typeof req.status === 'string') {
                            isPending = req.status.toUpperCase() === 'PENDING';
                        } else if (typeof req.status === 'object' && req.status.name) {
                            isPending = req.status.name.toUpperCase() === 'PENDING';
                        }
                    }

                    const requestId = req.requestId || req.id;

                    const actions = isPending
                        ? `<button class="btn btn-success btn-sm" onclick="acceptFriendRequest(${requestId})">Accept</button>
                           <button class="btn btn-danger btn-sm" onclick="rejectFriendRequest(${requestId})">Reject</button>`
                        : '';

                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${senderName}</td>
                        <td>${typeof req.status === 'object' && req.status.name ? req.status.name : req.status}</td>
                        <td>${actions}</td>
                    `;
                    tbody.appendChild(row);
                });
            })
            .catch(error => {
                console.error('Error loading friend requests:', error);
                tbody.innerHTML = '<tr><td colspan="3" class="text-center">Error loading friend requests.</td></tr>';
            });
    }

    // Accept/Reject actions - Make these globally available
    window.acceptFriendRequest = function(requestId) {
        console.log("Accepting friend request:", requestId);

        ApiClient.friends.acceptFriendRequest(requestId)
            .then(response => {
                console.log("Accept friend request response:", response);
                showNotification('Friend request accepted!', 'success');
                loadFriendRequests();

                // Also reload the chat contacts list to show the new friend
                loadChatContacts();
            })
            .catch(error => {
                console.error("Error accepting friend request:", error);
                showNotification('Failed to accept friend request', 'error');
            });
    };
    
    window.rejectFriendRequest = function(requestId) {
        console.log("Rejecting friend request:", requestId);

        ApiClient.friends.rejectFriendRequest(requestId)
            .then(response => {
                console.log("Reject friend request response:", response);
                showNotification('Friend request rejected', 'info');
                loadFriendRequests();
            })
            .catch(error => {
                console.error("Error rejecting friend request:", error);
                showNotification('Failed to reject friend request', 'error');
            });
    };

    // --- Send Friend Request Form Logic ---
    const sendFriendForm = document.getElementById('send-friend-form');
    if (sendFriendForm) {
        sendFriendForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const usernameOrEmail = document.getElementById('friend-username').value.trim();
            const feedbackDiv = document.getElementById('send-friend-feedback');
            feedbackDiv.innerHTML = '';
            if (!usernameOrEmail) {
                feedbackDiv.innerHTML = '<div class="alert alert-danger">Please enter a username or email.</div>';
                return;
            }
            
            console.log("Searching for user:", usernameOrEmail);

            // Find user by username/email
            ApiClient.friends.findUser(usernameOrEmail)
                .then(response => {
                    console.log("User search response:", response);

                    if (!response || !response.success || !response.user) {
                        feedbackDiv.innerHTML = '<div class="alert alert-danger">User not found.</div>';
                        return;
                    }

                    const user = response.user;
                    console.log("Found user:", user);

                    // Send friend request
                    ApiClient.friends.sendFriendRequest(window.currentUserId, user.userId)
                        .then((response) => {
                            console.log("Friend request response:", response);

                            if (response && response.success) {
                                feedbackDiv.innerHTML = '<div class="alert alert-success">Friend request sent!</div>';
                            } else {
                                const errorMsg = response && response.error ? response.error : 'Friend request already sent or failed.';
                                feedbackDiv.innerHTML = `<div class="alert alert-warning">${errorMsg}</div>`;
                            }
                        })
                        .catch(error => {
                            console.error("Error sending friend request:", error);
                            feedbackDiv.innerHTML = '<div class="alert alert-danger">Error sending friend request.</div>';
                        });
                })
                .catch(error => {
                    console.error("Error searching for user:", error);
                    feedbackDiv.innerHTML = '<div class="alert alert-danger">Error searching for user.</div>';
                });
        });
    }

    // --- Chat Tab Logic ---

    /**
     * Starts polling for new chat messages
     * @param {number} contactId - The contact ID to poll messages for
     */
    function startChatPolling(contactId) {
        if (chatPollingInterval) clearInterval(chatPollingInterval);
        chatPollingInterval = setInterval(() => {
            loadChatMessages(contactId);
        }, 3000); // Poll every 3 seconds
    }

    /**
     * Stops polling for chat messages
     */
    function stopChatPolling() {
        if (chatPollingInterval) clearInterval(chatPollingInterval);
        chatPollingInterval = null;
    }

    /**
     * Starts the auto-refresh interval for contacts list
     */
    function startContactsAutoRefresh() {
        // Clear any existing interval first
        stopContactsAutoRefresh();

        // Only start auto-refresh if we're on the chat tab
        if (!document.getElementById('chat').classList.contains('show')) return;

        console.log(`Starting auto-refresh for contacts (every ${CONTACTS_REFRESH_INTERVAL/1000}s)`);
        contactsRefreshInterval = setInterval(() => {
            // Only refresh if we're on the chat tab
            if (document.getElementById('chat').classList.contains('show')) {
                console.log("Auto-refreshing contacts list...");
                loadChatContacts(true); // true means it's an auto-refresh
            }
        }, CONTACTS_REFRESH_INTERVAL);
    }

    /**
     * Stops the auto-refresh interval for contacts list
     */
    function stopContactsAutoRefresh() {
        if (contactsRefreshInterval) {
            console.log("Stopping contacts auto-refresh");
            clearInterval(contactsRefreshInterval);
            contactsRefreshInterval = null;
        }
    }

    /**
     * Loads chat contacts (friends) list
     * @param {boolean} isAutoRefresh - Whether this is an automatic refresh (to preserve selection)
     */
    function loadChatContacts(isAutoRefresh = false) {
        console.log('Loading chat contacts...' + (isAutoRefresh ? ' (auto-refresh)' : ''));

        // Remember the currently selected contact ID if this is an auto-refresh
        const previouslySelectedContactId = selectedContactId;

        // First, get the current status of all users
        ApiClient.user.getAllUserStatuses()
            .then(statusResponse => {
                // Create a map of userId -> status
                const userStatuses = {};
                if (statusResponse && Array.isArray(statusResponse)) {
                    statusResponse.forEach(statusInfo => {
                        if (statusInfo && statusInfo.userId) {
                            userStatuses[statusInfo.userId] = statusInfo.status || 'offline';
                        }
                    });
                }

                console.log('User statuses loaded:', userStatuses);

                // Now get the friends list
                return ApiClient.friends.getFriendsList();
            })
            .then(response => {
                console.log('Chat contacts response:', response);

                // Extract the friends array from the response
                let contacts = [];
                if (response && response.success && response.friends) {
                    contacts = response.friends;
                } else if (Array.isArray(response)) {
                    contacts = response;
                }

                console.log('Processed contacts for chat:', contacts);

                const contactsList = document.getElementById('chat-contacts-list');
                if (!contactsList) {
                    console.error('Chat contacts list element not found');
                    return;
                }

                contactsList.innerHTML = '';
                if (!contacts || !contacts.length) {
                    contactsList.innerHTML = '<div class="text-center p-3">No contacts yet.</div>';
                    return;
                }

                let onlineCount = 0;

                // For each contact, get their current status
                const statusPromises = contacts.map(contact =>
                    ApiClient.user.getStatus(contact.userId)
                        .then(statusData => {
                            // Save status to our local store
                            const status = statusData && statusData.status ? statusData.status : 'offline';
                            appState.contactStatuses[contact.userId] = status;
                            return { contact, status };
                        })
                        .catch(error => {
                            console.error(`Error getting status for user ${contact.userId}:`, error);
                            appState.contactStatuses[contact.userId] = 'offline';
                            return { contact, status: 'offline' };
                        })
                );

                // Once we have all statuses, update the UI
                Promise.all(statusPromises).then(contactStatuses => {
                    contactStatuses.forEach(({ contact, status }) => {
                        if (status === 'online') {
                            onlineCount++;
                        }

                        const contactName = contact.username || contact.email || `User ${contact.userId}`;
                        const nameInitial = contactName.charAt(0).toUpperCase();

                        // Create modern contact item with status indicator
                        const item = document.createElement('div');
                        item.className = 'contact-list-item';
                        item.setAttribute('data-user-id', contact.userId);

                        // Mark as active if this is the selected contact (or was previously selected in auto-refresh)
                        if (contact.userId === (isAutoRefresh ? previouslySelectedContactId : selectedContactId)) {
                            item.classList.add('active');
                        }

                        item.innerHTML = `
                            <div class="contact-avatar">
                                ${nameInitial}
                                <span class="status-badge status-${status}"></span>
                            </div>
                            <div class="contact-info">
                                <div class="contact-name">${contactName}</div>
                                <div class="contact-status">
                                    <span class="status-indicator status-${status}"></span>
                                    <span>${status.charAt(0).toUpperCase() + status.slice(1)}</span>
                                </div>
                            </div>
                        `;

                        item.addEventListener('click', function() {
                            selectedContactId = contact.userId;
                            document.getElementById('chat-with-label').textContent = `Chat with ${contactName}`;

                            // Update contact status in the header
                            const statusIndicator = document.getElementById('contact-status-indicator');
                            const statusText = document.getElementById('contact-status-text');

                            if (statusIndicator && statusText) {
                                const contactStatus = appState.contactStatuses[contact.userId];
                                statusIndicator.classList.remove('d-none');

                                // Remove all status classes
                                statusIndicator.querySelector('.status-indicator').className = 'status-indicator';
                                statusIndicator.querySelector('.status-indicator').classList.add(`status-${contactStatus}`);

                                statusText.textContent = contactStatus.charAt(0).toUpperCase() + contactStatus.slice(1);
                            }

                            document.getElementById('chat-form').style.display = 'flex';
                            loadChatMessages(selectedContactId);
                            startChatPolling(selectedContactId);

                            // Remove active from all contacts
                            document.querySelectorAll('.contact-list-item').forEach(el => {
                                el.classList.remove('active');
                            });

                            // Add active class to this contact
                            item.classList.add('active');
                        });

                        contactsList.appendChild(item);
                    });

                    // Update online count badge
                    const onlineCountElement = document.getElementById('online-count');
                    if (onlineCountElement) {
                        onlineCountElement.textContent = `${onlineCount} online`;
                    }

                    // If this was an auto-refresh and we had a selected contact,
                    // make sure the chat messages are still loaded and the contact is still marked as selected
                    if (isAutoRefresh && previouslySelectedContactId) {
                        // Find the contact in the list
                        const selectedContactItem = contactsList.querySelector(`.contact-list-item[data-user-id="${previouslySelectedContactId}"]`);
                        if (selectedContactItem) {
                            // Add active class to this contact
                            selectedContactItem.classList.add('active');
                        }
                    }
                });
            })
            .catch(error => {
                console.error('Error loading chat contacts:', error);
                const contactsList = document.getElementById('chat-contacts-list');
                if (contactsList) {
                    contactsList.innerHTML = '<div class="text-danger p-3 text-center">Error loading contacts</div>';
                }
            });
    }

    /**
     * Gets the status of a contact
     * @param {number} contactId - The contact ID
     * @returns {string|null} The contact status or null if not found
     */
    function getContactStatus(contactId) {
        return appState.contactStatuses[contactId] || null;
    }

    /**
     * Initialize the status check system
     * This would typically connect to a server for real status updates
     */
    function initStatusSystem() {
        // Try to load saved status from localStorage
        try {
            const savedStatus = localStorage.getItem('userStatus');
            if (savedStatus && ['online', 'away', 'offline'].includes(savedStatus)) {
                appState.userStatus = savedStatus;
            }
        } catch (e) {
            console.error('Error loading status from localStorage:', e);
        }

        // Initialize status display
        updateStatusDisplay(appState.userStatus);
        updateFooterStatus(appState.userStatus);

        // In a real app, you'd set up WebSocket or periodic checking
        // for other users' status updates here

        // For demo purposes, simulate occasional status changes for contacts
        setInterval(() => {
            simulateContactStatusChanges();
        }, 30000); // Every 30 seconds
    }

    /**
     * Simulates status changes for demo purposes
     * In a real app, this would be replaced with server notifications
     */
    function simulateContactStatusChanges() {
        if (!appState.friends || !appState.friends.length) return;

        const statuses = ['online', 'away', 'offline'];
        const contactToUpdate = appState.friends[Math.floor(Math.random() * appState.friends.length)];

        if (contactToUpdate && contactToUpdate.userId) {
            const newStatus = statuses[Math.floor(Math.random() * statuses.length)];
            appState.contactStatuses[contactToUpdate.userId] = newStatus;

            // If chat contacts are visible, update the display
            if (document.getElementById('chat').classList.contains('active')) {
                loadChatContacts();
            }
        }
    }

    /**
     * Loads chat messages between current user and a contact
     * @param {number} contactId - The contact ID to load messages for
     */
    function loadChatMessages(contactId) {
        ApiClient.messages.getChatMessages(window.currentUserId, contactId)
            .then(messages => {
                const chatMessages = document.getElementById('chat-messages');
                chatMessages.innerHTML = '';
                if (!messages || !messages.length) {
                    chatMessages.innerHTML = '<div class="text-center p-5 text-muted">No messages yet.</div>';
                    return;
                }
                messages.forEach(msg => {
                    // Use sender.userId to determine who sent the message
                    const isOwn = msg.sender && msg.sender.userId === window.currentUserId;
                    let date = msg.createdAt;
                    // If date is a string and not null, format it
                    let formattedDate = '';
                    if (date) {
                        try {
                            formattedDate = new Date(date).toLocaleString();
                        } catch (e) {
                            formattedDate = date;
                        }
                    }
                    const messageClass = isOwn ? 'chat-message-own' : 'chat-message-other';
                    const senderName = msg.sender && msg.sender.username ? msg.sender.username : 'Unknown';
                    chatMessages.innerHTML += `
                        <div class="${messageClass}">
                            <div class="chat-message-header">
                                <span class="chat-sender">${isOwn ? 'You' : senderName}</span>
                                <span class="chat-date">${formattedDate}</span>
                            </div>
                            <div class="chat-message-body">${msg.messageText}</div>
                        </div>
                    `;
                });
                chatMessages.scrollTop = chatMessages.scrollHeight;
            })
            .catch(error => {
                console.error('Error loading chat messages:', error);
                const chatMessages = document.getElementById('chat-messages');
                chatMessages.innerHTML = '<div class="text-danger p-3 text-center">Error loading messages</div>';
            });
    }

    /**
     * Setup chat form submission handler
     */
    function setupChatForm() {
        const chatForm = document.getElementById('chat-form');
        if (chatForm) {
            chatForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const input = document.getElementById('chat-input');
                const message = input.value.trim();
                if (!message || !selectedContactId) return;

                ApiClient.messages.sendMessage(window.currentUserId, selectedContactId, message)
                    .then(() => {
                        input.value = '';
                        loadChatMessages(selectedContactId);
                    })
                    .catch(error => {
                        console.error('Error sending message:', error);
                        showNotification('Failed to send message. Please try again.', 'error');
                    });
            });
        }
    }

    // Set up tab switching for chat tab
    const chatTab = document.getElementById('chat-tab');
    if (chatTab) {
        chatTab.addEventListener('click', function() {
            console.log("Chat tab clicked");
        });
    }

    // Set up tab switching for friend requests tab
    const friendTab = document.getElementById('friend-tab');
    if (friendTab) {
        friendTab.addEventListener('click', function() {
            if (window.currentUserId) {
                loadFriendRequests();
            }
        });
    }
});
