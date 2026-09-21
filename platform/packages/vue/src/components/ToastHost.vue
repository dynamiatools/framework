<!-- ToastHost.vue: renders the ToastManager queue as a stacked list, teleported to <body>.
     Mount exactly once near the app root — every useToast().show(...) call anywhere else
     in the app shares this same instance. -->
<template>
  <Teleport to="body">
    <div class="dynamia-toast-stack">
      <div
          v-for="item in toasts"
          :key="item.id"
          :class="`dynamia-toast dynamia-toast-${item.variant}`"
          role="status"
      >
        <div class="dynamia-toast-content">
          <span v-if="item.title" class="dynamia-toast-title">{{ item.title }}</span>
          <span class="dynamia-toast-message">{{ item.message }}</span>
        </div>
        <button
            type="button"
            class="dynamia-toast-close"
            aria-label="Dismiss"
            @click="dismiss(item.id)"
        >
          &times;
        </button>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { useToast } from '../composables/useToast.js';

const { toasts, dismiss } = useToast();
</script>
