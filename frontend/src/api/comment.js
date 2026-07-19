import api from './index'

export function getComments(bookId) {
  return api.get(`/comments/${bookId}`)
}

export function getLatestComments(bookId, limit = 5) {
  return api.get(`/comments/${bookId}/latest`, { params: { limit } })
}

export function addComment(data) {
  return api.post('/comments', data)
}
