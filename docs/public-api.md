# Public API Documentation

## Overview
Public APIs are accessible without authentication and don't require JWT tokens. These endpoints expose limited, non-sensitive data that can be accessed by anyone.

## Base URL
```
/open/v1
```

## Authentication
**No authentication required** - All endpoints in this section are publicly accessible.

---

## App Settings Public API

### 1. Get All Public App Settings
Retrieve all app settings marked as public. Only returns `name`, `value`, and `lang` fields.

```http
GET /open/v1/app-settings
```

**Response:**
```json
{
  "status": {
    "code": 200,
    "message": "Public app settings retrieved successfully"
  },
  "data": [
    {
      "name": "app_name",
      "value": "My Blog",
      "lang": "EN"
    },
    {
      "name": "app_name",
      "value": "我的博客",
      "lang": "ZH"
    },
    {
      "name": "app_description",
      "value": "A modern blog system",
      "lang": "EN"
    }
  ],
  "meta": {
    "serverDateTime": "2025-10-05 12:00:00",
    "requestId": "req-123",
    "sessionId": "session-456"
  }
}
```

---

## Data Model

### OpenAppSettingDto
```json
{
  "name": "string",        // Setting name
  "value": "string",       // Setting value
  "lang": "string"         // Language code (EN or ZH)
}
```

---

## Security & Privacy

### What's Exposed
- Only settings marked as `isPublic = true` in the database
- Limited fields: `name`, `value`, `lang` only
- No sensitive information like timestamps, user IDs, or system flags

### What's NOT Exposed
- Settings marked as `isPublic = false`
- System settings
- Audit information (created_by, updated_by, timestamps)
- Internal identifiers (IDs)

---

## Usage Examples

### cURL Example
```bash
curl -X GET "http://localhost:8081/api/open/v1/app-settings"
```

### JavaScript/Fetch Example
```javascript
// Get all public settings
fetch('http://localhost:8081/api/open/v1/app-settings')
  .then(response => response.json())
  .then(data => {
    console.log('Public settings:', data.data);
  });
```

### React Example
```javascript
import { useEffect, useState } from 'react';

function AppSettings() {
  const [settings, setSettings] = useState([]);
  
  useEffect(() => {
    fetch('http://localhost:8081/api/open/v1/app-settings')
      .then(res => res.json())
      .then(data => setSettings(data.data))
      .catch(err => console.error(err));
  }, []);
  
  return (
    <div>
      {settings.map(setting => (
        <div key={`${setting.name}-${setting.lang}`}>
          <strong>{setting.name} ({setting.lang}):</strong> {setting.value}
        </div>
      ))}
    </div>
  );
}
```

---

## CORS Configuration
Public endpoints support CORS for the following origins:
- https://ganjianping.com
- https://www.ganjianping.com
- http://localhost:8081
- http://localhost:3000

---

## Rate Limiting
Currently, no rate limiting is applied to public endpoints. Consider implementing rate limiting for production environments to prevent abuse.

---

## Notes
- All public endpoints return data in JSON format
- Standard HTTP status codes are used (200 for success, etc.)
- All responses follow the standard `ApiResponse` structure
- No pagination is currently implemented (returns all matching records)
- Settings are ordered by name (ascending) and language for consistency
- The `/open/v1` prefix is used for all open (public) API endpoints
