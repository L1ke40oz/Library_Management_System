<template>
  <div class="page-shell">
    <header class="page-header">
      <div class="container page-header__inner">
        <div class="page-header-left">
          <a href="#" class="back-link" @click.prevent="$router.back()">&larr; 返回</a>
          <router-link to="/" class="page-logo">书院图书馆</router-link>
        </div>
        <span class="page-title">馆藏范围</span>
      </div>
    </header>

    <main class="container page-main">
      <section class="intro-section">
        <h1>馆藏资源概览</h1>
        <p class="intro-text">
          书院图书馆馆藏涵盖多个学科领域，致力于为师生提供丰富、优质的文献资源，支撑教学科研与人文素养培育。
        </p>
      </section>

      <section class="stats-section">
        <div class="stat-card" v-for="stat in stats" :key="stat.label">
          <span class="stat-number">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </section>

      <section class="scope-section">
        <h2>馆藏学科范围</h2>
        <div class="scope-grid">
          <div class="scope-item" v-for="item in scopes" :key="item.title">
            <span class="scope-icon">{{ item.icon }}</span>
            <div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </div>
          </div>
        </div>
      </section>

      <section class="resource-section">
        <h2>资源类型</h2>
        <div class="resource-grid">
          <div class="resource-card" v-for="res in resources" :key="res.title">
            <h3>{{ res.title }}</h3>
            <p>{{ res.desc }}</p>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
const stats = [
  { value: '50,000+', label: '纸质图书' },
  { value: '120,000+', label: '电子资源' },
  { value: '200+', label: '期刊订阅' },
  { value: '6', label: '学科大类' }
]

const scopes = [
  { icon: '💻', title: '计算机科学与技术', desc: '涵盖数据结构、算法、操作系统、计算机网络、数据库等核心课程教材及前沿技术著作。' },
  { icon: '📐', title: '数学与统计学', desc: '包括高等数学、线性代数、概率论、数理统计、离散数学等基础与应用数学文献。' },
  { icon: '🤖', title: '人工智能与大数据', desc: '机器学习、深度学习、自然语言处理、计算机视觉等新兴领域的经典与最新著作。' },
  { icon: '📖', title: '文学与人文社科', desc: '中外文学名著、哲学、历史、社会学等人文素养类图书，丰富精神世界。' },
  { icon: '🔬', title: '自然科学', desc: '物理、化学、生物等自然科学基础教材与科普读物。' },
  { icon: '💼', title: '经济与管理', desc: '经济学原理、管理学、市场营销、财务会计等商科类文献资源。' }
]

const resources = [
  { title: '纸质图书', desc: '馆内可借阅的实体图书，涵盖教材、专著、工具书等。' },
  { title: '电子图书', desc: '通过校园网访问的数字化图书资源，支持在线阅读与下载。' },
  { title: '学术期刊', desc: '国内外核心期刊、学术论文数据库，支持文献检索与全文获取。' },
  { title: '多媒体资源', desc: '教学视频、有声读物、学术讲座录像等视听资料。' }
]
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
  padding: 40px 0 60px;
}

.intro-section {
  margin-bottom: 40px;
}

.intro-section h1 {
  margin: 0 0 12px;
  font-size: 28px;
  color: var(--color-primary);
}

.intro-text {
  margin: 0;
  font-size: 16px;
  color: var(--color-text-light);
  line-height: 1.8;
  max-width: 720px;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 48px;
}

.stat-card {
  padding: 28px 20px;
  border-radius: 18px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 6px;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-light);
}

.scope-section,
.resource-section {
  margin-bottom: 48px;
}

.scope-section h2,
.resource-section h2 {
  margin: 0 0 24px;
  font-size: 20px;
  color: var(--color-primary);
}

.scope-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.scope-item {
  display: flex;
  gap: 16px;
  padding: 24px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.scope-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.scope-item h3 {
  margin: 0 0 6px;
  font-size: 16px;
  color: var(--color-primary);
}

.scope-item p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-light);
  line-height: 1.6;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.resource-card {
  padding: 24px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.resource-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: var(--color-primary);
}

.resource-card p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-light);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .scope-grid,
  .resource-grid {
    grid-template-columns: 1fr;
  }
}
</style>
