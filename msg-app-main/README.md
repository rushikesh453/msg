# Message App - Secure Messaging Platform

A modern, secure messaging platform built with Spring Boot and modern JavaScript.

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2**
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access with Hibernate
- **H2 Database** - Development database (configurable for production)
- **Lombok** - Reduces boilerplate code
- **BCrypt** - Password encryption

### Frontend
- **HTML5/CSS3/JavaScript**
- **Bootstrap 5** - Responsive UI components
- **Thymeleaf** - Server-side templating

## Architecture Overview

The application follows a layered architecture with clear separation of concerns:

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│             │     │             │     │             │     │             │
│  Controller │────▶│   Service   │────▶│ Repository  │────▶│  Database   │
│    Layer    │     │    Layer    │     │    Layer    │     │             │
│             │     │             │     │             │     │             │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

- **Controller Layer** - Handles HTTP requests/responses
- **Service Layer** - Contains business logic and validation
- **Repository Layer** - Data access and persistence
- **Entity Layer** - Domain objects mapping to database tables
- **DTO Layer** - Data transfer objects for API communication

## Key Features

1. **User Authentication**
   - Registration with email verification
   - Secure login with session management
   - Password encryption with BCrypt

2. **Friend Management**
   - Send friend requests
   - Accept/reject friend requests
   - View pending friend requests

3. **Messaging**
   - Real-time messaging between friends
   - Message history
   - Read receipts (coming soon)

4. **Security**
   - CSRF protection
   - XSS prevention
   - Content Security Policy
   - Session management

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Log in (returns JWT token)
- `POST /api/auth/logout` - Log out
- `GET /api/auth/me` - Get current user info
- `GET /api/auth/validate` - Validate session

### Users
- `GET /api/users` - Get all users (with pagination)
- `GET /api/users/{userId}` - Get user by ID
- `GET /api/users/find?query={username|email}` - Find user by username or email
- `PUT /api/users/{userId}` - Update user profile
- `DELETE /api/users/{userId}` - Delete user account

### Friend Requests
- `GET /api/friend-requests` - Get friend requests for current user
- `GET /api/friend-requests/{userId}` - Get friend requests for specified user
- `POST /api/friend-requests/send` - Send friend request
- `POST /api/friend-requests/accept` - Accept friend request
- `POST /api/friend-requests/reject` - Reject friend request
- `POST /api/friend-requests/cancel` - Cancel sent friend request

### Messaging
- `GET /api/messages/{userId1}/{userId2}` - Get messages between two users
- `POST /api/messages/send` - Send message
- `GET /api/messages/all/{userId}` - Get all messages for a user

### Friends
- `GET /api/friends/list` - Get friends list for current user
- `GET /api/friends/list/{userId}` - Get friends list for specified user

## Setup Instructions

### Prerequisites
- JDK 17+
- Maven 3.6+
- MySQL (optional, can use H2 for development)

### Running Locally
1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/message-app.git
   cd message-app
   ```

2. Build the project
   ```bash
   mvn clean install
   ```

3. Run the application
   ```bash
   mvn spring-boot:run
   ```

4. Access the application at http://localhost:8081

### Configuration
Key configuration options in `application.yml`:

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:messagedb
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    com.ma.message_apps: DEBUG
```

## Security Considerations

1. **Password Storage**
   - Passwords are stored using BCrypt with a strength factor of 12
   - Plain passwords are never stored or logged

2. **Session Management**
   - Session fixation protection
   - Session timeout after inactivity
   - Prevention of concurrent sessions

3. **API Security**
   - Authentication required for all API endpoints except login/register
   - Proper error handling to prevent information leakage

## Future Enhancements

1. **Real-time Messaging**
   - WebSocket integration for instant messaging
   - Typing indicators

2. **Media Sharing**
   - Image sharing
   - File attachments

3. **User Profiles**
   - Profile pictures
   - Status messages
   - Online/offline indicators

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.
