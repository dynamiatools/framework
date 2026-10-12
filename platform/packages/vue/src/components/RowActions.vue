<!-- RowActions.vue: per-row actions of a table — the main one as a button, the rest in a "more" menu -->
<template>
  <div class="dynamia-row-actions" @click.stop>
    <button
      v-if="showEdit"
      type="button"
      class="dynamia-row-action dynamia-row-action-edit"
      :title="editLabel"
      :aria-label="editLabel"
      @click="emit('edit', row)"
    >
      <i class="dynamia-action-icon fa fa-pencil-alt" aria-hidden="true" />
    </button>

    <button
      v-if="actions.length > 0"
      ref="trigger"
      type="button"
      class="dynamia-row-action dynamia-row-action-more"
      aria-haspopup="menu"
      :aria-expanded="open"
      :title="moreLabel"
      :aria-label="moreLabel"
      @click="toggle"
    >
      <i class="dynamia-action-icon fa fa-ellipsis-v" aria-hidden="true" />
    </button>

    <!--
      Teleported: the table wrapper scrolls horizontally (overflow), which would clip an absolutely positioned menu.
      Mounted on first open and kept (v-show) so the actions inside can finish a confirm/flow after the menu closes.
    -->
    <Teleport to="body">
      <div
        v-if="mounted"
        v-show="open"
        ref="menu"
        class="dynamia-row-menu"
        role="menu"
        :style="position"
        @keydown.esc="close"
      >
        <Actions
          v-for="action in actions"
          :key="action.id"
          class="dynamia-row-menu-item"
          :class="{ 'dynamia-row-menu-item-danger': isDeleteCrudAction(action) }"
          :actions="[action]"
          :view="view"
          :client="client"
          :auto-execute="autoExecute"
          :entity-class-name="entityClassName"
          :request="{ data: row }"
          @action="close"
          @action-executed="emit('action-executed', $event)"
          @action-response="emit('action-response', $event)"
          @action-error="emit('action-error', $event)"
        />
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref } from 'vue';
import type { ActionMetadata, DynamiaClient } from '@dynamia-tools/sdk';
import type { ActionExecutionErrorEvent, ActionExecutionEvent, View } from '@dynamia-tools/ui-core';
import Actions from './Actions.vue';
import { isDeleteCrudAction } from '../actions/crudActionUtils.js';

/**
 * Actions of one table row: an edit button and a "more" menu with the rest of the actions the server published for the
 * entity (`Delete`, `View`, the ones of the extensions…). Each action of the menu runs through {@link Actions} with the
 * row as the `data` of the request, so it goes through the same client / headless / flow dispatch as a toolbar action.
 */
withDefaults(defineProps<{
  /** The row (entity) these actions work on */
  row: unknown;
  /** Server actions to list in the menu */
  actions: ActionMetadata[];
  /** Whether to show the built-in edit button */
  showEdit?: boolean;
  /** View the actions belong to */
  view?: View | undefined;
  /** DynamiaClient used to execute non-local actions */
  client?: DynamiaClient | undefined;
  /** Execute non-local actions with the client */
  autoExecute?: boolean | undefined;
  /** Entity class name for entity-scoped execution */
  entityClassName?: string | undefined;
  /** Accessible label of the edit button */
  editLabel?: string;
  /** Accessible label of the menu button */
  moreLabel?: string;
}>(), {
  showEdit: true,
  autoExecute: false,
  editLabel: 'Edit',
  moreLabel: 'More actions',
});

const emit = defineEmits<{
  edit: [row: unknown];
  'action-executed': [action: ActionMetadata];
  'action-response': [event: ActionExecutionEvent];
  'action-error': [event: ActionExecutionErrorEvent];
}>();

const MENU_WIDTH = 208;
const trigger = ref<HTMLElement | null>(null);
const menu = ref<HTMLElement | null>(null);
const open = ref(false);
const mounted = ref(false);
const position = ref<Record<string, string>>({});

async function toggle(): Promise<void> {
  if (open.value) {
    close();
    return;
  }
  mounted.value = true;
  place();
  open.value = true;
  await nextTick();
  // Flip above the button when there is not enough room below.
  const box = trigger.value?.getBoundingClientRect();
  const height = menu.value?.offsetHeight ?? 0;
  if (box && box.bottom + height + 8 > window.innerHeight && box.top - height - 4 > 0) {
    position.value = { ...position.value, top: `${box.top - height - 4}px` };
  }
  document.addEventListener('pointerdown', onOutside, true);
  document.addEventListener('keydown', onKey, true);
  window.addEventListener('scroll', close, true);
  window.addEventListener('resize', close);
}

function place(): void {
  const box = trigger.value?.getBoundingClientRect();
  if (!box) return;
  const left = Math.max(8, Math.min(box.right - MENU_WIDTH, window.innerWidth - MENU_WIDTH - 8));
  position.value = { top: `${box.bottom + 4}px`, left: `${left}px`, width: `${MENU_WIDTH}px` };
}

function close(): void {
  if (!open.value) return;
  open.value = false;
  document.removeEventListener('pointerdown', onOutside, true);
  document.removeEventListener('keydown', onKey, true);
  window.removeEventListener('scroll', close, true);
  window.removeEventListener('resize', close);
}

function onOutside(event: Event): void {
  const target = event.target as Node;
  if (menu.value?.contains(target) || trigger.value?.contains(target)) return;
  close();
}

function onKey(event: KeyboardEvent): void {
  if (event.key !== 'Escape') return;
  close();
  trigger.value?.focus();
}

onBeforeUnmount(() => {
  open.value = false;
  document.removeEventListener('pointerdown', onOutside, true);
  document.removeEventListener('keydown', onKey, true);
  window.removeEventListener('scroll', close, true);
  window.removeEventListener('resize', close);
});
</script>
