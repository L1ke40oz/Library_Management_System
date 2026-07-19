<template>
  <div class="detail-page">
    <header class="detail-header">
      <div class="container detail-header__inner">
        <div class="detail-left">
          <a href="#" class="detail-back" @click.prevent="goBack">&larr; 返回</a>
          <router-link to="/" class="detail-logo">书院图书馆</router-link>
        </div>
      </div>
    </header>

    <main class="container detail-main" v-if="book">
      <div class="detail-card">
        <div class="detail-cover">
          <span>{{ book.bookName.substring(0, 4) }}</span>
        </div>
        <div class="detail-info">
          <h1 class="detail-title">{{ book.bookName }}</h1>
          <div class="detail-meta-grid">
            <div class="meta-item">
              <span class="meta-label">作者</span>
              <span class="meta-value">{{ book.author }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">出版社</span>
              <span class="meta-value">{{ book.publisher }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">ISBN</span>
              <span class="meta-value">{{ book.isbn }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">分类</span>
              <span class="meta-value">{{ book.category }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">馆藏总量</span>
              <span class="meta-value">{{ book.totalCount }} 本</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">当前可借</span>
              <span class="meta-value" :class="{ 'text-danger': available <= 0, 'text-success': available > 0 }">
                {{ available > 0 ? `${available} 本` : '已借完' }}
              </span>
            </div>
          </div>

          <div class="detail-actions">
            <button
              class="btn-borrow"
              :disabled="available <= 0 || borrowLoading"
              @click="handleBorrow"
            >
              {{ borrowLoading ? '处理中...' : (available > 0 ? '借阅此书' : '暂无可借') }}
            </button>
            <button
              class="btn-favorite"
              :class="{ favorited: isFavorited }"
              @click="handleFavorite"
            >
              {{ isFavorited ? '★ 已收藏' : '☆ 收藏' }}
            </button>
          </div>

          <p v-if="actionMsg" class="action-msg" :class="actionMsgType">{{ actionMsg }}</p>
        </div>
      </div>

      <div class="detail-description" v-if="book.description">
        <h2>图书简介</h2>
        <p>{{ book.description }}</p>
      </div>

      <div class="detail-comments">
        <h2>读者评论</h2>
        <form v-if="getUserId()" class="comment-form" @submit.prevent="handleComment">
          <textarea
            v-model="commentContent"
            placeholder="写下你对这本书的评价..."
            maxlength="200"
            rows="3"
          ></textarea>
          <div class="comment-form-footer">
            <span class="char-count">{{ commentContent.length }}/200</span>
            <button type="submit" :disabled="commentLoading || !commentContent.trim()">
              {{ commentLoading ? '发送中...' : '发表评论' }}
            </button>
          </div>
        </form>
        <p v-else class="comment-login-tip">
          <router-link to="/login">登录</router-link> 后即可发表评论
        </p>

        <div class="comment-list" v-if="comments.length > 0">
          <div class="comment-item" v-for="c in comments" :key="c.id">
            <div class="comment-header">
              <strong>{{ c.username }}</strong>
              <span>{{ formatDate(c.createdAt) }}</span>
            </div>
            <p>{{ c.content }}</p>
          </div>
        </div>
        <p v-else class="comment-empty">暂无评论，来写第一条吧</p>
      </div>
    </main>

    <main class="container detail-main" v-else-if="loading">
      <p class="detail-loading">加载中...</p>
    </main>

    <main class="container detail-main" v-else>
      <p class="detail-loading">图书不存在</p>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBookDetail, borrowBook, addFavorite, removeFavorite, checkFavorite } from '../api/book'
import { getComments, addComment } from '../api/comment'

const route = useRoute()
const router = useRouter()
const book = ref(null)
const loading = ref(true)
const borrowLoading = ref(false)
const isFavorited = ref(false)
const actionMsg = ref('')
const actionMsgType = ref('')

// 实时获取 userId，避免组件缓存导致登录状态丢失
function getUserId() {
  return localStorage.getItem('userId')
}

const available = computed(() => {
  if (!book.value) return 0
  return book.value.totalCount - book.value.borrowCount
})

const comments = ref([])
const commentContent = ref('')
const commentLoading = ref(false)

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

async function loadComments() {
  try {
    const res = await getComments(route.params.id)
    if (res.code === 200) comments.value = res.data
  } catch (err) { /* ignore */ }
}

async function handleComment() {
  if (!commentContent.value.trim()) return
  commentLoading.value = true
  try {
    const res = await addComment({
      userId: Number(getUserId()),
      bookId: Number(route.params.id),
      content: commentContent.value.trim()
    })
    if (res.code === 200) {
      commentContent.value = ''
      loadComments()
    }
  } catch (err) {
    console.error(err)
  } finally {
    commentLoading.value = false
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/search')
  }
}

async function handleBorrow() {
  const userId = getUserId()
  if (!userId) {
    actionMsg.value = '请先登录后再借阅'
    actionMsgType.value = 'error'
    return
  }
  borrowLoading.value = true
  actionMsg.value = ''
  try {
    const res = await borrowBook(userId, book.value.bookId)
    if (res.code === 200) {
      actionMsg.value = res.message
      actionMsgType.value = 'success'
      book.value.borrowCount++
    } else {
      actionMsg.value = res.message
      actionMsgType.value = 'error'
    }
  } catch (err) {
    actionMsg.value = err.message || '借阅失败'
    actionMsgType.value = 'error'
  } finally {
    borrowLoading.value = false
  }
}

async function handleFavorite() {
  const userId = getUserId()
  if (!userId) {
    actionMsg.value = '请先登录后再收藏'
    actionMsgType.value = 'error'
    return
  }
  actionMsg.value = ''
  try {
    if (isFavorited.value) {
      const res = await removeFavorite(userId, book.value.bookId)
      if (res.code === 200) {
        isFavorited.value = false
        actionMsg.value = '已取消收藏'
        actionMsgType.value = 'success'
      }
    } else {
      const res = await addFavorite(userId, book.value.bookId)
      if (res.code === 200) {
        isFavorited.value = true
        actionMsg.value = '收藏成功'
        actionMsgType.value = 'success'
      }
    }
  } catch (err) {
    actionMsg.value = err.message || '操作失败'
    actionMsgType.value = 'error'
  }
}

async function loadFavoriteStatus() {
  const userId = getUserId()
  if (!userId) return
  try {
    const res = await checkFavorite(userId, route.params.id)
    if (res.code === 200) {
      isFavorited.value = res.data
    }
  } catch (err) {
    // ignore
  }
}

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getBookDetail(id)
    if (res.code === 200) {
      book.value = res.data
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
  loadFavoriteStatus()
  loadComments()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f4ec 0%, #f5efe3 100%);
}

.detail-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(248, 244, 236, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
  padding: 16px 0;
}

.detail-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-logo {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
}

.detail-back {
  font-size: 14px;
  color: var(--color-text-light);
  transition: color 0.2s;
}

.detail-back:hover {
  color: var(--color-primary);
}

.detail-main {
  padding: 48px 0;
}

.detail-loading {
  text-align: center;
  color: var(--color-text-light);
  font-size: 16px;
}

.detail-card {
  display: flex;
  gap: 40px;
  padding: 40px;
  border-radius: 24px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  box-shadow: 0 16px 40px rgba(31, 58, 86, 0.08);
}

.detail-cover {
  flex-shrink: 0;
  width: 160px;
  height: 220px;
  border-radius: 18px;
  background: linear-gradient(135deg, #31577c, #1f3a56);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.92);
  font-size: 20px;
  text-align: center;
  line-height: 1.5;
  letter-spacing: 0.1em;
  box-shadow: 0 12px 32px rgba(31, 58, 86, 0.2);
}

.detail-info {
  flex: 1;
  min-width: 0;
}

.detail-title {
  margin: 0 0 24px;
  font-size: 26px;
  color: var(--color-primary);
  line-height: 1.3;
}

.detail-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px 32px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: var(--color-text-light);
  letter-spacing: 0.05em;
}

.meta-value {
  font-size: 15px;
  color: var(--color-text);
  font-weight: 500;
}

.text-success { color: #28a745; }
.text-danger { color: #dc3545; }

.detail-actions {
  margin-top: 28px;
  display: flex;
  gap: 14px;
}

.btn-borrow {
  padding: 12px 28px;
  border-radius: 12px;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  transition: background 0.2s, transform 0.1s;
}

.btn-borrow:hover:not(:disabled) {
  background: var(--color-primary-soft);
}

.btn-borrow:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-favorite {
  padding: 12px 24px;
  border-radius: 12px;
  background: rgba(154, 123, 79, 0.1);
  color: var(--color-accent);
  font-size: 15px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-favorite:hover {
  background: rgba(154, 123, 79, 0.2);
}

.btn-favorite.favorited {
  background: var(--color-accent);
  color: #fff;
}

.action-msg {
  margin-top: 14px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
}

.action-msg.success {
  background: rgba(40, 167, 69, 0.08);
  color: #28a745;
}

.action-msg.error {
  background: rgba(220, 53, 69, 0.08);
  color: #dc3545;
}

.detail-description {
  margin-top: 32px;
  padding: 32px;
  border-radius: 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.detail-description h2 {
  margin: 0 0 16px;
  font-size: 18px;
  color: var(--color-primary);
}

.detail-description p {
  margin: 0;
  line-height: 1.8;
  color: var(--color-text-light);
  font-size: 15px;
}

.detail-comments {
  margin-top: 32px;
  padding: 32px;
  border-radius: 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.detail-comments h2 {
  margin: 0 0 20px;
  font-size: 18px;
  color: var(--color-primary);
}

.comment-form textarea {
  width: 100%;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid rgba(31, 58, 86, 0.14);
  background: #fffdfa;
  font-size: 14px;
  color: var(--color-text);
  resize: vertical;
  min-height: 80px;
}

.comment-form textarea:focus {
  outline: none;
  border-color: var(--color-primary-soft);
  box-shadow: 0 0 0 3px rgba(43, 83, 119, 0.1);
}

.comment-form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.char-count {
  font-size: 12px;
  color: var(--color-text-light);
}

.comment-form-footer button {
  padding: 8px 20px;
  border-radius: 10px;
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.comment-form-footer button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.comment-login-tip {
  margin: 0 0 20px;
  font-size: 14px;
  color: var(--color-text-light);
}

.comment-login-tip a {
  color: var(--color-accent);
  font-weight: 600;
}

.comment-list {
  margin-top: 24px;
  display: grid;
  gap: 16px;
}

.comment-item {
  padding: 16px;
  border-radius: 12px;
  background: rgba(31, 58, 86, 0.03);
  border: 1px solid rgba(31, 58, 86, 0.06);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-header strong {
  font-size: 14px;
  color: var(--color-primary);
}

.comment-header span {
  font-size: 12px;
  color: var(--color-text-light);
}

.comment-item p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-light);
  line-height: 1.6;
}

.comment-empty {
  margin: 20px 0 0;
  text-align: center;
  color: var(--color-text-light);
  font-size: 14px;
}

@media (max-width: 768px) {
  .detail-card {
    flex-direction: column;
    align-items: center;
    padding: 28px;
    gap: 24px;
  }

  .detail-cover {
    width: 120px;
    height: 168px;
    font-size: 16px;
  }

  .detail-meta-grid {
    grid-template-columns: 1fr;
  }

  .detail-title {
    font-size: 22px;
    text-align: center;
  }

  .detail-actions {
    flex-direction: column;
  }
}
</style>
