<template>
  <footer class="mt-6 flex items-center justify-between border-t border-gray-200 pt-4 text-xs text-gray-400 dark:border-gray-800 dark:text-gray-500">
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
