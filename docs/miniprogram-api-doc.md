# AI No-Code Application API Documentation

## 1. Business Logic Overview

This is an AI-powered no-code application platform that allows users to create and deploy applications through conversation. The core features include:

- **App Creation**: Users can create applications by describing their requirements in natural language
- **AI Code Generation**: The AI generates application code (HTML/Vue projects) based on user conversations
- **Live Preview**: Real-time preview of generated applications
- **App Deployment**: Users can deploy their applications to a public URL
- **Version Management**: Support for multiple versions of an application
- **Chat History**: Persistent chat history for each application

### User Roles

- **Regular User**: Create, edit, delete their own applications; chat with AI to generate code
- **Admin**: Manage all applications, view all chat histories

---

## 2. API Reference

### 2.1 User APIs (`/user/*`)

| API | Method | Description | Request Body |
|-----|--------|-------------|--------------|
| `/user/register` | POST | User registration | `{ userAccount, email, userPassword, checkPassword }` |
| `/user/login` | POST | User login | `{ userAccount, userPassword }` |
| `/user/logout` | POST | User logout | - |
| `/user/get/login` | GET | Get current logged-in user | - |
| `/user/get/vo` | GET | Get user info by ID | `{ id }` |
| `/user/update/my` | POST | Update current user profile | `{ userName, userAvatar, userProfile }` |
| `/user/update` | POST | Admin update user | `{ id, userName, userAvatar, userProfile, userRole }` |
| `/user/list/page/vo` | POST | Get user list (paginated) | `{ current, pageSize, ...filters }` |
| `/user/add` | POST | Admin add user | `{ userName, userAccount, userAvatar, email, userRole }` |
| `/user/delete` | POST | Admin delete user | `{ deleteId }` |

### 2.2 App APIs (`/app/*`)

| API | Method | Description | Request Body/Params |
|-----|--------|-------------|---------------------|
| `/app/add` | POST | Create new app | `{ appName, initPrompt, codeGenType }` |
| `/app/update/my` | POST | Update my app | `{ id, appName }` |
| `/app/update/admin` | POST | Admin update app | `{ id, appName, cover, priority }` |
| `/app/delete` | POST | Delete my app | `{ deleteId }` |
| `/app/delete/admin` | POST | Admin delete app | `{ deleteId }` |
| `/app/get/vo` | GET | Get app details | `{ id }` |
| `/app/get/admin` | GET | Admin get app | `{ id }` |
| `/app/my/list/page/vo` | POST | Get my app list (paginated) | `{ current, pageSize, ...filters }` |
| `/app/featured/list/page/vo` | POST | Get featured app list (paginated) | `{ current, pageSize, ...filters }` |
| `/app/list/page/admin` | POST | Admin get all apps (paginated) | `{ current, pageSize, ...filters }` |
| `/app/chat/gen/code` | GET | Chat with AI to generate code (SSE stream) | `{ appId, message }` |
| `/app/deploy` | POST | Deploy app | `{ appId, version }` |
| `/app/{appId}/versions` | GET | Get app versions | `{ appId }` |
| `/app/{appId}/versions/{version}` | GET | Get specific version | `{ appId, version }` |
| `/app/{appId}/versions/{version}` | DELETE | Delete app version | `{ appId, version }` |

#### Code Generation Types (`codeGenType`)
- `html`: Single HTML file
- `vueProject`: Vue.js project

### 2.3 Chat History APIs (`/chatHistory/*`)

| API | Method | Description | Request Body/Params |
|-----|--------|-------------|---------------------|
| `/chatHistory/app/{appId}` | GET | Get chat history for app | `{ appId, pageSize, lastCreateTime }` |
| `/chatHistory/admin/list/page/vo` | POST | Admin get all chat history (paginated) | `{ current, pageSize, appId, message, messageType, userId, lastCreateTime }` |

### 2.4 Static Resource APIs (`/static/*`)

| API | Method | Description | Params |
|-----|--------|-------------|--------|
| `/static/{deployKey}/**` | GET | Serve static deployed app | `{ deployKey }` |
| `/static/preview/{appId}/**` | GET | Preview latest version | `{ appId }` |
| `/static/preview/{appId}/{version}/**` | GET | Preview specific version | `{ appId, version }` |

### 2.5 Health Check API

| API | Method | Description |
|-----|--------|-------------|
| `/health` | GET | Server health check |

---

## 3. Data Models

### User
```
{
  id, account, password, email, name, avatar, profile, role,
  createTime, updateTime, isDelete
}
```

### UserVO (View Object)
```
{
  id, account, email, name, avatar, profile, role,
  createTime, updateTime
}
```

### LoginUserVO
```
{
  id, userName, userAvatar, userProfile, userRole,
  createTime, updateTime
}
```

### App
```
{
  id, appName, cover, initPrompt, codeGenType, deployKey,
  deployedTime, priority, userId, editTime, createTime,
  updateTime, isDelete
}
```

### AppVO
```
{
  id, appName, cover, initPrompt, codeGenType, deployKey,
  deployedTime, priority, userId, editTime, createTime,
  updateTime, user: UserVO
}
```

### AppVersionVO
```
{
  version, createTime, message, isDeployed, fileSize,
  previewUrl, chatHistoryId
}
```

### ChatHistory
```
{
  id, message, messageType, appId, userId, isCode,
  codeVersion, createTime, updateTime, isDelete
}
```

---

## 4. Response Format

All APIs return a standardized response:

```typescript
// Success response
{
  code: 0,
  data: <T>,
  message: "success"
}

// Paginated response
{
  code: 0,
  data: {
    records: [],
    pageNumber: 1,
    pageSize: 10,
    totalPage: 5,
    totalRow: 50
  },
  message: "success"
}

// Error response
{
  code: <errorCode>,
  data: null,
  message: "error message"
}
```

---

## 5. Authentication

- Most APIs require authentication via session/cookie
- Admin APIs require `userRole: "admin"`
- Use `/user/login` to login and `/user/logout` to logout
- Get current user via `/user/get/login`

---

## 6. Usage Flow

### Create and Generate App
1. Call `/user/login` to authenticate
2. Call `/app/add` to create a new app
3. Navigate to chat page (`/app/chat/:id`)
4. Call `/app/chat/gen/code?appId=xxx&message=xxx` with SSE to stream AI responses
5. Use `/static/preview/{appId}/**` to preview the generated app

### Deploy App
1. Ensure app has generated code (at least 2 chat rounds)
2. Call `/app/deploy` with `{ appId, version }`
3. Access deployed app via `/static/{deployKey}/**`