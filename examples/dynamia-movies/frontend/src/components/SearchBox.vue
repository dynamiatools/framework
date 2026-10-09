<template>
  <form class="relative w-full" role="search" @submit.prevent="submit">
    <div
      class="flex items-center gap-3 rounded-full border border-ink-700 bg-ink-900 px-5 shadow-lg shadow-black/30 transition focus-within:border-gold/70 focus-within:bg-ink-800"
      :class="large ? 'h-14 text-lg' : 'h-11'"
    >
      <svg viewBox="0 0 24 24" class="h-5 w-5 shrink-0 fill-none stroke-ink-500" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7" /><path d="M20 20l-3.5-3.5" /></svg>
      <input
        v-model="text"
        type="search"
        autocomplete="off"
        :placeholder="placeholder"
        class="h-full min-w-0 flex-1 bg-transparent outline-none placeholder:text-ink-500"
        @input="onInput"
        @focus="open = true"
        @blur="closeSoon"
        @keydown.down.prevent="move(1)"
        @keydown.up.prevent="move(-1)"
        @keydown.esc="open = false"
      />
      <button v-if="text" type="button" class="text-ink-500 hover:text-white" aria-label="Clear" @click="clear">✕</button>
    </div>

    <ul
      v-if="open && suggestions.length"
      class="absolute inset-x-3 z-30 mt-2 overflow-hidden rounded-2xl border border-ink-700 bg-ink-900/95 shadow-2xl backdrop-blur"
    >
      <li v-for="(movie, index) in suggestions" :key="movie.id">
        <RouterLink
          :to="{ name: 'movie', params: { id: movie.id } }"
          class="flex items-center gap-3 px-4 py-2.5 text-sm hover:bg-ink-800"
          :class="{ 'bg-ink-800': index === active }"
          @mousedown.prevent="go(movie.id)"
        >
          <span class="w-10 shrink-0 text-ink-500">{{ movie.year }}</span>
          <span class="min-w-0 flex-1 truncate">{{ movie.title }}</span>
          <span class="hidden truncate text-xs text-ink-500 sm:block">{{ movie.directorName }}</span>
          <RatingBadge :rating="movie.rating" class="text-xs" />
        </RouterLink>
      </li>
      <li class="border-t border-ink-700 px-4 py-2 text-xs text-ink-500">Press Enter to search “{{ text }}”</li>
    </ul>
  </form>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { suggestMovies, type Movie } from '../lib/api'
import RatingBadge from './RatingBadge.vue'

const props = withDefaults(defineProps<{ modelValue?: string; large?: boolean; placeholder?: string }>(), {
  modelValue: '',
  placeholder: 'Search by title, director, actor, genre or year…',
})
const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const router = useRouter()
const text = ref(props.modelValue)
const suggestions = ref<Movie[]>([])
const open = ref(false)
const active = ref(-1)
let timer: ReturnType<typeof setTimeout> | undefined
let requestId = 0

watch(() => props.modelValue, (value) => (text.value = value))
onBeforeUnmount(() => clearTimeout(timer))

function onInput() {
  emit('update:modelValue', text.value)
  clearTimeout(timer)
  active.value = -1
  timer = setTimeout(async () => {
    const id = ++requestId
    const found = text.value.trim().length >= 2 ? await suggestMovies(text.value).catch(() => []) : []
    if (id === requestId) suggestions.value = found
  }, 180)
}

function move(step: number) {
  if (!suggestions.value.length) return
  active.value = (active.value + step + suggestions.value.length) % suggestions.value.length
}

function go(id: number) {
  open.value = false
  router.push({ name: 'movie', params: { id } })
}

function submit() {
  const selected = suggestions.value[active.value]
  if (selected) return go(selected.id)
  open.value = false
  router.push({ name: 'search', query: { q: text.value.trim() || undefined } })
}

function clear() {
  text.value = ''
  suggestions.value = []
  emit('update:modelValue', '')
}

function closeSoon() {
  setTimeout(() => (open.value = false), 120)
}
</script>
