<!-- Tree.vue: Headless tree component driven by VueTreeView (flat-node model).
  Builds the visual hierarchy from TreeNode.parentId links.
  Slot "node"    — custom node label rendering     props: { node, depth }
  Slot "actions" — per-node action buttons          props: { node }
  Slot "loading" — loading state override
  Slot "empty"   — empty state override
-->
<template>
  <div class="dynamia-tree">
    <div v-if="view.isLoading.value" class="dynamia-tree-loading">
      <slot name="loading"><span>Loading…</span></slot>
    </div>

    <template v-else>
      <ul v-if="!view.isEmpty()" class="dynamia-tree-list" role="tree">
        <li
          v-for="item in flattenedTree"
          :key="item.node.id"
          class="dynamia-tree-item"
          :class="{
            'dynamia-tree-item--selected': view.selectedNode.value?.id === item.node.id,
            'dynamia-tree-item--has-children': item.hasChildren,
            'dynamia-tree-item--expanded': item.hasChildren && view.expandedIds.value.has(item.node.id),
          }"
          :style="{ '--tree-depth': item.depth }"
          role="treeitem"
          :aria-expanded="item.hasChildren ? view.expandedIds.value.has(item.node.id) : undefined"
          :aria-selected="view.selectedNode.value?.id === item.node.id"
        >
          <div class="dynamia-tree-item-row" @click="handleNodeClick(item.node, item.hasChildren)">
            <!-- Indentation spacer -->
            <span
              v-for="n in item.depth"
              :key="n"
              class="dynamia-tree-indent"
              aria-hidden="true"
            />

            <!-- Expand / collapse toggle -->
            <button
              v-if="item.hasChildren"
              type="button"
              class="dynamia-tree-toggle"
              :aria-label="view.expandedIds.value.has(item.node.id) ? 'Collapse' : 'Expand'"
              @click.stop="view.toggle(item.node)"
            >
              {{ view.expandedIds.value.has(item.node.id) ? '▾' : '▸' }}
            </button>
            <span v-else class="dynamia-tree-toggle-spacer" aria-hidden="true" />

            <!-- Node icon -->
            <span v-if="item.node.icon" class="dynamia-tree-icon" aria-hidden="true">
              {{ item.node.icon }}
            </span>

            <!-- Node label (customisable via slot) -->
            <slot name="node" :node="item.node" :depth="item.depth">
              <span class="dynamia-tree-label">{{ item.node.label }}</span>
            </slot>

            <!-- Per-node actions -->
            <span v-if="$slots['actions']" class="dynamia-tree-actions">
              <slot name="actions" :node="item.node" />
            </span>
          </div>
        </li>
      </ul>

      <div v-else class="dynamia-tree-empty">
        <slot name="empty"><span>No data</span></slot>
      </div>
    </template>

    <div v-if="view.errorMessage.value" class="dynamia-tree-error">
      {{ view.errorMessage.value }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { TreeNode } from '@dynamia-tools/ui-core';
import type { VueTreeView } from '../views/VueTreeView.js';

interface FlatItem {
  node: TreeNode;
  depth: number;
  hasChildren: boolean;
}

const props = defineProps<{
  /** The VueTreeView instance to render */
  view: VueTreeView;
  /** Whether in read-only mode (hides action slots) */
  readOnly?: boolean;
}>();

const emit = defineEmits<{
  select: [node: TreeNode];
}>();

// ── Build the flat display list from the flat node array ──────────────────────

/** Map of parentId → children, built from parentId links AND embedded children arrays */
const childrenMap = computed((): Map<string, TreeNode[]> => {
  const map = new Map<string, TreeNode[]>();
  const nodeIds = new Set(props.view.nodes.value.map(n => n.id));

  for (const node of props.view.nodes.value) {
    // parentId-based links
    if (node.parentId && nodeIds.has(node.parentId)) {
      if (!map.has(node.parentId)) map.set(node.parentId, []);
      map.get(node.parentId)!.push(node);
    }
    // embedded children (deduplication: only add if not already in the flat list)
    if (node.children) {
      if (!map.has(node.id)) map.set(node.id, []);
      for (const child of node.children) {
        if (!nodeIds.has(child.id)) {
          map.get(node.id)!.push(child);
        }
      }
    }
  }
  return map;
});

/** Root nodes: nodes without a parentId or whose parentId is not in the list */
const rootNodes = computed((): TreeNode[] => {
  const nodeIds = new Set(props.view.nodes.value.map(n => n.id));
  return props.view.nodes.value.filter(n => !n.parentId || !nodeIds.has(n.parentId));
});

/** Ordered flat list with depth info — respects expand/collapse state */
const flattenedTree = computed((): FlatItem[] => {
  const result: FlatItem[] = [];
  appendNodes(rootNodes.value, 0, result);
  return result;
});

function appendNodes(nodes: TreeNode[], depth: number, result: FlatItem[]): void {
  for (const node of nodes) {
    const children = childrenMap.value.get(node.id) ?? [];
    const hasChildren = children.length > 0;
    result.push({ node, depth, hasChildren });
    if (hasChildren && props.view.expandedIds.value.has(node.id)) {
      appendNodes(children, depth + 1, result);
    }
  }
}

// ── Event handlers ────────────────────────────────────────────────────────────

function handleNodeClick(node: TreeNode, hasChildren: boolean): void {
  props.view.selectNode(node);
  emit('select', node);
  // Expand on first click when the node has children and is not yet expanded
  if (hasChildren && !props.view.expandedIds.value.has(node.id)) {
    props.view.expand(node);
  }
}
</script>

