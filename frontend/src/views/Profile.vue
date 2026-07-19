<template>
  <div class="profile-page">
    <header class="profile-header">
      <div class="container profile-header__inner">
        <div class="profile-left">
          <a href="#" class="back-link" @click.prevent="$router.back()">&larr; 返回</a>
          <router-link to="/" class="profile-logo">书院图书馆</router-link>
        </div>
        <span class="profile-title">个人中心</span>
      </div>
    </header>

    <main class="container profile-main">
      <nav class="profile-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <span v-if="tab.key === 'messages' && unreadCount > 0" class="tab-badge">{{ unreadCount }}</span>
        </button>
      </nav>

      <!-- 借阅记录 -->
      <section v-if="activeTab === 'borrows'" class="profile-section">
        <h2>我的借阅</h2>
        <div v-if="borrows.length === 0" class="empty-state">暂无借阅记录</div>
        <div v-else class="record-list">
          <div v-for="item in borrows" :key="item.borrowId" class="record-item">
            <div class="record-info">
              <h4>{{ item.bookName }}</h4>
              <p>{{ item.author }}</p>
              <p class="record-dates">
                借阅：{{ item.borrowDate }} · 应还：{{ item.dueDate }}
                <span v-if="item.returnDate"> · 已还：{{ item.returnDate }}</span>
              </p>
            </div>
            <div class="record-status">
              <span class="status-tag" :class="item.status.toLowerCase()">
                {{ statusText(item.status) }}
              </span>
              <button
                v-if="item.status === 'BORROWED' || item.status === 'OVERDUE'"
                class="btn-return"
                @click="handleReturn(item)"
              >归还</button>
            </div>
          </div>
        </div>
      </section>

      <!-- 我的收藏 -->
      <section v-if="activeTab === 'favorites'" class="profile-section">
        <h2>我的收藏</h2>
        <div v-if="favorites.length === 0" class="empty-state">暂无收藏</div>
        <div v-else class="book-list">
          <router-link
            v-for="book in favorites"
            :key="book.bookId"
            :to="`/book/${book.bookId}`"
            class="book-list-item"
          >
            <div class="book-list-cover">
              <span>{{ book.bookName.substring(0, 4) }}</span>
            </div>
            <div class="book-list-info">
              <h4>{{ book.bookName }}</h4>
              <p>{{ book.author }} · {{ book.publisher }}</p>
            </div>
          </router-link>
        </div>
      </section>

      <!-- 站内信 -->
      <section v-if="activeTab === 'messages'" class="profile-section">
        <div class="messages-header">
          <h2>站内信</h2>
          <button v-if="messages.some(m => !m.isRead)" class="btn-read-all" @click="handleMarkAllRead">全部已读</button>
        </div>
        <div v-if="messages.length === 0" class="empty-state">暂无消息</div>
        <div v-else class="message-list">
          <div
            v-for="msg in messages"
            :key="msg.messageId"
            class="message-item"
            :class="{ unread: !msg.isRead }"
          >
            <div class="message-content">
              <div class="message-title">
                <span v-if="!msg.isRead" class="unread-dot"></span>
                {{ msg.title }}
              </div>
              <p class="message-body">{{ msg.content }}</p>
              <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
            </div>
            <button v-if="!msg.isRead" class="btn-mark-read" @click="handleMarkRead(msg)">标为已读</button>
          </div>
        </div>
      </section>

      <!-- 个人信息 -->
      <section v-if="activeTab === 'settings'" class="profile-section">
        <h2>个人信息</h2>
        <form class="settings-form" @submit.prevent="handleUpdateProfile">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="profileForm.username" type="text" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="profileForm.email" type="email" />
          </div>
          <p v-if="settingsMsg" class="settings-msg" :class="settingsMsgType">{{ settingsMsg }}</p>
          <button type="submit" class="btn-save">保存修改</button>
        </form>

        <div class="section-divider"></div>

        <h2>修改密码</h2>
        <form class="settings-form" @submit.prevent="handleChangePassword">
          <div class="form-group">
            <label>原密码</label>
            <input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" />
          </div>
          <div class="form-group">
            <label>确认新密码</label>
            <input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
          </div>
          <p v-if="passwordMsg" class="settings-msg" :class="passwordMsgType">{{ passwordMsg }}</p>
          <button type="submit" class="btn-save">修改密码</button>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getBorrowList, returnBook, getFavoriteList } from '../api/book'
import { getUserInfo, updateUser, changePassword } from '../api/user'
import { getMessages, markAsRead, markAllAsRead, getUnreadCount } from '../api/message'

const router = useRouter()
const route = useRoute()
const userId = localStorage.getItem('userId')

const tabs = [
  { key: 'borrows', label: '借阅记录' },
  { key: 'favorites', label: '我的收藏' },
  { key: 'messages', label: '站内信' },
  { key: 'settings', label: '个人信息' }
]
const activeTab = ref('borrows')

const borrows = ref([])
const favorites = ref([])
const messages = ref([])
const unreadCount = ref(0)
const profileForm = reactive({ username: '', email: '' })
const settingsMsg = ref('')
const settingsMsgType = ref('')

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordMsg = ref('')
const passwordMsgType = ref('')

function statusText(status) {
  const map = { BORROWED: '借阅中', RETURNED: '已归还', OVERDUE: '已逾期' }
  return map[status] || status
}

async function loadBorrows() {
  try {
    const res = await getBorrowList(userId)
    if (res.code === 200) borrows.value = res.data
  } catch (err) { console.error(err) }
}

async function loadFavorites() {
  try {
    const res = await getFavoriteList(userId)
    if (res.code === 200) favorites.value = res.data
  } catch (err) { console.error(err) }
}

async function loadProfile() {
  try {
    const res = await getUserInfo(userId)
    if (res.code === 200) {
      profileForm.username = res.data.username
      profileForm.email = res.data.email
    }
  } catch (err) { console.error(err) }
}

async function loadMessages() {
  try {
    const res = await getMessages(userId)
    if (res.code === 200) messages.value = res.data || []
    const countRes = await getUnreadCount(userId)
    if (countRes.code === 200) unreadCount.value = countRes.data.count || 0
  } catch (err) { console.error(err) }
}

async function handleMarkRead(msg) {
  try {
    await markAsRead(msg.messageId)
    msg.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch (err) { console.error(err) }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead(userId)
    messages.value.forEach(m => m.isRead = 1)
    unreadCount.value = 0
  } catch (err) { console.error(err) }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

async function handleReturn(item) {
  try {
    const res = await returnBook(userId, item.borrowId)
    if (res.code === 200) {
      item.status = 'RETURNED'
      item.returnDate = new Date().toISOString().split('T')[0]
    }
  } catch (err) { console.error(err) }
}

async function handleUpdateProfile() {
  settingsMsg.value = ''
  try {
    const res = await updateUser({
      userId: Number(userId),
      username: profileForm.username,
      email: profileForm.email
    })
    if (res.code === 200) {
      settingsMsg.value = '修改成功'
      settingsMsgType.value = 'success'
      localStorage.setItem('username', profileForm.username)
      localStorage.setItem('email', profileForm.email)
    } else {
      settingsMsg.value = res.message
      settingsMsgType.value = 'error'
    }
  } catch (err) {
    settingsMsg.value = err.message || '修改失败'
    settingsMsgType.value = 'error'
  }
}

async function handleChangePassword() {
  passwordMsg.value = ''
  if (!passwordForm.oldPassword) {
    passwordMsg.value = '请输入原密码'
    passwordMsgType.value = 'error'
    return
  }
  if (!passwordForm.newPassword) {
    passwordMsg.value = '请输入新密码'
    passwordMsgType.value = 'error'
    return
  }
  if (passwordForm.newPassword.length < 6) {
    passwordMsg.value = '新密码长度不能少于6位'
    passwordMsgType.value = 'error'
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordMsg.value = '两次输入的新密码不一致'
    passwordMsgType.value = 'error'
    return
  }
  try {
    const res = await changePassword({
      userId: Number(userId),
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      passwordMsg.value = '密码修改成功'
      passwordMsgType.value = 'success'
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      passwordMsg.value = res.message
      passwordMsgType.value = 'error'
    }
  } catch (err) {
    passwordMsg.value = err.message || '修改失败'
    passwordMsgType.value = 'error'
  }
}

onMounted(() => {
  if (!userId) {
    router.push('/login')
    return
  }
  // 支持通过 query 参数切换 tab
  const tab = route.query.tab
  if (tab && tabs.some(t => t.key === tab)) {
    activeTab.value = tab
  }
  loadBorrows()
  loadFavorites()
  loadProfile()
  loadMessages()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f4ec 0%, #f5efe3 100%);
}

.profile-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(248, 244, 236, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
  padding: 16px 0;
}

.profile-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.profile-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-link {
  font-size: 14px;
  color: var(--color-text-light);
  transition: color 0.2s;
}

.back-link:hover {
  color: var(--color-primary);
}

.profile-logo {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
}

.profile-title {
  font-size: 16px;
  color: var(--color-text-light);
}

.profile-main {
  padding: 32px 0;
}

.profile-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 12px;
}

.profile-tabs button {
  padding: 10px 20px;
  border-radius: 10px;
  background: transparent;
  color: var(--color-text-light);
  font-size: 15px;
  transition: all 0.2s;
}

.profile-tabs button:hover {
  background: rgba(31, 58, 86, 0.06);
}

.profile-tabs button.active {
  background: var(--color-primary);
  color: #fff;
  font-weight: 600;
}

.tab-badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #dc3545;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  text-align: center;
  margin-left: 6px;
}

.profile-section h2 {
  margin: 0 0 20px;
  font-size: 20px;
  color: var(--color-primary);
}

.empty-state {
  padding: 48px;
  text-align: center;
  color: var(--color-text-light);
  font-size: 15px;
  background: var(--color-surface);
  border-radius: 16px;
  border: 1px solid var(--color-border);
}

.record-list {
  display: grid;
  gap: 12px;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.record-info h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: var(--color-primary);
}

.record-info p {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-light);
}

.record-dates {
  margin-top: 4px;
}

.record-status {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-tag.borrowed {
  background: rgba(255, 193, 7, 0.12);
  color: #e6a800;
}

.status-tag.returned {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.status-tag.overdue {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.btn-return {
  padding: 8px 16px;
  border-radius: 10px;
  background: var(--color-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.btn-return:hover {
  background: var(--color-primary-soft);
}

.book-list {
  display: grid;
  gap: 12px;
}

.book-list-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
}

.book-list-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(31, 58, 86, 0.1);
}

.book-list-cover {
  flex-shrink: 0;
  width: 52px;
  height: 72px;
  border-radius: 10px;
  background: linear-gradient(135deg, #31577c, #1f3a56);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 11px;
  text-align: center;
}

.book-list-info h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: var(--color-primary);
}

.book-list-info p {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-light);
}

.settings-form {
  max-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
}

.form-group input {
  height: 44px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid rgba(31, 58, 86, 0.14);
  background: #fffdfa;
  font-size: 15px;
  color: var(--color-text);
}

.form-group input:focus {
  outline: none;
  border-color: var(--color-primary-soft);
  box-shadow: 0 0 0 3px rgba(43, 83, 119, 0.1);
}

.btn-save {
  align-self: flex-start;
  padding: 12px 28px;
  border-radius: 12px;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}

.btn-save:hover {
  background: var(--color-primary-soft);
}

.settings-msg {
  margin: 0;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
}

.settings-msg.success {
  background: rgba(40, 167, 69, 0.08);
  color: #28a745;
}

.settings-msg.error {
  background: rgba(220, 53, 69, 0.08);
  color: #dc3545;
}

.section-divider {
  margin: 36px 0;
  border-top: 1px solid var(--color-border);
  max-width: 400px;
}

.messages-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.messages-header h2 {
  margin: 0;
}

.btn-read-all {
  padding: 6px 14px;
  border-radius: 8px;
  background: var(--color-primary);
  color: #fff;
  font-size: 13px;
  border: none;
  cursor: pointer;
}

.btn-read-all:hover {
  opacity: 0.9;
}

.message-list {
  display: grid;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  transition: background 0.2s;
}

.message-item.unread {
  background: rgba(13, 110, 253, 0.03);
  border-color: rgba(13, 110, 253, 0.15);
}

.message-content {
  flex: 1;
}

.message-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dc3545;
  flex-shrink: 0;
}

.message-body {
  margin: 0 0 6px;
  font-size: 13px;
  color: var(--color-text-light);
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.btn-mark-read {
  padding: 5px 12px;
  border-radius: 6px;
  border: 1px solid rgba(31, 58, 86, 0.15);
  background: #fff;
  color: #555;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
}

.btn-mark-read:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .profile-tabs {
    overflow-x: auto;
  }

  .record-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .record-status {
    align-self: flex-end;
  }
}
</style>
