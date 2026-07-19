<template>
  <div class="page-shell">
    <header class="page-header">
      <div class="container page-header__inner">
        <div class="page-header-left">
          <a href="#" class="back-link" @click.prevent="$router.back()">&larr; 返回</a>
          <router-link to="/" class="page-logo">书院图书馆</router-link>
        </div>
        <span class="page-title">公告资讯</span>
      </div>
    </header>

    <main class="container page-main">
      <section class="intro-section">
        <h1>公告与资讯</h1>
        <p class="intro-text">图书馆最新通知、活动预告与服务动态。</p>
      </section>

      <div class="news-list">
        <article class="news-item" v-for="item in newsList" :key="item.id">
          <div class="news-date">
            <span class="news-day">{{ item.day }}</span>
            <span class="news-month">{{ item.month }}</span>
          </div>
          <div class="news-content">
            <span class="news-tag" :class="item.type">{{ item.tagText }}</span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.summary }}</p>
          </div>
        </article>
      </div>
    </main>
  </div>
</template>

<script setup>
const newsList = [
  {
    id: 1,
    day: '10',
    month: '2026-05',
    type: 'notice',
    tagText: '通知',
    title: '关于2026年暑期图书馆开放时间调整的通知',
    summary: '暑假期间（7月15日-8月31日），图书馆开放时间调整为每日9:00-17:00，请各位读者合理安排借阅时间。'
  },
  {
    id: 2,
    day: '08',
    month: '2026-05',
    type: 'activity',
    tagText: '活动',
    title: '「数字资源使用导引」专题讲座',
    summary: '本周六下午2:00，图书馆三楼报告厅将举办数字资源使用培训，涵盖知网、万方、Web of Science等数据库的高效检索技巧。'
  },
  {
    id: 3,
    day: '05',
    month: '2026-05',
    type: 'notice',
    tagText: '通知',
    title: '新书上架通知 —— 2026年春季第三批',
    summary: '本批次新增图书120余册，涵盖人工智能、数据科学、文学经典等领域，欢迎读者前来借阅。'
  },
  {
    id: 4,
    day: '01',
    month: '2026-05',
    type: 'activity',
    tagText: '活动',
    title: '「世界读书日」系列活动回顾',
    summary: '4月23日世界读书日期间，图书馆成功举办读书马拉松、经典诵读、图书漂流等活动，累计参与师生500余人次。'
  },
  {
    id: 5,
    day: '25',
    month: '2026-04',
    type: 'service',
    tagText: '服务',
    title: '自助借还机全面升级完成',
    summary: '一楼大厅自助借还设备已完成系统升级，新增人脸识别功能，借还效率提升50%，欢迎体验。'
  },
  {
    id: 6,
    day: '20',
    month: '2026-04',
    type: 'notice',
    tagText: '通知',
    title: '关于清理超期未还图书的通知',
    summary: '请借阅超期的读者尽快归还图书，超期30天以上将暂停借阅权限。可通过个人中心查看借阅状态。'
  }
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
  margin-bottom: 36px;
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
}

.news-list {
  display: grid;
  gap: 16px;
}

.news-item {
  display: flex;
  gap: 24px;
  padding: 28px;
  border-radius: 18px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  transition: transform 0.2s, box-shadow 0.2s;
}

.news-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(31, 58, 86, 0.08);
}

.news-date {
  flex-shrink: 0;
  width: 64px;
  text-align: center;
}

.news-day {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
}

.news-month {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-light);
}

.news-content {
  flex: 1;
  min-width: 0;
}

.news-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 8px;
}

.news-tag.notice {
  background: rgba(31, 58, 86, 0.08);
  color: var(--color-primary);
}

.news-tag.activity {
  background: rgba(154, 123, 79, 0.12);
  color: var(--color-accent);
}

.news-tag.service {
  background: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.news-content h3 {
  margin: 0 0 8px;
  font-size: 17px;
  color: var(--color-primary);
  line-height: 1.4;
}

.news-content p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-light);
  line-height: 1.7;
}

@media (max-width: 768px) {
  .news-item {
    flex-direction: column;
    gap: 12px;
  }

  .news-date {
    display: flex;
    align-items: baseline;
    gap: 8px;
    width: auto;
  }

  .news-day {
    font-size: 24px;
  }
}
</style>
