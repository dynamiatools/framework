<!-- Dialog.vue: generic modal shell (backdrop + panel + title/body/footer slots), teleported to <body> -->
<template>
  <Teleport to="body">
    <div class="dynamia-dialog-backdrop" @click="handleBackdropClick">
      <div :class="['dynamia-dialog', panelClass]" role="dialog" aria-modal="true" @click.stop>
        <div v-if="title || closable" class="dynamia-dialog-header">
          <span v-if="title" class="dynamia-dialog-title">{{ title }}</span>
          <button
              v-if="closable"
              type="button"
              class="dynamia-dialog-close"
              aria-label="Close"
              @click="emit('close')"
          >
            &times;
          </button>
        </div>
        <div class="dynamia-dialog-body">
          <slot/>
        </div>
        <div v-if="$slots['footer']" class="dynamia-dialog-footer">
          <slot name="footer"/>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  /** Optional heading shown in the dialog header. */
  title?: string;
  /** Whether the close (×) button is shown and the backdrop click closes the dialog. Defaults to `true`. */
  closable?: boolean;
  /**
   * Extra class(es) applied to the dialog panel itself.
   * Note: plain `class="..."` on `<DynamiaDialog>` does NOT reach the panel — this component's
   * template root is `<Teleport>`, and Vue's attribute fallthrough does not propagate through it.
   */
  panelClass?: string;
}>(), {
  closable: true,
});

const emit = defineEmits<{
  close: [];
}>();

function handleBackdropClick(): void {
  if (props.closable) emit('close');
}
</script>
