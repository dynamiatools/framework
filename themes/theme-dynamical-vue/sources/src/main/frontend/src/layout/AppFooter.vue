<template>
  <footer class="flex items-center justify-between border-t border-slate-200 bg-white px-4 py-2 text-xs text-slate-400">
    <span>{{ appInfo?.name ?? 'Dynamia' }}<template v-if="appInfo?.version"> · v{{ appInfo.version }}</template></span>
    <span>
      Powered by
      <a href="https://dynamia.tools" target="_blank" rel="noopener" class="hover:underline">Dynamia Tools</a>
    </span>
  </footer>
</template>

<script setup lang="ts">
import type { ApplicationMetadata } from '@dynamia-tools/sdk';

defineProps<{
  appInfo?: ApplicationMetadata | null;
}>();
</script>
