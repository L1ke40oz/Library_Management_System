import api from './index'

// 获取所有图书（管理员，分页）
export function adminGetBooks(userId, page = 1, size = 20) {
  return api.get('/admin/books/list', { params: { userId, page, size } })
}

// 获取单本图书详情（含标签）
export function adminGetBook(bookId, userId) {
  return api.get(`/admin/books/${bookId}`, { params: { userId } })
}

// 添加图书
export function adminAddBook(data) {
  return api.post('/admin/books/add', data)
}

// 修改图书
export function adminUpdateBook(data) {
  return api.put('/admin/books/update', data)
}

// 删除图书
export function adminDeleteBook(bookId, userId) {
  return api.delete(`/admin/books/delete/${bookId}`, { params: { userId } })
}

// 搜索图书（管理员，分页）
export function adminSearchBooks(userId, keyword, page = 1, size = 20) {
  return api.get('/admin/books/search', { params: { userId, keyword, page, size } })
}

// ===== 借阅管理 =====

// 获取借阅记录列表（分页，可按状态筛选）
export function adminGetBorrows(userId, page = 1, size = 20, status = '') {
  return api.get('/admin/borrow/list', { params: { userId, page, size, status } })
}

// 搜索借阅记录
export function adminSearchBorrows(userId, keyword, page = 1, size = 20) {
  return api.get('/admin/borrow/search', { params: { userId, keyword, page, size } })
}

// 管理员处理归还
export function adminReturnBook(borrowId, userId) {
  return api.post(`/admin/borrow/return/${borrowId}`, null, { params: { userId } })
}

// 管理员处理续借
export function adminRenewBook(borrowId, userId) {
  return api.post(`/admin/borrow/renew/${borrowId}`, null, { params: { userId } })
}

// 获取逾期列表
export function adminGetOverdue(userId) {
  return api.get('/admin/borrow/overdue', { params: { userId } })
}

// 检查逾期并发送提醒
export function adminCheckOverdue(userId) {
  return api.post('/admin/borrow/check-overdue', null, { params: { userId } })
}

// 管理员发送消息
export function adminSendMessage(data) {
  return api.post('/admin/borrow/send-message', data)
}

// 管理员修改到期日期
export function adminChangeDueDate(data) {
  return api.put('/admin/borrow/change-due-date', data)
}


// ===== 读者管理 =====

// 获取用户列表（分页，可按角色筛选）
export function adminGetUsers(userId, page = 1, size = 20, role = '') {
  return api.get('/admin/users/list', { params: { userId, page, size, role } })
}

// 搜索用户
export function adminSearchUsers(userId, keyword, page = 1, size = 20) {
  return api.get('/admin/users/search', { params: { userId, keyword, page, size } })
}

// 获取用户详情
export function adminGetUser(targetId, userId) {
  return api.get(`/admin/users/${targetId}`, { params: { userId } })
}

// 获取用户借阅记录
export function adminGetUserBorrows(targetId, userId) {
  return api.get(`/admin/users/${targetId}/borrows`, { params: { userId } })
}

// 创建用户
export function adminAddUser(data) {
  return api.post('/admin/users/add', data)
}

// 修改用户信息
export function adminUpdateUser(data) {
  return api.put('/admin/users/update', data)
}

// 重置用户密码
export function adminResetPassword(data) {
  return api.put('/admin/users/reset-password', data)
}

// 修改用户角色
export function adminUpdateRole(data) {
  return api.put('/admin/users/role', data)
}

// 删除用户
export function adminDeleteUser(targetId, userId) {
  return api.delete(`/admin/users/delete/${targetId}`, { params: { userId } })
}
