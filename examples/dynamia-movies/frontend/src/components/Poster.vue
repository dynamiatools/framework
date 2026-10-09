<template>
  <div
    class="relative aspect-[2/3] w-full overflow-hidden rounded-lg border border-white/5 shadow-lg shadow-black/40"
    :style="movie.posterUrl ? undefined : { background: posterGradient(movie.title) }"
  >
    <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title" class="h-full w-full object-cover" loading="lazy" />
    <div v-else class="flex h-full flex-col justify-between p-3">
      <span class="text-[10px] font-semibold tracking-[0.2em] text-white/50 uppercase">{{ movie.genresText?.split(',')[0] }}</span>
      <span class="font-display leading-tight font-bold text-white drop-shadow" :class="large ? 'text-3xl' : 'text-base'">{{ movie.title }}</span>
      <span class="text-xs text-white/60">{{ movie.year }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Movie } from '../lib/api'
import { posterGradient } from '../lib/poster'

defineProps<{ movie: Pick<Movie, 'title' | 'year' | 'posterUrl' | 'genresText'>; large?: boolean }>()
</script>
