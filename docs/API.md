# 后端 API 文档

图书管理系统后端 REST API 说明。

## 基础信息

- **Base URL**：`http://localhost:8088/api`
- **数据格式**：请求体与响应体均为 `application/json`
- **跨域**：后端已放开所有来源（`WebConfig`），本地前后端可直接联调。

### 统一响应结构

所有接口（除 `/api/hello`）均返回如下结构（`ApiResponse<T>`）：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | int | 状态码，`200` 表示成功，其余为错误 |
| `message` | string | 提示信息 |
| `data` | any | 业务数据，可能为对象、数组或 `null` |

### 通用错误码

| code | 含义 |
| --- | --- |
| 200 | 成功 |
| 400 | 请求参数错误 / 校验不通过 |
| 401 | 未登录或密码错误 |
| 403 | 权限不足（非管理员访问管理员接口） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 鉴权说明

> 本项目为教学 Demo：登录返回的 `token` 为随机 UUID 占位，后端并未做真正的 token 校验。管理员接口通过请求中携带的 `userId` 反查用户角色来判断权限（`role == "ADMIN"`），普通业务接口通过 `userId` 标识当前用户。前端在 `localStorage` 中保存 `token`、`userId`、`role` 等信息。

---

## 1. 认证 Auth

### 1.1 注册

- **POST** `/api/auth/register`

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名 |
| `email` | string | 是 | 邮箱 |
| `password` | string | 是 | 密码 |

```json
{ "username": "alice", "email": "alice@example.com", "password": "123456" }
```

响应：注册成功 `data` 为 `null`。用户名或邮箱重复返回 `code=400`。

### 1.2 登录

- **POST** `/api/auth/login`

支持使用**用户名或邮箱**登录（含 `@` 视为邮箱）。

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名或邮箱 |
| `password` | string | 是 | 密码 |

成功响应：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "随机UUID",
    "userId": 1,
    "username": "admin",
    "email": "admin@library.com",
    "role": "ADMIN"
  }
}
```

错误：用户不存在 `404`，密码错误 `401`。

---

## 2. 用户 User

### 2.1 获取用户信息

- **GET** `/api/user/info?userId={userId}`

| 参数 | 位置 | 说明 |
| --- | --- | --- |
| `userId` | query | 用户 ID |

响应 `data`：`{ id, username, email, role, createdAt }`。

### 2.2 修改个人信息

- **PUT** `/api/user/update`

请求体：`{ "userId": 1, "username": "newName", "email": "new@example.com" }`。用户名 / 邮箱被占用返回 `400`。

### 2.3 修改密码

- **PUT** `/api/user/change-password`

请求体：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userId` | long | 用户 ID |
| `oldPassword` | string | 原密码 |
| `newPassword` | string | 新密码（≥6 位，且不能与原密码相同） |

原密码错误返回 `401`，校验不通过返回 `400`。

---

## 3. 图书 Book（公开）

### 3.1 图书详情

- **GET** `/api/books/{id}`

路径参数 `id` 为图书编号。响应 `data` 为图书对象（见文末数据结构）。不存在返回 `404`。

### 3.2 搜索图书

- **GET** `/api/books/search?keyword={keyword}`

`keyword` 可选，按书名 / 作者等模糊匹配。响应 `data` 为图书数组。

### 3.3 按分类查询

- **GET** `/api/books/category?name={分类名}`

响应 `data` 为图书数组。

### 3.4 获取所有分类

- **GET** `/api/books/categories`

响应 `data` 为分类名字符串数组。

### 3.5 按标签查询

- **GET** `/api/books/tag?name={标签名}`

如 `新书速递`、`文学经典`、`考试专区`、`馆藏排行`。响应 `data` 为图书数组。

### 3.6 随机图书

- **GET** `/api/books/random?count={数量}`

`count` 默认 3。响应 `data` 为图书数组，用于首页推荐。

---

## 4. 借阅 Borrow

### 4.1 借书

- **POST** `/api/borrow/{bookId}?userId={userId}`

路径参数 `bookId`，query 参数 `userId`。库存不足或已借阅时返回错误信息。

### 4.2 还书

- **POST** `/api/borrow/return/{borrowId}?userId={userId}`

路径参数 `borrowId`（借阅记录 ID），query 参数 `userId`。

### 4.3 续借

- **POST** `/api/borrow/renew/{borrowId}?userId={userId}`

延长借阅记录的到期日期。

### 4.4 我的借阅列表

- **GET** `/api/borrow/list?userId={userId}`

响应 `data` 为借阅记录数组（含联查的 `bookName`、`author`）。

---

## 5. 收藏 Favorite

### 5.1 添加收藏

- **POST** `/api/favorite/{bookId}?userId={userId}`

### 5.2 取消收藏

- **DELETE** `/api/favorite/{bookId}?userId={userId}`

### 5.3 检查是否已收藏

- **GET** `/api/favorite/check/{bookId}?userId={userId}`

响应 `data` 为布尔值。

### 5.4 我的收藏列表

- **GET** `/api/favorite/list?userId={userId}`

响应 `data` 为图书数组。

---

## 6. 评论 Comment

### 6.1 发表评论

- **POST** `/api/comments`

请求体：`{ "userId": 1, "bookId": 2, "content": "很棒的书" }`。内容不能为空且不超过 200 字，否则返回 `400`。

### 6.2 获取图书全部评论

- **GET** `/api/comments/{bookId}`

响应 `data` 为评论数组。

### 6.3 获取图书最新评论

- **GET** `/api/comments/{bookId}/latest?limit={数量}`

`limit` 默认 5。

---

## 7. 站内消息 Message

### 7.1 消息列表

- **GET** `/api/message/list?userId={userId}`

响应 `data` 为消息数组。

### 7.2 未读消息数量

- **GET** `/api/message/unread-count?userId={userId}`

响应 `data` 为 `{ "count": 3 }`。

### 7.3 标记单条已读

- **PUT** `/api/message/read/{messageId}`

### 7.4 标记全部已读

- **PUT** `/api/message/read-all?userId={userId}`

---

## 8. 管理员 - 图书管理 Admin Book

> 以下接口均需管理员身份。请求需带上管理员的 `userId`（GET/DELETE 放 query，POST/PUT 放请求体），后端会校验其 `role == ADMIN`，否则返回 `403`。

### 8.1 图书列表（分页）

- **GET** `/api/admin/books/list?userId={userId}&page={page}&size={size}`

`page` 默认 1，`size` 默认 20。响应 `data` 为分页对象（含 `list`、`total`、`page`、`size`、`totalPages`）。

### 8.2 图书详情（含标签）

- **GET** `/api/admin/books/{bookId}?userId={userId}`

响应 `data`：`{ "book": {...}, "tags": ["标签1", "标签2"] }`。

### 8.3 添加图书

- **POST** `/api/admin/books/add`

请求体：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userId` | long | 管理员 ID |
| `bookName` | string | 书名 |
| `author` | string | 作者 |
| `publisher` | string | 出版社 |
| `isbn` | string | ISBN |
| `category` | string | 分类 |
| `description` | string | 简介 |
| `totalCount` | int | 馆藏数量 |
| `tags` | string[] | 标签数组 |

### 8.4 修改图书

- **PUT** `/api/admin/books/update`

请求体在 8.3 基础上增加 `bookId`。

### 8.5 删除图书

- **DELETE** `/api/admin/books/delete/{bookId}?userId={userId}`

### 8.6 搜索图书（分页）

- **GET** `/api/admin/books/search?userId={userId}&keyword={keyword}&page={page}&size={size}`

按书名 / 作者 / ISBN 搜索，`keyword` 可选。

---

## 9. 管理员 - 借阅管理 Admin Borrow

### 9.1 借阅记录列表（分页）

- **GET** `/api/admin/borrow/list?userId={userId}&page={page}&size={size}&status={status}`

`status` 可选（`BORROWED` / `RETURNED` / `OVERDUE`），不传返回全部。

### 9.2 搜索借阅记录

- **GET** `/api/admin/borrow/search?userId={userId}&keyword={keyword}&page={page}&size={size}`

按用户名或书名搜索。

### 9.3 管理员处理归还

- **POST** `/api/admin/borrow/return/{borrowId}?userId={userId}`

### 9.4 管理员处理续借

- **POST** `/api/admin/borrow/renew/{borrowId}?userId={userId}`

### 9.5 修改到期 / 借阅日期

- **PUT** `/api/admin/borrow/change-due-date`

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | long | 是 | 管理员 ID |
| `borrowId` | long | 是 | 借阅记录 ID |
| `dueDate` | string | 是 | 新到期日期 `yyyy-MM-dd` |
| `borrowDate` | string | 否 | 新借阅日期 `yyyy-MM-dd` |

到期日期不能早于借阅日期，日期格式错误返回 `400`。

### 9.6 逾期列表

- **GET** `/api/admin/borrow/overdue?userId={userId}`

### 9.7 检查逾期并发送站内提醒

- **POST** `/api/admin/borrow/check-overdue?userId={userId}`

扫描逾期记录并向对应用户发送站内消息。

### 9.8 给用户发送消息

- **POST** `/api/admin/borrow/send-message`

请求体：`{ "userId": 1, "targetUserId": 2, "title": "标题", "content": "内容" }`。`userId` 为管理员，`targetUserId` 为接收方。

---

## 10. 管理员 - 用户管理 Admin User

### 10.1 用户列表（分页）

- **GET** `/api/admin/users/list?userId={userId}&page={page}&size={size}&role={role}`

`role` 可选（`USER` / `ADMIN`）筛选。响应 `data` 含 `list`、`total`、`page`、`size`、`totalPages`，密码字段已清空。

### 10.2 搜索用户

- **GET** `/api/admin/users/search?userId={userId}&keyword={keyword}&page={page}&size={size}`

按用户名或邮箱搜索。

### 10.3 用户详情

- **GET** `/api/admin/users/{targetId}?userId={userId}`

### 10.4 查看用户借阅记录

- **GET** `/api/admin/users/{targetId}/borrows?userId={userId}`

### 10.5 创建用户

- **POST** `/api/admin/users/add`

请求体：`{ "userId": 1, "username": "bob", "email": "bob@x.com", "password": "123456", "role": "USER" }`。`role` 缺省为 `USER`。

### 10.6 修改用户信息

- **PUT** `/api/admin/users/update`

请求体：`{ "userId": 1, "targetId": 2, "username": "...", "email": "...", "role": "..." }`。角色变化会同步迁移子表。

### 10.7 重置用户密码

- **PUT** `/api/admin/users/reset-password`

请求体：`{ "userId": 1, "targetId": 2, "newPassword": "654321" }`。

### 10.8 修改用户角色

- **PUT** `/api/admin/users/role`

请求体：`{ "userId": 1, "targetId": 2, "role": "ADMIN" }`。角色仅可为 `USER` / `ADMIN`，不能修改自己的角色。

### 10.9 删除用户

- **DELETE** `/api/admin/users/delete/{targetId}?userId={userId}`

会先清理该用户的借阅、收藏、评论、消息等关联数据，不能删除自己。

---

## 11. 测试接口

### 11.1 Hello

- **GET** `/api/hello`

用于验证后端是否正常运行。直接返回：

```json
{ "code": 200, "message": "成功从后端获取信息！", "data": "Hello from Spring Boot Backend!" }
```

---

## 附录：主要数据结构

### Book（图书）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `bookId` | long | 图书编号 |
| `bookName` | string | 书名 |
| `author` | string | 作者 |
| `publisher` | string | 出版社 |
| `isbn` | string | ISBN |
| `category` | string | 分类 |
| `description` | string | 简介 |
| `totalCount` | int | 馆藏数量 |
| `borrowCount` | int | 已借出数量 |

### Borrow（借阅记录）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `borrowId` | long | 借阅编号 |
| `userId` | long | 用户 ID |
| `bookId` | long | 图书 ID |
| `borrowDate` | date | 借阅日期 |
| `dueDate` | date | 应还日期 |
| `returnDate` | date | 归还日期（未还为 null） |
| `status` | string | `BORROWED` / `RETURNED` / `OVERDUE` |
| `bookName` | string | 联查字段：书名 |
| `author` | string | 联查字段：作者 |
| `username` | string | 联查字段：借阅人 |

### User（用户）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | long | 用户 ID |
| `username` | string | 用户名 |
| `email` | string | 邮箱 |
| `role` | string | `USER` / `ADMIN` |
| `createdAt` | datetime | 创建时间 |

### Message（站内消息）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `messageId` | long | 消息编号 |
| `userId` | long | 接收用户 ID |
| `title` | string | 标题 |
| `content` | string | 内容 |
| `type` | string | `SYSTEM` / `OVERDUE` / `NOTICE` |
| `isRead` | int | 0 未读 / 1 已读 |
| `createdAt` | datetime | 创建时间 |
