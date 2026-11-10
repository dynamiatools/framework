<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, provide, ref, shallowRef } from 'vue';
import { EmailApiError, type EmailTemplatesApi, type MigrationReport, type TemplateData } from '../api.js';
import { compile } from '../compiler/compile.js';
import { emptyDesign } from '../model/defaults.js';
import { DesignError, type Design } from '../model/types.js';
import { parseDesign, serializeDesign } from '../model/validate.js';
import EmailEditor from './EmailEditor.vue';
import { translator } from './i18n.js';
import PreviewPane from './PreviewPane.vue';
import type { TemplateVariable } from './variables.js';

const props = withDefaults(defineProps<{ api: EmailTemplatesApi; templateId: number; locale?: string; source?: string }>(), { locale: 'en' });
const emit = defineEmits<{ (event: 'saved', template: TemplateData): void }>();

const lang = computed(() => props.locale ?? 'en');
const t = computed(() => translator(lang.value));
provide('t', (key: string, values?: Record<string, string | number>) => t.value(key, values));

const template = ref<TemplateData | null>(null);
const initial = shallowRef<Design | null>(null);
const current = shallowRef<Design | null>(null);
const variables = ref<TemplateVariable[]>([]);
const subject = ref('');
const dirty = ref(false);
const loading = ref(true);
const busy = ref(false);
const message = ref('');
const problem = ref(false);
const showPreview = ref(false);
const report = ref<MigrationReport | null>(null);
const revertWarning = ref(false);

function fail(error: unknown, fallbackKey: string): void {
  problem.value = true;
  message.value = error instanceof EmailApiError || error instanceof DesignError ? error.message : t.value(fallbackKey);
}

function ok(text: string): void {
  problem.value = false;
  message.value = text;
}

async function load(): Promise<void> {
  loading.value = true;
  message.value = '';
  try {
    const [loaded, vars] = await Promise.all([props.api.get(props.templateId), props.api.variables(props.source).catch(() => [])]);
    template.value = loaded;
    subject.value = loaded.subject;
    variables.value = vars.map((v) => ({ name: v.name, ...(v.label ? { label: v.label } : {}), ...(v.sample !== undefined && v.sample !== null ? { sample: v.sample } : {}) }));
    if (loaded.engine === 'DYNAMIA') {
      initial.value = loaded.design ? parseDesign(loaded.design) : emptyDesign();
      current.value = initial.value;
    } else {
      initial.value = null;
      current.value = null;
    }
    dirty.value = false;
  } catch (error) {
    template.value = null;
    fail(error, 'loadFailed');
  } finally {
    loading.value = false;
  }
}

function onChange(design: Design): void {
  current.value = design;
  dirty.value = true;
}

async function save(): Promise<boolean> {
  if (!current.value || busy.value) {
    return false;
  }
  busy.value = true;
  message.value = t.value('saving');
  problem.value = false;
  try {
    const compiled = await compile(current.value);
    const saved = await props.api.saveDesign(props.templateId, { design: serializeDesign(current.value), content: compiled.html, subject: subject.value });
    template.value = saved;
    dirty.value = false;
    ok(compiled.errors.length > 0 ? `${t.value('saved')} · ${compiled.errors.join(' · ')}` : t.value('saved'));
    emit('saved', saved);
    return true;
  } catch (error) {
    fail(error, 'saveFailed');
    return false;
  } finally {
    busy.value = false;
  }
}

async function sendTest(): Promise<void> {
  if (dirty.value && !(await save())) {
    return;
  }
  busy.value = true;
  try {
    const result = await props.api.testSend(props.templateId, props.source ? { source: props.source } : {});
    if (result.sent) {
      ok(t.value('testSent'));
    } else {
      problem.value = true;
      message.value = result.message ?? t.value('testFailed');
    }
  } catch (error) {
    fail(error, 'testFailed');
  } finally {
    busy.value = false;
  }
}

async function checkMigration(): Promise<void> {
  busy.value = true;
  try {
    report.value = await props.api.migrate(props.templateId, true);
  } catch (error) {
    fail(error, 'loadFailed');
  } finally {
    busy.value = false;
  }
}

async function migrate(): Promise<void> {
  busy.value = true;
  try {
    const result = await props.api.migrate(props.templateId, false);
    report.value = result;
    if (result.applied) {
      await load();
      ok(t.value('migrated'));
    }
  } catch (error) {
    fail(error, 'loadFailed');
  } finally {
    busy.value = false;
  }
}

async function revert(confirm: boolean): Promise<void> {
  busy.value = true;
  try {
    const result = await props.api.revert(props.templateId, confirm);
    if (result.reverted) {
      revertWarning.value = false;
      report.value = null;
      await load();
      ok(t.value('reverted'));
    } else if (result.designEdited) {
      revertWarning.value = true;
    } else {
      problem.value = true;
      message.value = result.reason ?? t.value('saveFailed');
    }
  } catch (error) {
    fail(error, 'saveFailed');
  } finally {
    busy.value = false;
  }
}

function warnBeforeLeaving(event: BeforeUnloadEvent): void {
  if (dirty.value) {
    event.preventDefault();
  }
}

onMounted(() => {
  window.addEventListener('beforeunload', warnBeforeLeaving);
  void load();
});
onBeforeUnmount(() => window.removeEventListener('beforeunload', warnBeforeLeaving));

defineExpose({ save, load });
</script>

<template>
  <div class="host">
    <p v-if="loading" role="status">{{ t('loading') }}</p>
    <p v-else-if="!template" class="error" role="alert" data-load-error>{{ message }}</p>

    <!-- a Velocity template cannot be edited with blocks: offer the migration -->
    <section v-else-if="template.engine !== 'DYNAMIA'" class="velocity" data-velocity>
      <h2>{{ template.name }}</h2>
      <p>{{ t('velocityNotice') }}</p>
      <div class="actions">
        <button type="button" data-action="check-migration" :disabled="busy" @click="checkMigration">{{ t('checkMigration') }}</button>
        <button type="button" data-action="migrate" :disabled="busy || (report !== null && !report.portable)" @click="migrate">{{ t('migrate') }}</button>
      </div>
      <div v-if="report" class="report" data-report>
        <p v-if="report.portable">{{ t('migrationOk') }}</p>
        <template v-else>
          <p>{{ t('migrationBlocked') }}</p>
          <ul><li v-for="(issue, i) in report.issues" :key="i"><code>{{ issue.field }}:{{ issue.line }}</code> {{ issue.snippet }} — {{ issue.reason }}</li></ul>
        </template>
        <ul v-if="report.warnings.length > 0" class="warnings"><li v-for="w in report.warnings" :key="w">{{ w }}</li></ul>
      </div>
      <p v-if="message" :class="{ error: problem }" role="status" data-message>{{ message }}</p>
    </section>

    <EmailEditor v-else-if="initial" class="fill" :design="initial" :locale="lang" :variables="variables" @change="onChange">
      <template #toolbar>
        <label class="subject"><span class="sr-only">{{ t('subject') }}</span>
          <input v-model="subject" type="text" :placeholder="t('subject')" data-subject @input="dirty = true" />
        </label>
        <button type="button" data-action="preview" @click="showPreview = true">👁 {{ t('preview') }}</button>
        <button type="button" data-action="send-test" :disabled="busy" @click="sendTest">✉ {{ t('sendTest') }}</button>
        <button type="button" class="primary" data-action="save" :disabled="busy || !dirty" @click="save">{{ busy ? t('saving') : t('save') }}</button>
        <button v-if="template.hasBackup && !revertWarning" type="button" data-action="revert" :disabled="busy" @click="revert(false)">{{ t('revertMigration') }}</button>
        <span v-if="dirty" class="dirty" data-dirty>{{ t('unsaved') }}</span>
        <span v-if="message" :class="['status', { error: problem }]" role="status" data-message>{{ message }}</span>
        <span v-if="revertWarning" class="warn" data-revert-warning>{{ t('revertEdited') }}
          <button type="button" data-action="confirm-revert" @click="revert(true)">{{ t('confirmRevert') }}</button>
        </span>
      </template>
    </EmailEditor>

    <PreviewPane v-if="showPreview && current && template" :design="current" :variables="variables" :subject="subject" :escape-values="template.escapeValues" :locale="lang" @close="showPreview = false" />
  </div>
</template>

<style scoped>
.host { height: 100%; display: flex; flex-direction: column; }
.fill { flex: 1; }
.velocity { max-width: 720px; margin: 24px auto; padding: 0 16px; font: 14px/1.5 system-ui, sans-serif; }
.actions { display: flex; gap: 8px; margin: 12px 0; }
button { padding: 6px 12px; border: 1px solid #c4cad3; background: #fff; border-radius: 6px; cursor: pointer; font: inherit; }
button.primary { background: #1a73e8; color: #fff; border-color: #1a73e8; }
button:disabled { opacity: 0.5; cursor: default; }
.subject { flex: 1; min-width: 160px; }
.subject input { width: 100%; box-sizing: border-box; padding: 6px 10px; border: 1px solid #c4cad3; border-radius: 6px; font: inherit; }
.sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); }
.status { font-size: 13px; color: #188038; }
.error, .status.error { color: #c5221f; }
.dirty { font-size: 12px; color: #b06000; }
.warn { font-size: 12px; color: #b06000; display: flex; gap: 6px; align-items: center; }
.report { background: #f6f7f9; border-radius: 6px; padding: 8px 12px; font-size: 13px; }
.warnings { color: #5f6368; font-size: 12px; }
</style>
