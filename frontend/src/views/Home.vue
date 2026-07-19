<template>
  <div class="reader-homepage">
    <TopQuickLinks />

    <header class="site-header">
      <div class="container site-header__inner">
        <div>
          <p class="site-header__eyebrow">Campus Library</p>
          <h2>书院图书馆</h2>
        </div>
        <nav aria-label="主导航" class="site-header__nav">
          <router-link to="/">首页</router-link>
          <router-link to="/categories">图书分类</router-link>
          <router-link to="/collection">馆藏范围</router-link>
          <router-link to="/services">读者服务</router-link>
          <router-link to="/news">公告资讯</router-link>
        </nav>
      </div>
    </header>

    <main>
      <HeroSearch />
      <CategoryNav />
      <ServiceHighlights />
      <RecommendationSection />
    </main>
  </div>
</template>

<script setup>
import TopQuickLinks from '../components/TopQuickLinks.vue'
import HeroSearch from '../components/HeroSearch.vue'
import CategoryNav from '../components/CategoryNav.vue'
import ServiceHighlights from '../components/ServiceHighlights.vue'
import RecommendationSection from '../components/RecommendationSection.vue'
</script>
