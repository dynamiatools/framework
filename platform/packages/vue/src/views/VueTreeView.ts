// VueTreeView: Vue-reactive extension of TreeView

import { ref } from 'vue';
import type { Ref } from 'vue';
import { TreeView } from '@dynamia-tools/ui-core';
import type { TreeNode, TreeLoader } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';

/**
 * Vue-reactive extension of {@link TreeView}.
 * Exposes reactive refs for nodes, expanded IDs, selected node and loading state
 * so Vue templates can bind to them directly.
 *
 * Example:
 * <pre>{@code
 * const view = new VueTreeView(descriptor, metadata);
 * view.setLoader(async () => ({ nodes: await api.getCategories() }));
 * await view.initialize();
 * await view.load();
 * }</pre>
 */
export class VueTreeView extends TreeView {
  /** Reactive flat node list */
  readonly nodes: Ref<TreeNode[]> = ref([]);
  /** Reactive set of expanded node IDs */
  readonly expandedIds: Ref<Set<string>> = ref(new Set());
  /** Reactive selected node */
  readonly selectedNode: Ref<TreeNode | null> = ref(null);
  /** Reactive loading flag */
  readonly isLoading: Ref<boolean> = ref(false);
  /** Reactive error message */
  readonly errorMessage: Ref<string | null> = ref(null);
  /** Reactive initialized flag */
  readonly isInitialized: Ref<boolean> = ref(false);

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(descriptor, entityMetadata);
  }

  override async initialize(): Promise<void> {
    await super.initialize();
    this.isInitialized.value = true;
  }

  override setLoader(loader: TreeLoader): void {
    super.setLoader(loader);
  }

  override async load(params: Record<string, unknown> = {}): Promise<void> {
    this.isLoading.value = true;
    this.errorMessage.value = null;
    try {
      await super.load(params);
      this.nodes.value = [...super.getNodes()];
    } catch (e) {
      this.errorMessage.value = String(e);
      throw e;
    } finally {
      this.isLoading.value = false;
    }
  }

  override setSelected(item: unknown): void {
    super.setSelected(item);
    this.selectedNode.value = super.getSelected() as TreeNode | null;
  }

  override selectNode(node: TreeNode): void {
    super.selectNode(node);
    this.selectedNode.value = node;
  }

  override expand(node: TreeNode): void {
    super.expand(node);
    this.expandedIds.value = new Set(this.state.expandedNodeIds);
  }

  override collapse(node: TreeNode): void {
    super.collapse(node);
    this.expandedIds.value = new Set(this.state.expandedNodeIds);
  }

  override toggle(node: TreeNode): void {
    super.toggle(node);
    this.expandedIds.value = new Set(this.state.expandedNodeIds);
  }
}
