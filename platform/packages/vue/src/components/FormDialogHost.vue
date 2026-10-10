<!-- FormDialogHost.vue: renders the current DialogFormManager request (if any) as a Dialog wrapping a
     DynamiaForm. Mount exactly once near the app root — every useFormDialog().showForm(...) call
     anywhere else (e.g. runActionFlow's DIALOG step) shares this same instance. -->
<template>
  <Dialog
      v-if="current"
      :title="current.title ?? ''"
      panel-class="dynamia-form-dialog"
      :panel-style="panelStyle"
      @close="respond(null)"
  >
    <template v-if="current.readonly">
      <fieldset disabled class="dynamia-form-readonly">
        <Form :view="current.view">
          <template #actions><span/></template>
        </Form>
      </fieldset>
      <div class="dynamia-form-actions">
        <button type="button" @click="respond({})">Close</button>
      </div>
    </template>
    <template v-else>
      <div v-if="current.message" class="dynamia-form-message" :class="`dynamia-form-message--${(current.messageType ?? 'info').toLowerCase()}`"
           role="alert">{{ current.message }}</div>
      <Form :view="current.view" @submit="respond" @cancel="respond(null)"/>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useFormDialog } from '../composables/useFormDialog.js';
import Dialog from './Dialog.vue';
import Form from './Form.vue';

const { current, answer } = useFormDialog();

const panelStyle = computed(() => {
  const request = current.value;
  const style = [request?.width ? `width: ${request.width}` : '', request?.height ? `height: ${request.height}` : ''];
  return style.filter(Boolean).join('; ') || undefined;
});

function respond(values: Record<string, unknown> | null): void {
  if (current.value) answer(current.value.id, values);
}
</script>

<!--
  FormDialogHost.vue is intentionally headless (see Dialog.vue/Form.vue) — style via:

  .dynamia-form-dialog — panel class applied to the underlying <DynamiaDialog>
  .dynamia-form-message(--error|--warning|--info) — why the form is shown again; field errors use .dynamia-field-error
  (the form itself uses Form.vue's own .dynamia-form-* class names)
-->
