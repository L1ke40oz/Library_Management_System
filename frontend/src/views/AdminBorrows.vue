<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1>📚 管理后台</h1>
      <div class="header-actions">
        <router-link to="/admin" class="btn-nav">图书管理</router-link>
        <router-link to="/admin/borrows" class="btn-nav active">借阅管理</router-link>
        <router-link to="/admin/users" class="btn-nav">读者管理</router-link>
        <button class="btn-overdue" @click="handleCheckOverdue">检查逾期并提醒</button>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="admin-toolbar">
      <div class="filter-group">
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索用户名或书名..."
            @keyup.enter="handleSearch"
          />
          <button class="btn-search" @click="handleSearch">搜索</button>
        </div>
        <div class="status-filter">
          <button
            v-for="s in statusOptions"
            :key="s.value"
            class="filter-btn"
            :class="{ active: currentStatus === s.value }"
            @click="filterByStatus(s.value)"
          >{{ s.label }}</button>
        </div>
      </div>
    </div>

    <!-- 借阅列表 -->
    <div class="book-table-wrapper">
      <table class="book-table">
        <thead>
          <tr>
            <th>借阅ID</th>
            <th>用户</th>
            <th>书名</th>
            <th>作者</th>
            <th>借阅日期</th>
            <th>到期日期</th>
            <th>归还日期</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in borrows" :key="b.borrowId" :class="{ 'row-overdue': isOverdue(b) }">
            <td>{{ b.borrowId }}</td>
            <td>{{ b.username }}</td>
            <td class="td-name">{{ b.bookName }}</td>
            <td>{{ b.author }}</td>
            <td>{{ b.borrowDate }}</td>
            <td>{{ b.dueDate }}</td>
            <td>{{ b.returnDate || '-' }}</td>
            <td>
              <span class="status-tag" :class="'status-' + b.status.toLowerCase()">
                {{ statusLabel(b.status) }}
              </span>
            </td>
            <td class="td-actions">
              <template v-if="b.status === 'BORROWED' || b.status === 'OVERDUE'">
                <button class="btn-return" @click="handleReturn(b)">归还</button>
                <button class="btn-renew" @click="handleRenew(b)">续借</button>
                <button class="btn-due" @click="openChangeDueDate(b)">改期</button>
              </template>
              <span v-else class="text-muted">已完成</span>
            </td>
          </tr>
          <tr v-if="borrows.length === 0">
            <td colspan="9" class="td-empty">暂无借阅记录</td>
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

    <!-- 返回 -->
    <div class="admin-footer">
      <router-link to="/" class="btn-back">← 返回前台首页</router-link>
    </div>

    <!-- 修改到期日弹窗 -->
    <div v-if="dueDateDialogVisible" class="dialog-overlay" @click.self="dueDateDialogVisible = false">
      <div class="dialog-box">
        <h3>修改借阅日期</h3>
        <p>借阅：《{{ dueDateTarget.bookName }}》 - {{ dueDateTarget.username }}</p>
        <div class="form-group">
          <label>借阅日期</label>
          <input type="date" v-model="newBorrowDate" />
        </div>
        <div class="form-group">
          <label>到期日期</label>
          <input type="date" v-model="newDueDate" />
        </div>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="dueDateDialogVisible = false">取消</button>
          <button class="btn-confirm" @click="handleChangeDueDate">确认修改</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminGetBorrows, adminSearchBorrows, adminReturnBook, adminRenewBook, adminCheckOverdue, adminChangeDueDate } from '../api/admin'

const router = useRouter()
const userId = ref(localStorage.getItem('userId'))

const borrows = ref([])
const searchKeyword = ref('')
const currentStatus = ref('')
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const totalPages = ref(0)

const dueDateDialogVisible = ref(false)
const dueDateTarget = ref({})
const newDueDate = ref('')
const newBorrowDate = ref('')

const statusOptions = [
  { label: '全部', value: '' },
  { label: '借阅中', value: 'BORROWED' },
  { label: '已归还', value: 'RETURNED' },
  { label: '已逾期', value: 'OVERDUE' }
]

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

onMounted(() => {
  const role = localStorage.getItem('role')
  if (role !== 'ADMIN') {
    router.push('/login')
    return
  }
  loadBorrows()
})

async function loadBorrows() {
  try {
    let res
    if (searchKeyword.value.trim()) {
      res = await adminSearchBorrows(userId.value, searchKeyword.value.trim(), currentPage.value, pageSize)
    } else {
      res = await adminGetBorrows(userId.value, currentPage.value, pageSize, currentStatus.value)
    }
    if (res.code === 200) {
      borrows.value = res.data.list || []
      total.value = res.data.total || 0
      totalPages.value = res.data.totalPages || 0
    }
  } catch (err) {
    console.error('加载借阅记录失败', err)
  }
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadBorrows()
}

function handleSearch() {
  currentPage.value = 1
  currentStatus.value = ''
  loadBorrows()
}

function filterByStatus(status) {
  currentStatus.value = status
  searchKeyword.value = ''
  currentPage.value = 1
  loadBorrows()
}

function isOverdue(b) {
  if (b.status === 'OVERDUE') return true
  if (b.status === 'BORROWED' && b.dueDate) {
    return new Date(b.dueDate) < new Date()
  }
  return false
}

function statusLabel(status) {
  switch (status) {
    case 'BORROWED': return '借阅中'
    case 'RETURNED': return '已归还'
    case 'OVERDUE': return '已逾期'
    default: return status
  }
}

async function handleReturn(b) {
  if (!confirm(`确认处理《${b.bookName}》的归还？`)) return
  try {
    const res = await adminReturnBook(b.borrowId, userId.value)
    if (res.code === 200) {
      loadBorrows()
    } else {
      alert(res.message || '操作失败')
    }
  } catch (err) {
    alert(err.message || '操作失败')
  }
}

async function handleRenew(b) {
  if (!confirm(`确认为用户 ${b.username} 续借《${b.bookName}》30天？`)) return
  try {
    const res = await adminRenewBook(b.borrowId, userId.value)
    if (res.code === 200) {
      alert(res.message || '续借成功')
      loadBorrows()
    } else {
      alert(res.message || '操作失败')
    }
  } catch (err) {
    alert(err.message || '操作失败')
  }
}

async function handleCheckOverdue() {
  if (!confirm('确认检查所有逾期记录并向逾期用户发送站内提醒？')) return
  try {
    const res = await adminCheckOverdue(userId.value)
    if (res.code === 200) {
      alert(res.message || '逾期检查完成')
      loadBorrows()
    } else {
      alert(res.message || '操作失败')
    }
  } catch (err) {
    alert(err.message || '操作失败')
  }
}

function openChangeDueDate(b) {
  dueDateTarget.value = b
  newBorrowDate.value = b.borrowDate
  newDueDate.value = b.dueDate
  dueDateDialogVisible.value = true
}

async function handleChangeDueDate() {
  if (!newDueDate.value) {
    alert('请选择到期日期')
    return
  }
  if (newBorrowDate.value && newDueDate.value < newBorrowDate.value) {
    alert('到期日期不能在借阅日期之前')
    return
  }
  try {
    const res = await adminChangeDueDate({
      userId: userId.value,
      borrowId: dueDateTarget.value.borrowId,
      borrowDate: newBorrowDate.value,
      dueDate: newDueDate.value
    })
    if (res.code === 200) {
      alert(res.message || '修改成功')
      dueDateDialogVisible.value = false
      loadBorrows()
    } else {
      alert(res.message || '修改失败')
    }
  } catch (err) {
    alert(err.message || '修改失败')
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
  gap: 12px;
  align-items: center;
}

.btn-nav {
  height: 36px;
  line-height: 36px;
  padding: 0 16px;
  border-radius: 8px;
  background: var(--color-primary, #2b5377);
  color: #fff;
  font-size: 13px;
  text-decoration: none;
  transition: opacity 0.2s;
}

.btn-nav:hover {
  opacity: 0.9;
}

.btn-nav.active {
  background: #0d6efd;
  pointer-events: none;
}

.btn-overdue {
  height: 36px;
  padding: 0 16px;
  border-radius: 8px;
  background: #e67e22;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-overdue:hover {
  background: #d35400;
}

.admin-toolbar {
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  gap: 8px;
}

.search-box input {
  height: 36px;
  width: 240px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid rgba(31, 58, 86, 0.15);
  font-size: 14px;
  background: #fff;
}

.search-box input:focus {
  outline: none;
  border-color: var(--color-primary, #2b5377);
}

.btn-search {
  height: 36px;
  padding: 0 14px;
  border-radius: 8px;
  background: var(--color-primary, #2b5377);
  color: #fff;
  font-size: 13px;
  border: none;
  cursor: pointer;
}

.status-filter {
  display: flex;
  gap: 6px;
}

.filter-btn {
  height: 32px;
  padding: 0 14px;
  border-radius: 6px;
  border: 1px solid rgba(31, 58, 86, 0.15);
  background: #fff;
  color: #555;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn.active {
  background: var(--color-primary, #2b5377);
  color: #fff;
  border-color: var(--color-primary, #2b5377);
}

.filter-btn:hover:not(.active) {
  border-color: var(--color-primary, #2b5377);
  color: var(--color-primary, #2b5377);
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

.row-overdue {
  background: rgba(220, 53, 69, 0.04) !important;
}

.td-name {
  max-width: 180px;
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

.status-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-borrowed {
  background: rgba(0, 123, 255, 0.1);
  color: #007bff;
}

.status-returned {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.status-overdue {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.btn-return {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  border: none;
  cursor: pointer;
  background: #28a745;
  color: #fff;
  margin-right: 6px;
}

.btn-return:hover {
  background: #218838;
}

.btn-renew {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  border: none;
  cursor: pointer;
  background: #17a2b8;
  color: #fff;
}

.btn-renew:hover {
  background: #138496;
}

.btn-due {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  border: none;
  cursor: pointer;
  background: #e67e22;
  color: #fff;
  margin-left: 6px;
}

.btn-due:hover {
  background: #d35400;
}

.text-muted {
  color: #999;
  font-size: 12px;
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

.dialog-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-box {
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
  min-width: 360px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.15);
}

.dialog-box h3 {
  margin: 0 0 12px;
  font-size: 18px;
  color: var(--color-primary, #2b5377);
}

.dialog-box p {
  margin: 0 0 8px;
  font-size: 14px;
  color: #555;
}

.dialog-box .form-group {
  margin-top: 16px;
}

.dialog-box .form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 6px;
  color: var(--color-primary, #2b5377);
}

.dialog-box .form-group input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid rgba(31,58,86,0.15);
  font-size: 14px;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-cancel {
  padding: 8px 20px;
  border-radius: 8px;
  border: 1px solid #ccc;
  background: #fff;
  color: #555;
  font-size: 14px;
  cursor: pointer;
}

.btn-confirm {
  padding: 8px 20px;
  border-radius: 8px;
  border: none;
  background: var(--color-primary, #2b5377);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.btn-confirm:hover {
  opacity: 0.9;
}
</style>
