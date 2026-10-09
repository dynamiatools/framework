<!-- ReportChart.vue: draws a chart of a report result; needs the optional `chart.js` peer dependency -->
<template>
  <figure class="dynamia-report-chart">
    <figcaption v-if="chart.title">{{ chart.title }}</figcaption>
    <span v-if="error" class="dynamia-report-chart-error" role="alert">{{ error }}</span>
    <canvas v-show="!error" ref="canvas" role="img" :aria-label="chart.title"></canvas>
  </figure>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import type { ReportChartResult } from '@dynamia-tools/reports-sdk';
import { resolveLabels } from '../labels.js';
import type { ReportLabels } from '../labels.js';

const props = defineProps<{
  chart: ReportChartResult;
  labels?: Partial<ReportLabels> | undefined;
}>();

const text = computed(() => resolveLabels(props.labels));
const canvas = ref<HTMLCanvasElement | null>(null);
const error = ref<string | null>(null);
let instance: { destroy(): void } | null = null;

async function draw(): Promise<void> {
  instance?.destroy();
  instance = null;
  if (!canvas.value) return;
  const circular = props.chart.type === 'pie' || props.chart.type === 'doughnut';
  try {
    const { Chart } = await import('chart.js/auto');
    instance = new Chart(canvas.value, {
      type: props.chart.type,
      data: {
        labels: props.chart.labels,
        datasets: props.chart.datasets.map((dataset) => ({
          label: dataset.label,
          data: dataset.data,
          backgroundColor: dataset.backgroundColor,
        })),
      },
      options: { responsive: true, plugins: { legend: { display: circular } } },
    } as never);
    error.value = null;
  } catch (e) {
    error.value = `${text.value.chartError}: ${e instanceof Error ? e.message : String(e)}. Is "chart.js" installed?`;
  }
}

onMounted(draw);
watch(() => props.chart, draw, { deep: true });
onBeforeUnmount(() => instance?.destroy());
</script>

<style scoped>
.dynamia-report-chart { margin: 0; }
.dynamia-report-chart figcaption { font-weight: 600; margin-bottom: 0.5rem; }
</style>
