<template>
  <div class="admin-page">
    <div class="admin-header">
      <h1>📚 管理后台</h1>
      <div class="header-actions">
        <router-link to="/admin" class="btn-nav">图书管理</router-link>
        <router-link to="/admin/borrows" class="btn-nav">借阅管理</router-link>
        <router-link to="/admin/users" class="btn-nav active">读者管理</router-link>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="admin-toolbar">
      <div class="filter-group">
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索用户名或邮箱..."
            @keyup.enter="handleSearch"
          />
          <button class="btn-search" @click="handleSearch">搜索</button>
        </div>
        <div class="status-filter">
          <button
            v-for="r in roleOptions"
            :key="r.value"
            class="filter-btn"
            :class="{ active: currentRole === r.value }"
            @click="filterByRole(r.value)"
          >{{ r.label }}</button>
        </div>
      </div>
      <button class="btn-add" @click="openAddDialog">+ 创建用户</button>
    </div>

    <!-- 用户列表 -->
    <div class="book-table-wrapper">
      <table class="book-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.email }}</td>
            <td>
              <span class="role-tag" :class="'role-' + u.role.toLowerCase()">
                {{ u.role === 'ADMIN' ? '管理员' : '读者' }}
              </span>
            </td>
            <td>{{ formatDate(u.createdAt) }}</td>
            <td class="td-actions">
              <button class="btn-edit" @click="openEditDialog(u)">编辑</button>
              <button class="btn-role" @click="toggleRole(u)">
                {{ u.role === 'ADMIN' ? '降为读者' : '升为管理员' }}
              </button>
              <button class="btn-pwd" @click="openResetPwd(u)">重置密码</button>
              <button class="btn-borrow" @click="viewBorrows(u)">借阅记录</button>
              <button class="btn-delete" @click="handleDelete(u)">删除</button>
            </td>
          </tr>
          <tr v-if="users.length === 0">
            <td colspan="6" class="td-empty">暂无用户数据</td>
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

    <!-- 创建/编辑用户对话框 -->
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="closeDialog">
      <div class="dialog-box">
        <h2>{{ isEdit ? '编辑用户' : '创建用户' }}</h2>
        <form class="dialog-form" @submit.prevent="handleSubmit">
          <div class="form-row">
            <label>用户名 <span class="required">*</span></label>
            <input v-model="form.username" type="text" placeholder="请输入用户名" required />
          </div>
          <div class="form-row">
            <label>邮箱 <span class="required">*</span></label>
            <input v-model="form.email" type="email" placeholder="请输入邮箱" required />
          </div>
          <div class="form-row" v-if="!isEdit">
            <label>密码 <span class="required">*</span></label>
            <input v-model="form.password" type="text" placeholder="请输入初始密码" required />
          </div>
          <div class="form-row">
            <label>角色</label>
            <select v-model="form.role">
              <option value="USER">读者</option>
              <option value="ADMIN">管理员</option>
            </select>
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

    <!-- 重置密码对话框 -->
    <div v-if="pwdDialogVisible" class="dialog-overlay" @click.self="closePwdDialog">
      <div class="dialog-box dialog-sm">
        <h2>重置密码</h2>
        <p class="dialog-desc">为用户 <strong>{{ pwdTarget.username }}</strong> 设置新密码</p>
        <form class="dialog-form" @submit.prevent="handleResetPwd">
          <div class="form-row">
            <label>新密码 <span class="required">*</span></label>
            <input v-model="newPassword" type="text" placeholder="请输入新密码" required />
          </div>
          <p v-if="pwdError" class="form-error">{{ pwdError }}</p>
          <div class="dialog-actions">
            <button type="button" class="btn-cancel" @click="closePwdDialog">取消</button>
            <button type="submit" class="btn-confirm">确认重置</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 借阅记录对话框 -->
    <div v-if="borrowDialogVisible" class="dialog-overlay" @click.self="closeBorrowDialog">
      <div class="dialog-box dialog-lg">
        <h2>{{ borrowTarget.username }} 的借阅记录</h2>
        <div class="borrow-list" v-if="borrowList.length > 0">
          <table class="book-table inner-table">
            <thead>
              <tr>
                <th>书名</th>
                <th>作者</th>
                <th>借阅日期</th>
                <th>到期日期</th>
                <th>归还日期</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="b in borrowList" :key="b.borrowId">
                <td>{{ b.bookName }}</td>
                <td>{{ b.author }}</td>
                <td>{{ b.borrowDate }}</td>
                <td>{{ b.dueDate }}</td>
                <td>{{ b.returnDate || '-' }}</td>
                <td>
                  <span class="status-tag" :class="'status-' + b.status.toLowerCase()">
                    {{ statusLabel(b.status) }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="no-data">暂无借阅记录</p>
        <div class="dialog-actions">
          <button type="button" class="btn-cancel" @click="closeBorrowDialog">关闭</button>
        </div>
      </div>
    </div>

    <!-- 返回 -->
    <div class="admin-footer">
      <router-link to="/" class="btn-back">← 返回前台首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  adminGetUsers, adminSearchUsers, adminAddUser, adminUpdateUser,
  adminResetPassword, adminUpdateRole, adminDeleteUser, adminGetUserBorrows
} from '../api/admin'

const router = useRouter()
const userId = ref(localStorage.getItem('userId'))

const users = ref([])
const searchKeyword = ref('')
const currentRole = ref('')
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const totalPages = ref(0)

// 对话框状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formError = ref('')

const pwdDialogVisible = ref(false)
const pwdTarget = ref({})
const newPassword = ref('')
const pwdError = ref('')

const borrowDialogVisible = ref(false)
const borrowTarget = ref({})
const borrowList = ref([])

const form = reactive({
  targetId: null,
  username: '',
  email: '',
  password: '',
  role: 'USER'
})

const roleOptions = [
  { label: '全部', value: '' },
  { label: '读者', value: 'USER' },
  { label: '管理员', value: 'ADMIN' }
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
  loadUsers()
})

async function loadUsers() {
  try {
    let res
    if (searchKeyword.value.trim()) {
      res = await adminSearchUsers(userId.value, searchKeyword.value.trim(), currentPage.value, pageSize)
    } else {
      res = await adminGetUsers(userId.value, currentPage.value, pageSize, currentRole.value)
    }
    if (res.code === 200) {
      users.value = res.data.list || []
      total.value = res.data.total || 0
      totalPages.value = res.data.totalPages || 0
    }
  } catch (err) {
    console.error('加载用户失败', err)
  }
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadUsers()
}

function handleSearch() {
  currentPage.value = 1
  currentRole.value = ''
  loadUsers()
}

function filterByRole(role) {
  currentRole.value = role
  searchKeyword.value = ''
  currentPage.value = 1
  loadUsers()
}

function formatDate(dt) {
  if (!dt) return '-'
  return dt.replace('T', ' ').substring(0, 16)
}

function statusLabel(status) {
  switch (status) {
    case 'BORROWED': return '借阅中'
    case 'RETURNED': return '已归还'
    case 'OVERDUE': return '已逾期'
    default: return status
  }
}

// ===== 创建/编辑 =====
function openAddDialog() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(u) {
  isEdit.value = true
  formError.value = ''
  Object.assign(form, {
    targetId: u.id,
    username: u.username,
    email: u.email,
    password: '',
    role: u.role
  })
  dialogVisible.value = true
}

function closeDialog() {
  dialogVisible.value = false
  resetForm()
}

function resetForm() {
  Object.assign(form, { targetId: null, username: '', email: '', password: '', role: 'USER' })
  formError.value = ''
}

async function handleSubmit() {
  formError.value = ''
  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await adminUpdateUser({
        userId: userId.value,
        targetId: form.targetId,
        username: form.username,
        email: form.email,
        role: form.role
      })
    } else {
      res = await adminAddUser({
        userId: userId.value,
        username: form.username,
        email: form.email,
        password: form.password,
        role: form.role
      })
    }
    if (res.code === 200) {
      closeDialog()
      loadUsers()
    } else {
      formError.value = res.message || '操作失败'
    }
  } catch (err) {
    formError.value = err.message || '操作失败'
  } finally {
    submitting.value = false
  }
}

// ===== 角色切换 =====
async function toggleRole(u) {
  const newRole = u.role === 'ADMIN' ? 'USER' : 'ADMIN'
  const label = newRole === 'ADMIN' ? '管理员' : '读者'
  if (!confirm(`确认将用户 ${u.username} 的角色修改为「${label}」？`)) return
  try {
    const res = await adminUpdateRole({ userId: userId.value, targetId: u.id, role: newRole })
    if (res.code === 200) {
      loadUsers()
    } else {
      alert(res.message || '操作失败')
    }
  } catch (err) {
    alert(err.message || '操作失败')
  }
}

// ===== 重置密码 =====
function openResetPwd(u) {
  pwdTarget.value = u
  newPassword.value = ''
  pwdError.value = ''
  pwdDialogVisible.value = true
}

function closePwdDialog() {
  pwdDialogVisible.value = false
}

async function handleResetPwd() {
  if (!newPassword.value.trim()) {
    pwdError.value = '密码不能为空'
    return
  }
  try {
    const res = await adminResetPassword({
      userId: userId.value,
      targetId: pwdTarget.value.id,
      newPassword: newPassword.value
    })
    if (res.code === 200) {
      alert('密码重置成功')
      closePwdDialog()
    } else {
      pwdError.value = res.message || '操作失败'
    }
  } catch (err) {
    pwdError.value = err.message || '操作失败'
  }
}

// ===== 借阅记录 =====
async function viewBorrows(u) {
  borrowTarget.value = u
  borrowList.value = []
  borrowDialogVisible.value = true
  try {
    const res = await adminGetUserBorrows(u.id, userId.value)
    if (res.code === 200) {
      borrowList.value = res.data || []
    }
  } catch (err) {
    console.error('获取借阅记录失败', err)
  }
}

function closeBorrowDialog() {
  borrowDialogVisible.value = false
}

// ===== 删除 =====
async function handleDelete(u) {
  if (!confirm(`确定要删除用户「${u.username}」吗？此操作不可恢复。`)) return
  try {
    const res = await adminDeleteUser(u.id, userId.value)
    if (res.code === 200) {
      loadUsers()
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
  gap: 10px;
}

.btn-nav {
  height: 34px;
  line-height: 34px;
  padding: 0 16px;
  border-radius: 8px;
  background: var(--color-primary, #2b5377);
  color: #fff;
  font-size: 13px;
  text-decoration: none;
  transition: opacity 0.2s;
}

.btn-nav:hover { opacity: 0.9; }

.btn-nav.active {
  background: #0d6efd;
  pointer-events: none;
}

.admin-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
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

.btn-add {
  height: 36px;
  padding: 0 18px;
  border-radius: 8px;
  background: #28a745;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.btn-add:hover { background: #218838; }

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

.td-actions {
  white-space: nowrap;
}

.td-empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

.role-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.role-user {
  background: rgba(0, 123, 255, 0.1);
  color: #007bff;
}

.role-admin {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.btn-edit, .btn-role, .btn-pwd, .btn-borrow, .btn-delete {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  border: none;
  cursor: pointer;
  margin-right: 4px;
  margin-bottom: 4px;
}

.btn-edit { background: #007bff; color: #fff; }
.btn-edit:hover { background: #0056b3; }

.btn-role { background: #6f42c1; color: #fff; }
.btn-role:hover { background: #5a32a3; }

.btn-pwd { background: #fd7e14; color: #fff; }
.btn-pwd:hover { background: #e8690b; }

.btn-borrow { background: #17a2b8; color: #fff; }
.btn-borrow:hover { background: #138496; }

.btn-delete { background: #dc3545; color: #fff; }
.btn-delete:hover { background: #c82333; }

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

.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }

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
  width: 480px;
  max-height: 85vh;
  overflow-y: auto;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.dialog-sm { width: 400px; }
.dialog-lg { width: 700px; }

.dialog-box h2 {
  margin: 0 0 16px;
  font-size: 20px;
  color: var(--color-primary, #2b5377);
}

.dialog-desc {
  margin: 0 0 16px;
  color: #666;
  font-size: 14px;
}

.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
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

.required { color: #dc3545; }

.form-row input,
.form-row select {
  height: 40px;
  padding: 0 14px;
  border-radius: 8px;
  border: 1px solid rgba(31, 58, 86, 0.15);
  font-size: 14px;
  background: #fafafa;
}

.form-row input:focus,
.form-row select:focus {
  outline: none;
  border-color: var(--color-primary, #2b5377);
  box-shadow: 0 0 0 2px rgba(43, 83, 119, 0.1);
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
  margin-top: 12px;
}

.btn-cancel, .btn-confirm {
  height: 38px;
  padding: 0 22px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.btn-cancel { background: #e8e4dc; color: #555; }
.btn-cancel:hover { background: #ddd8cf; }

.btn-confirm { background: var(--color-primary, #2b5377); color: #fff; }
.btn-confirm:hover { opacity: 0.9; }
.btn-confirm:disabled { opacity: 0.6; cursor: not-allowed; }

/* 借阅记录弹窗内表格 */
.inner-table th {
  font-size: 13px;
  padding: 10px 12px;
}

.inner-table td {
  font-size: 13px;
  padding: 8px 12px;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.status-borrowed { background: rgba(0, 123, 255, 0.1); color: #007bff; }
.status-returned { background: rgba(40, 167, 69, 0.1); color: #28a745; }
.status-overdue { background: rgba(220, 53, 69, 0.1); color: #dc3545; }

.no-data {
  text-align: center;
  color: #999;
  padding: 24px 0;
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
