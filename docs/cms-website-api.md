# CMS Website Management API

## Overview
This API provides CRUD operations for managing websites in the CMS system with multi-language support (English and Chinese).

## Base URL
```
/v1/cms/websites
```

## Authentication
Most endpoints require authentication with appropriate roles:
- **ROLE_SUPER_ADMIN**: Full access to all operations
- **ROLE_ADMIN**: Full access to all operations  
- **ROLE_EDITOR**: Can create, update, activate/deactivate websites
- **Public**: Can access read-only endpoints (get websites, by language, by tag, top websites)

## Endpoints

### 1. Get All Websites (with filtering and pagination)
```http
GET /v1/cms/websites?searchTerm={term}&lang={lang}&isActive={boolean}&page={page}&size={size}
```

**Parameters:**
- `searchTerm` (optional): Search in name, description, or tags
- `lang` (optional): Filter by language (`EN` or `ZH`)
- `isActive` (optional): Filter by status (`true` or `false`)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:**
```json
{
  "status": {
    "code": 200,
    "message": "Websites retrieved successfully"
  },
  "data": {
    "content": [...],
    "totalElements": 50,
    "totalPages": 3,
    "size": 20,
    "number": 0
  }
}
```

### 2. Get Website by ID
```http
GET /v1/cms/websites/{id}
```

### 3. Get Websites by Language
```http
GET /v1/cms/websites/by-language/{lang}?activeOnly={boolean}
```

**Parameters:**
- `lang`: Language (`EN` or `ZH`)
- `activeOnly` (optional): Only active websites (default: `false`)

### 4. Get Websites by Tag
```http
GET /v1/cms/websites/by-tag?tag={tag}&activeOnly={boolean}
```

**Parameters:**
- `tag`: Tag to search for
- `activeOnly` (optional): Only active websites (default: `true`)

### 5. Get Top Websites
```http
GET /v1/cms/websites/top?limit={limit}
```

**Parameters:**
- `limit` (optional): Number of websites to return (default: 10)

### 6. Get Statistics (Admin only)
```http
GET /v1/cms/websites/statistics
```

**Response:**
```json
{
  "status": {
    "code": 200,
    "message": "Statistics retrieved successfully"
  },
  "data": {
    "totalWebsites": 100,
    "activeWebsites": 85,
    "englishWebsites": 60,
    "chineseWebsites": 40
  }
}
```

### 7. Create Website (Editor+ required)
```http
POST /v1/cms/websites
```

**Request Body:**
```json
{
  "name": "Example Website",
  "url": "https://example.com",
  "logoUrl": "https://example.com/logo.png",
  "description": "An example website for demonstration",
  "tags": "tech,programming,tutorial",
  "lang": "EN",
  "displayOrder": 0,
  "isActive": true
}
```

### 8. Update Website (Editor+ required)
```http
PUT /v1/cms/websites/{id}
```

**Request Body:** (all fields optional)
```json
{
  "name": "Updated Website Name",
  "url": "https://updated-example.com",
  "logoUrl": "https://updated-example.com/logo.png",
  "description": "Updated description",
  "tags": "tech,programming,updated",
  "lang": "EN",
  "displayOrder": 1,
  "isActive": true
}
```

### 9. Delete Website (Admin+ required)
```http
DELETE /v1/cms/websites/{id}
```

### 10. Deactivate Website (Editor+ required)
```http
PATCH /v1/cms/websites/{id}/deactivate
```

### 11. Activate Website (Editor+ required)
```http
PATCH /v1/cms/websites/{id}/activate
```

### 12. Bulk Operations (Admin+ required)

#### Bulk Activate
```http
PATCH /v1/cms/websites/bulk/activate
```

**Request Body:**
```json
["id1", "id2", "id3"]
```

#### Bulk Deactivate
```http
PATCH /v1/cms/websites/bulk/deactivate
```

**Request Body:**
```json
["id1", "id2", "id3"]
```

## Data Model

### Website Response
```json
{
  "id": "uuid-string",
  "name": "Website Name",
  "url": "https://example.com",
  "logoUrl": "https://example.com/logo.png",
  "description": "Website description",
  "tags": "tag1,tag2,tag3",
  "lang": "EN",
  "displayOrder": 0,
  "isActive": true,
  "createdAt": "2023-01-01T12:00:00",
  "updatedAt": "2023-01-01T12:00:00",
  "createdBy": "user-id",
  "updatedBy": "user-id"
}
```

### Language Enum
- `EN`: English
- `ZH`: Chinese

## Validation Rules

### Create Website Request
- `name`: Required, max 128 characters
- `url`: Required, max 500 characters, must start with http/https
- `logoUrl`: Optional, max 500 characters
- `description`: Optional, max 1000 characters
- `tags`: Optional, max 500 characters (comma-separated)
- `lang`: Required, must be `EN` or `ZH`
- `displayOrder`: Optional, must be >= 0, default: 0
- `isActive`: Optional, default: true

### Update Website Request
- All fields are optional
- Same validation rules apply when fields are provided
- Unique name constraint per language

## Error Responses

### 400 Bad Request
```json
{
  "status": {
    "code": 400,
    "message": "Validation failed",
    "errors": {
      "name": "Website name is required",
      "url": "Website URL must not exceed 500 characters"
    }
  }
}
```

### 404 Not Found
```json
{
  "status": {
    "code": 404,
    "message": "Website not found with ID: {id}"
  }
}
```

### 409 Conflict
```json
{
  "status": {
    "code": 409,
    "message": "Website with name 'Example' already exists for language 'EN'"
  }
}
```

## Usage Examples

### Get all active English websites
```bash
curl -X GET "/v1/cms/websites?lang=EN&isActive=true"
```

### Create a new website
```bash
curl -X POST "/v1/cms/websites" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "My Blog",
    "url": "https://myblog.com",
    "description": "Personal blog about technology",
    "tags": "blog,tech,personal",
    "lang": "EN"
  }'
```

### Search websites
```bash
curl -X GET "/v1/cms/websites?searchTerm=blog&lang=EN&page=0&size=10"
```

### Get websites by tag
```bash
curl -X GET "/v1/cms/websites/by-tag?tag=tech&activeOnly=true"
```