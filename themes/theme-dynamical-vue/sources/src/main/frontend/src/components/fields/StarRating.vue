<!-- StarRating.vue: `component: rating`, drawn with the TailAdmin Rating.
     params: `max` (stars, default 5) and `scale` (top of the stored value, default `max`): a 0-10 rating drawn with
     5 stars is `max: 5, scale: 10`. In a table (read only) it shows the stored value next to the stars. -->
<template>
  <span class="inline-flex items-center gap-2">
    <Rating
      :model-value="stars"
      :max="max"
      :readonly="readOnly ?? false"
      :size="readOnly ? 'sm' : 'md'"
      :aria-label="field.resolvedLabel"
      @update:model-value="onSelect"
    />
    <span v-if="readOnly && numeric !== null" class="text-xs tabular-nums text-gray-500 dark:text-gray-400">{{ numeric }}</span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Rating from '@dynamia-tools/tailadmin-vue/components/ext/input/Rating.vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean | undefined;
  params?: Record<string, unknown> | undefined;
}>();

const emit = defineEmits<{ 'update:modelValue': [value: number] }>();

function param(name: string, fallback: number): number {
  const value = Number(props.params?.[name]);
  return Number.isFinite(value) && value > 0 ? value : fallback;
}

const max = computed(() => param('max', 5));
const scale = computed(() => param('scale', max.value));
const numeric = computed(() => {
  if (props.modelValue === null || props.modelValue === undefined || props.modelValue === '') return null;
  const value = Number(props.modelValue);
  return Number.isFinite(value) ? value : null;
});
const stars = computed(() => Math.round(((numeric.value ?? 0) / scale.value) * max.value));

function onSelect(star: number): void {
  emit('update:modelValue', (star / max.value) * scale.value);
}
</script>
