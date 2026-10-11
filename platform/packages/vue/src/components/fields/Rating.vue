<!-- Rating.vue: basic star rating (`component: rating`). A theme can replace it by registering its own `rating`
     component in FieldComponentRegistry. params: `max` (stars, default 5), `scale` (top of the stored value,
     default `max`), e.g. a 0-10 rating drawn with 5 stars is `max: 5, scale: 10`. -->
<template>
  <span class="dynamia-rating" :class="{ 'dynamia-rating-editable': !readOnly }">
    <button
      v-for="star in max"
      :key="star"
      type="button"
      class="dynamia-rating-star"
      :class="{ 'dynamia-rating-star-on': star <= stars }"
      :disabled="readOnly"
      :aria-label="`${star} / ${max}`"
      @click="select(star)"
    >{{ star <= stars ? '★' : '☆' }}</button>
    <span v-if="numeric !== null" class="dynamia-rating-value">{{ numeric }}</span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean;
  params?: Record<string, unknown>;
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

function select(star: number): void {
  if (props.readOnly) return;
  // Clicking the lit last star clears the rating.
  emit('update:modelValue', star === stars.value ? 0 : (star / max.value) * scale.value);
}
</script>
