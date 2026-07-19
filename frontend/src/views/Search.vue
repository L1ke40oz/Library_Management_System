<template>
  <div class="search-page">
    <header class="search-page-header">
      <div class="container search-page-header__inner">
        <div class="search-page-left">
          <a href="#" class="back-link" @click.prevent="$router.back()">&larr; 返回</a>
          <router-link to="/" class="search-page-logo">书院图书馆</router-link>
        </div>
        <form class="search-page-bar" @submit.prevent="doSearch">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索书名、作者或ISBN..."
            class="search-page-input"
          />
          <button type="submit" class="search-page-btn">搜索</button>
        </form>
      </div>
    </header>

    <main class="container search-page-main">
      <aside class="search-sidebar">
        <h3>图书分类</h3>
        <ul class="category-list">
          <li
            :class="{ active: selectedCategory === '' }"
            @click="selectCategory('')"
          >全部</li>
          <li
            v-for="cat in categories"
            :key="cat"
            :class="{ active: selectedCategory === cat }"
            @click="selectCategory(cat)"
          >{{ cat }}</li>
        </ul>
      </aside>

      <section class="search-results">
        <div class="search-results-info">
          <p v-if="loading">正在搜索...</p>
          <p v-else-if="books.length === 0">未找到相关图书</p>
          <p v-else>共找到 <strong>{{ books.length }}</strong> 本图书</p>
        </div>

        <div class="book-grid">
          <router-link
            v-for="book in books"
            :key="book.bookId"
            :to="`/book/${book.bookId}`"
            class="book-item"
          >
            <div class="book-cover">
              <span>{{ book.bookName.substring(0, 4) }}</span>
            </div>
            <div class="book-info">
              <h4 class="book-title">{{ book.bookName }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-publisher">{{ book.publisher }}</p>
              <div class="book-meta">
                <span class="book-category">{{ book.category }}</span>
                <span class="book-stock" :class="{ 'out-of-stock': availableCount(book) <= 0 }">
                  {{ availableCount(book) > 0 ? `可借 ${availableCount(book)} 本` : '已借完' }}
                </span>
              </div>
            </div>
          </router-link>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchBooks, getBooksByCategory, getCategories, getBooksByTag } from '../api/book'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const books = ref([])
const categories = ref([])
const selectedCategory = ref('')
const loading = ref(false)

function availableCount(book) {
  return book.totalCount - book.borrowCount
}

async function doSearch() {
  loading.value = true
  selectedCategory.value = ''
  try {
    const res = await searchBooks(keyword.value)
    if (res.code === 200) {
      books.value = res.data
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
  // 更新 URL query
  router.replace({ query: { q: keyword.value } })
}

async function selectCategory(cat) {
  selectedCategory.value = cat
  loading.value = true
  try {
    if (cat === '') {
      const res = await searchBooks(keyword.value)
      if (res.code === 200) books.value = res.data
    } else {
      const res = await getBooksByCategory(cat)
      if (res.code === 200) books.value = res.data
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (err) {
    console.error(err)
  }
}

async function loadByTag(tag) {
  loading.value = true
  try {
    const res = await getBooksByTag(tag)
    if (res.code === 200) books.value = res.data
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 从 URL 读取搜索关键词、分类或标签
  keyword.value = route.query.q || ''
  const cat = route.query.category || ''
  const tag = route.query.tag || ''
  loadCategories()
  if (tag) {
    loadByTag(tag)
  } else if (cat) {
    selectCategory(cat)
  } else {
    doSearch()
  }
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f4ec 0%, #f5efe3 100%);
}

.search-page-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(248, 244, 236, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
  padding: 16px 0;
}

.search-page-header__inner {
  display: flex;
  align-items: center;
  gap: 24px;
}

.search-page-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.back-link {
  font-size: 14px;
  color: var(--color-text-light);
  transition: color 0.2s;
}

.back-link:hover {
  color: var(--color-primary);
}

.search-page-logo {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
  white-space: nowrap;
}

.search-page-bar {
  flex: 1;
  display: flex;
  gap: 12px;
}

.search-page-input {
  flex: 1;
  height: 44px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1px solid rgba(31, 58, 86, 0.14);
  background: #fffdfa;
  font-size: 15px;
  color: var(--color-text);
}

.search-page-input:focus {
  outline: none;
  border-color: var(--color-primary-soft);
  box-shadow: 0 0 0 3px rgba(43, 83, 119, 0.1);
}

.search-page-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 12px;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}

.search-page-btn:hover {
  background: var(--color-primary-soft);
}

.search-page-main {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 32px;
  padding: 32px 0;
}

.search-sidebar h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: var(--color-primary);
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-list li {
  padding: 10px 16px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  color: var(--color-text-light);
  transition: all 0.2s;
}

.category-list li:hover {
  background: rgba(31, 58, 86, 0.06);
  color: var(--color-primary);
}

.category-list li.active {
  background: var(--color-primary);
  color: #fff;
  font-weight: 600;
}

.search-results-info {
  margin-bottom: 20px;
}

.search-results-info p {
  margin: 0;
  color: var(--color-text-light);
  font-size: 14px;
}

.search-results-info strong {
  color: var(--color-primary);
}

.book-grid {
  display: grid;
  gap: 16px;
}

.book-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  transition: transform 0.2s, box-shadow 0.2s;
  text-decoration: none;
  color: inherit;
}

.book-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(31, 58, 86, 0.1);
}

.book-cover {
  flex-shrink: 0;
  width: 72px;
  height: 100px;
  border-radius: 12px;
  background: linear-gradient(135deg, #31577c, #1f3a56);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  text-align: center;
  line-height: 1.4;
  letter-spacing: 0.05em;
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  margin: 0 0 6px;
  font-size: 16px;
  color: var(--color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author,
.book-publisher {
  margin: 0 0 4px;
  font-size: 13px;
  color: var(--color-text-light);
}

.book-meta {
  margin-top: 8px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.book-category {
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(154, 123, 79, 0.1);
  color: var(--color-accent);
  font-size: 12px;
}

.book-stock {
  font-size: 12px;
  color: #28a745;
  font-weight: 600;
}

.book-stock.out-of-stock {
  color: #dc3545;
}

@media (max-width: 768px) {
  .search-page-main {
    grid-template-columns: 1fr;
  }

  .search-sidebar {
    order: -1;
  }

  .category-list {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .category-list li {
    padding: 6px 14px;
  }

  .search-page-header__inner {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
