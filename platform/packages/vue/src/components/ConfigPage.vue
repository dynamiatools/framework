<!-- ConfigPage.vue: auto-renders the form of a ConfigPage NavigationNode (application parameters) -->
<template>
  <div class="dynamia-config-page">
    <div v-if="loading" class="dynamia-config-page-loading">
      <slot name="loading">
        <span>Loading...</span>
      </slot>
    </div>

    <div v-else-if="error" class="dynamia-config-page-error">
      <slot name="error" :error="error">
        <span>{{ error }}</span>
      </slot>
    </div>

    <Form v-else-if="view" :view="view" @submit="onSubmit">
      <template #actions>
        <slot name="actions" :save="save" :saving="saving">
          <button type="submit" class="dynamia-config-page-save" :disabled="saving">Save</button>
        </slot>
      </template>
    </Form>
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import type { NavigationNode, DynamiaClient } from '@dynamia-tools/sdk';
import { useConfigPage } from '../composables/useConfigPage.js';
import Form from './Form.vue';

/**
 * Fully-automatic page for a `ConfigPage` NavigationNode: the form of its `config` descriptor with the values of the
 * application parameters and a Save button. Fields whose component is unknown to this frontend are text inputs.
 *
 * Example:
 * ```vue
 * <ConfigPage :node="navigationNode" :client="dynamiaClient" />
 * ```
 */
const props = defineProps<{
  /** NavigationNode with type "ConfigPage" */
  node: NavigationNode;
  /** DynamiaClient instance for all API calls */
  client: DynamiaClient;
}>();

const emit = defineEmits<{
  /** Emitted after the values were saved */
  save: [values: Record<string, unknown>];
}>();

const { view, loading, saving, error, reload, save } = useConfigPage({
  node: props.node,
  client: props.client,
});

watch(() => props.node, (node, previous) => {
  if (node !== previous) void reload(node);
});

async function onSubmit(): Promise<void> {
  if (await save() && view.value) emit('save', { ...view.value.values.value });
}
</script>
