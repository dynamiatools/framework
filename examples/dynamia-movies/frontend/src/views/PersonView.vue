<template>
  <main v-if="person" class="mx-auto max-w-6xl px-4 py-10">
    <header class="flex items-center gap-5">
      <span class="flex h-20 w-20 shrink-0 items-center justify-center rounded-full text-2xl font-bold" :style="{ background: posterGradient(person.name) }">
        {{ person.name.split(' ').map((w) => w[0]).slice(0, 2).join('') }}
      </span>
      <div>
        <h1 class="font-display text-4xl font-bold tracking-tight">{{ person.name }}</h1>
        <p class="mt-1 text-ink-300">
          {{ filmography.length }} movies in the catalog<span v-if="roles"> · {{ roles }}</span>
          <span v-if="person.birthYear"> · born {{ person.birthYear }}</span>
        </p>
      </div>
    </header>

    <p v-if="person.biography" class="mt-6 max-w-3xl text-ink-300">{{ person.biography }}</p>

    <h2 class="mt-10 mb-4 text-xl font-semibold tracking-tight">Filmography</h2>
    <ul class="divide-y divide-ink-800 rounded-xl border border-ink-700 bg-ink-900">
      <li v-for="credit in filmography" :key="credit.id">
        <RouterLink :to="{ name: 'movie', params: { id: credit.movie.id } }" class="flex items-center gap-4 px-4 py-3 hover:bg-ink-800">
          <span class="w-12 shrink-0 text-sm text-ink-500">{{ credit.movie.year }}</span>
          <span class="min-w-0 flex-1">
            <span class="block truncate font-medium">{{ credit.movie.title }}</span>
            <span class="block truncate text-xs text-ink-500">{{ credit.movie.genresText }}</span>
          </span>
          <span class="rounded-full border border-ink-700 px-2 py-0.5 text-xs text-ink-300">{{ credit.role === 'DIRECTOR' ? 'Director' : 'Actor' }}</span>
          <RatingBadge v-if="credit.movie.rating" :rating="credit.movie.rating" class="text-sm" />
        </RouterLink>
      </li>
    </ul>
  </main>
  <main v-else class="mx-auto max-w-xl px-4 py-24 text-center text-ink-300">{{ error || 'Loading…' }}</main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { creditsOfPerson, getPerson, type Credit, type Person } from '../lib/api'
import { posterGradient } from '../lib/poster'
import RatingBadge from '../components/RatingBadge.vue'

const props = defineProps<{ id: string }>()

const person = ref<Person>()
const credits = ref<Credit[]>([])
const error = ref('')

const filmography = computed(() => [...credits.value].sort((a, b) => (b.movie.year ?? 0) - (a.movie.year ?? 0)))
const roles = computed(() => [...new Set(credits.value.map((c) => (c.role === 'DIRECTOR' ? 'Director' : 'Actor')))].join(' & '))

async function load(id: number) {
  person.value = undefined
  error.value = ''
  try {
    const [p, c] = await Promise.all([getPerson(id), creditsOfPerson(id)])
    person.value = p
    credits.value = c
    document.title = `${p.name} · Dynamia Movies`
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Person not found'
  }
}

watch(() => props.id, (id) => load(Number(id)), { immediate: true })
</script>
