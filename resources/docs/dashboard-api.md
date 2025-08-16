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

## Audit Logs

### Get Audit Logs

**Endpoint:** `GET /v1/audit`

**Authorization:** Required - Admin or Super Admin role

**Description:** Retrieves audit logs with pagination and filtering support. The page size should be determined by the "Rows per page" setting with a default of 20.

**Enhanced Feature:** The audit system now captures user identification from JWT tokens for every API call:
- `userId`: Unique user identifier (UUID) extracted from JWT token claims
- `username`: Username (subject) extracted from JWT token for display purposes

#### Query Parameters

- `page` (optional): Page number (0-based indexing). Default: 0
- `size` (optional): Number of records per page from "Rows per page" setting. Default: 20
- `sort` (optional): Sort criteria (e.g., `timestamp,desc`). Default: `timestamp,desc`

**Basic Search Parameters:**
- `userId` (optional): Filter by user ID (exact match)
- `username` (optional): Filter by username (partial match, case-insensitive)
- `httpMethod` (optional): Filter by HTTP method (GET, POST, PUT, DELETE, etc.)
- `endpoint` (optional): Filter by endpoint (partial match)
- `result` (optional): Filter by result message (partial match)
- `statusCode` (optional): Filter by HTTP status code (exact match)
- `ipAddress` (optional): Filter by IP address (exact match)

**Duration Parameters:**
- `minDurationMs` (optional): Filter by minimum request duration in milliseconds
- `maxDurationMs` (optional): Filter by maximum request duration in milliseconds

**Date/Time Parameters:**
- `startDate` (optional): Filter from date (YYYY-MM-DD format)
- `endDate` (optional): Filter to date (YYYY-MM-DD format)
- `startTime` (optional): Filter from datetime (ISO 8601 format)
- `endTime` (optional): Filter to datetime (ISO 8601 format)

**Legacy Parameters (backward compatibility):**
- `resultPattern` (optional): Filter by result message pattern (use `result` instead)
- `endpointPattern` (optional): Filter by endpoint pattern (use `endpoint` instead)
- `endDate` (optional): Filter to date (YYYY-MM-DD format)
- `startTime` (optional): Filter from datetime (ISO 8601 format)
- `endTime` (optional): Filter to datetime (ISO 8601 format)

#### Example Requests

**Basic date range search:**
```
GET /v1/audit?page=0&size=20&sort=timestamp,desc&startDate=2025-08-11&endDate=2025-08-12
```

**Search by username and HTTP method:**
```
GET /v1/audit?username=gjpb&httpMethod=POST&page=0&size=20
```

**Search by status code and IP address:**
```
GET /v1/audit?statusCode=200&ipAddress=127.0.0.1&page=0&size=20
```

**Search by endpoint and result:**
```
GET /v1/audit?endpoint=/auth/tokens&result=successful&page=0&size=20
```

**Search by duration range:**
```
GET /v1/audit?minDurationMs=100&maxDurationMs=5000&page=0&size=20
```

**Combined search with multiple criteria:**
```
GET /v1/audit?username=gjpb&httpMethod=POST&statusCode=200&startDate=2025-08-11&endDate=2025-08-12&page=0&size=20
```

#### Response Format
```json
{
  "status": {
    "code": 200,
    "message": "Audit logs retrieved successfully",
    "errors": null
  },
  "data": {
    "content": [
      {
        "id": "ff674f83-5067-49a3-b53f-121b3a05f254",
        "userId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
        "username": "gjpb",
        "httpMethod": "POST",
        "endpoint": "/api/v1/auth/tokens",
        "requestId": "7e3a942f-3af1-4e74-bccf-c052f0a69de8",
        "result": "Token operation successful",
        "statusCode": 200,
        "errorMessage": null,
        "ipAddress": "127.0.0.1",
        "userAgent": "PostmanRuntime/7.45.0",
        "sessionId": "no-session",
        "durationMs": 167,
        "timestamp": "2025-08-11T20:25:01.867514"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "empty": false,
        "unsorted": false,
        "sorted": true
      },
      "offset": 0,
      "unpaged": false,
      "paged": true
    },
    "last": true,
    "totalPages": 1,
    "totalElements": 2,
    "first": true,
    "size": 20,
    "number": 0,
    "sort": {
      "empty": false,
      "unsorted": false,
      "sorted": true
    },
    "numberOfElements": 2,
    "empty": false
  },
  "meta": {
    "serverDateTime": "2025-08-13 14:30:00",
    "requestId": "audit-logs-001",
    "sessionId": "session-123"
  }
}
```

#### Audit Log Fields Explanation

- **id**: Unique identifier for the audit log entry
- **userId**: Unique user ID (UUID) extracted from JWT token `userId` claim
- **username**: Username extracted from JWT token `sub` (subject) claim
- **httpMethod**: HTTP method used (GET, POST, PUT, DELETE, etc.)
- **endpoint**: API endpoint that was accessed
- **requestId**: Unique identifier for the request
- **result**: Result message describing the operation outcome
- **statusCode**: HTTP status code returned
- **errorMessage**: Error details (null for successful operations)
- **ipAddress**: Client IP address
- **userAgent**: Client user agent string
- **sessionId**: Session identifier
- **durationMs**: Request duration in milliseconds
- **timestamp**: When the operation occurred

#### Pagination Settings

- **Default Page Size**: 20 (configurable via "Rows per page" setting)
- **Supported Page Sizes**: 10, 20, 50, 100
- **Default Sort**: `timestamp,desc` (newest first)
- **Supported Sort Fields**: `timestamp`, `username`, `httpMethod`, `endpoint`, `result`, `ipAddress`, `durationMs`

### Usage Examples

#### cURL - Get Recent Audit Logs
```bash
curl -X GET "http://localhost:8080/v1/audit?page=0&size=20&sort=timestamp,desc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### cURL - Filter by Date Range
```bash
curl -X GET "http://localhost:8080/v1/audit?startDate=2025-08-11&endDate=2025-08-12&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### cURL - Filter by Username and HTTP Method
```bash
curl -X GET "http://localhost:8080/v1/audit?username=gjpb&httpMethod=POST&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### cURL - Filter by Status Code and Duration
```bash
curl -X GET "http://localhost:8080/v1/audit?statusCode=200&minDurationMs=100&maxDurationMs=5000&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### cURL - Combined Search with Multiple Criteria
```bash
curl -X GET "http://localhost:8080/v1/audit?username=gjpb&endpoint=/auth/tokens&statusCode=200&startDate=2025-08-11&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### JavaScript/Fetch - Dynamic Page Size with Filters
```javascript
const rowsPerPage = 20; // From "Rows per page" setting
const page = 0;
const searchParams = new URLSearchParams({
  page: page,
  size: rowsPerPage,
  sort: 'timestamp,desc',
  username: 'gjpb',
  httpMethod: 'POST',
  statusCode: '200'
});

fetch(`/v1/audit?${searchParams}`, {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('Audit logs:', data.data);
  console.log(`Total elements: ${data.data.totalElements}`);
  console.log(`Current page size: ${data.data.size}`);
});
```

#### JavaScript/Fetch - Date Range and Duration Search
```javascript
const searchParams = new URLSearchParams({
  startDate: '2025-08-11',
  endDate: '2025-08-12',
  minDurationMs: '100',
  maxDurationMs: '5000',
  page: '0',
  size: '20'
});

fetch(`/v1/audit?${searchParams}`, {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('Filtered audit logs:', data.data);
});
```

### Audit Logs Error Responses

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
    "serverDateTime": "2025-08-13 14:30:00",
    "requestId": "audit-logs-001",
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
    "serverDateTime": "2025-08-13 14:30:00",
    "requestId": "audit-logs-001",
    "sessionId": "session-123"
  }
}
```
