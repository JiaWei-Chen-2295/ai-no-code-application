# 小程序界面设计文档

## 1. 概述

本文档描述AI No-Code小程序所有界面的组件需求和业务逻辑，供后续界面设计使用。

## 2. 界面列表

### 2.1 登录页面 (`pages/login/index`)
**功能**：用户登录
**需要组件**：
- Logo图标
- 标题文本
- 账号输入框
- 密码输入框
- 登录按钮
- 注册链接

**业务逻辑**：
- 输入验证：账号和密码不能为空
- 登录成功：跳转首页，显示成功提示
- 登录失败：显示错误信息

**数据**：
- 账号（userAccount）
- 密码（userPassword）

### 2.2 注册页面 (`pages/register/index`)
**功能**：用户注册
**需要组件**：
- 返回按钮
- 标题文本
- 账号输入框
- 邮箱输入框
- 密码输入框
- 确认密码输入框
- 注册按钮
- 登录链接

**业务逻辑**：
- 输入验证：所有字段必填，两次密码一致
- 注册成功：跳转登录页，显示成功提示
- 注册失败：显示错误信息

**数据**：
- 账号（userAccount）
- 邮箱（email）
- 密码（userPassword）
- 确认密码（checkPassword）

### 2.3 首页 (`pages/home/index`)
**功能**：应用列表浏览
**需要组件**：
- 页面标题
- 副标题
- 标签栏（精选/我的应用）
- 应用卡片列表
- 创建应用浮动按钮

**业务逻辑**：
- 标签切换：切换显示精选应用和我的应用
- 应用卡片点击：跳转应用详情页
- 创建按钮：跳转创建应用页
- 下拉刷新：刷新应用列表

**数据**：
- 精选应用列表（AppVO[]）
- 我的应用列表（AppVO[]）

### 2.4 应用详情页面 (`pages/app-detail/index`)
**功能**：查看应用详情和版本历史
**需要组件**：
- 应用封面图
- 应用名称
- 作者信息
- 应用类型标签
- 应用描述
- 版本列表
- 开始对话按钮
- 预览应用按钮

**业务逻辑**：
- 版本选择：点击版本切换当前版本
- 开始对话：跳转聊天页面
- 预览应用：跳转预览页面

**数据**：
- 应用详情（AppVO）
- 版本列表（AppVersionVO[]）

### 2.5 创建应用页面 (`pages/create-app/index`)
**功能**：创建新应用
**需要组件**：
- 页面标题
- 应用名称输入框
- 代码类型选择器
- 应用描述输入框
- 创建按钮

**业务逻辑**：
- 代码类型选择：HTML单页面/Vue项目
- 创建成功：跳转应用详情页
- 创建失败：显示错误提示

**数据**：
- 应用名称（appName）
- 代码类型（codeGenType）
- 应用描述（initPrompt）

### 2.6 聊天页面 (`pages/chat/index`)
**功能**：AI对话生成代码
**需要组件**：
- 应用标题
- 聊天消息列表
- 消息输入框
- 发送按钮

**业务逻辑**：
- 消息发送：流式生成代码
- 历史记录：自动加载聊天历史
- 消息滚动：自动滚动到最新消息

**数据**：
- 应用ID（appId）
- 聊天消息（Message[]）
- 用户输入（input）

### 2.7 预览页面 (`pages/preview/index`)
**功能**：预览生成的应用
**需要组件**：
- WebView组件

**业务逻辑**：
- 自动加载：根据应用ID和版本加载预览

**数据**：
- 应用ID（appId）
- 版本号（version）

### 2.8 我的页面 (`pages/my/index`)
**功能**：用户个人中心
**需要组件**：
- 用户头像
- 用户昵称
- 用户角色
- 菜单列表
  - 我的应用
  - 设置
  - 关于我们
- 退出登录按钮

**业务逻辑**：
- 菜单点击：跳转对应页面
- 退出登录：清除登录状态，跳转登录页

**数据**：
- 用户信息（LoginUserVO）

### 2.9 设置页面 (`pages/settings/index`)
**功能**：应用设置
**需要组件**：
- 页面标题
- 设置选项列表
  - 账号安全
  - 通知设置
  - 隐私设置
  - 清除缓存
  - 版本信息

**业务逻辑**：
- 选项点击：跳转对应设置页面或执行操作

**数据**：
- 应用版本号
- 缓存大小

### 2.10 关于页面 (`pages/about/index`)
**功能**：关于应用
**需要组件**：
- 应用Logo
- 应用名称
- 版本信息
- 功能介绍
- 联系方式链接
- 隐私政策链接
- 用户协议链接

**业务逻辑**：
- 链接点击：跳转对应页面

**数据**：
- 应用版本号
- 构建时间

### 2.11 用户资料编辑页面 (`pages/profile-edit/index`)
**功能**：编辑用户资料
**需要组件**：
- 页面标题
- 保存按钮
- 头像上传组件
- 昵称输入框
- 简介输入框

**业务逻辑**：
- 头像上传：选择图片并上传
- 保存成功：返回我的页面
- 保存失败：显示错误信息

**数据**：
- 用户头像（userAvatar）
- 用户昵称（userName）
- 用户简介（userProfile）

### 2.12 应用管理页面 (`pages/app-manage/index`)
**功能**：管理员管理所有应用
**需要组件**：
- 搜索框
- 筛选条件选择器
- 应用列表
- 编辑按钮
- 删除按钮
- 分页控件

**业务逻辑**：
- 搜索：按应用名称搜索
- 筛选：按状态、类型筛选
- 编辑：跳转编辑页面
- 删除：确认删除

**数据**：
- 应用列表（AppVO[]）
- 搜索关键词
- 筛选条件

### 2.13 版本管理页面 (`pages/version-manage/index`)
**功能**：管理应用版本
**需要组件**：
- 应用信息展示
- 版本列表
  - 版本号
  - 时间
  - 状态标签
  - 操作按钮
- 部署按钮

**业务逻辑**：
- 版本切换：切换当前版本
- 版本删除：确认删除
- 版本部署：部署选中版本

**数据**：
- 应用ID（appId）
- 版本列表（AppVersionVO[]）

### 2.14 部署管理页面 (`pages/deploy-manage/index`)
**功能**：管理应用部署
**需要组件**：
- 应用信息展示
- 部署状态展示
- 部署地址展示
- 重新部署按钮
- 取消部署按钮
- 复制地址按钮

**业务逻辑**：
- 部署：部署最新版本
- 取消部署：取消当前部署
- 复制地址：复制部署地址

**数据**：
- 应用ID（appId）
- 部署状态
- 部署地址

## 3. 公共组件

### 3.1 应用卡片组件 (`components/AppCard.tsx`)
**功能**：显示应用基本信息
**属性**：
- app: AppVO
- onClick: () => void

**需要元素**：
- 应用封面图
- 应用名称
- 应用描述
- 作者信息
- 时间信息

### 3.2 版本列表组件 (`components/VersionList.tsx`)
**功能**：显示版本历史
**属性**：
- versions: AppVersionVO[]
- currentVersion: string
- onVersionSelect: (version: string) => void

**需要元素**：
- 版本号
- 时间
- 状态标签

### 3.3 聊天气泡组件 (`components/ChatBubble.tsx`)
**功能**：显示聊天消息
**属性**：
- message: Message
- isUser: boolean

**需要元素**：
- 消息内容
- 发送者标识

### 3.4 空状态组件 (`components/EmptyState.tsx`)
**功能**：显示空状态
**属性**：
- title: string
- description?: string
- actionText?: string
- onAction?: () => void

**需要元素**：
- 提示图标
- 标题
- 描述文本
- 操作按钮（可选）

### 3.5 加载状态组件 (`components/LoadingState.tsx`)
**功能**：显示加载状态
**属性**：
- text?: string

**需要元素**：
- 加载动画
- 加载文本（可选）

## 4. 导航结构

### 4.1 底部标签栏
- 首页：`pages/home/index`
- 我的：`pages/my/index`

### 4.2 页面跳转
- 登录 → 注册：`/pages/register/index`
- 登录 → 首页：`/pages/home/index`
- 首页 → 应用详情：`/pages/app-detail/index?id={appId}`
- 首页 → 创建应用：`/pages/create-app/index`
- 应用详情 → 聊天：`/pages/chat/index?appId={appId}`
- 应用详情 → 预览：`/pages/preview/index?appId={appId}&version={version}`
- 我的 → 设置：`/pages/settings/index`
- 我的 → 关于：`/pages/about/index`
- 我的 → 用户资料：`/pages/profile-edit/index`

## 5. 状态管理

### 5.1 用户状态
```typescript
interface UserState {
  user: LoginUserVO | null
  isLoggedIn: boolean
  login: (account: string, password: string) => Promise<void>
  logout: () => Promise<void>
  updateUser: (updates: Partial<LoginUserVO>) => void
}
```

### 5.2 应用状态
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

## 6. API 接口

### 6.1 用户相关
- 登录：`POST /user/login`
- 注册：`POST /user/register`
- 登出：`POST /user/logout`
- 获取当前用户：`GET /user/get/login`
- 更新用户：`POST /user/update/my`

### 6.2 应用相关
- 创建应用：`POST /app/add`
- 更新应用：`POST /app/update/my`
- 删除应用：`POST /app/delete`
- 获取应用详情：`GET /app/get/vo`
- 获取精选应用：`POST /app/featured/list/page/vo`
- 获取我的应用：`POST /app/my/list/page/vo`
- 生成代码：`GET /app/chat/gen/code`
- 部署应用：`POST /app/deploy`
- 获取版本：`GET /app/{appId}/versions`

### 6.3 聊天历史
- 获取聊天历史：`GET /chatHistory/app/{appId}`

## 7. 错误处理

### 7.1 网络错误
- 显示网络错误提示
- 提供重试按钮

### 7.2 业务错误
- 显示具体错误信息
- 引导用户操作

### 7.3 权限错误
- 跳转登录页面
- 显示权限不足提示

## 8. 性能优化

### 8.1 列表优化
- 分页加载
- 虚拟滚动
- 图片懒加载

### 8.2 缓存策略
- 用户信息缓存
- 应用列表缓存
- 聊天历史缓存

---

*本文档描述界面组件需求和业务逻辑，供后续设计使用。*
