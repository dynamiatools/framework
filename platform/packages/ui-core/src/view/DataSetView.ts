// DataSetView: abstract base for views that display and manage a collection of items.
// Mirrors tools.dynamia.viewers.DataSetView from the Java/ZK platform.

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { View } from './View.js';
import type { ViewType } from './ViewType.js';
import type { DataSetViewState } from '../types/state.js';

/**
 * Abstract base class for views that display and manage a collection of data items.
 * Mirrors {@code tools.dynamia.viewers.DataSetView}.
 *
 * Concrete implementations ({@link TableView}, {@link TreeView}) extend this class
 * and are registered in {@link DataSetViewRegistry}.
 * {@link CrudView} holds a single `dataSetView` instance selected at construction
 * time via the registry according to the descriptor param `dataSetViewType`
 * (default: `'table'`).
 *
 * Example:
 * <pre>{@code
 * const view: DataSetView = resolveDataSetView('table', descriptor, metadata);
 * await view.initialize();
 * await view.load();
 * const selected = view.getSelected();
 * }</pre>
 */
export abstract class DataSetView extends View {
  protected override state: DataSetViewState;

  constructor(viewType: ViewType, descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(viewType, descriptor, entityMetadata);
    this.state = { loading: false, error: null, initialized: false, selectedItem: null };
  }

  /**
   * Load / refresh the dataset.
   * Implementations call their internal loader and update state.
   */
  abstract load(params?: Record<string, unknown>): Promise<void>;

  /**
   * Returns the currently selected item, or `null` if nothing is selected.
   * Mirrors {@code DataSetView.getSelected()}.
   */
  abstract getSelected(): unknown;

  /**
   * Programmatically select an item.
   * Mirrors {@code DataSetView.setSelected()}.
   */
  abstract setSelected(item: unknown): void;

  /**
   * Returns `true` when the dataset contains no items.
   * Mirrors {@code DataSetView.isEmpty()}.
   */
  abstract isEmpty(): boolean;

  // ── Narrowing helpers ──────────────────────────────────────────────────────
  // These return false by default and are overridden in each concrete subclass.
  // Use them to narrow a DataSetView reference without importing the subclass
  // (which would create a circular dependency).  Cast to the specific subclass
  // after checking, or use CrudView.tableView / CrudView.treeView getters.

  /** Returns `true` if this instance is a {@link TableView}. */
  isTableView(): boolean { return false; }

  /** Returns `true` if this instance is a {@link TreeView}. */
  isTreeView(): boolean { return false; }
}

