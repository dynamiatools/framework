<!-- ChartWidget.vue: built-in renderer of `chart` widgets; needs the optional `chart.js` peer dependency -->
<template>
  <div class="dynamia-chart">
    <span v-if="error" class="dynamia-chart-error">{{ error }}</span>
    <canvas v-show="!error" ref="canvas"></canvas>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import type { ChartWidgetData } from '@dynamia-tools/dashboard-sdk';

const props = defineProps<{
  /** The widget data: a Chart.js `type`, `data` and optional `options` */
  data: ChartWidgetData | null;
}>();

const canvas = ref<HTMLCanvasElement | null>(null);
const error = ref<string | null>(null);
let chart: { destroy(): void } | null = null;

async function draw(): Promise<void> {
  chart?.destroy();
  chart = null;
  if (!props.data || !canvas.value) return;
  try {
    const { Chart } = await import('chart.js/auto');
    chart = new Chart(canvas.value, {
      type: props.data.type,
      data: props.data.data,
      options: props.data.options ?? undefined,
    } as never);
    error.value = null;
  } catch (e) {
    error.value = `Cannot render chart: ${e instanceof Error ? e.message : String(e)}. Is "chart.js" installed?`;
  }
}

onMounted(draw);
watch(() => props.data, draw, { deep: true });
onBeforeUnmount(() => chart?.destroy());
</script>
