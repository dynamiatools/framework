<script setup lang="ts">
import { Monitor, Smartphone, X } from 'lucide-vue-next';
import { DynamiaTemplateEngine } from '@dynamia-tools/template-engine';
import { computed, inject, ref, watch } from 'vue';
import { compile } from '../compiler/compile.js';
import type { Design } from '../model/types.js';
import { sampleData, type TemplateVariable } from './variables.js';

const props = defineProps<{ design: Design; variables: TemplateVariable[]; subject: string; escapeValues: boolean; locale: string }>();
const emit = defineEmits<{ (event: 'close'): void }>();
const t = inject<(key: string, values?: Record<string, string | number>) => string>('t')!;

const device = ref<'desktop' | 'mobile'>('desktop');
const html = ref('');
const subject = ref('');
const notes = ref<string[]>([]);
const loading = ref(true);
const renderId = ref(0);
const failure = ref('');
const dataText = ref('');
const dataError = ref('');

const width = computed(() => (device.value === 'mobile' ? 375 : Math.min(props.design.settings.width + 40, 920)));

function data(): Record<string, unknown> {
  const base = sampleData(props.variables);
  dataError.value = '';
  if (dataText.value.trim() === '') {
    return base;
  }
  try {
    const extra = JSON.parse(dataText.value) as unknown;
    if (typeof extra !== 'object' || extra === null || Array.isArray(extra)) {
      throw new Error('object');
    }
    return { ...base, ...(extra as Record<string, unknown>) };
  } catch {
    dataError.value = t('invalidJson');
    return base;
  }
}

async function render(): Promise<void> {
  loading.value = true;
  failure.value = '';
  try {
    const result = await compile(props.design);
    const engine = new DynamiaTemplateEngine({ locale: props.locale, escapeHtml: props.escapeValues });
    const values = data();
    html.value = engine.evaluate(result.html, values);
    subject.value = new DynamiaTemplateEngine({ locale: props.locale }).evaluate(props.subject, values);
    notes.value = [...result.errors, ...result.warnings];
  } catch (error) {
    failure.value = error instanceof Error ? error.message : String(error);
  } finally {
    renderId.value++;
    loading.value = false;
  }
}

watch(() => [props.design, dataText.value], render, { immediate: true });
</script>

<template>
  <div class="overlay" role="dialog" aria-modal="true" :aria-label="t('preview')" data-preview @keydown.esc="emit('close')">
    <div class="box">
      <header>
        <strong>{{ t('preview') }}</strong>
        <span class="subject" data-subject>{{ subject }}</span>
        <span class="devices" role="group">
          <button type="button" data-device="desktop" :aria-pressed="device === 'desktop'" @click="device = 'desktop'"><Monitor :size="15" aria-hidden="true" />{{ t('desktop') }}</button>
          <button type="button" data-device="mobile" :aria-pressed="device === 'mobile'" @click="device = 'mobile'"><Smartphone :size="15" aria-hidden="true" />{{ t('mobile') }}</button>
        </span>
        <button type="button" data-action="close-preview" :aria-label="t('close')" @click="emit('close')"><X :size="16" aria-hidden="true" /></button>
      </header>
      <details class="data">
        <summary>{{ t('sampleData') }}</summary>
        <textarea v-model="dataText" rows="4" spellcheck="false" :aria-label="t('sampleData')" placeholder='{ "user": { "name": "Ada" } }' />
        <small v-if="dataError" class="error" role="alert">{{ dataError }}</small>
      </details>
      <p v-if="notes.length > 0" class="notes" data-notes>{{ notes.join(' · ') }}</p>
      <p v-if="failure" class="error" role="alert">{{ failure }}</p>
      <div class="stage">
        <p v-if="loading" role="status">{{ t('loading') }}</p>
        <!-- no allow-scripts: the email is shown, never run. A new frame per render: Chrome does not repaint a hidden frame whose srcdoc changes -->
        <iframe v-if="!loading" :key="renderId" :title="t('preview')" sandbox="" :srcdoc="html" :style="{ width: `${width}px` }" data-frame />
      </div>
    </div>
  </div>
</template>

<style scoped>
.overlay { position: fixed; inset: 0; z-index: 50; background: rgba(15, 23, 42, 0.55); backdrop-filter: blur(2px); display: flex; align-items: stretch; justify-content: center; padding: 24px; font-family: Inter, ui-sans-serif, system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif; font-size: 14px; color: #0f172a; }
.box { background: #fff; border-radius: 14px; display: flex; flex-direction: column; width: min(1100px, 100%); overflow: hidden; box-shadow: 0 25px 50px -12px rgba(15, 23, 42, 0.45); }
header { display: flex; gap: 12px; align-items: center; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; }
.subject { flex: 1; color: #64748b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.devices { display: inline-flex; padding: 2px; background: #f1f5f9; border-radius: 8px; gap: 2px; }
header button { display: inline-flex; align-items: center; gap: 6px; padding: 5px 10px; border: 0; background: transparent; border-radius: 6px; cursor: pointer; font: inherit; font-size: 13px; color: #475569; }
header button:hover { background: #e2e8f0; }
header button[aria-pressed='true'] { background: #fff; color: #1d4ed8; box-shadow: 0 1px 2px rgba(15, 23, 42, 0.12); }
header button[data-action='close-preview'] { padding: 6px; }
.data { padding: 8px 16px; border-bottom: 1px solid #e2e8f0; font-size: 13px; color: #475569; }
.data summary { cursor: pointer; }
.data textarea { width: 100%; box-sizing: border-box; font-family: ui-monospace, monospace; font-size: 12px; margin-top: 6px; border: 1px solid #cbd5e1; border-radius: 6px; padding: 6px 8px; }
.notes { margin: 0; padding: 8px 16px; background: #fffbeb; color: #92400e; font-size: 12px; }
.error { color: #dc2626; margin: 6px 16px; font-size: 12px; }
.stage { flex: 1; overflow: auto; background: #e2e8f0; display: flex; justify-content: center; padding: 20px; min-height: 0; }
iframe { height: 100%; min-height: 480px; max-width: 100%; border: 0; background: #fff; border-radius: 4px; box-shadow: 0 4px 12px rgba(15, 23, 42, 0.18); }
</style>
