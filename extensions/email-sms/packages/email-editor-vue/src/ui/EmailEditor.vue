<script setup lang="ts">
import './tailwind.css';
import { Redo2, Undo2 } from 'lucide-vue-next';
import { btn } from './classes.js';
import { computed, provide, watch } from 'vue';
import { emptyDesign } from '../model/defaults.js';
import type { Design } from '../model/types.js';
import BlockList from './BlockList.vue';
import { translator } from './i18n.js';
import Palette from './Palette.vue';
import PropertiesPanel from './PropertiesPanel.vue';
import VariablesPanel from './VariablesPanel.vue';
import type { TemplateVariable } from './variables.js';
import { useEditorState } from './state.js';

const props = withDefaults(defineProps<{ design?: Design; locale?: string; variables?: TemplateVariable[] }>(), { locale: 'en', variables: () => [] });
const emit = defineEmits<{ (event: 'change', design: Design): void }>();

const editor = useEditorState(props.design ?? emptyDesign(), (design) => emit('change', design));
const t = computed(() => translator(props.locale));
provide('editor', editor);
provide('t', (key: string, values?: Record<string, string | number>) => t.value(key, values));

watch(
  () => props.design,
  (next) => {
    if (next && next !== editor.design.value) {
      editor.load(next);
    }
  },
);

function onKeyDown(event: KeyboardEvent): void {
  const target = event.target as HTMLElement | null;
  if (target && /^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName)) {
    return;
  }
  if ((event.ctrlKey || event.metaKey) && !event.shiftKey && event.key.toLowerCase() === 'z') {
    event.preventDefault();
    editor.undo();
  } else if ((event.ctrlKey || event.metaKey) && (event.key.toLowerCase() === 'y' || (event.shiftKey && event.key.toLowerCase() === 'z'))) {
    event.preventDefault();
    editor.redo();
  }
}

const canvasStyle = computed(() => ({ maxWidth: `${editor.design.value.settings.width}px`, background: editor.design.value.settings.contentBackground }));

defineExpose({ editor, getDesign: () => editor.design.value, setDesign: (design: Design) => editor.load(design) });
</script>

<template>
  <div class="editor de:flex de:h-full de:min-h-[480px] de:flex-col de:bg-slate-100 de:text-sm de:text-slate-800" @keydown="onKeyDown" @click="editor.select(null)">
    <header class="toolbar de:z-10 de:flex de:flex-wrap de:items-center de:gap-2 de:border-b de:border-slate-200 de:bg-white de:px-4 de:py-2 de:shadow-sm" @click.stop>
      <div class="de:flex de:items-center de:gap-1">
        <button type="button" :class="btn" :disabled="!editor.canUndo.value" data-action="undo" :title="t('undo')" @click="editor.undo()"><Undo2 :size="16" aria-hidden="true" />{{ t('undo') }}</button>
        <button type="button" :class="btn" :disabled="!editor.canRedo.value" data-action="redo" :title="t('redo')" @click="editor.redo()"><Redo2 :size="16" aria-hidden="true" />{{ t('redo') }}</button>
      </div>
      <slot name="toolbar" />
    </header>
    <div class="main">
      <aside class="de:overflow-auto de:border-r de:border-slate-200 de:bg-white de:p-4" @click.stop><Palette /></aside>
      <main class="de:overflow-auto de:px-4 de:py-10" :style="{ background: editor.design.value.settings.backgroundColor }">
        <div class="canvas de:mx-auto de:min-h-[200px] de:rounded-sm de:shadow-md de:ring-1 de:ring-black/5" :style="canvasStyle" data-canvas @click.stop>
          <BlockList :blocks="editor.design.value.blocks" :parent-id="null" :parent-type="null" slot-name="children" :empty-label="t('emptyEmail')" />
        </div>
      </main>
      <aside class="de:overflow-auto de:border-l de:border-slate-200 de:bg-white de:p-4" @click.stop>
        <slot name="properties" :editor="editor"><PropertiesPanel /></slot>
        <VariablesPanel :variables="variables ?? []" />
      </aside>
    </div>
  </div>
</template>

<style scoped>
.editor { font-family: Inter, ui-sans-serif, system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif; line-height: 1.4; }
.main { display: grid; grid-template-columns: 232px minmax(0, 1fr) 320px; flex: 1; min-height: 0; }
@media (max-width: 900px) {
  .main { grid-template-columns: 1fr; grid-auto-rows: auto; }
}
</style>
