<!-- StarRatingHost.vue: renders the current starRatingManager request (if any) as a DynamiaDialog with
     a clickable star picker. Mount exactly once near the app root, alongside DynamiaConfirmHost/
     DynamiaPromptHost/etc. — this is the renderer registered under "star-rating" in main.ts, the
     CUSTOM flow step example for RateBookAction (see docs/design/SERVER_DRIVEN_ACTION_FLOWS.md §6). -->
<template>
  <DynamiaDialog
      v-if="current"
      title="Rate this book"
      :closable="false"
      panel-class="dynamia-star-rating"
  >
    <p class="dynamia-star-rating-message">How many stars?</p>
    <div class="dynamia-star-rating-stars">
      <button
          v-for="n in current.max"
          :key="n"
          type="button"
          class="dynamia-star-rating-star"
          :class="{ 'dynamia-star-rating-star-filled': n <= hovered || (hovered === 0 && n <= selected) }"
          @mouseenter="hovered = n"
          @mouseleave="hovered = 0"
          @click="selected = n"
      >
        ★
      </button>
    </div>
    <template #footer>
      <button type="button" class="dynamia-star-rating-cancel" @click="respond(null)">Cancel</button>
      <button type="button" class="dynamia-star-rating-confirm" :disabled="selected === 0" @click="respond(selected)">
        Submit
      </button>
    </template>
  </DynamiaDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { DynamiaDialog } from '@dynamia-tools/vue';
import { answerStarRating, starRatingState } from '../lib/starRatingManager.js';

const current = ref(starRatingState.current);
watch(
    () => starRatingState.current,
    value => {
      current.value = value;
      selected.value = 0;
      hovered.value = 0;
    },
);

const selected = ref(0);
const hovered = ref(0);

function respond(value: number | null): void {
  if (!current.value) return;
  answerStarRating(current.value.id, value);
}
</script>
