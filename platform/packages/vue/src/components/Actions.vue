<!-- Actions.vue: Toolbar rendering resolved actions as buttons -->
<template>
  <div class="dynamia-actions">
    <button
      v-for="action in actions"
      :key="action.id"
      class="dynamia-action-btn"
      :title="action.description"
      @click="$emit('action', action)"
    >
      <span v-if="action.icon" :class="action.icon" class="dynamia-action-icon" />
      {{ action.name }}
    </button>
    <slot />
  </div>
</template>

<script setup lang="ts">
import type { ActionMetadata } from '@dynamia-tools/sdk';
import type { View } from '@dynamia-tools/ui-core';

defineProps<{
  /** List of resolved actions to display */
  actions: ActionMetadata[];
  /** The view this actions toolbar belongs to */
  view?: View;
}>();

defineEmits<{
  /** Emitted when an action button is clicked */
  action: [action: ActionMetadata];
}>();
</script>
