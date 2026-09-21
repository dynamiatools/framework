<!-- ConfirmHost.vue: renders the current ConfirmManager request (if any) as a Dialog.
     Mount exactly once near the app root — every useConfirm().confirm(...) call anywhere
     else in the app shares this same instance. -->
<template>
  <Dialog
      v-if="current"
      :title="current.title ?? ''"
      :closable="false"
      :panel-class="`dynamia-confirm dynamia-confirm-${current.variant}`"
  >
    <p class="dynamia-confirm-message">{{ current.message }}</p>
    <template #footer>
      <button type="button" class="dynamia-confirm-cancel" @click="respond(false)">
        {{ current.cancelLabel }}
      </button>
      <button type="button" class="dynamia-confirm-confirm" @click="respond(true)">
        {{ current.confirmLabel }}
      </button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { useConfirm } from '../composables/useConfirm.js';
import Dialog from './Dialog.vue';

const { current, answer } = useConfirm();

function respond(result: boolean): void {
  if (current.value) answer(current.value.id, result);
}
</script>
