<template>
  <section class="service-highlights section-block section-muted">
    <div class="container">
      <div class="section-heading">
        <p>读者服务</p>
        <h2>常用功能一目了然</h2>
      </div>
      <div class="highlight-grid">
        <article
          v-for="item in items"
          :key="item.title"
          class="highlight-card"
          :class="{ clickable: item.link }"
          @click="handleClick(item)"
        >
          <strong>{{ item.title }}</strong>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const items = [
  { title: '借阅查询', description: '查看当前借阅状态、到期时间与续借入口。', link: '/profile?tab=borrows' },
  { title: '预约到馆', description: '查询自习座位、专题阅览室与活动预约信息。', link: null },
  { title: '办事指南', description: '统一查看借阅规则、入馆说明与读者常见问题。', link: null },
  { title: '资源推荐', description: '结合热门借阅与专题推荐，发现值得阅读的内容。', link: null },
]

function handleClick(item) {
  if (item.link) {
    const token = localStorage.getItem('token')
    if (!token) {
      router.push('/login')
    } else {
      router.push(item.link)
    }
  }
}
</script>

<style scoped>
.highlight-card.clickable {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.highlight-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(31, 58, 86, 0.1);
}
</style>
