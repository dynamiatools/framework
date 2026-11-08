<!-- PromptHost.vue: renders the current PromptManager request (if any) as a Dialog with a single input.
     Mount exactly once near the app root — every useInput().prompt(...) call anywhere else in the app
     shares this same instance. -->
<template>
  <Dialog
      v-if="current"
      :title="current.title ?? ''"
      :closable="false"
      panel-class="dynamia-prompt"
  >
    <p class="dynamia-prompt-message">{{ current.message }}</p>
    <div v-if="current.inputType === 'daterange'" class="dynamia-prompt-range">
      <input
          ref="inputRef"
          v-model="rangeStart"
          class="dynamia-prompt-input dynamia-prompt-range-start"
          type="date"
          :max="rangeEnd || undefined"
          @keyup.esc="respond(false)"
      />
      <input
          v-model="rangeEnd"
          class="dynamia-prompt-input dynamia-prompt-range-end"
          type="date"
          :min="rangeStart || undefined"
          @keyup.enter="respond(true)"
          @keyup.esc="respond(false)"
      />
    </div>
    <input
        v-else
        ref="inputRef"
        v-model="value"
        class="dynamia-prompt-input"
        :type="current.inputType"
        :placeholder="current.placeholder"
        @keyup.enter="respond(true)"
        @keyup.esc="respond(false)"
    />
    <template #footer>
      <button type="button" class="dynamia-prompt-cancel" @click="respond(false)">
        {{ current.cancelLabel }}
      </button>
      <button type="button" class="dynamia-prompt-confirm" @click="respond(true)">
        {{ current.confirmLabel }}
      </button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue';
import { useInput } from '../composables/useInput.js';
import Dialog from './Dialog.vue';

const { current, answer } = useInput();
const value = ref('');
const rangeStart = ref('');
const rangeEnd = ref('');
const inputRef = ref<HTMLInputElement | null>(null);

watch(current, request => {
  value.value = request?.defaultValue ?? '';
  const [start = '', end = ''] = (request?.defaultValue ?? '').split('/');
  rangeStart.value = request?.inputType === 'daterange' ? start : '';
  rangeEnd.value = request?.inputType === 'daterange' ? end : '';
  if (request) {
    nextTick(() => inputRef.value?.focus());
  }
});

function respond(confirmed: boolean): void {
  if (!current.value) return;
  if (!confirmed) {
    answer(current.value.id, null);
  } else if (current.value.inputType === 'daterange') {
    // "start/end"; an empty side is an open range, both empty is no answer
    answer(current.value.id, rangeStart.value || rangeEnd.value ? `${rangeStart.value}/${rangeEnd.value}` : null);
  } else {
    answer(current.value.id, value.value);
  }
}
</script>

<!--
  PromptHost.vue is intentionally headless (see Dialog.vue/Form.vue) — style these class names:

  .dynamia-prompt          — panel class applied to the underlying <DynamiaDialog>
  .dynamia-prompt-message  — the question/message text
  .dynamia-prompt-input    — the <input> (two of them, inside .dynamia-prompt-range, for a date range)
  .dynamia-prompt-cancel   — cancel button
  .dynamia-prompt-confirm  — confirm/OK button
-->
