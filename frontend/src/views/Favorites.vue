<template>
  <div class="favorites-page">
    <header class="favorites-header">
      <div class="container favorites-header__inner">
        <div class="favorites-left">
          <a href="#" class="back-link" @click.prevent="$router.back()">&larr; 返回</a>
          <router-link to="/" class="favorites-logo">书院图书馆</router-link>
        </div>
        <span class="favorites-title">我的收藏</span>
      </div>
    </header>

    <main class="container favorites-main">
      <div v-if="books.length === 0" class="empty-state">
        {{ loading ? '加载中...' : '暂无收藏，去搜索页发现好书吧' }}
      </div>
      <div v-else class="favorites-grid">
        <router-link
          v-for="book in books"
          :key="book.bookId"
          :to="`/book/${book.bookId}`"
          class="fav-item"
        >
          <div class="fav-cover">
            <span>{{ book.bookName.substring(0, 4) }}</span>
          </div>
          <div class="fav-info">
            <h4>{{ book.bookName }}</h4>
            <p>{{ book.author }}</p>
            <span class="fav-category">{{ book.category }}</span>
          </div>
        </router-link>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavoriteList } from '../api/book'

const router = useRouter()
const books = ref([])
const loading = ref(true)
const userId = localStorage.getItem('userId')

onMounted(async () => {
  if (!userId) {
    router.push('/login')
    return
  }
  try {
    const res = await getFavoriteList(userId)
    if (res.code === 200) books.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f4ec 0%, #f5efe3 100%);
}

.favorites-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(248, 244, 236, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
  padding: 16px 0;
}

.favorites-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.favorites-left {
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

.favorites-logo {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
}

.favorites-title {
  font-size: 16px;
  color: var(--color-text-light);
}

.favorites-main {
  padding: 32px 0;
}

.empty-state {
  padding: 64px;
  text-align: center;
  color: var(--color-text-light);
  font-size: 15px;
  background: var(--color-surface);
  border-radius: 16px;
  border: 1px solid var(--color-border);
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.fav-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
}

.fav-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(31, 58, 86, 0.1);
}

.fav-cover {
  flex-shrink: 0;
  width: 64px;
  height: 88px;
  border-radius: 12px;
  background: linear-gradient(135deg, #31577c, #1f3a56);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 12px;
  text-align: center;
}

.fav-info h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: var(--color-primary);
}

.fav-info p {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--color-text-light);
}

.fav-category {
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(154, 123, 79, 0.1);
  color: var(--color-accent);
  font-size: 12px;
}
</style>
