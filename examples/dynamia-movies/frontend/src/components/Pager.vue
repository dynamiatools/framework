<template>
  <nav v-if="pages > 1" class="flex flex-wrap items-center justify-center gap-1 text-sm" aria-label="Pagination">
    <button class="rounded-md px-3 py-1.5 text-ink-300 hover:bg-ink-800 disabled:opacity-30" :disabled="page <= 1" @click="$emit('change', page - 1)">‹ Prev</button>
    <button
      v-for="n in visible"
      :key="n"
      class="min-w-9 rounded-md px-3 py-1.5"
      :class="n === page ? 'bg-gold font-semibold text-ink-950' : 'text-ink-300 hover:bg-ink-800'"
      @click="$emit('change', n)"
    >
      {{ n }}
    </button>
    <button class="rounded-md px-3 py-1.5 text-ink-300 hover:bg-ink-800 disabled:opacity-30" :disabled="page >= pages" @click="$emit('change', page + 1)">Next ›</button>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ page: number; pages: number }>()
defineEmits<{ (e: 'change', page: number): void }>()

const visible = computed(() => {
  const from = Math.max(1, Math.min(props.page - 2, props.pages - 4))
  const to = Math.min(props.pages, from + 4)
  return Array.from({ length: to - from + 1 }, (_, i) => from + i)
})
</script>
