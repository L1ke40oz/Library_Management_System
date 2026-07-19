<template>
  <div class="page-shell">
    <header class="page-header">
      <div class="container page-header__inner">
        <div class="page-header-left">
          <a href="#" class="back-link" @click.prevent="$router.back()">&larr; 返回</a>
          <router-link to="/" class="page-logo">书院图书馆</router-link>
        </div>
        <span class="page-title">读者服务</span>
      </div>
    </header>

    <main class="container page-main">
      <section class="intro-section">
        <h1>读者服务指南</h1>
        <p class="intro-text">
          书院图书馆为全校师生提供多元化的阅读与学术服务，助力学习与研究。
        </p>
      </section>

      <section class="services-grid">
        <div class="service-card" v-for="service in services" :key="service.title">
          <span class="service-icon">{{ service.icon }}</span>
          <h3>{{ service.title }}</h3>
          <p>{{ service.desc }}</p>
        </div>
      </section>

      <section class="rules-section">
        <h2>借阅规则</h2>
        <div class="rules-card">
          <ul>
            <li v-for="rule in rules" :key="rule">{{ rule }}</li>
          </ul>
        </div>
      </section>

      <section class="hours-section">
        <h2>开放时间</h2>
        <div class="hours-grid">
          <div class="hours-card" v-for="item in hours" :key="item.day">
            <span class="hours-day">{{ item.day }}</span>
            <span class="hours-time">{{ item.time }}</span>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
const services = [
  { icon: '📚', title: '图书借阅', desc: '凭校园卡或系统账号借阅馆藏图书，每人最多借阅5本，借期30天，可续借一次。' },
  { icon: '🔍', title: '馆藏检索', desc: '通过线上系统检索馆藏图书，支持按书名、作者、ISBN、分类等多维度搜索。' },
  { icon: '📖', title: '阅览室服务', desc: '提供安静舒适的自习与阅读空间，配备充电插座与无线网络。' },
  { icon: '🖨️', title: '打印复印', desc: '一楼服务台提供自助打印、复印、扫描服务，支持校园卡支付。' },
  { icon: '💡', title: '参考咨询', desc: '专业馆员提供文献检索指导、学术资源推荐、论文写作辅助等咨询服务。' },
  { icon: '🎓', title: '学术讲座', desc: '定期举办信息素养培训、数据库使用讲座、读书分享会等学术活动。' },
  { icon: '📱', title: '移动服务', desc: '支持手机端查询馆藏、续借图书、预约座位等便捷功能。' },
  { icon: '🏠', title: '馆际互借', desc: '与兄弟院校图书馆合作，提供馆际互借与文献传递服务。' }
]

const rules = [
  '每位读者最多同时借阅 5 本图书',
  '借阅期限为 30 天，到期前可续借一次（延长15天）',
  '逾期未还将暂停借阅权限，归还后自动恢复',
  '图书如有损坏或遗失，需按规定赔偿',
  '工具书、期刊、特藏文献仅限馆内阅览，不外借',
  '寒暑假期间借阅的图书，还书日期自动顺延至开学后一周'
]

const hours = [
  { day: '周一至周五', time: '08:00 - 22:00' },
  { day: '周六', time: '09:00 - 21:00' },
  { day: '周日', time: '09:00 - 18:00' },
  { day: '法定节假日', time: '10:00 - 16:00' }
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
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin-bottom: 48px;
}

.service-card {
  padding: 28px;
  border-radius: 18px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.service-icon {
  font-size: 32px;
  display: block;
  margin-bottom: 12px;
}

.service-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: var(--color-primary);
}

.service-card p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-light);
  line-height: 1.6;
}

.rules-section,
.hours-section {
  margin-bottom: 48px;
}

.rules-section h2,
.hours-section h2 {
  margin: 0 0 20px;
  font-size: 20px;
  color: var(--color-primary);
}

.rules-card {
  padding: 28px 32px;
  border-radius: 18px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.rules-card ul {
  list-style: disc;
  padding-left: 20px;
  margin: 0;
}

.rules-card li {
  padding: 8px 0;
  font-size: 15px;
  color: var(--color-text-light);
  line-height: 1.6;
}

.hours-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}

.hours-card {
  padding: 20px;
  border-radius: 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  text-align: center;
}

.hours-day {
  display: block;
  font-size: 14px;
  color: var(--color-text-light);
  margin-bottom: 6px;
}

.hours-time {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .services-grid {
    grid-template-columns: 1fr;
  }
}
</style>
