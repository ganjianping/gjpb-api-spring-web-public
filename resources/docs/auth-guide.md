# Authentication and Authorization Guide

This guide provides an overview of the authentication and authorization system in the GJPB API.

## Key Components

1. **JWT Authentication**: JSON Web Token-based authentication for stateless API security.
2. **Role-Based Access Control (RBAC)**: Authorization based on user roles.
3. **Security Configuration**: Spring Security setup for securing endpoints.
4. **TokenBlacklistService**: Management of invalidated tokens.
5. **RefreshTokenService**: Handling token refresh for extended sessions.

## Authentication Flow

### Registration Process

1. User submits registration data to `/v1/auth/register` endpoint
2. System validates the data and checks for duplicate usernames/emails
3. User is created with default `ROLE_USER` role
4. Response includes basic user info (no tokens at this stage)

### Login Process

1. User submits credentials to `/v1/auth/login` endpoint
2. `CustomAuthenticationProvider` authenticates the credentials
3. Upon successful authentication:
   - Access token (JWT) is generated with a short expiry (typically 15-30 minutes)
   - Refresh token is generated with a longer expiry (typically 7 days)
   - Both tokens are returned to the client

### Token Usage

1. Client includes access token in the `Authorization` header as a Bearer token
2. `JwtAuthenticationFilter` validates the token for each request
3. If valid, `SecurityContextHolder` is populated with authentication details
4. Request proceeds to controller methods with appropriate authorization

### Token Refresh

1. When access token expires, client sends refresh token to `/v1/auth/refresh` endpoint
2. `RefreshTokenService` validates the refresh token
3. If valid, a new access token is generated and returned
4. The original refresh token remains valid until its expiration

### Logout Process

1. User calls `/v1/auth/logout` endpoint with their access token
2. Token is added to the blacklist in `TokenBlacklistService`
3. Refresh token is invalidated
4. Any subsequent requests with the same tokens will be rejected

## Authorization Structure

### Default Roles

The system includes the following predefined roles:

- **ROLE_USER**: Basic access to non-administrative functions
- **ROLE_ADMIN**: Access to administrative functions
- **ROLE_SUPER_ADMIN**: Complete system access with all privileges

### Role Assignment

- Users can have multiple roles
- Roles are stored in the `roles` table
- User-role relationships are stored in the `user_roles` table with additional metadata

### Securing Endpoints

Endpoints are secured using Spring Security annotations:

```java
// Require any authenticated user
@PreAuthorize("isAuthenticated()")
public ResponseEntity<?> getUserProfile() { ... }

// Require specific role
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> adminOperation() { ... }

// Require any of multiple roles
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public ResponseEntity<?> restrictedOperation() { ... }

// Require specific authority (full name with ROLE_ prefix)
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public ResponseEntity<?> superAdminOperation() { ... }

// Complex authorization logic
@PreAuthorize("hasRole('ADMIN') and #userId == authentication.principal.id")
public ResponseEntity<?> updateUser(@PathVariable String userId) { ... }
```

## JWT Configuration

The JWT configuration is stored in `application.yml`:

```yaml
security:
  jwt:
    secret-key: your-secret-key-here
    expiration-ms: 900000  # 15 minutes
    refresh-expiration-ms: 604800000  # 7 days
```

## Security Considerations

### Password Storage

- Passwords are hashed using BCrypt before storage
- Raw passwords are never logged or stored

### Token Security

- Access tokens have short lifespans to minimize risk
- Refresh tokens are stored in the database for validation
- Tokens are blacklisted upon logout

### Failed Authentication

- Failed login attempts are logged and can trigger rate limiting
- Multiple failed attempts may lead to temporary account locking

## Integration Examples

### Frontend Login Example

```javascript
async function login(username, password) {
  try {
    const response = await fetch('/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    
    if (!response.ok) {
      throw new Error('Login failed');
    }
    
    const data = await response.json();
    
    // Store tokens securely
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    
    return data;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
}
```

### Adding Auth Headers to Requests

```javascript
function createAuthHeaders() {
  const token = localStorage.getItem('accessToken');
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };
}

async function fetchProtectedData() {
  try {
    const response = await fetch('/v1/protected-endpoint', {
      headers: createAuthHeaders()
    });
    
    if (response.status === 401) {
      // Token expired, attempt refresh
      const success = await refreshToken();
      if (success) {
        return fetchProtectedData(); // Retry with new token
      } else {
        // Redirect to login
      }
    }
    
    return response.json();
  } catch (error) {
    console.error('API error:', error);
    throw error;
  }
}
```

### Token Refresh Example

```javascript
async function refreshToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) return false;
  
  try {
    const response = await fetch('/v1/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    
    if (!response.ok) {
      // Clear tokens and return to login
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      return false;
    }
    
    const data = await response.json();
    localStorage.setItem('accessToken', data.accessToken);
    return true;
  } catch (error) {
    console.error('Token refresh error:', error);
    return false;
  }
}
```

## Debugging Authentication Issues

### Common Status Codes

- **401 Unauthorized**: Invalid or expired token, or missing authentication
- **403 Forbidden**: Valid authentication but insufficient permissions
- **422 Unprocessable Entity**: Invalid login/registration data

### Troubleshooting Steps

1. Verify token expiration (check if refresh is needed)
2. Confirm proper token format in Authorization header
3. Validate user has the required roles for the endpoint
4. Check logs for authentication failures (using the request ID for correlation)
5. Verify the token hasn't been blacklisted due to logout

### Logging Authentication Events

Authentication events are logged with the following information:

- Request ID for correlation
- Username (success) or attempted username (failure)
- IP address of the client
- Timestamp
- Result (success/failure)
- Failure reason when applicable

## Extending the Auth System

### Adding Custom Roles

1. Insert new roles into the `roles` table
2. Update the `RoleService` to expose the new roles
3. Use the new roles in `@PreAuthorize` annotations

### Custom Authorization Logic

For complex authorization requirements, create custom authorization methods:

```java
@Service
@RequiredArgsConstructor
public class CustomAuthorizationService {
  
  private final UserService userService;
  
  public boolean canAccessResource(String resourceId, Authentication authentication) {
    // Custom logic here
    return true; // or false
  }
}
```

Then use in your security expressions:

```java
@PreAuthorize("@customAuthorizationService.canAccessResource(#resourceId, authentication)")
public ResponseEntity<?> getResource(@PathVariable String resourceId) {
  // ...
}
```
