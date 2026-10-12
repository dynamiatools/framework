<template>
  <header class="sticky top-0 z-40 border-b border-white/5 bg-ink-950/85 backdrop-blur">
    <div class="mx-auto flex max-w-6xl items-center gap-4 px-4 py-3">
      <RouterLink to="/" class="flex shrink-0 items-center gap-2 font-display text-xl font-bold tracking-tight">
        <img src="/favicon.svg" alt="" class="h-7 w-7" />
        <span class="hidden sm:inline">Dynamia <span class="text-gold">Movies</span></span>
      </RouterLink>
      <div class="max-w-2xl flex-1">
        <SearchBox v-model="query" />
      </div>
      <nav class="hidden items-center gap-1 text-sm text-ink-300 md:flex">
        <RouterLink :to="{ name: 'search', query: { sort: 'rating' } }" class="rounded-md px-3 py-1.5 hover:bg-ink-800 hover:text-white">Top rated</RouterLink>
        <RouterLink :to="{ name: 'search', query: { sort: 'year' } }" class="rounded-md px-3 py-1.5 hover:bg-ink-800 hover:text-white">Newest</RouterLink>
        <a :href="adminUrl" class="rounded-md px-3 py-1.5 text-ink-500 hover:bg-ink-800 hover:text-white" title="Backoffice (served by the backend)">Admin</a>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import SearchBox from './SearchBox.vue'

const route = useRoute()
const query = ref(String(route.query.q ?? ''))
/** The backoffice is served by the backend (Dynamical Vue theme). */
const adminUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8484') + '/'
watch(() => route.query.q, (q) => (query.value = String(q ?? '')))
</script>
