import api from './index'

export function getMessages(userId) {
  return api.get('/message/list', { params: { userId } })
}

export function getUnreadCount(userId) {
  return api.get('/message/unread-count', { params: { userId } })
}

export function markAsRead(messageId) {
  return api.put(`/message/read/${messageId}`)
}

export function markAllAsRead(userId) {
  return api.put('/message/read-all', null, { params: { userId } })
}
