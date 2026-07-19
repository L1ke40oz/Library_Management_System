<template>
  <section class="recommendation-section section-block">
    <div class="container">
      <div class="section-heading section-heading--split">
        <div>
          <p>今日推荐</p>
          <h2>从馆员精选中遇见下一本好书</h2>
        </div>
        <div class="heading-actions">
          <button class="btn-refresh" :class="{ spinning: refreshing }" @click="loadRecommendations">
            <span class="refresh-icon">↻</span> 换一批
          </button>
          <router-link to="/search">查看更多推荐</router-link>
        </div>
      </div>
      <div class="recommendation-grid" v-if="books.length > 0">
        <RecommendationCard v-for="book in books" :key="book.id" :book="book" />
      </div>
      <div v-else class="recommendation-empty">加载中...</div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import RecommendationCard from './RecommendationCard.vue'
import { getLatestComments } from '../api/comment'
import { getRandomBooks } from '../api/book'

const books = ref([])
const refreshing = ref(false)

// 标签池，随机分配
const tagPool = ['科幻热门', '学术经典', '文学经典', '编程必读', '新书推荐', '馆员精选', '读者最爱', '经典重读']
// 位置池
const locationPool = ['A区文学借阅处', 'B区计算机科学', 'C区社科阅览室', 'D区自然科学', 'E区综合阅览']

async function loadRecommendations() {
  refreshing.value = true
  try {
    const res = await getRandomBooks(3)
    if (res.code === 200 && res.data.length > 0) {
      const result = []
      for (let i = 0; i < res.data.length; i++) {
        const raw = res.data[i]
        const book = {
          id: raw.bookId,
          bookId: raw.bookId,
          title: raw.bookName,
          author: raw.author || '未知',
          description: raw.description || `${raw.bookName}，值得一读的好书。`,
          tag: tagPool[Math.floor(Math.random() * tagPool.length)],
          location: locationPool[Math.floor(Math.random() * locationPool.length)],
          cover: raw.bookName.length > 4 ? raw.bookName.substring(0, 4) : raw.bookName,
          comments: []
        }

        // 加载评论
        try {
          const cRes = await getLatestComments(raw.bookId, 5)
          if (cRes.code === 200 && cRes.data.length > 0) {
            book.comments = cRes.data.map((c, idx) => ({
              id: `${raw.bookId}-${c.id}`,
              authorName: c.username,
              content: c.content,
              duration: `${14 + idx * 2}s`,
              delay: `-${idx * 3 + 2}s`,
              lane: idx % 3
            }))
          }
        } catch (err) { /* ignore */ }

        if (book.comments.length === 0) {
          book.comments = [
            { id: `${raw.bookId}-default`, authorName: '图书馆', content: '快来写下你的第一条评论吧', duration: '16s', delay: '-2s', lane: 0 }
          ]
        }

        result.push(book)
      }
      books.value = result
    }
  } catch (err) {
    console.error('加载推荐失败', err)
  } finally {
    refreshing.value = false
  }
}

onMounted(() => {
  loadRecommendations()
})
</script>

<style scoped>
.heading-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-refresh {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  background: rgba(31, 58, 86, 0.08);
  color: var(--color-primary, #2b5377);
  font-size: 14px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}

.btn-refresh:hover {
  background: rgba(31, 58, 86, 0.14);
  transform: scale(1.03);
}

.refresh-icon {
  display: inline-block;
  font-size: 16px;
  transition: transform 0.5s ease;
}

.btn-refresh.spinning .refresh-icon {
  animation: spin 0.6s ease;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.recommendation-empty {
  text-align: center;
  padding: 48px;
  color: var(--color-text-light, #5f6e7c);
  font-size: 15px;
}
</style>
