<script setup lang="ts">
import { computed, inject } from 'vue';
import type { Block } from '../model/types.js';
import BlockList from './BlockList.vue';
import { sanitizeHtml } from './sanitize.js';
import { DRAG_MIME, type EditorState } from './state.js';

const props = defineProps<{ block: Block }>();
const editor = inject<EditorState>('editor')!;
const t = inject<(key: string, values?: Record<string, string | number>) => string>('t')!;

const selected = computed(() => editor.selectedId.value === props.block.id);
const p = computed(() => props.block.props);
const isColumn = computed(() => props.block.type === 'column');
const hidden = computed(() => (p.value['hideOn'] === 'mobile' ? t('hiddenOn', { device: t('mobile') }) : p.value['hideOn'] === 'desktop' ? t('hiddenOn', { device: t('desktop') }) : ''));
const html = computed(() => sanitizeHtml(String(p.value['html'] ?? '')));
const pad = computed(() => String(p.value['padding'] ?? ''));
const text = (name: string): string => String(p.value[name] ?? '');

function onDragStart(event: DragEvent): void {
  if (isColumn.value) {
    return;
  }
  event.dataTransfer?.setData(DRAG_MIME, JSON.stringify({ kind: 'move', id: props.block.id }));
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
  }
  editor.dragging.value = props.block.type;
}

function onKeyDown(event: KeyboardEvent): void {
  if (event.target !== event.currentTarget) {
    return; // typing inside a field of this block
  }
  if (event.key === 'Delete' || event.key === 'Backspace') {
    event.preventDefault();
    if (!isColumn.value) editor.removeBlock(props.block.id);
  } else if (event.altKey && event.key === 'ArrowUp') {
    event.preventDefault();
    editor.moveBy(props.block.id, -1);
  } else if (event.altKey && event.key === 'ArrowDown') {
    event.preventDefault();
    editor.moveBy(props.block.id, 1);
  }
}
</script>

<template>
  <div
    class="blk"
    :class="[`t-${block.type}`, { selected, column: isColumn }]"
    tabindex="0"
    role="group"
    :aria-label="t(block.type)"
    :aria-selected="selected"
    :data-block="block.id"
    :data-type="block.type"
    :draggable="!isColumn"
    @click.stop="editor.select(block.id)"
    @focus.stop="editor.select(block.id)"
    @keydown="onKeyDown"
    @dragstart.stop="onDragStart"
    @dragend.stop="editor.dragging.value = null"
  >
    <div v-if="selected && !isColumn" class="bar" role="toolbar" :aria-label="t(block.type)">
      <span class="tag">{{ t(block.type) }}</span>
      <button type="button" :title="t('moveUp')" :aria-label="t('moveUp')" data-action="up" @click.stop="editor.moveBy(block.id, -1)">↑</button>
      <button type="button" :title="t('moveDown')" :aria-label="t('moveDown')" data-action="down" @click.stop="editor.moveBy(block.id, 1)">↓</button>
      <button type="button" :title="t('duplicate')" :aria-label="t('duplicate')" data-action="duplicate" @click.stop="editor.duplicateBlock(block.id)">⧉</button>
      <button type="button" :title="t('delete')" :aria-label="t('delete')" data-action="delete" @click.stop="editor.removeBlock(block.id)">✕</button>
    </div>
    <span v-if="hidden" class="hidden-badge">{{ hidden }}</span>

    <!-- leaf blocks: an approximation of what the email shows -->
    <div v-if="block.type === 'header'" class="v-header" :style="{ textAlign: text('align') as never, background: text('backgroundColor') || undefined, padding: pad }">
      <img v-if="text('logoUrl')" :src="text('logoUrl')" :alt="text('logoAlt')" :width="Number(p['logoWidth']) || 120" />
      <div v-if="text('title')" class="title">{{ text('title') }}</div>
      <div v-if="!text('logoUrl') && !text('title')" class="placeholder">{{ t('header') }}</div>
    </div>
    <div v-else-if="block.type === 'text'" class="v-text" :style="{ textAlign: text('align') as never, color: text('color') || undefined, fontSize: `${Number(p['fontSize']) || 16}px`, lineHeight: String(p['lineHeight'] ?? 1.5), padding: pad }" v-html="html" />
    <div v-else-if="block.type === 'footer'" class="v-text" :style="{ textAlign: text('align') as never, color: text('color') || undefined, fontSize: `${Number(p['fontSize']) || 12}px`, padding: pad }" v-html="html" />
    <div v-else-if="block.type === 'image'" class="v-image" :style="{ textAlign: text('align') as never, padding: pad }">
      <img v-if="text('src')" :src="text('src')" :alt="text('alt')" :style="{ maxWidth: '100%', width: Number(p['width']) > 0 ? `${p['width']}px` : undefined }" />
      <div v-else class="placeholder">{{ t('noImage') }}</div>
    </div>
    <div v-else-if="block.type === 'button'" class="v-button" :style="{ textAlign: text('align') as never, padding: pad }">
      <span class="btn" :style="{ background: text('backgroundColor'), color: text('color'), borderRadius: `${Number(p['borderRadius']) || 0}px`, fontSize: `${Number(p['fontSize']) || 16}px`, display: p['fullWidth'] ? 'block' : 'inline-block' }">{{ text('text') }}</span>
    </div>
    <div v-else-if="block.type === 'divider'" class="v-divider" :style="{ padding: pad }"><hr :style="{ borderTop: `${Number(p['thickness']) || 1}px solid ${text('color')}` }" /></div>
    <div v-else-if="block.type === 'spacer'" class="v-spacer" :style="{ height: `${Number(p['height']) || 0}px` }" />
    <div v-else-if="block.type === 'html'" class="v-html">
      <span class="badge">{{ p['fullDocument'] ? t('htmlDocument') : t('html') }}</span>
      <code>{{ text('html').replace(/\s+/g, ' ').slice(0, 140) }}</code>
    </div>

    <!-- containers -->
    <div v-else-if="block.type === 'columns'" class="v-columns">
      <BlockView v-for="column in block.children" :key="column.id" :block="column" :style="{ flex: `${Number(column.props['width']) || 50} 1 0` }" />
    </div>
    <BlockList v-else-if="block.type === 'column'" :blocks="block.children ?? []" :parent-id="block.id" parent-type="column" slot-name="children" :empty-label="t('emptyColumn')" />
    <div v-else-if="block.type === 'repeat'" class="v-frame">
      <div class="label">{{ t('repeatFor', { alias: text('alias'), source: text('source') }) }}</div>
      <BlockList :blocks="block.children ?? []" :parent-id="block.id" parent-type="repeat" slot-name="children" :empty-label="t('emptyRepeat')" />
    </div>
    <div v-else-if="block.type === 'condition'" class="v-frame">
      <div class="label">{{ t('ifCondition', { expression: text('expression') }) }}</div>
      <BlockList :blocks="block.children ?? []" :parent-id="block.id" parent-type="condition" slot-name="children" :empty-label="t('emptyThen')" />
      <div class="label else">{{ t('otherwise') }}</div>
      <BlockList :blocks="block.elseChildren ?? []" :parent-id="block.id" parent-type="condition" slot-name="elseChildren" :empty-label="t('emptyElse')" />
    </div>
  </div>
</template>

<style scoped>
.blk { position: relative; outline: 2px solid transparent; outline-offset: -2px; cursor: pointer; min-width: 0; }
.blk:hover { outline-color: rgba(26, 115, 232, 0.35); }
.blk.selected { outline-color: #1a73e8; }
.blk:focus-visible { outline: 2px solid #1a73e8; }
.bar { position: absolute; top: -26px; right: 0; z-index: 5; display: flex; gap: 2px; background: #1a73e8; color: #fff; border-radius: 4px 4px 0 0; padding: 2px 4px; font-size: 12px; align-items: center; }
.bar .tag { margin-right: 6px; font-weight: 600; }
.bar button { background: transparent; color: inherit; border: 0; cursor: pointer; padding: 2px 6px; border-radius: 3px; font-size: 13px; }
.bar button:hover, .bar button:focus-visible { background: rgba(255, 255, 255, 0.25); }
.hidden-badge { position: absolute; top: 2px; left: 2px; z-index: 4; background: #5f6368; color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 8px; }
.placeholder { color: #8a94a3; padding: 14px; text-align: center; border: 1px dashed #c4cad3; border-radius: 4px; }
.v-text :deep(p) { margin: 0 0 0.6em; }
.v-text :deep(p:last-child) { margin-bottom: 0; }
.v-header img, .v-image img { max-width: 100%; height: auto; }
.v-header .title { font-size: 24px; font-weight: 700; }
.btn { padding: 10px 24px; text-align: center; font-family: inherit; }
.v-divider hr { margin: 0; border: 0; }
.v-html { background: #f6f7f9; border: 1px dashed #c4cad3; padding: 8px 10px; font-size: 12px; overflow: hidden; }
.v-html .badge { display: inline-block; background: #e8eaed; border-radius: 8px; padding: 1px 8px; margin-right: 6px; }
.v-columns { display: flex; gap: 6px; }
.blk.column { border: 1px dashed #d4d9e0; padding: 4px; }
.v-frame { border-left: 3px solid #7e57c2; padding: 4px 0 4px 8px; background: rgba(126, 87, 194, 0.05); }
.v-frame .label { font-size: 12px; color: #5e35b1; font-weight: 600; padding: 2px 0; }
.v-frame .label.else { margin-top: 6px; }
</style>
