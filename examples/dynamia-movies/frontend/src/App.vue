<template>
  <SiteHeader v-if="!isHome" />
  <RouterView v-slot="{ Component }">
    <component :is="Component" />
  </RouterView>
  <footer class="mt-16 border-t border-white/5 py-8 text-center text-xs text-ink-500">
    Dynamia Movies · demo built with <a class="text-ink-300 hover:text-gold" href="https://dynamia.tools" target="_blank" rel="noopener">DynamiaTools</a>.
    Ratings, votes and figures are approximate sample data.
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SiteHeader from './components/SiteHeader.vue'

const route = useRoute()
const isHome = computed(() => route.name === 'home')
</script>
