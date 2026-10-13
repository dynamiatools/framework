<!-- Viewer.vue: Universal view host component — primary component for consumers -->
<template>
  <div class="dynamia-viewer">
    <div v-if="loading" class="dynamia-viewer-loading">
      <slot name="loading">
        <span>Loading...</span>
      </slot>
    </div>
    <div v-else-if="error" class="dynamia-viewer-error">
      <slot name="error" :error="error">
        <span>{{ error }}</span>
      </slot>
    </div>
    <template v-else-if="view">
      <component
        :is="resolvedComponent"
        v-if="resolvedComponent"
        :view="view"
        :read-only="readOnly"
        v-bind="$attrs"
      />
      <slot v-else name="unsupported">
        <span>Unsupported view type: {{ viewType }}</span>
      </slot>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, shallowRef, watch } from 'vue';
import type { Component } from 'vue';
import type { DynamiaClient } from '@dynamia-tools/sdk';
import type { ViewerConfig, View } from '@dynamia-tools/ui-core';
import { ViewRendererRegistry } from '@dynamia-tools/ui-core';
import { VueViewer } from '../views/VueViewer.js';
import { useDynamiaClient } from '../composables/useDynamiaClient.js';

/** Props for the Viewer component */
export interface ViewerProps extends Omit<ViewerConfig, 'client'> {
  /** View type name ('form', 'table', 'crud', etc.) */
  viewType?: string;
  /** Entity id: the entity class simple name (e.g. 'Book') */
  beanClass?: string;
  /** Whether the view is in read-only mode */
  readOnly?: boolean;
  /**
   * Client used to fetch the descriptor (`descriptorId`, `beanClass` + `viewType`). Defaults to the one provided to
   * `DynamiaVue`. Not needed with a `descriptor` object.
   */
  client?: DynamiaClient;
}

const props = withDefaults(defineProps<ViewerProps>(), {
  readOnly: false,
});

const emit = defineEmits<{
  /** Emitted when the view is initialized */
  ready: [view: View];
  /** Emitted on error */
  error: [message: string];
}>();

const injectedClient = useDynamiaClient();

const viewer = shallowRef<VueViewer | null>(null);
const loading = computed(() => viewer.value?.loading.value ?? true);
const error = computed(() => viewer.value?.error.value ?? null);
const view = computed(() => viewer.value?.currentView.value ?? null);
const resolvedComponent = shallowRef<Component | null>(null);

let generation = 0;

/** (Re)builds the viewer from the current props. A load that finishes after a newer one started is discarded. */
async function load(): Promise<void> {
  const current = ++generation;
  const client = props.client ?? injectedClient;
  const next = new VueViewer({
    viewType: props.viewType ?? null,
    beanClass: props.beanClass ?? null,
    readOnly: props.readOnly,
    ...(client != null ? { client } : {}),
    ...(props.descriptor != null ? { descriptor: props.descriptor } : {}),
    ...(props.descriptorId != null ? { descriptorId: props.descriptorId } : {}),
    ...(props.value !== undefined ? { value: props.value } : {}),
    ...(props.source !== undefined ? { source: props.source } : {}),
  });
  viewer.value?.destroy();
  viewer.value = next;
  resolvedComponent.value = null;
  try {
    await next.initialize();
    if (current !== generation) return;
    if (next.resolvedViewType && ViewRendererRegistry.hasRenderer(next.resolvedViewType)) {
      const renderer = ViewRendererRegistry.getRenderer(next.resolvedViewType);
      resolvedComponent.value = renderer.render(next.currentView.value!) as Component;
    }
    if (next.currentView.value) emit('ready', next.currentView.value);
  } catch (e) {
    if (current === generation) emit('error', String(e));
  }
}

onMounted(load);

// A descriptor written inline (`:descriptor="{ ... }"`) is a new object on every parent render: compare its content.
const descriptorKey = computed(() => JSON.stringify(props.descriptor ?? null));
watch(
  () => [descriptorKey.value, props.descriptorId, props.viewType, props.beanClass, props.client] as const,
  () => { void load(); },
);
watch(() => props.source, (source) => viewer.value?.setSource(source));
watch(() => props.value, (value) => viewer.value?.setValue(value));
watch(() => props.readOnly, (readOnly) => viewer.value?.setReadonly(readOnly));

onUnmounted(() => {
  generation++;
  viewer.value?.destroy();
});

/** Get the primary value from the view */
const getValue = () => viewer.value?.getValue();

/** Set the primary value on the view */
const setValue = (value: unknown) => viewer.value?.setValue(value);

defineExpose({ viewer, getValue, setValue });
</script>
