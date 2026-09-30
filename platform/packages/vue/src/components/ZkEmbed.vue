<!-- ZkEmbed.vue: mounts a server-rendered ZK view/page inline (same origin, no iframe) -->
<template>
  <div class="dynamia-zk-embed">
    <!-- ZK owns everything inside this div: Vue never renders children into it (see mount()). -->
    <div ref="host" class="dynamia-zk-embed-host"/>
    <div v-if="loading" class="dynamia-zk-embed-loading" role="status" aria-live="polite">
      <slot name="loading">Loading…</slot>
    </div>
    <div v-if="error" class="dynamia-zk-embed-error" role="alert">
      <slot name="error" :error="error">Failed to load embedded view</slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { mountInline, type InlineHandle } from '@dynamia-tools/ui-core/embed';

const props = defineProps<{
  /**
   * Same-origin URL of a ZK view/page, e.g. `/books` or `/page-embed/library/books`.
   * Cross-origin URLs are rejected (use an iframe / `<dynamia-embed>` for those).
   * Changing it unmounts the current ZK desktop and mounts the new one.
   */
  src: string;
  /** Milliseconds before fetching / loading assets / waiting for the ZK desktop is abandoned. */
  timeout?: number;
}>();

const emit = defineEmits<{
  /** ZK desktop(s) created and mounted. */
  load: [payload: { src: string; desktopIds: readonly string[] }];
  error: [error: unknown];
}>();

const host = ref<HTMLElement | null>(null);
const loading = ref(false);
const error = ref<unknown>(null);

let handle: InlineHandle | null = null;
let pending: AbortController | null = null;

function teardown(): void {
  pending?.abort();
  pending = null;
  handle?.destroy();
  handle = null;
}

async function mount(): Promise<void> {
  teardown();
  const target = host.value;
  if (!target || !props.src) return;

  const abort = new AbortController();
  pending = abort;
  loading.value = true;
  error.value = null;

  try {
    const mounted = await mountInline(target, props.src, { signal: abort.signal, ...(props.timeout ? { timeoutMs: props.timeout } : {}) });
    if (abort.signal.aborted) {
      mounted.destroy();
      return;
    }
    handle = mounted;
    emit('load', { src: props.src, desktopIds: mounted.desktopIds });
  } catch (e) {
    if (abort.signal.aborted) return; // superseded by a newer mount or unmounted: not an error
    error.value = e;
    emit('error', e);
  } finally {
    if (pending === abort) {
      pending = null;
      loading.value = false;
    }
  }
}

onMounted(mount);
watch(() => props.src, mount);
onBeforeUnmount(teardown);

defineExpose({
  /** Unmounts and mounts again (e.g. after a server-side change). */
  reload: mount,
  /** ZK desktop ids of the current mount (empty while loading). */
  desktopIds: (): readonly string[] => handle?.desktopIds ?? [],
});
</script>
