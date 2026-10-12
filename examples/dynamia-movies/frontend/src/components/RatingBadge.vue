<template>
  <span class="inline-flex items-center gap-1 font-semibold text-gold" :title="`${rating} / 10`">
    <svg viewBox="0 0 20 20" class="h-4 w-4 fill-current" aria-hidden="true"><path d="M10 1.5l2.6 5.5 6 .8-4.4 4.1 1.1 5.9L10 14.9 4.7 17.8l1.1-5.9L1.4 7.8l6-.8z" /></svg>
    {{ rating.toFixed(1) }}
    <span v-if="votes" class="text-xs font-normal text-ink-500">({{ formatVotes(votes) }})</span>
  </span>
</template>

<script setup lang="ts">
import { formatVotes } from '../lib/poster'

defineProps<{ rating: number; votes?: number }>()
</script>
