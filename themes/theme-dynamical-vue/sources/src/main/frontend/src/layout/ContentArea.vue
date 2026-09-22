<template>
  <DynamiaCrudPage v-if="node.type === 'CrudPage'" :node="node" :client="client" />

  <!--
    AdminLayout scrolls the whole page (no fixed-height content area), so an embedded page gets an
    explicit viewport-relative height and scrolls internally — instead of dynamia-embed's default
    auto-resize, meant for content snippets, not full sub-apps like an embedded ZK page. The 12rem
    covers the sticky header + the layout's vertical padding.
  -->
  <dynamia-embed
    v-else-if="embedSrc"
    :src="embedSrc"
    height="100%"
    no-resize
    class="block h-[calc(100vh-12rem)] w-full"
  />

  <div v-else class="flex h-64 items-center justify-center text-sm text-gray-400">
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
