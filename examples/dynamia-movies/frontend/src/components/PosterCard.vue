<template>
  <RouterLink :to="{ name: 'movie', params: { id: movie.id } }" class="group block w-full">
    <div class="transition-transform duration-200 group-hover:-translate-y-1">
      <Poster :movie="movie" />
    </div>
    <div class="mt-2 px-0.5">
      <RatingBadge :rating="movie.rating" class="text-sm" />
      <h3 class="mt-0.5 line-clamp-2 text-sm leading-snug font-medium text-slate-100 group-hover:text-gold">{{ movie.title }}</h3>
      <p class="text-xs text-ink-500">{{ movie.year }}</p>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
import type { Movie } from '../lib/api'
import Poster from './Poster.vue'
import RatingBadge from './RatingBadge.vue'

defineProps<{ movie: Movie }>()
</script>
