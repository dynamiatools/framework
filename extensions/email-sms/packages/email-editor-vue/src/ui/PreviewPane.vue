<script setup lang="ts">
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
          <button type="button" data-device="desktop" :aria-pressed="device === 'desktop'" @click="device = 'desktop'">🖥 {{ t('desktop') }}</button>
          <button type="button" data-device="mobile" :aria-pressed="device === 'mobile'" @click="device = 'mobile'">📱 {{ t('mobile') }}</button>
        </span>
        <button type="button" data-action="close-preview" :aria-label="t('close')" @click="emit('close')">✕</button>
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
.overlay { position: fixed; inset: 0; z-index: 50; background: rgba(32, 33, 36, 0.55); display: flex; align-items: stretch; justify-content: center; padding: 20px; }
.box { background: #fff; border-radius: 8px; display: flex; flex-direction: column; width: min(1100px, 100%); overflow: hidden; }
header { display: flex; gap: 12px; align-items: center; padding: 10px 14px; border-bottom: 1px solid #d4d9e0; }
.subject { flex: 1; color: #5f6368; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
header button { padding: 5px 10px; border: 1px solid #c4cad3; background: #fff; border-radius: 4px; cursor: pointer; font: inherit; }
header button[aria-pressed='true'] { background: #1a73e8; color: #fff; border-color: #1a73e8; }
.data { padding: 6px 14px; border-bottom: 1px solid #e3e6ea; }
.data textarea { width: 100%; box-sizing: border-box; font-family: ui-monospace, monospace; font-size: 12px; margin-top: 6px; }
.notes { margin: 0; padding: 6px 14px; background: #fff8e1; font-size: 12px; }
.error { color: #c5221f; margin: 6px 14px; font-size: 12px; }
.stage { flex: 1; overflow: auto; background: #e8eaed; display: flex; justify-content: center; padding: 16px; min-height: 0; }
iframe { height: 100%; min-height: 480px; max-width: 100%; border: 0; background: #fff; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2); }
</style>
