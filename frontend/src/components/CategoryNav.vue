<template>
  <section class="category-nav section-block">
    <div class="container">
      <div class="section-heading">
        <p>分类导览</p>
        <h2>快速进入常用馆藏范围</h2>
      </div>
      <div class="category-grid">
        <router-link
          v-for="item in items"
          :key="item.title"
          class="category-card"
          :to="item.to"
        >
          <span class="category-icon">{{ item.icon }}</span>
          <strong>{{ item.title }}</strong>
          <span>{{ item.description }}</span>
        </router-link>
      </div>
    </div>
  </section>
</template>

<script setup>
const items = [
  { icon: '📖', title: '文学艺术', description: '经典著作与艺术赏析馆藏', to: { path: '/search', query: { category: '文学' } } },
  { icon: '💻', title: '计算机科学', description: '数据结构、网络与系统原理', to: { path: '/search', query: { category: '计算机科学' } } },
  { icon: '⌨️', title: '编程语言', description: 'Java、Python、前端开发技术', to: { path: '/search', query: { category: '编程语言' } } },
  { icon: '🤖', title: '人工智能', description: '机器学习与深度学习前沿', to: { path: '/search', query: { category: '人工智能' } } },
]
</script>

<style scoped>
.category-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 12px;
}
</style>
