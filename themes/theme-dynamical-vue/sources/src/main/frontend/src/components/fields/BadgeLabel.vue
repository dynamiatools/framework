<!-- BadgeLabel.vue: `component: badge` (a pill, TailAdmin Badge) and `component: status` (a pill with a dot, TailAdmin
     Status) for a value such as an enum constant. params: `color` for every value or `colors`, a map of value to
     color (primary, success, error, warning, info, light, dark); values without a color are neutral. -->
<template>
  <component :is="dot ? Status : Badge" v-if="label" v-bind="dot ? { label, color: statusColor } : { color, size: 'sm' }">
    <template v-if="!dot">{{ label }}</template>
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Badge from '@dynamia-tools/tailadmin-vue/components/ui/Badge.vue';
import Status from '@dynamia-tools/tailadmin-vue/components/ext/display/Status.vue';
import type { ResolvedField } from '@dynamia-tools/ui-core';

const props = defineProps<{
  field: ResolvedField;
  modelValue?: unknown;
  readOnly?: boolean | undefined;
  params?: Record<string, unknown> | undefined;
  /** Draw it as a status (with a dot) */
  dot?: boolean;
}>();

type BadgeColor = 'primary' | 'success' | 'error' | 'warning' | 'info' | 'light' | 'dark';
const COLORS = new Set<string>(['primary', 'success', 'error', 'warning', 'info', 'light', 'dark']);

const raw = computed(() => (props.modelValue === null || props.modelValue === undefined ? '' : String(props.modelValue)));

// "OUT_STOCK" -> "Out Stock"; words of up to 2 letters or digits ("PG_13", "NR") are kept as written.
const label = computed(() => raw.value.split('_').filter(Boolean)
  .map(word => (word.length <= 2 ? word : word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()))
  .join(' '));

const color = computed<BadgeColor>(() => {
  const colors = props.params?.['colors'] as Record<string, string> | undefined;
  const named = colors?.[raw.value] ?? (props.params?.['color'] as string | undefined);
  return named && COLORS.has(named) ? (named as BadgeColor) : 'light';
});

// Status has no primary/light/dark: map them to its neutral/info palette.
const statusColor = computed(() => {
  const c = color.value;
  if (c === 'success' || c === 'error' || c === 'warning' || c === 'info') return c;
  return c === 'primary' ? 'info' : 'neutral';
});
</script>
