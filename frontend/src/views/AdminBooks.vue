<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1>📚 管理后台</h1>
      <div class="header-actions">
        <router-link to="/admin" class="btn-nav active">图书管理</router-link>
        <router-link to="/admin/borrows" class="btn-nav">借阅管理</router-link>
        <router-link to="/admin/users" class="btn-nav">读者管理</router-link>
        <span class="admin-user">管理员：{{ username }}</span>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="admin-toolbar">
      <div class="search-box">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索书名、作者、ISBN..."
          @keyup.enter="handleSearch"
        />
        <button class="btn-search" @click="handleSearch">搜索</button>
        <button class="btn-reset" @click="resetSearch">重置</button>
      </div>
      <button class="btn-add" @click="openAddDialog">+ 添加图书</button>
    </div>

    <!-- 图书列表 -->
    <div class="book-table-wrapper">
      <table class="book-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>书名</th>
            <th>作者</th>
            <th>出版社</th>
            <th>ISBN</th>
            <th>分类</th>
            <th>馆藏</th>
            <th>借出</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in books" :key="book.bookId">
            <td>{{ book.bookId }}</td>
            <td class="td-name">{{ book.bookName }}</td>
            <td>{{ book.author }}</td>
            <td>{{ book.publisher }}</td>
            <td>{{ book.isbn }}</td>
            <td>{{ book.category }}</td>
            <td>{{ book.totalCount }}</td>
            <td>{{ book.borrowCount }}</td>
            <td class="td-actions">
              <button class="btn-edit" @click="openEditDialog(book)">编辑</button>
              <button class="btn-delete" @click="handleDelete(book)">删除</button>
            </td>
          </tr>
          <tr v-if="books.length === 0">
            <td colspan="9" class="td-empty">暂无图书数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button class="page-btn" :disabled="currentPage <= 1" @click="goToPage(1)">首页</button>
      <button class="page-btn" :disabled="currentPage <= 1" @click="goToPage(currentPage - 1)">上一页</button>
      <button
        v-for="p in displayedPages"
        :key="p"
        class="page-btn"
        :class="{ active: p === currentPage }"
        @click="goToPage(p)"
      >{{ p }}</button>
      <button class="page-btn" :disabled="currentPage >= totalPages" @click="goToPage(currentPage + 1)">下一页</button>
      <button class="page-btn" :disabled="currentPage >= totalPages" @click="goToPage(totalPages)">末页</button>
      <span class="page-info">共 {{ total }} 条，第 {{ currentPage }}/{{ totalPages }} 页</span>
    </div>

    <!-- 添加/编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog-box">
        <h2>{{ isEdit ? '编辑图书' : '添加图书' }}</h2>
        <form class="dialog-form" @submit.prevent="handleSubmit">
          <div class="form-row">
            <label>书名 <span class="required">*</span></label>
            <input v-model="form.bookName" type="text" placeholder="请输入书名" required />
          </div>
          <div class="form-row">
            <label>作者</label>
            <input v-model="form.author" type="text" placeholder="请输入作者" />
          </div>
          <div class="form-row">
            <label>出版社</label>
            <input v-model="form.publisher" type="text" placeholder="请输入出版社" />
          </div>
          <div class="form-row">
            <label>ISBN</label>
            <input v-model="form.isbn" type="text" placeholder="请输入ISBN" />
          </div>
          <div class="form-row">
            <label>分类</label>
            <input v-model="form.category" type="text" placeholder="请输入分类" />
          </div>
          <div class="form-row">
            <label>简介</label>
            <textarea v-model="form.description" placeholder="请输入图书简介" rows="3"></textarea>
          </div>
          <div class="form-row">
            <label>馆藏数量</label>
            <input v-model.number="form.totalCount" type="number" min="0" placeholder="0" />
          </div>
          <div class="form-row">
            <label>标签</label>
            <input v-model="tagsInput" type="text" placeholder="多个标签用逗号分隔，如：文学,经典,小说" />
          </div>

          <p v-if="formError" class="form-error">{{ formError }}</p>

          <div class="dialog-actions">
            <button type="button" class="btn-cancel" @click="closeDialog">取消</button>
            <button type="submit" class="btn-confirm" :disabled="submitting">
              {{ submitting ? '提交中...' : '确认' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 返回前台 -->
    <div class="admin-footer">
      <router-link to="/" class="btn-back">← 返回前台首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminGetBooks, adminAddBook, adminUpdateBook, adminDeleteBook, adminSearchBooks, adminGetBook } from '../api/admin'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '')
const userId = ref(localStorage.getItem('userId'))

const books = ref([])
const searchKeyword = ref('')
const isSearching = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formError = ref('')
const tagsInput = ref('')

// 分页状态
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const totalPages = ref(0)

// 计算显示的页码按钮
const displayedPages = computed(() => {
  const pages = []
  const maxShow = 7
  let start = Math.max(1, currentPage.value - Math.floor(maxShow / 2))
  let end = Math.min(totalPages.value, start + maxShow - 1)
  if (end - start < maxShow - 1) {
    start = Math.max(1, end - maxShow + 1)
  }
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const form = reactive({
  bookId: null,
  bookName: '',
  author: '',
  publisher: '',
  isbn: '',
  category: '',
  description: '',
  totalCount: 0
})

onMounted(() => {
  const role = localStorage.getItem('role')
  if (role !== 'ADMIN') {
    router.push('/login')
    return
  }
  loadBooks()
})

async function loadBooks() {
  try {
    let res
    if (isSearching.value && searchKeyword.value.trim()) {
      res = await adminSearchBooks(userId.value, searchKeyword.value.trim(), currentPage.value, pageSize)
    } else {
      res = await adminGetBooks(userId.value, currentPage.value, pageSize)
    }
    if (res.code === 200) {
      books.value = res.data.list || []
      total.value = res.data.total || 0
      totalPages.value = res.data.totalPages || 0
    }
  } catch (err) {
    console.error('加载图书失败', err)
  }
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadBooks()
}

async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    resetSearch()
    return
  }
  isSearching.value = true
  currentPage.value = 1
  loadBooks()
}

function resetSearch() {
  searchKeyword.value = ''
  isSearching.value = false
  currentPage.value = 1
  loadBooks()
}

function openAddDialog() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(book) {
  isEdit.value = true
  formError.value = ''

  try {
    const res = await adminGetBook(book.bookId, userId.value)
    if (res.code === 200) {
      const data = res.data
      Object.assign(form, {
        bookId: data.book.bookId,
        bookName: data.book.bookName || '',
        author: data.book.author || '',
        publisher: data.book.publisher || '',
        isbn: data.book.isbn || '',
        category: data.book.category || '',
        description: data.book.description || '',
        totalCount: data.book.totalCount || 0
      })
      tagsInput.value = (data.tags || []).join(', ')
    }
  } catch (err) {
    Object.assign(form, {
      bookId: book.bookId,
      bookName: book.bookName || '',
      author: book.author || '',
      publisher: book.publisher || '',
      isbn: book.isbn || '',
      category: book.category || '',
      description: book.description || '',
      totalCount: book.totalCount || 0
    })
    tagsInput.value = ''
  }

  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
  resetForm()
}

function resetForm() {
  Object.assign(form, {
    bookId: null,
    bookName: '',
    author: '',
    publisher: '',
    isbn: '',
    category: '',
    description: '',
    totalCount: 0
  })
  tagsInput.value = ''
  formError.value = ''
}

async function handleSubmit() {
  formError.value = ''
  submitting.value = true

  const tags = tagsInput.value
    .split(/[,，]/)
    .map(t => t.trim())
    .filter(t => t.length > 0)

  const payload = {
    ...form,
    userId: userId.value,
    tags
  }

  try {
    let res
    if (isEdit.value) {
      res = await adminUpdateBook(payload)
    } else {
      res = await adminAddBook(payload)
    }

    if (res.code === 200) {
      closeDialog()
      loadBooks()
    } else {
      formError.value = res.message || '操作失败'
    }
  } catch (err) {
    formError.value = err.message || '操作失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

async function handleDelete(book) {
  if (!confirm(`确定要删除《${book.bookName}》吗？此操作不可恢复。`)) {
    return
  }

  try {
    const res = await adminDeleteBook(book.bookId, userId.value)
    if (res.code === 200) {
      loadBooks()
    } else {
      alert(res.message || '删除失败')
    }
  } catch (err) {
    alert(err.message || '删除失败')
  }
}
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  padding: 32px 48px;
  background: linear-gradient(180deg, #f8f4ec 0%, #f5efe3 100%);
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.admin-header h1 {
  margin: 0;
  font-size: 26px;
  color: var(--color-primary, #2b5377);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-nav {
  height: 34px;
  line-height: 34px;
  padding: 0 16px;
  border-radius: 8px;
  background: #17a2b8;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s;
}

.btn-nav:hover {
  background: #138496;
}

.btn-nav.active {
  background: #0d6efd;
  pointer-events: none;
}

.admin-user {
  color: var(--color-text-light, #6b7b8d);
  font-size: 14px;
}

.admin-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-box input {
  height: 38px;
  width: 280px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid rgba(31, 58, 86, 0.15);
  font-size: 14px;
  background: #fff;
}

.search-box input:focus {
  outline: none;
  border-color: var(--color-primary, #2b5377);
  box-shadow: 0 0 0 2px rgba(43, 83, 119, 0.1);
}

.btn-search, .btn-reset {
  height: 38px;
  padding: 0 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.btn-search {
  background: var(--color-primary, #2b5377);
  color: #fff;
}

.btn-search:hover {
  opacity: 0.9;
}

.btn-reset {
  background: #e8e4dc;
  color: #555;
}

.btn-reset:hover {
  background: #ddd8cf;
}

.btn-add {
  height: 38px;
  padding: 0 20px;
  border-radius: 8px;
  background: #28a745;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-add:hover {
  background: #218838;
}

.book-table-wrapper {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid rgba(31, 58, 86, 0.1);
  background: #fff;
  box-shadow: 0 4px 20px rgba(31, 58, 86, 0.06);
}

.book-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.book-table th {
  background: var(--color-primary, #2b5377);
  color: #fff;
  padding: 12px 14px;
  text-align: left;
  font-weight: 600;
  white-space: nowrap;
}

.book-table td {
  padding: 10px 14px;
  border-bottom: 1px solid #f0ece4;
  color: #333;
}

.book-table tbody tr:hover {
  background: rgba(43, 83, 119, 0.03);
}

.td-name {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.td-actions {
  white-space: nowrap;
}

.td-empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

.btn-edit, .btn-delete {
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 13px;
  border: none;
  cursor: pointer;
  margin-right: 6px;
}

.btn-edit {
  background: #007bff;
  color: #fff;
}

.btn-edit:hover {
  background: #0056b3;
}

.btn-delete {
  background: #dc3545;
  color: #fff;
}

.btn-delete:hover {
  background: #c82333;
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.page-btn {
  min-width: 36px;
  height: 34px;
  padding: 0 10px;
  border-radius: 6px;
  border: 1px solid rgba(31, 58, 86, 0.15);
  background: #fff;
  color: #333;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled):not(.active) {
  border-color: var(--color-primary, #2b5377);
  color: var(--color-primary, #2b5377);
}

.page-btn.active {
  background: var(--color-primary, #2b5377);
  color: #fff;
  border-color: var(--color-primary, #2b5377);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  margin-left: 12px;
  font-size: 13px;
  color: var(--color-text-light, #6b7b8d);
}

/* 对话框 */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-box {
  width: 520px;
  max-height: 85vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.dialog-box h2 {
  margin: 0 0 24px;
  font-size: 20px;
  color: var(--color-primary, #2b5377);
}

.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row label {
  font-size: 13px;
  font-weight: 600;
  color: #555;
}

.required {
  color: #dc3545;
}

.form-row input,
.form-row textarea {
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid rgba(31, 58, 86, 0.15);
  font-size: 14px;
  background: #fafafa;
  transition: border-color 0.2s;
}

.form-row input:focus,
.form-row textarea:focus {
  outline: none;
  border-color: var(--color-primary, #2b5377);
  box-shadow: 0 0 0 2px rgba(43, 83, 119, 0.1);
}

.form-row textarea {
  resize: vertical;
}

.form-error {
  margin: 0;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(220, 53, 69, 0.08);
  color: #dc3545;
  font-size: 13px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.btn-cancel, .btn-confirm {
  height: 40px;
  padding: 0 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.btn-cancel {
  background: #e8e4dc;
  color: #555;
}

.btn-cancel:hover {
  background: #ddd8cf;
}

.btn-confirm {
  background: var(--color-primary, #2b5377);
  color: #fff;
}

.btn-confirm:hover {
  opacity: 0.9;
}

.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.admin-footer {
  margin-top: 32px;
  text-align: center;
}

.btn-back {
  color: var(--color-text-light, #6b7b8d);
  font-size: 14px;
  transition: color 0.2s;
}

.btn-back:hover {
  color: var(--color-primary, #2b5377);
}
</style>
