<template>
  <main v-if="movie">
    <section class="relative border-b border-white/5" :style="{ background: posterGradient(movie.title) }">
      <div class="absolute inset-0 bg-gradient-to-t from-ink-950 via-ink-950/60 to-ink-950/20"></div>
      <div class="relative mx-auto grid max-w-6xl gap-8 px-4 py-10 md:grid-cols-[15rem_1fr]">
        <div class="mx-auto w-48 md:w-full"><Poster :movie="movie" large /></div>
        <div>
          <p class="text-sm text-ink-300">
            <RouterLink :to="{ name: 'search', query: { year: undefined, decade: String(Math.floor(movie.year / 10) * 10) } }" class="hover:text-gold">{{ movie.year }}</RouterLink>
            <span v-if="movie.certification"> · {{ certificationLabel(movie.certification) }}</span>
            <span v-if="movie.runtime"> · {{ formatRuntime(movie.runtime) }}</span>
          </p>
          <h1 class="mt-1 font-display text-4xl font-bold tracking-tight sm:text-5xl">{{ movie.title }}</h1>
          <div class="mt-3 flex flex-wrap items-center gap-3">
            <RatingBadge :rating="movie.rating" :votes="movie.votes" class="text-lg" />
            <RouterLink v-for="name in genreNames" :key="name" :to="{ name: 'search', query: { genre: name } }" class="chip">{{ name }}</RouterLink>
          </div>
          <p class="mt-5 max-w-3xl text-lg leading-relaxed text-slate-200">{{ movie.synopsis }}</p>

          <dl class="mt-6 grid max-w-3xl grid-cols-2 gap-x-6 gap-y-3 text-sm sm:grid-cols-3">
            <div v-if="directors.length">
              <dt class="text-ink-500">Director</dt>
              <dd><RouterLink v-for="d in directors" :key="d.id" :to="{ name: 'person', params: { id: d.person.id } }" class="block hover:text-gold">{{ d.person.name }}</RouterLink></dd>
            </div>
            <div><dt class="text-ink-500">Country</dt><dd>{{ movie.country || '—' }}</dd></div>
            <div><dt class="text-ink-500">Language</dt><dd>{{ movie.language || '—' }}</dd></div>
            <div><dt class="text-ink-500">Budget</dt><dd>{{ formatMoney(movie.budget) }}</dd></div>
            <div><dt class="text-ink-500">Box office</dt><dd>{{ formatMoney(movie.boxOffice) }}</dd></div>
          </dl>
        </div>
      </div>
    </section>

    <div class="mx-auto max-w-6xl space-y-12 px-4 py-10">
      <section v-if="cast.length">
        <h2 class="mb-4 text-xl font-semibold tracking-tight">Cast</h2>
        <ul class="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
          <li v-for="credit in cast" :key="credit.id">
            <RouterLink :to="{ name: 'person', params: { id: credit.person.id } }" class="flex items-center gap-3 rounded-xl border border-ink-700 bg-ink-900 p-3 hover:border-gold/60">
              <span class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-bold" :style="{ background: posterGradient(credit.person.name) }">{{ initials(credit.person.name) }}</span>
              <span class="min-w-0">
                <span class="block truncate text-sm font-medium">{{ credit.person.name }}</span>
                <span class="block text-xs text-ink-500">#{{ credit.billing }} billed</span>
              </span>
            </RouterLink>
          </li>
        </ul>
      </section>

      <section>
        <div class="mb-4 flex items-baseline justify-between">
          <h2 class="text-xl font-semibold tracking-tight">Community reviews</h2>
          <span v-if="reviews.length" class="text-sm text-ink-500">{{ reviews.length }} reviews · average {{ averageScore }}/10</span>
        </div>
        <p v-if="!reviews.length" class="text-ink-500">No reviews yet.</p>
        <ul class="grid gap-4 md:grid-cols-2">
          <li v-for="review in reviews" :key="review.id" class="rounded-xl border border-ink-700 bg-ink-900 p-4">
            <div class="flex items-center justify-between">
              <span class="font-medium">{{ review.author }}</span>
              <span class="rounded-md bg-gold/10 px-2 py-0.5 text-sm font-semibold text-gold">{{ review.score }}/10</span>
            </div>
            <p class="mt-2 text-sm text-ink-300">{{ review.comment }}</p>
            <p class="mt-2 text-xs text-ink-500">{{ review.reviewDate }}</p>
          </li>
        </ul>
      </section>

      <MovieRail v-if="related.length" title="More like this" :movies="related" />
    </div>
  </main>

  <main v-else class="mx-auto max-w-xl px-4 py-24 text-center text-ink-300">
    <p v-if="error">{{ error }}</p>
    <p v-else>Loading…</p>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { creditsOfMovie, getMovie, reviewsOfMovie, searchMovies, type Credit, type Movie, type Review } from '../lib/api'
import { certificationLabel, formatMoney, formatRuntime, posterGradient } from '../lib/poster'
import MovieRail from '../components/MovieRail.vue'
import Poster from '../components/Poster.vue'
import RatingBadge from '../components/RatingBadge.vue'

const props = defineProps<{ id: string }>()

const movie = ref<Movie>()
const credits = ref<Credit[]>([])
const reviews = ref<Review[]>([])
const related = ref<Movie[]>([])
const error = ref('')

const genreNames = computed(() => (movie.value?.genresText ?? '').split(',').map((g) => g.trim()).filter(Boolean))
const directors = computed(() => credits.value.filter((c) => c.role === 'DIRECTOR'))
const cast = computed(() => credits.value.filter((c) => c.role === 'ACTOR'))
const averageScore = computed(() => (reviews.value.reduce((sum, r) => sum + r.score, 0) / Math.max(1, reviews.value.length)).toFixed(1))

const initials = (name: string) => name.split(' ').map((w) => w[0]).slice(0, 2).join('')

async function load(id: number) {
  movie.value = undefined
  error.value = ''
  try {
    const m = await getMovie(id)
    movie.value = m
    document.title = `${m.title} (${m.year}) · Dynamia Movies`
    const [c, r, rel] = await Promise.all([
      creditsOfMovie(id),
      reviewsOfMovie(id),
      searchMovies({ genre: genreNames.value[0], sort: 'rating', size: 12 }),
    ])
    credits.value = c
    reviews.value = r
    related.value = rel.items.filter((x) => x.id !== id).slice(0, 10)
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Movie not found'
  }
}

watch(() => props.id, (id) => load(Number(id)), { immediate: true })
</script>
