# App Settings API Documentation

## Overview
The App Settings API provides CRUD operations for managing application settings with internationalization support (English and Chinese).

## Base URL
```
/v1/bm/app-settings
```

## Authentication
- Most endpoints require `ROLE_ADMIN` or `ROLE_SUPER_ADMIN` authorization
- Public endpoints are accessible to all authenticated users

## Data Model

### AppSetting Entity
```json
{
  "id": "string (UUID)",
  "name": "string (max 50 chars)",
  "value": "string (max 500 chars)",
  "lang": "EN | ZH",
  "isSystem": "boolean",
  "isPublic": "boolean",
  "createdAt": "datetime",
  "createdBy": "string (UUID)",
  "updatedAt": "datetime",
  "updatedBy": "string (UUID)"
}
```

### Language Enum
- `EN` - English
- `ZH` - Chinese (Simplified)

## API Endpoints

### 1. Get All Settings (Paginated)
```http
GET /v1/bm/app-settings
```

**Parameters:**
- `searchTerm` (optional): Search in name or value
- `lang` (optional): Filter by language (`EN` or `ZH`)
- `isPublic` (optional): Filter by public visibility
- `isSystem` (optional): Filter by system settings
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort criteria (e.g., `name,asc`)

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

**Response:**
```json
{
  "status": {
    "code": 200,
    "message": "App settings retrieved successfully"
  },
  "data": {
    "content": [/* AppSetting objects */],
    "pageable": {/* pagination info */},
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### 2. Get Setting by ID
```http
GET /v1/bm/app-settings/{id}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

### 3. Get Setting by Name and Language
```http
GET /v1/bm/app-settings/by-name?name={name}&lang={lang}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

### 4. Get All Settings for a Name (All Languages)
```http
GET /v1/bm/app-settings/by-name/{name}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

### 5. Get Settings by Language
```http
GET /v1/bm/app-settings/by-language/{lang}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

### 6. Get Public Settings (All Languages)
```http
GET /v1/bm/app-settings/public
```

**Authorization:** Any authenticated user

**Description:** Returns settings where `isPublic = true`

### 7. Get Public Settings by Language
```http
GET /v1/bm/app-settings/public/{lang}
```

**Authorization:** Any authenticated user

### 8. Get User-Editable Settings
```http
GET /v1/bm/app-settings/user-editable
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

**Description:** Returns settings where `isSystem = false`

### 9. Get User-Editable Settings by Language
```http
GET /v1/bm/app-settings/user-editable/{lang}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

### 10. Get Setting Value
```http
GET /v1/bm/app-settings/value?name={name}&lang={lang}&defaultValue={default}
```

**Parameters:**
- `name`: Setting name
- `lang`: Language
- `defaultValue` (optional): Default value if setting not found

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

**Response:**
```json
{
  "status": {
    "code": 200,
    "message": "App setting value retrieved successfully"
  },
  "data": "setting_value_string"
}
```

### 11. Get Distinct Setting Names
```http
GET /v1/bm/app-settings/names
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

### 12. Get Statistics
```http
GET /v1/bm/app-settings/statistics
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

**Response:**
```json
{
  "status": {
    "code": 200,
    "message": "Setting statistics retrieved successfully"
  },
  "data": {
    "totalSettings": 100,
    "englishSettings": 50,
    "chineseSettings": 50,
    "publicSettings": 20,
    "systemSettings": 30,
    "userEditableSettings": 70,
    "distinctNames": 50,
    "settingNames": ["app_name", "app_version", ...]
  }
}
```

### 13. Create Setting
```http
POST /v1/bm/app-settings
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

**Request Body:**
```json
{
  "name": "app_name",
  "value": "My Application",
  "lang": "EN",
  "isSystem": false,
  "isPublic": true
}
```

**Validation:**
- `name`: Required, max 50 characters
- `value`: Optional, max 500 characters
- `lang`: Required
- `isSystem`: Optional, defaults to `false`
- `isPublic`: Optional, defaults to `false`

### 14. Update Setting
```http
PUT /v1/bm/app-settings/{id}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

**Request Body:**
```json
{
  "value": "Updated Application Name",
  "lang": "EN",
  "isPublic": false
}
```

**Notes:**
- System settings (`isSystem = true`) cannot be modified
- All fields are optional
- Changing language requires ensuring no duplicate name+lang combination

### 15. Update Setting Value by Name and Language
```http
PUT /v1/bm/app-settings/value?name={name}&lang={lang}&value={newValue}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

**Notes:**
- System settings cannot be modified
- Convenient way to update just the value

### 16. Delete Setting
```http
DELETE /v1/bm/app-settings/{id}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

**Notes:**
- System settings cannot be deleted

### 17. Delete Setting by Name and Language
```http
DELETE /v1/bm/app-settings/by-name?name={name}&lang={lang}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

### 18. Check if Setting Exists
```http
GET /v1/bm/app-settings/exists?name={name}&lang={lang}
```

**Authorization:** `ROLE_ADMIN` or `ROLE_SUPER_ADMIN`

**Response:**
```json
{
  "status": {
    "code": 200,
    "message": "Setting existence check completed"
  },
  "data": true
}
```

## Example Usage

### Creating App Settings in Both Languages

```bash
# Create English setting
curl -X POST "http://localhost:8080/v1/bm/app-settings" \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "app_name",
    "value": "GJP Blog System",
    "lang": "EN",
    "isSystem": false,
    "isPublic": true
  }'

# Create Chinese setting
curl -X POST "http://localhost:8080/v1/bm/app-settings" \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "app_name",
    "value": "GJP博客系统",
    "lang": "ZH",
    "isSystem": false,
    "isPublic": true
  }'
```

### Getting Public Settings for Frontend

```bash
# Get all public settings
curl -X GET "http://localhost:8080/v1/bm/app-settings/public" \
  -H "Authorization: Bearer {jwt_token}"

# Get public English settings only
curl -X GET "http://localhost:8080/v1/bm/app-settings/public/EN" \
  -H "Authorization: Bearer {jwt_token}"
```

### Searching and Filtering

```bash
# Search for settings containing "app"
curl -X GET "http://localhost:8080/v1/bm/app-settings?searchTerm=app&size=10" \
  -H "Authorization: Bearer {jwt_token}"

# Get only Chinese settings
curl -X GET "http://localhost:8080/v1/bm/app-settings?lang=ZH" \
  -H "Authorization: Bearer {jwt_token}"

# Get only user-editable, public settings
curl -X GET "http://localhost:8080/v1/bm/app-settings?isSystem=false&isPublic=true" \
  -H "Authorization: Bearer {jwt_token}"
```

## Error Responses

### Validation Error (400)
```json
{
  "status": {
    "code": 400,
    "message": "Validation error"
  },
  "data": {
    "name": "Setting name is required",
    "lang": "Language is required"
  }
}
```

### Business Logic Error (400)
```json
{
  "status": {
    "code": 400,
    "message": "Business error"
  },
  "data": {
    "error": "System setting cannot be modified: app_version"
  }
}
```

### Not Found (404)
```json
{
  "status": {
    "code": 404,
    "message": "Resource not found"
  },
  "data": {
    "error": "App setting not found with id: 123e4567-e89b-12d3-a456-426614174000"
  }
}
```

### Access Denied (403)
```json
{
  "status": {
    "code": 403,
    "message": "Access denied"
  },
  "data": {
    "error": "You don't have permission to access this resource"
  }
}
```

## Database Schema

The API uses the `bm_app_settings` table with the following structure:

```sql
CREATE TABLE IF NOT EXISTS bm_app_settings (
    id CHAR(36) NOT NULL COMMENT 'Primary Key (UUID)',
    name VARCHAR(50) NOT NULL COMMENT 'Setting name (unique identifier)',
    value VARCHAR(500) DEFAULT NULL COMMENT 'Setting value',
    lang ENUM('EN', 'ZH') NOT NULL DEFAULT 'EN' COMMENT 'Language for the setting',
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'System config (not user editable)',
    is_public BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Public config (visible to non-admin users)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    created_by CHAR(36) DEFAULT NULL COMMENT 'Created by user ID',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    updated_by CHAR(36) DEFAULT NULL COMMENT 'Last updated by user ID',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_bm_app_settings_name_lang (name, lang),
    KEY idx_bm_app_settings_is_public (is_public),
    KEY idx_bm_app_settings_is_system (is_system),
    KEY idx_bm_app_settings_created_by (created_by),
    KEY idx_bm_app_settings_updated_by (updated_by),
    
    CONSTRAINT fk_bm_app_settings_created_by FOREIGN KEY (created_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_bm_app_settings_updated_by FOREIGN KEY (updated_by) REFERENCES auth_users (id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Application settings with internationalization support';
```

## Best Practices

1. **System Settings**: Mark critical settings as `isSystem = true` to prevent accidental modification
2. **Public Settings**: Use `isPublic = true` for settings that should be visible to all users (e.g., app name, version)
3. **Internationalization**: Always create settings in both English and Chinese when applicable
4. **Naming Convention**: Use snake_case for setting names (e.g., `app_name`, `user_max_login_attempts`)
5. **Value Storage**: Store simple values as strings; for complex data, consider JSON format in the value field
