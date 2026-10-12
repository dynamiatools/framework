<template>
  <section>
    <div class="mb-3 flex items-end justify-between gap-4">
      <h2 class="text-xl font-semibold tracking-tight">{{ title }}</h2>
      <RouterLink v-if="to" :to="to" class="text-sm text-ink-300 hover:text-gold">See all →</RouterLink>
    </div>
    <div v-if="loading" class="flex gap-4 overflow-hidden">
      <div v-for="n in 7" :key="n" class="aspect-[2/3] w-36 shrink-0 animate-pulse rounded-lg bg-ink-800 sm:w-40"></div>
    </div>
    <div v-else class="scroll-hide -mx-4 flex gap-4 overflow-x-auto px-4 pb-2">
      <div v-for="movie in movies" :key="movie.id" class="w-36 shrink-0 sm:w-40">
        <PosterCard :movie="movie" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router'
import type { Movie } from '../lib/api'
import PosterCard from './PosterCard.vue'

defineProps<{ title: string; movies: Movie[]; loading?: boolean; to?: RouteLocationRaw }>()
</script>
