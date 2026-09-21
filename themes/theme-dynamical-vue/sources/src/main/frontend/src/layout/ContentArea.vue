<template>
  <DynamiaCrudPage v-if="node.type === 'CrudPage'" :node="node" :client="client" class="h-full" />

  <!--
    height="100%" + no-resize: this theme's content area has a fixed height (flex-1 inside
    <main>), so embedded pages should fill it and scroll internally — not grow the iframe to
    match content height (dynamia-embed's default auto-resize behavior, meant for content
    snippets, not full sub-apps like an embedded ZK page).
  -->
  <dynamia-embed v-else-if="embedSrc" :src="embedSrc" height="100%" no-resize class="block h-full w-full" />

  <div v-else class="flex h-full items-center justify-center text-sm text-slate-400">
    <p>
      Page <code>{{ node.name }}</code> (type <code>{{ node.type }}</code>) has no embeddable URL —
      set its <code>file</code> to a real HTTP resource to use it with this theme.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { DynamiaClient, NavigationNode } from '@dynamia-tools/sdk';
import { resolveEmbedSrc } from '../lib/resolveEmbedSrc.js';

const props = defineProps<{
  node: NavigationNode;
  client: DynamiaClient;
}>();

const embedSrc = computed(() => resolveEmbedSrc(props.node));
</script>
