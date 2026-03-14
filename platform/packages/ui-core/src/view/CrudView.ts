// CrudView: orchestrates FormView + DataSetView + action lifecycle

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { View } from './View.js';
import { ViewTypes } from './ViewType.js';
import { FormView } from './FormView.js';
import type { DataSetView } from './DataSetView.js';
import { resolveDataSetView } from './DataSetViewRegistry.js';
import type { TableView } from './TableView.js';
import type { TreeView } from './TreeView.js';
import type { CrudState, CrudMode } from '../types/state.js';

/**
 * CRUD view that orchestrates a {@link FormView} and a {@link DataSetView}.
 * The concrete DataSetView type is resolved from the descriptor param
 * `dataSetViewType` (default: `'table'`) via {@link DataSetViewRegistry}.
 *
 * Use {@link tableView} / {@link treeView} to narrow the dataset view to its
 * concrete type when needed.
 *
 * Example:
 * <pre>{@code
 * const view = new CrudView(descriptor, metadata);
 * await view.initialize();
 * view.startCreate();
 * view.formView.setFieldValue('name', 'John');
 * await view.save();
 * }</pre>
 */
export class CrudView extends View {
  protected override state: CrudState;

  readonly formView: FormView;
  readonly dataSetView: DataSetView;

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(ViewTypes.Crud, descriptor, entityMetadata);
    this.state = { loading: false, error: null, initialized: false, mode: 'list' };
    this.formView = new FormView(descriptor, entityMetadata);
    this.dataSetView = resolveDataSetView(
      String(descriptor.params['dataSetViewType'] ?? 'table'),
      descriptor,
      entityMetadata,
    );
  }

  // ── Narrowing convenience getters ─────────────────────────────────────────

  /** Returns the dataSetView as {@link TableView}, or `null` if it is not a table */
  get tableView(): TableView | null {
    return this.dataSetView.isTableView() ? (this.dataSetView as unknown as TableView) : null;
  }

  /** Returns the dataSetView as {@link TreeView}, or `null` if it is not a tree */
  get treeView(): TreeView | null {
    return this.dataSetView.isTreeView() ? (this.dataSetView as unknown as TreeView) : null;
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  async initialize(): Promise<void> {
    this.state.loading = true;
    try {
      await Promise.all([this.formView.initialize(), this.dataSetView.initialize()]);
      this.state.initialized = true;
      this.emit('ready');
    } catch (e) {
      this.state.error = String(e);
    } finally {
      this.state.loading = false;
    }
  }

  validate(): boolean { return this.formView.validate(); }

  override getValue(): unknown { return this.formView.getValue(); }
  override setValue(value: unknown): void { this.formView.setValue(value); }

  // ── Mode transitions ──────────────────────────────────────────────────────

  /** Get the current CRUD mode */
  getMode(): CrudMode { return this.state.mode; }

  /** Start creating a new entity */
  startCreate(): void {
    this.formView.reset();
    this.state.mode = 'create';
    this.emit('mode-change', 'create');
  }

  /** Start editing an existing entity */
  startEdit(entity: unknown): void {
    this.formView.setValue(entity);
    this.state.mode = 'edit';
    this.emit('mode-change', 'edit');
  }

  /** Cancel edit / create and return to list */
  cancelEdit(): void {
    this.formView.reset();
    this.state.mode = 'list';
    this.emit('mode-change', 'list');
  }

  /**
   * Save the current entity (create or update).
   * Validates, emits `'save'` and transitions mode to `'list'`.
   * Callers / event handlers are responsible for the actual API call and
   * reloading the dataset so the reload happens *after* the persist completes.
   */
  async save(): Promise<void> {
    if (!this.formView.validate()) return;
    const data = this.formView.getValue();
    this.emit('save', { mode: this.state.mode, data });
    this.state.mode = 'list';
    this.emit('mode-change', 'list');
  }

  /**
   * Delete an entity.
   * Emits `'delete'`. Callers / event handlers are responsible for the actual
   * API call and reloading the dataset after the delete completes.
   */
  async delete(entity: unknown): Promise<void> {
    this.emit('delete', entity);
  }
}
