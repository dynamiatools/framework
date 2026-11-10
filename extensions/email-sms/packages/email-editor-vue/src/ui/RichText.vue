<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { safeUrl } from '../compiler/mjml.js';
import { sanitizeHtml } from './sanitize.js';
import type { EditorState } from './state.js';

const props = defineProps<{ modelValue: string; label: string }>();
const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>();
const editor = inject<EditorState>('editor')!;
const t = inject<(key: string, values?: Record<string, string | number>) => string>('t')!;

const area = ref<HTMLDivElement | null>(null);
const linkOpen = ref(false);
const linkUrl = ref('https://');
let saved: Range | null = null;

onMounted(() => {
  if (area.value) {
    area.value.innerHTML = sanitizeHtml(props.modelValue);
  }
});

// undo, redo and loading change the value from outside: show it without moving the caret while typing
watch(
  () => props.modelValue,
  (value) => {
    if (area.value && sanitizeHtml(area.value.innerHTML) !== value) {
      area.value.innerHTML = sanitizeHtml(value);
    }
  },
);

function changed(): void {
  if (area.value) {
    emit('update:modelValue', sanitizeHtml(area.value.innerHTML));
  }
}

function command(name: string, value?: string): void {
  area.value?.focus();
  document.execCommand(name, false, value);
  changed();
}

function insert(text: string): void {
  area.value?.focus();
  if (!document.execCommand('insertText', false, text)) {
    const selection = window.getSelection();
    if (selection && selection.rangeCount > 0) {
      const range = selection.getRangeAt(0);
      range.deleteContents();
      range.insertNode(document.createTextNode(text));
      range.collapse(false);
    } else if (area.value) {
      area.value.append(document.createTextNode(text));
    }
  }
  changed();
}

function onFocus(): void {
  editor.setInserter(insert);
}

function onPaste(event: ClipboardEvent): void {
  event.preventDefault();
  const html = event.clipboardData?.getData('text/html');
  if (html) {
    document.execCommand('insertHTML', false, sanitizeHtml(html));
  } else {
    document.execCommand('insertText', false, event.clipboardData?.getData('text/plain') ?? '');
  }
  changed();
}

function openLink(): void {
  const selection = window.getSelection();
  saved = selection && selection.rangeCount > 0 ? selection.getRangeAt(0).cloneRange() : null;
  linkOpen.value = !linkOpen.value;
}

function applyLink(): void {
  const url = safeUrl(linkUrl.value);
  linkOpen.value = false;
  if (!url) {
    return;
  }
  area.value?.focus();
  const selection = window.getSelection();
  if (saved && selection) {
    selection.removeAllRanges();
    selection.addRange(saved);
  }
  document.execCommand('createLink', false, url);
  changed();
}
</script>

<template>
  <div class="rich">
    <div class="tools" role="toolbar" :aria-label="label">
      <button type="button" data-cmd="bold" :title="t('bold')" :aria-label="t('bold')" @mousedown.prevent @click="command('bold')"><b>B</b></button>
      <button type="button" data-cmd="italic" :title="t('italic')" :aria-label="t('italic')" @mousedown.prevent @click="command('italic')"><i>I</i></button>
      <button type="button" data-cmd="underline" :title="t('underline')" :aria-label="t('underline')" @mousedown.prevent @click="command('underline')"><u>U</u></button>
      <button type="button" data-cmd="insertUnorderedList" :title="t('bulletList')" :aria-label="t('bulletList')" @mousedown.prevent @click="command('insertUnorderedList')">•</button>
      <button type="button" data-cmd="insertOrderedList" :title="t('numberList')" :aria-label="t('numberList')" @mousedown.prevent @click="command('insertOrderedList')">1.</button>
      <button type="button" data-cmd="link" :title="t('link')" :aria-label="t('link')" @mousedown.prevent @click="openLink">🔗</button>
      <button type="button" data-cmd="removeFormat" :title="t('clearFormat')" :aria-label="t('clearFormat')" @mousedown.prevent @click="command('removeFormat')">⌫</button>
    </div>
    <div v-if="linkOpen" class="link" data-link>
      <input v-model="linkUrl" type="text" inputmode="url" :aria-label="t('linkUrl')" @keydown.enter.prevent="applyLink" />
      <button type="button" data-action="apply-link" @click="applyLink">{{ t('apply') }}</button>
    </div>
    <div ref="area" class="area" contenteditable="true" role="textbox" aria-multiline="true" :aria-label="label" data-rich @input="changed" @focus="onFocus" @paste="onPaste" />
  </div>
</template>

<style scoped>
.rich { border: 1px solid #c4cad3; border-radius: 4px; background: #fff; }
.tools { display: flex; gap: 2px; padding: 4px; border-bottom: 1px solid #e3e6ea; flex-wrap: wrap; }
.tools button { min-width: 28px; padding: 3px 6px; border: 1px solid transparent; background: transparent; border-radius: 4px; cursor: pointer; font: inherit; }
.tools button:hover, .tools button:focus-visible { background: #eef2f8; border-color: #c4cad3; }
.link { display: flex; gap: 4px; padding: 4px; border-bottom: 1px solid #e3e6ea; }
.link input { flex: 1; padding: 4px 6px; border: 1px solid #c4cad3; border-radius: 4px; font: inherit; }
.link button { padding: 4px 8px; border: 1px solid #c4cad3; background: #fff; border-radius: 4px; cursor: pointer; font: inherit; }
.area { min-height: 110px; max-height: 260px; overflow: auto; padding: 8px; outline: none; }
.area:focus { box-shadow: inset 0 0 0 2px rgba(26, 115, 232, 0.35); }
.area :deep(p) { margin: 0 0 0.6em; }
</style>
