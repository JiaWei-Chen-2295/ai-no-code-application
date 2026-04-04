# 小程序界面生成指南

## 目标
生成AI No-Code小程序的所有界面，包括现有界面的优化和新增界面的实现。

## 技术栈
- 框架：Taro (React)
- 语言：TypeScript
- 样式：CSS
- 状态管理：React Context
- 网络请求：Taro.request

## 界面清单

### 1. 登录页面 (`pages/login/index`)
**文件**：`src/pages/login/index.tsx`, `src/pages/login/index.css`
**功能**：用户登录
**组件**：
- 账号输入框
- 密码输入框
- 登录按钮
- 注册链接
**API**：`POST /user/login`
**状态**：loading, error

### 2. 注册页面 (`pages/register/index`)
**文件**：`src/pages/register/index.tsx`, `src/pages/register/index.css`
**功能**：用户注册
**组件**：
- 账号输入框
- 邮箱输入框
- 密码输入框
- 确认密码输入框
- 注册按钮
- 登录链接
**API**：`POST /user/register`
**状态**：loading, error

### 3. 首页 (`pages/home/index`)
**文件**：`src/pages/home/index.tsx`, `src/pages/home/index.css`
**功能**：应用列表浏览
**组件**：
- 标题
- 标签栏（精选/我的应用）
- 应用卡片列表
- 创建按钮
**API**：`POST /app/featured/list/page/vo`, `POST /app/my/list/page/vo`
**状态**：activeTab, featuredApps, myApps, loading, error

### 4. 应用详情页面 (`pages/app-detail/index`)
**文件**：`src/pages/app-detail/index.tsx`, `src/pages/app-detail/index.css`
**功能**：查看应用详情
**组件**：
- 封面图
- 应用信息
- 版本列表
- 操作按钮
**API**：`GET /app/get/vo`, `GET /app/{appId}/versions`
**状态**：app, versions, currentVersion, loading

### 5. 创建应用页面 (`pages/create-app/index`)
**文件**：`src/pages/create-app/index.tsx`, `src/pages/create-app/index.css`
**功能**：创建新应用
**组件**：
- 应用名称输入框
- 代码类型选择器
- 应用描述输入框
- 创建按钮
**API**：`POST /app/add`
**状态**：appName, initPrompt, codeGenType, loading

### 6. 聊天页面 (`pages/chat/index`)
**文件**：`src/pages/chat/index.tsx`, `src/pages/chat/index.css`
**功能**：AI对话生成代码
**组件**：
- 聊天消息列表
- 输入框
- 发送按钮
**API**：`GET /app/chat/gen/code`, `GET /chatHistory/app/{appId}`
**状态**：messages, input, loading, appId

### 7. 预览页面 (`pages/preview/index`)
**文件**：`src/pages/preview/index.tsx`, `src/pages/preview/index.css`
**功能**：预览生成的应用
**组件**：
- WebView
**API**：无（直接加载URL）
**状态**：appId, version

### 8. 我的页面 (`pages/my/index`)
**文件**：`src/pages/my/index.tsx`, `src/pages/my/index.css`
**功能**：用户个人中心
**组件**：
- 用户信息
- 菜单列表
- 退出按钮
**API**：`POST /user/logout`
**状态**：user

### 9. 设置页面 (`pages/settings/index`) - 新增
**文件**：`src/pages/settings/index.tsx`, `src/pages/settings/index.css`
**功能**：应用设置
**组件**：
- 账号安全
- 通知设置
- 隐私设置
- 清除缓存
- 版本信息
**API**：无
**状态**：无

### 10. 关于页面 (`pages/about/index`) - 新增
**文件**：`src/pages/about/index.tsx`, `src/pages/about/index.css`
**功能**：关于应用
**组件**：
- 应用Logo
- 版本信息
- 功能介绍
- 联系方式
**API**：无
**状态**：无

### 11. 用户资料编辑页面 (`pages/profile-edit/index`) - 新增
**文件**：`src/pages/profile-edit/index.tsx`, `src/pages/profile-edit/index.css`
**功能**：编辑用户资料
**组件**：
- 头像上传
- 昵称输入框
- 简介输入框
- 保存按钮
**API**：`POST /user/update/my`
**状态**：userName, userAvatar, userProfile, loading

### 12. 应用管理页面 (`pages/app-manage/index`) - 新增
**文件**：`src/pages/app-manage/index.tsx`, `src/pages/app-manage/index.css`
**功能**：管理员管理应用
**组件**：
- 搜索框
- 筛选条件
- 应用列表
- 分页控件
**API**：`POST /app/list/page/admin`
**状态**：apps, searchKeyword, filters, pagination

### 13. 版本管理页面 (`pages/version-manage/index`) - 新增
**文件**：`src/pages/version-manage/index.tsx`, `src/pages/version-manage/index.css`
**功能**：管理应用版本
**组件**：
- 应用信息
- 版本列表
- 操作按钮
**API**：`GET /app/{appId}/versions`, `DELETE /app/{appId}/versions/{version}`
**状态**：appId, versions

### 14. 部署管理页面 (`pages/deploy-manage/index`) - 新增
**文件**：`src/pages/deploy-manage/index.tsx`, `src/pages/deploy-manage/index.css`
**功能**：管理应用部署
**组件**：
- 应用信息
- 部署状态
- 部署地址
- 操作按钮
**API**：`POST /app/deploy`
**状态**：appId, deployStatus, deployUrl

## 组件清单

### 1. 应用卡片组件 (`components/AppCard.tsx`)
**功能**：显示应用基本信息
**属性**：
- app: AppVO
- onClick: () => void
**布局**：封面图 + 应用信息

### 2. 版本列表组件 (`components/VersionList.tsx`)
**功能**：显示版本历史
**属性**：
- versions: AppVersionVO[]
- currentVersion: string
- onVersionSelect: (version: string) => void
**布局**：垂直列表

### 3. 聊天气泡组件 (`components/ChatBubble.tsx`)
**功能**：显示聊天消息
**属性**：
- message: Message
- isUser: boolean
**布局**：用户/AI消息样式

### 4. 空状态组件 (`components/EmptyState.tsx`)
**功能**：显示空状态
**属性**：
- title: string
- description?: string
- actionText?: string
- onAction?: () => void
**布局**：图标 + 文本 + 按钮

### 5. 加载状态组件 (`components/LoadingState.tsx`)
**功能**：显示加载状态
**属性**：
- text?: string
**布局**：加载动画 + 文本

## 页面路由

### 底部标签栏
- 首页：`pages/home/index`
- 我的：`pages/my/index`

### 页面跳转
- 登录 → 注册：`/pages/register/index`
- 登录 → 首页：`/pages/home/index`
- 首页 → 应用详情：`/pages/app-detail/index?id={appId}`
- 首页 → 创建应用：`/pages/create-app/index`
- 应用详情 → 聊天：`/pages/chat/index?appId={appId}`
- 应用详情 → 预览：`/pages/preview/index?appId={appId}&version={version}`
- 我的 → 设置：`/pages/settings/index`
- 我的 → 关于：`/pages/about/index`
- 我的 → 用户资料：`/pages/profile-edit/index`

## 数据模型

### 用户相关
```typescript
interface LoginUserVO {
  id: number
  userName: string
  userAvatar: string
  userProfile: string
  userRole: string
  createTime: string
  updateTime: string
}

interface UserVO {
  id: number
  account: string
  email: string
  name: string
  avatar: string
  profile: string
  role: string
  createTime: string
  updateTime: string
}
```

### 应用相关
```typescript
interface AppVO {
  id: number
  appName: string
  cover: string
  initPrompt: string
  codeGenType: string
  deployKey: string
  deployedTime: string
  priority: number
  userId: number
  editTime: string
  createTime: string
  updateTime: string
  user: UserVO
}

interface AppVersionVO {
  version: string
  createTime: string
  message: string
  isDeployed: boolean
  fileSize: number
  previewUrl: string
  chatHistoryId: number
}
```

### 聊天相关
```typescript
interface ChatHistory {
  id: number
  message: string
  messageType: number
  appId: number
  userId: number
  isCode: boolean
  codeVersion: number
  createTime: string
  updateTime: string
  isDelete: number
}

interface Message {
  id: number
  content: string
  isUser: boolean
  isCode?: boolean
}
```

## API 接口

### 用户相关
- 登录：`POST /user/login`
- 注册：`POST /user/register`
- 登出：`POST /user/logout`
- 获取当前用户：`GET /user/get/login`
- 更新用户：`POST /user/update/my`

### 应用相关
- 创建应用：`POST /app/add`
- 更新应用：`POST /app/update/my`
- 删除应用：`POST /app/delete`
- 获取应用详情：`GET /app/get/vo`
- 获取精选应用：`POST /app/featured/list/page/vo`
- 获取我的应用：`POST /app/my/list/page/vo`
- 生成代码：`GET /app/chat/gen/code`
- 部署应用：`POST /app/deploy`
- 获取版本：`GET /app/{appId}/versions`

### 聊天历史
- 获取聊天历史：`GET /chatHistory/app/{appId}`

## 状态管理

### 用户状态
```typescript
interface UserState {
  user: LoginUserVO | null
  isLoggedIn: boolean
  login: (account: string, password: string) => Promise<void>
  logout: () => Promise<void>
  updateUser: (updates: Partial<LoginUserVO>) => void
}
```

### 应用状态
```typescript
interface AppState {
  featuredApps: AppVO[]
  myApps: AppVO[]
  currentApp: AppVO | null
  versions: AppVersionVO[]
  loading: boolean
  error: string | null
}
```

## 实现步骤

### 1. 环境准备
- 安装依赖：`npm install`
- 配置API地址
- 配置小程序AppID

### 2. 状态管理
- 实现用户状态管理
- 实现应用状态管理

### 3. 组件开发
- 创建通用组件
- 创建业务组件

### 4. 页面开发
- 按照界面清单顺序开发
- 每个页面包含：组件、样式、逻辑、API调用

### 5. 路由配置
- 配置页面路由
- 配置底部标签栏

### 6. 测试验证
- 功能测试
- 界面测试
- 性能测试

## 注意事项

### 1. 代码规范
- 使用TypeScript
- 使用函数式组件
- 使用React Hooks
- 使用CSS模块化

### 2. 错误处理

#### 2.1 网络错误
- 显示网络错误提示
- 提供重试按钮

#### 2.2 业务错误
- 显示具体错误信息
- 引导用户操作

#### 2.3 权限错误
- 跳转登录页面
- 显示权限不足提示

### 3. 性能优化

#### 3.1 列表优化
- 分页加载
- 虚拟滚动
- 图片懒加载

#### 3.2 缓存策略
- 用户信息缓存
- 应用列表缓存
- 聊天历史缓存

### 4. 用户体验
- 加载状态提示
- 错误状态提示
- 空状态提示
- 操作反馈

### 5. 安全考虑
- 输入验证
- 权限验证
- 数据加密

## 文件结构

```
src/
├── api/                    # API接口
│   ├── app.ts             # 应用相关API
│   ├── user.ts            # 用户相关API
│   └── chatHistory.ts     # 聊天历史API
├── components/            # 组件
│   ├── AppCard.tsx        # 应用卡片
│   ├── VersionList.tsx    # 版本列表
│   ├── ChatBubble.tsx     # 聊天气泡
│   ├── EmptyState.tsx     # 空状态
│   └── LoadingState.tsx   # 加载状态
├── pages/                 # 页面
│   ├── login/             # 登录页
│   ├── register/          # 注册页
│   ├── home/              # 首页
│   ├── app-detail/        # 应用详情页
│   ├── create-app/        # 创建应用页
│   ├── chat/              # 聊天页
│   ├── preview/           # 预览页
│   ├── my/                # 我的页面
│   ├── settings/          # 设置页
│   ├── about/             # 关于页
│   ├── profile-edit/      # 用户资料编辑页
│   ├── app-manage/        # 应用管理页
│   ├── version-manage/    # 版本管理页
│   └── deploy-manage/     # 部署管理页
├── store/                 # 状态管理
│   └── user.tsx           # 用户状态
├── utils/                 # 工具函数
│   ├── http.ts            # 网络请求
│   └── system.ts          # 系统工具
├── app.tsx                # 应用入口
└── app.config.js          # 应用配置
```

## 生成命令

```bash
# 安装依赖
npm install

# 开发模式
npm run dev:weapp

# 构建
npm run build:weapp
```

---

*本文档供AI生成界面使用，请按照文档要求生成所有界面。*