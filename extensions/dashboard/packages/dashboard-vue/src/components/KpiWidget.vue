<!-- KpiWidget.vue: built-in renderer of `kpi` widgets -->
<template>
  <div class="dynamia-kpi">
    <div v-if="data?.label" class="dynamia-kpi-label">{{ data.label }}</div>
    <div class="dynamia-kpi-value">
      {{ data?.value }}<span v-if="data?.unit" class="dynamia-kpi-unit"> {{ data.unit }}</span>
    </div>
    <div
      v-if="trendText"
      class="dynamia-kpi-trend"
      :class="(data?.trend ?? 0) >= 0 ? 'dynamia-kpi-trend-up' : 'dynamia-kpi-trend-down'"
    >
      {{ trendText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { KpiWidgetData } from '@dynamia-tools/dashboard-sdk';

const props = defineProps<{
  /** The widget data */
  data: KpiWidgetData | null;
}>();

/** Variation as a signed percentage, e.g. `0.125` → `+12.5%` */
const trendText = computed(() => {
  const trend = props.data?.trend;
  if (trend === null || trend === undefined) return '';
  const percent = Math.round(trend * 1000) / 10;
  return `${percent >= 0 ? '+' : ''}${percent}%`;
});
</script>

<style scoped>
.dynamia-kpi-label { opacity: 0.7; font-size: 0.9em; }
.dynamia-kpi-value { font-size: 2em; font-weight: 600; }
.dynamia-kpi-unit { font-size: 0.5em; font-weight: 400; }
.dynamia-kpi-trend-up { color: #10b981; }
.dynamia-kpi-trend-down { color: #ef4444; }
</style>
