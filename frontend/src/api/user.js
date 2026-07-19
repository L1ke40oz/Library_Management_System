import api from './index'

export function getUserInfo(userId) {
  return api.get('/user/info', { params: { userId } })
}

export function updateUser(data) {
  return api.put('/user/update', data)
}

export function changePassword(data) {
  return api.put('/user/change-password', data)
}

// 验证登录状态是否有效
export function checkLoginStatus(userId) {
  return api.get('/user/info', { params: { userId } })
}
