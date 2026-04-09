// TreeView: headless hierarchical tree DataSetView (flat-node model)

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { DataSetView } from './DataSetView.js';
import { ViewTypes } from './ViewType.js';
import type { TreeState, TreeNode } from '../types/state.js';

/** Loader function type for TreeView — returns a flat array of nodes */
export type TreeLoader = (params: Record<string, unknown>) => Promise<{ nodes: TreeNode[] }>;

/**
 * DataSetView implementation for hierarchical tree data.
 * Stores a flat {@link TreeNode} array; parent-child links are expressed via
 * `TreeNode.parentId`.  UI components (e.g. Tree.vue) are responsible for
 * building the visual hierarchy from the flat list.
 *
 * Example:
 * <pre>{@code
 * const view = new TreeView(descriptor, metadata);
 * view.setLoader(async () => ({ nodes: await api.getCategories() }));
 * await view.initialize();
 * await view.load();
 * view.expand(view.getNodes()[0]);
 * }</pre>
 */
export class TreeView extends DataSetView {
  protected override state: TreeState;

  private _treeLoader?: TreeLoader;

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(ViewTypes.Tree, descriptor, entityMetadata);
    this.state = {
      loading: false,
      error: null,
      initialized: false,
      nodes: [],
      expandedNodeIds: new Set(),
      selectedItem: null,
    };
  }

  async initialize(): Promise<void> {
    this.state.initialized = true;
    this.emit('ready');
  }

  validate(): boolean { return true; }

  // ── DataSetView contract ──────────────────────────────────────────────────

  override isTreeView(): boolean { return true; }

  override getSelected(): TreeNode | null { return this.state.selectedItem; }

  override setSelected(item: unknown): void {
    if (item == null) {
      this.state.selectedItem = null;
      this.emit('select', null);
      return;
    }
    // Accept a TreeNode directly or match by id/reference
    const node = this._findNode(item);
    this.state.selectedItem = node ?? null;
    this.emit('select', this.state.selectedItem);
  }

  override isEmpty(): boolean { return this.state.nodes.length === 0; }

  /** Load nodes using the registered loader */
  override async load(params: Record<string, unknown> = {}): Promise<void> {
    this.state.loading = true;
    this.state.error = null;
    try {
      if (this._treeLoader) {
        const result = await this._treeLoader(params);
        this.state.nodes = result.nodes;
        this.emit('load', this.state.nodes);
      }
    } catch (e) {
      this.state.error = String(e);
      this.emit('error', e);
      throw e;
    } finally {
      this.state.loading = false;
    }
  }

  // ── Tree-specific API ─────────────────────────────────────────────────────

  /** Set the loader function that supplies flat tree nodes */
  setLoader(loader: TreeLoader): void { this._treeLoader = loader; }

  /** Get all flat nodes */
  getNodes(): TreeNode[] { return this.state.nodes; }

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

  /** Toggle expand/collapse for a node */
  toggle(node: TreeNode): void {
    if (this.isExpanded(node)) {
      this.collapse(node);
    } else {
      this.expand(node);
    }
  }

  /** Select a node (preferred API for tree-specific selection) */
  selectNode(node: TreeNode): void {
    this.state.selectedItem = node;
    this.emit('select', node);
  }

  /** Returns true if the given node is expanded */
  isExpanded(node: TreeNode): boolean { return this.state.expandedNodeIds.has(node.id); }

  /** Returns the ID of the selected node, or null */
  getSelectedNodeId(): string | null { return this.state.selectedItem?.id ?? null; }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private _findNode(item: unknown): TreeNode | undefined {
    if (typeof item === 'object' && item !== null && 'id' in item) {
      const id = (item as TreeNode).id;
      return this.state.nodes.find(n => n.id === id);
    }
    if (typeof item === 'string') {
      return this.state.nodes.find(n => n.id === item);
    }
    return undefined;
  }
}
