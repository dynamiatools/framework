// VueTreeView: Vue-reactive extension of TreeView

import { ref } from 'vue';
import type { Ref } from 'vue';
import { TreeView } from '@dynamia-tools/ui-core';
import type { TreeNode } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';

/**
 * Vue-reactive extension of TreeView.
 */
export class VueTreeView extends TreeView {
  /** Reactive tree nodes */
  readonly nodes: Ref<TreeNode[]> = ref([]);
  /** Reactive expanded node IDs */
  readonly expandedIds: Ref<Set<string>> = ref(new Set());
  /** Reactive selected node ID */
  readonly selectedId: Ref<string | null> = ref(null);

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(descriptor, entityMetadata);
  }

  override async initialize(): Promise<void> {
    await super.initialize();
  }

  override setSource(source: unknown): void {
    super.setSource(source);
    if (Array.isArray(source)) this.nodes.value = source as TreeNode[];
  }

  override expand(node: TreeNode): void {
    super.expand(node);
    this.expandedIds.value = new Set(this.state.expandedNodeIds);
  }

  override collapse(node: TreeNode): void {
    super.collapse(node);
    this.expandedIds.value = new Set(this.state.expandedNodeIds);
  }

  override selectNode(node: TreeNode): void {
    super.selectNode(node);
    this.selectedId.value = node.id;
  }
}
