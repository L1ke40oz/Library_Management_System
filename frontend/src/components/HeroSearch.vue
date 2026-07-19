<template>
  <section class="hero-search">
    <div class="container hero-search__inner">
      <div class="hero-search__content">
        <p class="eyebrow">学院书香 · 智慧服务</p>
        <h1>发现馆藏好书，开启沉静而丰盛的阅读旅程</h1>
        <p class="hero-search__description">
          面向普通读者提供图书检索、分类导览、馆藏推荐与常用服务入口，营造简洁正式的校园阅读体验。
        </p>
        <form class="search-panel" @submit.prevent="handleSearch">
          <label class="search-panel__label" for="book-search">馆藏检索</label>
          <div class="search-panel__controls">
            <input id="book-search" v-model="searchKeyword" type="text" placeholder="请输入书名、作者或关键词" />
            <button type="submit">立即搜索</button>
          </div>
          <ul class="search-tags">
            <li v-for="tag in tags" :key="tag" @click="quickSearch(tag)">{{ tag }}</li>
          </ul>
        </form>
      </div>
      <aside class="hero-search__panel" aria-label="图书馆信息概览">
        <div class="hero-card">
          <span>开放时间</span>
          <strong>08:00 - 22:00</strong>
        </div>
        <div class="hero-card">
          <span>本周讲座</span>
          <strong>数字资源使用导引</strong>
        </div>
        <div class="hero-card">
          <span>热门专题</span>
          <strong>人文经典 · 科学前沿</strong>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchKeyword = ref('')
const tags = ['新书速递', '文学经典', '考试专区', '馆藏排行']

function handleSearch() {
  router.push({ path: '/search', query: { q: searchKeyword.value } })
}

function quickSearch(tag) {
  router.push({ path: '/search', query: { tag } })
}
</script>
