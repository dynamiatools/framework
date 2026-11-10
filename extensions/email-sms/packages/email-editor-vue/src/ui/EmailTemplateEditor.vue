<script setup lang="ts">
import { AlertTriangle, CircleAlert, CircleCheck, Eye, Loader, RotateCcw, Save, Send, Wand2 } from 'lucide-vue-next';
import { btn, btnPrimary, field } from './classes.js';
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
    const loaded = await props.api.get(props.templateId);
    // the source of the page wins; otherwise the one the template names
    const vars = await props.api.variables(props.source || loaded.source || undefined).catch(() => []);
    template.value = loaded;
    subject.value = loaded.subject;
    variables.value = vars.map((v) => ({ name: v.name, ...(v.group ? { group: v.group } : {}), ...(v.label ? { label: v.label } : {}), ...(v.sample !== undefined && v.sample !== null ? { sample: v.sample } : {}) }));
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
    const result = await props.api.testSend(props.templateId, props.source || template.value?.source ? { source: props.source || template.value?.source || '' } : {});
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
    <section v-else-if="template.engine !== 'DYNAMIA'" class="velocity de:mx-auto de:my-10 de:w-full de:max-w-2xl de:rounded-xl de:border de:border-slate-200 de:bg-white de:p-6 de:shadow-sm" data-velocity>
      <h2 class="de:mb-1 de:flex de:items-center de:gap-2 de:text-lg de:font-semibold de:text-slate-800"><Wand2 :size="20" class="de:text-blue-600" aria-hidden="true" />{{ template.name }}</h2>
      <p class="de:text-sm de:text-slate-600">{{ t('velocityNotice') }}</p>
      <div class="actions de:my-4 de:flex de:gap-2">
        <button type="button" :class="btn" data-action="check-migration" :disabled="busy" @click="checkMigration">{{ t('checkMigration') }}</button>
        <button type="button" :class="btnPrimary" data-action="migrate" :disabled="busy || (report !== null && !report.portable)" @click="migrate"><Wand2 :size="16" aria-hidden="true" />{{ t('migrate') }}</button>
      </div>
      <div v-if="report" class="report de:rounded-lg de:bg-slate-50 de:p-3 de:text-sm de:text-slate-700" data-report>
        <p v-if="report.portable">{{ t('migrationOk') }}</p>
        <template v-else>
          <p>{{ t('migrationBlocked') }}</p>
          <ul class="de:mt-2 de:list-disc de:pl-5"><li v-for="(issue, i) in report.issues" :key="i"><code class="de:rounded de:bg-slate-200 de:px-1 de:text-xs">{{ issue.field }}:{{ issue.line }}</code> {{ issue.snippet }} — {{ issue.reason }}</li></ul>
        </template>
        <ul v-if="report.warnings.length > 0" class="warnings de:mt-2 de:list-disc de:pl-5 de:text-xs de:text-slate-500"><li v-for="w in report.warnings" :key="w">{{ w }}</li></ul>
      </div>
      <p v-if="message" :class="['de:mt-3 de:text-sm', problem ? 'error de:text-red-600' : 'de:text-green-700']" role="status" data-message>{{ message }}</p>
    </section>

    <EmailEditor v-else-if="initial" class="fill" :design="initial" :locale="lang" :variables="variables" @change="onChange">
      <template #toolbar>
        <label class="subject de:min-w-[160px] de:flex-1"><span class="de:sr-only">{{ t('subject') }}</span>
          <input v-model="subject" type="text" :class="field" :placeholder="t('subject')" data-subject @input="dirty = true" />
        </label>
        <button type="button" :class="btn" data-action="preview" @click="showPreview = true"><Eye :size="16" aria-hidden="true" />{{ t('preview') }}</button>
        <button type="button" :class="btn" data-action="send-test" :disabled="busy" @click="sendTest"><Send :size="16" aria-hidden="true" />{{ t('sendTest') }}</button>
        <button type="button" :class="btnPrimary" data-action="save" :disabled="busy || !dirty" @click="save"><Loader v-if="busy" :size="16" class="de:animate-spin" aria-hidden="true" /><Save v-else :size="16" aria-hidden="true" />{{ busy ? t('saving') : t('save') }}</button>
        <button v-if="template.hasBackup && !revertWarning" type="button" :class="btn" data-action="revert" :disabled="busy" @click="revert(false)"><RotateCcw :size="16" aria-hidden="true" />{{ t('revertMigration') }}</button>
        <span v-if="dirty" class="dirty de:inline-flex de:items-center de:gap-1 de:rounded-full de:bg-amber-50 de:px-2.5 de:py-0.5 de:text-xs de:font-medium de:text-amber-700" data-dirty><CircleAlert :size="13" aria-hidden="true" />{{ t('unsaved') }}</span>
        <span v-if="message" :class="['status de:inline-flex de:items-center de:gap-1 de:text-xs de:font-medium', problem ? 'error de:text-red-600' : 'de:text-green-700']" role="status" data-message><component :is="problem ? AlertTriangle : CircleCheck" :size="14" aria-hidden="true" />{{ message }}</span>
        <span v-if="revertWarning" class="warn de:inline-flex de:items-center de:gap-2 de:rounded-md de:bg-amber-50 de:px-2.5 de:py-1 de:text-xs de:text-amber-800" data-revert-warning>{{ t('revertEdited') }}
          <button type="button" :class="btn" data-action="confirm-revert" @click="revert(true)">{{ t('confirmRevert') }}</button>
        </span>
      </template>
    </EmailEditor>

    <PreviewPane v-if="showPreview && current && template" :design="current" :variables="variables" :subject="subject" :escape-values="template.escapeValues" :locale="lang" @close="showPreview = false" />
  </div>
</template>

<style scoped>
.host { height: 100%; display: flex; flex-direction: column; background: #f1f5f9; font-family: Inter, ui-sans-serif, system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif; }
.fill { flex: 1; }
</style>
