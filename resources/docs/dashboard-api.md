# Dashboard API Documentation

## Get Dashboard Statistics

**Endpoint:** `GET /v1/users/dashboard/stats`

**Authorization:** Required - Admin or Super Admin role

**Description:** Retrieves comprehensive user statistics for dashboard display.

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
    "activeSessions": 0
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
6. **activeSessions**: Number of currently active user sessions (placeholder - requires session management implementation)

### Usage Examples

#### cURL
```bash
curl -X GET "http://localhost:8080/v1/users/dashboard/stats" \
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
