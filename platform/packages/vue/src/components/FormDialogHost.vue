<!-- FormDialogHost.vue: renders the current DialogFormManager request (if any) as a Dialog wrapping a
     DynamiaForm. Mount exactly once near the app root — every useFormDialog().showForm(...) call
     anywhere else (e.g. runActionFlow's DIALOG step) shares this same instance. -->
<template>
  <Dialog
      v-if="current"
      :title="current.title ?? ''"
      panel-class="dynamia-form-dialog"
      @close="respond(null)"
  >
    <Form :view="current.view" @submit="respond" @cancel="respond(null)"/>
  </Dialog>
</template>

<script setup lang="ts">
import { useFormDialog } from '../composables/useFormDialog.js';
import Dialog from './Dialog.vue';
import Form from './Form.vue';

const { current, answer } = useFormDialog();

function respond(values: Record<string, unknown> | null): void {
  if (current.value) answer(current.value.id, values);
}
</script>

<!--
  FormDialogHost.vue is intentionally headless (see Dialog.vue/Form.vue) — style via:

  .dynamia-form-dialog — panel class applied to the underlying <DynamiaDialog>
  (the form itself uses Form.vue's own .dynamia-form-* class names)
-->
