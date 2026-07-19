<template>
  <div class="top-quick-links">
    <div class="container quick-links-inner">
      <p class="welcome-text">欢迎来到书院图书馆，静享阅读时光。</p>
      <nav aria-label="快捷入口" class="quick-links-nav">
        <template v-if="!isLoggedIn">
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
        <template v-else>
          <span class="user-greeting">{{ username }}</span>
          <router-link v-if="isAdmin" to="/admin" class="admin-link">进入后台</router-link>
          <a href="#" @click.prevent="handleLogout">退出</a>
        </template>
        <router-link to="/favorites">我的收藏</router-link>
        <router-link to="/profile" class="profile-link">
          个人中心
          <span v-if="unreadCount > 0" class="badge-dot"></span>
        </router-link>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { checkLoginStatus } from '../api/user'
import { getUnreadCount } from '../api/message'

const router = useRouter()
const isLoggedIn = ref(false)
const username = ref('')
const isAdmin = ref(false)
const unreadCount = ref(0)

function clearLoginState() {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('email')
  localStorage.removeItem('role')
  isLoggedIn.value = false
  username.value = ''
  isAdmin.value = false
}

async function checkLogin() {
  const token = localStorage.getItem('token')
  const userId = localStorage.getItem('userId')
  const name = localStorage.getItem('username')

  if (!token || !userId || !name) {
    clearLoginState()
    return
  }

  // 向后端验证 userId 是否有效
  try {
    const res = await checkLoginStatus(userId)
    if (res.code === 200) {
      isLoggedIn.value = true
      username.value = name
      isAdmin.value = localStorage.getItem('role') === 'ADMIN'
      // 获取未读消息数
      try {
        const msgRes = await getUnreadCount(userId)
        if (msgRes.code === 200) unreadCount.value = msgRes.data.count || 0
      } catch (e) { /* ignore */ }
    } else {
      clearLoginState()
    }
  } catch (err) {
    // 后端不可达或返回错误，清除登录状态
    clearLoginState()
  }
}

function handleLogout() {
  clearLoginState()
}

onMounted(() => {
  checkLogin()
})

// 监听 storage 变化（其他标签页退出时同步）
window.addEventListener('storage', checkLogin)
</script>

<style scoped>
.user-greeting {
  font-weight: 600;
  color: #d4be96;
}

.admin-link {
  color: #ffc107;
  font-weight: 600;
  transition: color 0.2s;
}

.admin-link:hover {
  color: #ffda57;
}

.profile-link {
  position: relative;
}

.badge-dot {
  position: absolute;
  top: -4px;
  right: -8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dc3545;
}
</style>
