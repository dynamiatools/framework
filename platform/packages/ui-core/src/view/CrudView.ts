// CrudView: orchestrates FormView + TableView + action lifecycle

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { View } from './View.js';
import { ViewTypes } from './ViewType.js';
import { FormView } from './FormView.js';
import { TableView } from './TableView.js';
import type { CrudState, CrudMode } from '../types/state.js';

/**
 * CRUD view that orchestrates a FormView and TableView.
 * Manages the mode transitions between list, create and edit.
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
  readonly tableView: TableView;

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(ViewTypes.Crud, descriptor, entityMetadata);
    this.state = { loading: false, error: null, initialized: false, mode: 'list' };
    this.formView = new FormView(descriptor, entityMetadata);
    this.tableView = new TableView(descriptor, entityMetadata);
  }

  async initialize(): Promise<void> {
    this.state.loading = true;
    try {
      await Promise.all([this.formView.initialize(), this.tableView.initialize()]);
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

  /** Cancel edit and return to list */
  cancelEdit(): void {
    this.formView.reset();
    this.state.mode = 'list';
    this.emit('mode-change', 'list');
  }

  /** Save the current entity (create or update) */
  async save(): Promise<void> {
    if (!this.formView.validate()) return;
    this.state.loading = true;
    try {
      const data = this.formView.getValue();
      this.emit('save', { mode: this.state.mode, data });
      this.state.mode = 'list';
      this.emit('mode-change', 'list');
      await this.tableView.load();
    } catch (e) {
      this.state.error = String(e);
      this.emit('error', e);
    } finally {
      this.state.loading = false;
    }
  }

  /** Delete an entity */
  async delete(entity: unknown): Promise<void> {
    this.state.loading = true;
    try {
      this.emit('delete', entity);
      await this.tableView.load();
    } catch (e) {
      this.state.error = String(e);
      this.emit('error', e);
    } finally {
      this.state.loading = false;
    }
  }
}
