<template>
  <div class="auth-page">
    <div class="auth-card">
      <router-link to="/" class="auth-back">&larr; 返回首页</router-link>
      <div class="auth-header">
        <h1>书院图书馆</h1>
        <p>创建新账号，开启阅读之旅</p>
      </div>

      <form class="auth-form" @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="请输入用户名"
            autocomplete="username"
            required
          />
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            placeholder="请输入邮箱地址"
            autocomplete="email"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            autocomplete="new-password"
            minlength="6"
            required
          />
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            autocomplete="new-password"
            required
          />
        </div>

        <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>
        <p v-if="successMsg" class="auth-success">{{ successMsg }}</p>

        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>

      <div class="auth-footer">
        <p>已有账号？<router-link to="/login">立即登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

async function handleRegister() {
  errorMsg.value = ''
  successMsg.value = ''

  // 前端校验
  if (form.password.length < 6) {
    errorMsg.value = '密码长度不能少于6位'
    return
  }

  if (form.password !== form.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  loading.value = true

  try {
    const res = await register({
      username: form.username,
      email: form.email,
      password: form.password
    })

    if (res.code === 200) {
      successMsg.value = '注册成功！正在跳转到登录页...'
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      errorMsg.value = res.message
    }
  } catch (err) {
    errorMsg.value = err.message || '注册失败，请稍后重试'
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

.auth-success {
  margin: 0;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(40, 167, 69, 0.08);
  color: #28a745;
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
