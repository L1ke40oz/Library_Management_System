<template>
  <div class="auth-page">
    <div class="auth-card">
      <router-link to="/" class="auth-back">&larr; 返回首页</router-link>
      <div class="auth-header">
        <h1>书院图书馆</h1>
        <p>欢迎回来，请登录您的账号</p>
      </div>

      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名 / 邮箱</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="请输入用户名或邮箱"
            autocomplete="username"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            required
          />
        </div>

        <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>

        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <div class="auth-footer">
        <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  password: ''
})

async function handleLogin() {
  errorMsg.value = ''
  loading.value = true

  try {
    const res = await login(form)
    if (res.code === 200) {
      // 保存登录信息
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userId', res.data.userId)
      localStorage.setItem('username', res.data.username)
      localStorage.setItem('email', res.data.email)
      localStorage.setItem('role', res.data.role)
      // 根据角色跳转不同页面
      if (res.data.role === 'ADMIN') {
        router.push('/admin')
      } else {
        router.push('/')
      }
    } else {
      errorMsg.value = res.message
    }
  } catch (err) {
    errorMsg.value = err.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(ellipse at top left, rgba(154, 123, 79, 0.1), transparent 50%),
    radial-gradient(ellipse at bottom right, rgba(31, 58, 86, 0.08), transparent 50%),
    linear-gradient(180deg, #f8f4ec 0%, #f5efe3 100%);
}

.auth-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  border-radius: 24px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  box-shadow: 0 20px 60px rgba(31, 58, 86, 0.1);
}

.auth-back {
  display: inline-block;
  margin-bottom: 20px;
  font-size: 14px;
  color: var(--color-text-light);
  transition: color 0.2s;
  text-align: left;
}

.auth-back:hover {
  color: var(--color-primary);
}

.auth-header {
  text-align: center;
  margin-bottom: 36px;
}

.auth-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  color: var(--color-primary);
}

.auth-header p {
  margin: 0;
  color: var(--color-text-light);
  font-size: 15px;
}

.auth-form {
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
  height: 48px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1px solid rgba(31, 58, 86, 0.14);
  background: #fffdfa;
  color: var(--color-text);
  font-size: 15px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: var(--color-primary-soft);
  box-shadow: 0 0 0 3px rgba(43, 83, 119, 0.12);
}

.auth-error {
  margin: 0;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(220, 53, 69, 0.08);
  color: #dc3545;
  font-size: 14px;
  text-align: center;
}

.auth-btn {
  height: 48px;
  border-radius: 12px;
  background: var(--color-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.05em;
  transition: background 0.2s, transform 0.1s;
}

.auth-btn:hover:not(:disabled) {
  background: var(--color-primary-soft);
}

.auth-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.auth-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-footer {
  margin-top: 24px;
  text-align: center;
}

.auth-footer p {
  margin: 0;
  color: var(--color-text-light);
  font-size: 14px;
}

.auth-footer a {
  color: var(--color-accent);
  font-weight: 600;
  transition: color 0.2s;
}

.auth-footer a:hover {
  color: var(--color-primary);
}
</style>
