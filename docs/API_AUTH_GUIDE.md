# API Authentication Guide

## Overview

This API uses JWT (JSON Web Token) for authentication. After login, you receive a token that must be included in subsequent API requests.

---

## 1. Register

**POST** `/api/user/register`

Request body:
```json
{
  "userAccount": "your_username",
  "userPassword": "your_password",
  "checkPassword": "your_password",
  "email": "your_email@example.com"
}
```

Response:
```json
{
  "code": 0,
  "data": 1,
  "message": "ok"
}
```

---

## 2. Login

**POST** `/api/user/login`

Request body:
```json
{
  "userAccount": "your_username",
  "userPassword": "your_password"
}
```

Response:
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "userName": "User Name",
    "userAvatar": "avatar_url",
    "userProfile": "profile",
    "userRole": "user",
    "createTime": "2024-01-01T00:00:00.000+00:00",
    "updateTime": "2024-01-01T00:00:00.000+00:00",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDI1OTIwMDB9.xxxxx"
  },
  "message": "ok"
}
```

**Important:** Save the `token` value for subsequent requests.

---

## 3. Authenticate API Requests

Include the token in the `Authorization` header with `Bearer ` prefix:

```
Authorization: Bearer <your_token>
```

### Example with curl

```bash
curl -X GET "http://localhost:8080/api/user/get/login" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Example with JavaScript (fetch)

```javascript
const token = "your_jwt_token_here";

fetch("http://localhost:8080/api/user/get/login", {
  headers: {
    "Authorization": `Bearer ${token}`
  }
})
.then(res => res.json())
.then(data => console.log(data));
```

### Example with Axios

```javascript
import axios from 'axios';

const token = "your_jwt_token_here";

axios.get("http://localhost:8080/api/user/get/login", {
  headers: {
    "Authorization": `Bearer ${token}`
  }
})
.then(res => console.log(res.data));
```

---

## 4. Get Current User Info

**GET** `/api/user/get/login`

Headers:
```
Authorization: Bearer <your_token>
```

Response:
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "userName": "User Name",
    "userAvatar": "avatar_url",
    "userRole": "user"
  },
  "message": "ok"
}
```

---

## 5. Protected Endpoints

Most endpoints require authentication. Some endpoints require admin role.

| Endpoint | Method | Auth Required | Admin Only |
|----------|--------|---------------|------------|
| `/api/user/register` | POST | No | No |
| `/api/user/login` | POST | No | No |
| `/api/user/get/login` | GET | Yes | No |
| `/api/user/add` | POST | Yes | Yes |
| `/api/user/delete` | POST | Yes | Yes |
| `/api/user/update` | POST | Yes | Yes |
| `/api/user/update/my` | POST | Yes | No |
| `/api/app/*` | * | Yes | Varies |
| `/api/chatHistory/*` | * | Yes | Varies |

---

## 6. Token Expiration

- Token validity: **30 days**
- After expiration, login again to get a new token

---

## 7. Error Responses

### Not Logged In (40100)
```json
{
  "code": 40100,
  "data": null,
  "message": "未登录"
}
```

### No Permission (40101)
```json
{
  "code": 40101,
  "data": null,
  "message": "无权限"
}
```

---

## 8. Quick Test Script

```bash
#!/bin/bash
BASE_URL="http://localhost:8080"

# Register
curl -X POST "$BASE_URL/api/user/register" \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"testuser","userPassword":"testpass123","checkPassword":"testpass123","email":"test@test.com"}'

# Login and save token
TOKEN=$(curl -s -X POST "$BASE_URL/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"testuser","userPassword":"testpass123"}' | jq -r '.data.token')

echo "Token: $TOKEN"

# Test authenticated request
curl -X GET "$BASE_URL/api/user/get/login" \
  -H "Authorization: Bearer $TOKEN"
```