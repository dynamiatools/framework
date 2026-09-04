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
    <input
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
const inputRef = ref<HTMLInputElement | null>(null);

watch(current, request => {
  value.value = request?.defaultValue ?? '';
  if (request) {
    nextTick(() => inputRef.value?.focus());
  }
});

function respond(confirmed: boolean): void {
  if (!current.value) return;
  answer(current.value.id, confirmed ? value.value : null);
}
</script>

<!--
  PromptHost.vue is intentionally headless (see Dialog.vue/Form.vue) — style these class names:

  .dynamia-prompt          — panel class applied to the underlying <DynamiaDialog>
  .dynamia-prompt-message  — the question/message text
  .dynamia-prompt-input    — the <input>
  .dynamia-prompt-cancel   — cancel button
  .dynamia-prompt-confirm  — confirm/OK button
-->
