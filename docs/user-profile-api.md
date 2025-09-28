# User Profile Management API Documentation

## Overview

This API provides endpoints for authenticated users to manage their personal profile information and change their passwords using their access tokens.

## Base URL
```
/v1/profile
```

## Authentication

All endpoints require authentication via JWT Bearer token in the Authorization header:
```
Authorization: Bearer <access_token>
```

## API Endpoints

### 1. Get Current User Profile

**GET** `/v1/profile`

Retrieves the current authenticated user's profile information.

**Response:**
```json
{
    "status": {
        "code": 200,
        "message": "Profile retrieved successfully"
    },
    "data": {
        "id": "user-uuid",
        "username": "johndoe",
        "nickname": "John Doe",
        "email": "john@example.com",
        "mobileCountryCode": "1",
        "mobileNumber": "1234567890",
        "accountStatus": "active",
        "lastLoginAt": "2024-01-15T10:30:00",
        "passwordChangedAt": "2024-01-01T00:00:00",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-15T09:15:00"
    },
    "meta": {
        "serverDateTime": "2024-01-15T12:00:00",
        "requestId": "req-12345",
        "sessionId": "session-67890"
    }
}
```

### 2. Update Profile Information

**PUT** `/v1/profile`

Updates the current user's profile information. All fields are optional.

**Request Body:**
```json
{
    "nickname": "John Smith",
    "email": "johnsmith@example.com",
    "mobileCountryCode": "86",
    "mobileNumber": "13800138000"
}
```

**Validation Rules:**
- `nickname`: Max 30 characters, allows letters, numbers, spaces, dots, hyphens, underscores
- `email`: Must be valid email format, max 128 characters, unique across system
- `mobileCountryCode`: 1-4 digits starting with non-zero
- `mobileNumber`: 4-15 digits
- Mobile country code and number must both be provided or both be empty

**Response:**
```json
{
    "status": {
        "code": 200,
        "message": "Profile updated successfully"
    },
    "data": {
        "id": "user-uuid",
        "username": "johndoe",
        "nickname": "John Smith",
        "email": "johnsmith@example.com",
        "mobileCountryCode": "86",
        "mobileNumber": "13800138000",
        "accountStatus": "active",
        "lastLoginAt": "2024-01-15T10:30:00",
        "passwordChangedAt": "2024-01-01T00:00:00",
        "createdAt": "2024-01-01T00:00:00",
        "updatedAt": "2024-01-15T12:00:00"
    }
}
```

**Error Responses:**
- `400`: Validation error (invalid format, mobile info mismatch)
- `409`: Email or mobile number already in use by another account

### 3. Change Password

**PUT** `/v1/profile/password`

Changes the current user's password.

**Request Body:**
```json
{
    "currentPassword": "oldPassword123",
    "newPassword": "newPassword123",
    "confirmPassword": "newPassword123"
}
```

**Validation Rules:**
- `currentPassword`: Required, must match user's current password
- `newPassword`: Required, 8-128 characters, must be different from current password
- `confirmPassword`: Required, must match `newPassword`

**Response:**
```json
{
    "status": {
        "code": 200,
        "message": "Password changed successfully"
    },
    "data": null
}
```

**Error Responses:**
- `400`: Validation error (passwords don't match, invalid format)
- `401`: Current password is incorrect
- `409`: New password must be different from current password

### 4. Check Email Availability

**GET** `/v1/profile/check-email?email={email}`

Checks if an email address is available for use (not taken by another user).

**Parameters:**
- `email`: Email address to check

**Response:**
```json
{
    "status": {
        "code": 200,
        "message": "Email availability checked"
    },
    "data": true
}
```

- `true`: Email is available
- `false`: Email is already in use by another account

### 5. Check Mobile Number Availability

**GET** `/v1/profile/check-mobile?countryCode={code}&number={number}`

Checks if a mobile number is available for use (not taken by another user).

**Parameters:**
- `countryCode`: Mobile country code (e.g., "1", "86")
- `number`: Mobile number (e.g., "1234567890")

**Response:**
```json
{
    "status": {
        "code": 200,
        "message": "Mobile number availability checked"
    },
    "data": true
}
```

- `true`: Mobile number is available
- `false`: Mobile number is already in use by another account

## Security Features

1. **JWT Token Validation**: All endpoints validate the JWT token and extract user ID
2. **Self-Service Only**: Users can only update their own profile information
3. **Uniqueness Validation**: Prevents duplicate emails and mobile numbers
4. **Password Security**: Requires current password for password changes
5. **Input Validation**: Comprehensive validation on all input fields
6. **Audit Trail**: All changes are logged with timestamps and user information

## Error Handling

All endpoints return consistent error responses:

```json
{
    "status": {
        "code": 400,
        "message": "Request error",
        "errors": {
            "field": "Validation error message"
        }
    },
    "data": null,
    "meta": {
        "serverDateTime": "2024-01-15T12:00:00",
        "requestId": "req-12345",
        "sessionId": "session-67890"
    }
}
```

Common HTTP status codes:
- `200`: Success
- `400`: Bad request (validation error)
- `401`: Unauthorized (invalid/missing token)
- `404`: User not found
- `409`: Conflict (email/mobile already in use)
- `500`: Internal server error

## Usage Examples

### cURL Examples

**Get Profile:**
```bash
curl -X GET "http://localhost:8080/v1/profile" \
  -H "Authorization: Bearer your-jwt-token"
```

**Update Profile:**
```bash
curl -X PUT "http://localhost:8080/v1/profile" \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "John Smith",
    "email": "johnsmith@example.com"
  }'
```

**Change Password:**
```bash
curl -X PUT "http://localhost:8080/v1/profile/password" \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "oldPassword123",
    "newPassword": "newPassword123",
    "confirmPassword": "newPassword123"
  }'
```

**Check Email Availability:**
```bash
curl -X GET "http://localhost:8080/v1/profile/check-email?email=test@example.com" \
  -H "Authorization: Bearer your-jwt-token"
```

### JavaScript Examples

```javascript
// Get profile
const response = await fetch('/v1/profile', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});
const profile = await response.json();

// Update profile
const updateResponse = await fetch('/v1/profile', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    nickname: 'New Nickname',
    email: 'newemail@example.com'
  })
});

// Change password
const passwordResponse = await fetch('/v1/profile/password', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    currentPassword: 'oldPassword',
    newPassword: 'newPassword123',
    confirmPassword: 'newPassword123'
  })
});
```

## Integration Notes

1. **Frontend Integration**: Use these endpoints to build user profile management interfaces
2. **Mobile Apps**: All endpoints are designed to work seamlessly with mobile applications
3. **Real-time Validation**: Use the check endpoints for real-time validation in forms
4. **Error Handling**: Implement proper error handling for all possible response codes
5. **Security**: Always use HTTPS in production and securely store JWT tokens