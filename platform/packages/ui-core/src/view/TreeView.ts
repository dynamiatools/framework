// TreeView: hierarchical tree data view

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { View } from './View.js';
import { ViewTypes } from './ViewType.js';
import type { TreeState, TreeNode } from '../types/state.js';

/**
 * View implementation for hierarchical tree data.
 *
 * Example:
 * <pre>{@code
 * const view = new TreeView(descriptor, metadata);
 * await view.initialize();
 * view.expand(node);
 * }</pre>
 */
export class TreeView extends View {
  protected override state: TreeState;

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(ViewTypes.Tree, descriptor, entityMetadata);
    this.state = { loading: false, error: null, initialized: false, nodes: [], expandedNodeIds: new Set(), selectedNodeId: null };
  }

  async initialize(): Promise<void> {
    this.state.initialized = true;
  }

  validate(): boolean { return true; }

  override getValue(): unknown { return this.state.selectedNodeId; }
  override setSource(source: unknown): void {
    if (Array.isArray(source)) this.state.nodes = source as TreeNode[];
  }

  /** Expand a tree node */
  expand(node: TreeNode): void {
    this.state.expandedNodeIds.add(node.id);
    this.emit('expand', node);
  }

  /** Collapse a tree node */
  collapse(node: TreeNode): void {
    this.state.expandedNodeIds.delete(node.id);
    this.emit('collapse', node);
  }

  /** Select a tree node */
  selectNode(node: TreeNode): void {
    this.state.selectedNodeId = node.id;
    this.emit('select', node);
  }

  getNodes(): TreeNode[] { return this.state.nodes; }
  isExpanded(node: TreeNode): boolean { return this.state.expandedNodeIds.has(node.id); }
  getSelectedNodeId(): string | null { return this.state.selectedNodeId; }
}
