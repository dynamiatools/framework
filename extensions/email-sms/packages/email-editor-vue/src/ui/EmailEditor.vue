<script setup lang="ts">
import { computed, provide, watch } from 'vue';
import { emptyDesign } from '../model/defaults.js';
import type { Design } from '../model/types.js';
import BlockList from './BlockList.vue';
import { translator } from './i18n.js';
import Palette from './Palette.vue';
import PropertiesPanel from './PropertiesPanel.vue';
import { useEditorState } from './state.js';

const props = withDefaults(defineProps<{ design?: Design; locale?: string }>(), { locale: 'en' });
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
  <div class="editor" @keydown="onKeyDown" @click="editor.select(null)">
    <header class="toolbar" @click.stop>
      <button type="button" :disabled="!editor.canUndo.value" data-action="undo" @click="editor.undo()">↶ {{ t('undo') }}</button>
      <button type="button" :disabled="!editor.canRedo.value" data-action="redo" @click="editor.redo()">↷ {{ t('redo') }}</button>
      <slot name="toolbar" />
    </header>
    <div class="main">
      <aside class="left" @click.stop><Palette /></aside>
      <main class="center" :style="{ background: editor.design.value.settings.backgroundColor }">
        <div class="canvas" :style="canvasStyle" data-canvas @click.stop>
          <BlockList :blocks="editor.design.value.blocks" :parent-id="null" :parent-type="null" slot-name="children" :empty-label="t('emptyEmail')" />
        </div>
      </main>
      <aside class="right" @click.stop>
        <slot name="properties" :editor="editor"><PropertiesPanel /></slot>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.editor { display: flex; flex-direction: column; height: 100%; min-height: 480px; background: #f1f3f6; color: #202124; font: 14px/1.4 system-ui, sans-serif; }
.toolbar { display: flex; gap: 8px; align-items: center; padding: 8px 12px; background: #fff; border-bottom: 1px solid #d4d9e0; }
.toolbar :deep(button), .toolbar button { padding: 6px 12px; border: 1px solid #d4d9e0; background: #fff; border-radius: 6px; cursor: pointer; font: inherit; }
.toolbar button:disabled { opacity: 0.45; cursor: default; }
.main { display: grid; grid-template-columns: 200px minmax(0, 1fr) 300px; flex: 1; min-height: 0; }
.left, .right { background: #fafbfc; padding: 12px; overflow: auto; }
.left { border-right: 1px solid #d4d9e0; }
.right { border-left: 1px solid #d4d9e0; }
.center { overflow: auto; padding: 36px 16px; }
.canvas { margin: 0 auto; min-height: 200px; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15); }
@media (max-width: 900px) {
  .main { grid-template-columns: 1fr; grid-auto-rows: auto; }
  .left, .right { border: 0; border-bottom: 1px solid #d4d9e0; }
}
</style>
