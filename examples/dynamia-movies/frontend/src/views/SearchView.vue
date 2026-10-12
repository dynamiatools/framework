<template>
  <main class="mx-auto grid max-w-6xl gap-8 px-4 py-6 lg:grid-cols-[14rem_1fr]">
    <aside class="space-y-6 text-sm lg:sticky lg:top-20 lg:self-start">
      <div>
        <h2 class="mb-2 text-xs font-semibold tracking-widest text-ink-500 uppercase">Sort by</h2>
        <div class="flex flex-wrap gap-2 lg:flex-col lg:gap-1">
          <button v-for="option in sorts" :key="option.key" class="chip lg:justify-start" :class="{ 'chip-active': sort === option.key }" @click="update({ sort: option.key })">
            {{ option.label }}
          </button>
        </div>
      </div>

      <div>
        <h2 class="mb-2 text-xs font-semibold tracking-widest text-ink-500 uppercase">Genre</h2>
        <div class="flex flex-wrap gap-2">
          <button v-for="g in genres" :key="g.id" class="chip" :class="{ 'chip-active': genre === g.name }" @click="update({ genre: genre === g.name ? undefined : g.name })">
            {{ g.name }}
          </button>
        </div>
      </div>

      <div>
        <h2 class="mb-2 text-xs font-semibold tracking-widest text-ink-500 uppercase">Decade</h2>
        <div class="flex flex-wrap gap-2">
          <button v-for="d in decades" :key="d" class="chip" :class="{ 'chip-active': decade === d }" @click="update({ decade: decade === d ? undefined : String(d) })">
            {{ d }}s
          </button>
        </div>
      </div>

      <div>
        <h2 class="mb-2 text-xs font-semibold tracking-widest text-ink-500 uppercase">Certification</h2>
        <div class="flex flex-wrap gap-2">
          <button v-for="c in certifications" :key="c" class="chip" :class="{ 'chip-active': certification === c }" @click="update({ cert: certification === c ? undefined : c })">
            {{ certificationLabel(c) }}
          </button>
        </div>
      </div>

      <button v-if="hasFilters" class="text-ink-300 underline-offset-2 hover:text-gold hover:underline" @click="clearAll">Clear all filters</button>
    </aside>

    <section>
      <p class="mb-4 text-sm text-ink-500">
        <template v-if="!loading">{{ results.total === 1 ? '1 result' : `About ${results.total.toLocaleString()} results` }} ({{ (results.tookMs / 1000).toFixed(2) }} seconds)</template>
        <template v-else>Searching…</template>
        <span v-if="text"> for <strong class="text-slate-200">“{{ text }}”</strong></span>
      </p>

      <p v-if="error" class="rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-300">{{ error }}</p>

      <div v-else-if="!loading && !results.items.length" class="rounded-xl border border-dashed border-ink-700 py-16 text-center text-ink-300">
        <p class="text-lg">No movies match your search.</p>
        <p class="mt-1 text-sm text-ink-500">Try fewer words or remove a filter.</p>
      </div>

      <ol class="space-y-6">
        <li v-for="movie in results.items" :key="movie.id" class="flex gap-4">
          <RouterLink :to="{ name: 'movie', params: { id: movie.id } }" class="hidden w-20 shrink-0 sm:block">
            <Poster :movie="movie" />
          </RouterLink>
          <div class="min-w-0 flex-1">
            <p class="truncate text-xs text-ink-500">
              dynamia.movies › {{ movie.year }}
              <template v-if="movie.genresText"> › {{ movie.genresText }}</template>
            </p>
            <RouterLink :to="{ name: 'movie', params: { id: movie.id } }" class="mt-0.5 block text-xl leading-snug text-sky-400 visited:text-violet-400 hover:underline">
              <span v-html="mark(movie.title)"></span> <span class="text-ink-500">({{ movie.year }})</span>
            </RouterLink>
            <p class="mt-1 line-clamp-2 text-sm text-ink-300" v-html="mark(movie.synopsis ?? '')"></p>
            <p class="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-ink-500">
              <RatingBadge :rating="movie.rating" :votes="movie.votes" />
              <span v-if="movie.directorName">Dir. <span v-html="mark(movie.directorName)"></span></span>
              <span v-if="movie.topCast" class="truncate" v-html="mark(movie.topCast)"></span>
              <span v-if="movie.runtime">{{ formatRuntime(movie.runtime) }}</span>
              <span v-if="movie.certification" class="rounded border border-ink-700 px-1">{{ certificationLabel(movie.certification) }}</span>
            </p>
          </div>
        </li>
      </ol>

      <div class="mt-10">
        <Pager :page="page" :pages="pages" @change="(n) => update({ page: String(n) }, false)" />
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter, type LocationQueryRaw } from 'vue-router'
import { listGenres, searchMovies, termsOf, type Genre, type MovieResults, type SortKey } from '../lib/api'
import { certificationLabel, formatRuntime, highlight } from '../lib/poster'
import Pager from '../components/Pager.vue'
import Poster from '../components/Poster.vue'
import RatingBadge from '../components/RatingBadge.vue'

const PAGE_SIZE = 10
const route = useRoute()
const router = useRouter()

const sorts: { key: SortKey; label: string }[] = [
  { key: 'rating', label: 'Best rated' },
  { key: 'votes', label: 'Most voted' },
  { key: 'year', label: 'Newest first' },
  { key: 'year_asc', label: 'Oldest first' },
  { key: 'title', label: 'Title A–Z' },
]
const decades = [1930, 1940, 1950, 1960, 1970, 1980, 1990, 2000, 2010, 2020]
const certifications = ['G', 'PG', 'PG_13', 'R', 'NC_17']

const genres = ref<Genre[]>([])
const loading = ref(true)
const error = ref('')
const results = ref<MovieResults>({ items: [], total: 0, tookMs: 0 })

const text = computed(() => String(route.query.q ?? '').trim())
const genre = computed(() => (route.query.genre ? String(route.query.genre) : undefined))
const decade = computed(() => (route.query.decade ? Number(route.query.decade) : undefined))
const certification = computed(() => (route.query.cert ? String(route.query.cert) : undefined))
const featured = computed(() => route.query.featured === '1')
const sort = computed<SortKey>(() => (sorts.some((s) => s.key === route.query.sort) ? (route.query.sort as SortKey) : 'rating'))
const page = computed(() => Math.max(1, Number(route.query.page ?? 1) || 1))
const pages = computed(() => Math.ceil(results.value.total / PAGE_SIZE))
const hasFilters = computed(() => !!(genre.value || decade.value || certification.value || featured.value))
const terms = computed(() => termsOf(text.value))

const mark = (value: string) => highlight(value, terms.value)

function update(changes: Record<string, string | undefined>, resetPage = true) {
  const query: LocationQueryRaw = { ...route.query, ...changes }
  if (resetPage && !('page' in changes)) delete query.page
  for (const key of Object.keys(query)) if (query[key] === undefined || query[key] === '') delete query[key]
  router.push({ name: 'search', query })
}

function clearAll() {
  router.push({ name: 'search', query: text.value ? { q: text.value } : {} })
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    results.value = await searchMovies({
      text: text.value,
      genre: genre.value,
      decade: decade.value,
      certification: certification.value,
      featured: featured.value || undefined,
      sort: sort.value,
      page: page.value,
      size: PAGE_SIZE,
    })
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

listGenres().then((g) => (genres.value = g)).catch(() => undefined)
watch(() => route.fullPath, load, { immediate: true })
</script>
