<template>
  <article class="recommendation-card">
    <div class="recommendation-card__hover-bridge" aria-hidden="true"></div>

    <div class="recommendation-card__bubble" aria-label="读者论坛评论">
      <div class="recommendation-card__bubble-header">论坛热评</div>
      <div class="recommendation-card__danmaku">
        <div
          v-for="comment in book.comments"
          :key="comment.id"
          class="recommendation-card__comment"
          :style="commentStyle(comment)"
          :title="`${comment.authorName}：${comment.content}`"
        >
          <strong>{{ comment.authorName }}</strong>
          <span>{{ comment.content }}</span>
        </div>
      </div>
    </div>

    <div class="recommendation-card__meta">
      <span>{{ book.tag }}</span>
      <span>{{ book.location }}</span>
    </div>

    <div class="recommendation-card__body">
      <div class="recommendation-card__content">
        <h3>{{ book.title }}</h3>
        <p class="recommendation-card__author">{{ book.author }}</p>
        <p class="recommendation-card__description">{{ book.description }}</p>
      </div>

      <div class="recommendation-card__cover" aria-hidden="true">
        <span>{{ book.cover }}</span>
      </div>
    </div>

    <router-link :to="`/book/${book.bookId}`">查看详情</router-link>
  </article>
</template>

<script setup>
defineProps({
  book: {
    type: Object,
    required: true,
  },
})

const commentStyle = (comment) => ({
  '--comment-duration': comment.duration,
  '--comment-delay': comment.delay,
  '--comment-top': `${comment.lane * 42 + 10}px`,
})
</script>
