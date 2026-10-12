<template>
  <ul class="feed">
    <li v-for="(review, index) in reviews" :key="index">
      <div class="feed-top">
        <strong>{{ review.movie }}</strong>
        <span class="score">{{ review.score }}/10</span>
      </div>
      <p>{{ review.comment }}</p>
      <small>{{ review.author }} · {{ review.date }}</small>
    </li>
    <li v-if="!reviews.length" class="empty">No reviews yet.</li>
  </ul>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface FeedReview {
  movie: string
  author: string
  score: number
  comment: string
  date: string
}

// Custom widget renderers receive the widget data (and the whole response) as props.
const props = defineProps<{ data?: unknown }>()
const reviews = computed(() => (Array.isArray(props.data) ? (props.data as FeedReview[]) : []))
</script>
