<template>
  <main>
    <section class="relative overflow-hidden">
      <div class="pointer-events-none absolute inset-0 bg-[radial-gradient(60%_50%_at_50%_0%,rgba(251,191,36,0.14),transparent_70%)]"></div>
      <div class="relative mx-auto flex max-w-3xl flex-col items-center px-4 pt-20 pb-12 text-center sm:pt-28">
        <img src="/favicon.svg" alt="" class="mb-4 h-14 w-14" />
        <h1 class="font-display text-5xl font-bold tracking-tight sm:text-6xl">Dynamia <span class="text-gold">Movies</span></h1>
        <p class="mt-3 text-ink-300">
          Search {{ total || 'the' }} movies by title, director, actor, genre or year.
        </p>
        <div class="mt-8 w-full">
          <SearchBox large />
        </div>
        <div class="mt-6 flex flex-wrap justify-center gap-2">
          <RouterLink v-for="g in genres" :key="g.id" :to="{ name: 'search', query: { genre: g.name } }" class="chip">{{ g.name }}</RouterLink>
        </div>
        <p v-if="error" class="mt-6 rounded-lg border border-red-500/30 bg-red-500/10 px-4 py-2 text-sm text-red-300">
          Could not reach the backend: {{ error }}. Start it with <code>./mvnw spring-boot:run</code> in <code>backend/</code>.
        </p>
      </div>
    </section>

    <div class="mx-auto max-w-6xl space-y-10 px-4">
      <MovieRail title="Featured" :movies="featured" :loading="loading" :to="{ name: 'search', query: { featured: '1' } }" />
      <MovieRail title="Top rated" :movies="topRated" :loading="loading" :to="{ name: 'search', query: { sort: 'rating' } }" />
      <MovieRail title="Newest" :movies="newest" :loading="loading" :to="{ name: 'search', query: { sort: 'year' } }" />
      <MovieRail title="Sci-Fi" :movies="scifi" :loading="loading" :to="{ name: 'search', query: { genre: 'Sci-Fi' } }" />
      <MovieRail title="Animation" :movies="animation" :loading="loading" :to="{ name: 'search', query: { genre: 'Animation' } }" />

      <section>
        <h2 class="mb-3 text-xl font-semibold tracking-tight">Browse by decade</h2>
        <div class="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-5">
          <RouterLink
            v-for="decade in decades"
            :key="decade"
            :to="{ name: 'search', query: { decade: String(decade), sort: 'rating' } }"
            class="rounded-xl border border-ink-700 bg-ink-900 px-4 py-5 text-center transition hover:-translate-y-0.5 hover:border-gold/60"
          >
            <span class="font-display text-2xl font-bold text-gold">{{ decade }}s</span>
          </RouterLink>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { countMovies, listGenres, searchMovies, type Genre, type Movie } from '../lib/api'
import MovieRail from '../components/MovieRail.vue'
import SearchBox from '../components/SearchBox.vue'

const loading = ref(true)
const error = ref('')
const total = ref(0)
const genres = ref<Genre[]>([])
const featured = ref<Movie[]>([])
const topRated = ref<Movie[]>([])
const newest = ref<Movie[]>([])
const scifi = ref<Movie[]>([])
const animation = ref<Movie[]>([])
const decades = [1930, 1940, 1950, 1960, 1970, 1980, 1990, 2000, 2010, 2020]

onMounted(async () => {
  try {
    const [g, f, t, n, s, a, count] = await Promise.all([
      listGenres(),
      searchMovies({ featured: true, sort: 'votes', size: 12 }),
      searchMovies({ sort: 'rating', size: 12 }),
      searchMovies({ sort: 'year', size: 12 }),
      searchMovies({ genre: 'Sci-Fi', sort: 'rating', size: 12 }),
      searchMovies({ genre: 'Animation', sort: 'rating', size: 12 }),
      countMovies(),
    ])
    genres.value = g
    featured.value = f.items
    topRated.value = t.items
    newest.value = n.items
    scifi.value = s.items
    animation.value = a.items
    total.value = count
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
})
</script>
