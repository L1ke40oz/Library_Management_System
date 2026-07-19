<template>
  <div class="page-shell">
    <header class="page-header">
      <div class="container page-header__inner">
        <div class="page-header-left">
          <a href="#" class="back-link" @click.prevent="$router.back()">&larr; 返回</a>
          <router-link to="/" class="page-logo">书院图书馆</router-link>
        </div>
        <span class="page-title">图书分类</span>
      </div>
    </header>

    <main class="container page-main">
      <p class="page-desc">按学科与主题分类浏览馆藏图书，快速定位您感兴趣的领域。</p>

      <div v-if="loading" class="loading-text">加载中...</div>

      <div v-else class="cat-grid">
        <router-link
          v-for="cat in categories"
          :key="cat.name"
          :to="{ path: '/search', query: { category: cat.name } }"
          class="cat-card"
        >
          <span class="cat-icon">{{ cat.icon }}</span>
          <h3>{{ cat.name }}</h3>
          <p>{{ cat.count }} 本馆藏</p>
        </router-link>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { searchBooks, getCategories, getBooksByCategory } from '../api/book'

const categories = ref([])
const loading = ref(true)

const iconMap = {
  '计算机科学': '💻',
  '编程语言': '⌨️',
  '人工智能': '🤖',
  '文学': '📖',
  '科幻': '🚀',
  '数学': '📐'
}

onMounted(async () => {
  try {
    const res = await getCategories()
    if (res.code === 200) {
      // 获取每个分类的图书数量
      const cats = []
      for (const name of res.data) {
        const booksRes = await getBooksByCategory(name)
        cats.push({
          name,
          icon: iconMap[name] || '📚',
          count: booksRes.code === 200 ? booksRes.data.length : 0
        })
      }
      categories.value = cats
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-shell {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f4ec 0%, #f5efe3 100%);
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(248, 244, 236, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
  padding: 16px 0;
}

.page-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header-left {
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

.page-logo {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
}

.page-title {
  font-size: 16px;
  color: var(--color-text-light);
}

.page-main {
  padding: 40px 0;
}

.page-desc {
  margin: 0 0 32px;
  color: var(--color-text-light);
  font-size: 15px;
}

.loading-text {
  text-align: center;
  color: var(--color-text-light);
  padding: 48px;
}

.cat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.cat-card {
  padding: 32px 24px;
  border-radius: 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  text-decoration: none;
  color: inherit;
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.cat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(31, 58, 86, 0.1);
}

.cat-icon {
  font-size: 40px;
  display: block;
  margin-bottom: 14px;
}

.cat-card h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--color-primary);
}

.cat-card p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-light);
}
</style>
