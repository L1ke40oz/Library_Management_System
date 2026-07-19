import api from './index'

export function searchBooks(keyword) {
  return api.get('/books/search', { params: { keyword } })
}

export function getBookDetail(id) {
  return api.get(`/books/${id}`)
}

export function getBooksByCategory(name) {
  return api.get('/books/category', { params: { name } })
}

export function getCategories() {
  return api.get('/books/categories')
}

export function getBooksByTag(name) {
  return api.get('/books/tag', { params: { name } })
}

export function getRandomBooks(count = 3) {
  return api.get('/books/random', { params: { count } })
}

// 借阅
export function borrowBook(userId, bookId) {
  return api.post(`/borrow/${bookId}`, null, { params: { userId } })
}

export function returnBook(userId, borrowId) {
  return api.post(`/borrow/return/${borrowId}`, null, { params: { userId } })
}

export function getBorrowList(userId) {
  return api.get('/borrow/list', { params: { userId } })
}

// 收藏
export function addFavorite(userId, bookId) {
  return api.post(`/favorite/${bookId}`, null, { params: { userId } })
}

export function removeFavorite(userId, bookId) {
  return api.delete(`/favorite/${bookId}`, { params: { userId } })
}

export function checkFavorite(userId, bookId) {
  return api.get(`/favorite/check/${bookId}`, { params: { userId } })
}

export function getFavoriteList(userId) {
  return api.get('/favorite/list', { params: { userId } })
}
