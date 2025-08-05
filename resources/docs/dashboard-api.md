# Dashboard API Documentation

## Get Dashboard Statistics

**Endpoint:** `GET /v1/users/dashboard`

**Authorization:** Required - Admin or Super Admin role

**Description:** Retrieves comprehensive user statistics for dashboard display with real-time active session tracking.

### Response Format

```json
{
  "status": {
    "code": 200,
    "message": "Dashboard statistics retrieved successfully",
    "errors": null
  },
  "data": {
    "totalUsers": 150,
    "activeUsers": 120,
    "lockedUsers": 5,
    "suspendedUsers": 8,
    "pendingVerificationUsers": 12,
    "activeSessions": 25
  },
  "meta": {
    "serverDateTime": "2025-07-29 10:30:00",
    "requestId": "dashboard-stats-001",
    "sessionId": "session-123"
  }
}
```

### Statistics Explained

1. **totalUsers**: Total number of registered users in the system
2. **activeUsers**: Number of users with `active = true`
3. **lockedUsers**: Number of users with `account_status = 'locked'`
4. **suspendedUsers**: Number of users with `account_status = 'suspended'`
5. **pendingVerificationUsers**: Number of users with `account_status = 'pending_verification'`
6. **activeSessions**: Number of currently active user sessions (real-time count from memory)

## Active Session Management

The system now tracks active user sessions in memory with the following features:

- **Real-time tracking**: Sessions are tracked when users authenticate via JWT tokens
- **Automatic cleanup**: Expired sessions (30+ minutes of inactivity) are automatically removed
- **Logout handling**: Sessions are removed when users logout
- **Memory efficient**: Uses ConcurrentHashMap for thread-safe operations

### Active Session Details

**Endpoint:** `GET /v1/admin/sessions/active`

**Authorization:** Required - Admin or Super Admin role

**Description:** Get detailed information about all currently active user sessions.

```json
{
  "status": {
    "code": 200,
    "message": "Active user sessions retrieved successfully",
    "errors": null
  },
  "data": {
    "user-id-1": {
      "userId": "user-id-1",
      "username": "john.doe",
      "loginTime": "2025-07-29T10:15:00",
      "lastActivity": "2025-07-29T10:28:00",
      "userAgent": "Mozilla/5.0...",
      "ipAddress": "192.168.1.100"
    },
    "user-id-2": {
      "userId": "user-id-2", 
      "username": "jane.smith",
      "loginTime": "2025-07-29T09:45:00",
      "lastActivity": "2025-07-29T10:25:00",
      "userAgent": "Chrome/91.0...",
      "ipAddress": "10.0.0.50"
    }
  }
}
```

### Session Statistics

**Endpoint:** `GET /v1/admin/sessions/stats`

**Authorization:** Required - Admin or Super Admin role

```json
{
  "status": {
    "code": 200,
    "message": "Active session statistics retrieved successfully",
    "errors": null
  },
  "data": {
    "activeSessionCount": 25,
    "sessionTimeoutMinutes": 30
  }
}
```

### Force Session Cleanup

**Endpoint:** `GET /v1/admin/sessions/cleanup`

**Authorization:** Required - Super Admin role

**Description:** Manually trigger cleanup of expired sessions.

```json
{
  "status": {
    "code": 200,
    "message": "Session cleanup completed successfully",
    "errors": null
  },
  "data": {
    "sessionsBeforeCleanup": 28,
    "sessionsAfterCleanup": 25,
    "sessionsRemoved": 3
  }
}
```

### Usage Examples

#### cURL - Dashboard Stats
```bash
curl -X GET "http://localhost:8080/v1/users/dashboard" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### JavaScript/Fetch
```javascript
fetch('/v1/users/dashboard/stats', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('Dashboard stats:', data.data);
});
```

### Error Responses

#### Unauthorized (401)
```json
{
  "status": {
    "code": 401,
    "message": "Unauthorized",
    "errors": {
      "error": "Invalid or expired token"
    }
  },
  "data": null,
  "meta": {
    "serverDateTime": "2025-07-29 10:30:00",
    "requestId": "dashboard-stats-001",
    "sessionId": "no-session"
  }
}
```

#### Forbidden (403)
```json
{
  "status": {
    "code": 403,
    "message": "Access Denied",
    "errors": {
      "error": "Insufficient privileges - Admin role required"
    }
  },
  "data": null,
  "meta": {
    "serverDateTime": "2025-07-29 10:30:00",
    "requestId": "dashboard-stats-001",
    "sessionId": "session-123"
  }
}
```
