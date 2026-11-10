<script setup lang="ts">
import type { Block, BlockType, Slot } from '../model/types.js';
import BlockView from './BlockView.vue';
import DropZone from './DropZone.vue';

defineProps<{ blocks: Block[]; parentId: string | null; parentType: BlockType | null; slotName: Slot; emptyLabel: string }>();
</script>

<template>
  <div class="list">
    <template v-for="(block, index) in blocks" :key="block.id">
      <DropZone :at="{ parentId, slot: slotName, index }" :parent-type="parentType" />
      <BlockView :block="block" />
    </template>
    <DropZone
      :at="{ parentId, slot: slotName, index: blocks.length }"
      :parent-type="parentType"
      :label="blocks.length === 0 ? emptyLabel : undefined"
    />
  </div>
</template>
