// CrudView: orchestrates FormView + DataSetView + action lifecycle

import type { ActionExecutionRequest, ActionMetadata, EntityMetadata, ViewDescriptor } from '@dynamia-tools/sdk';
import { View } from './View.js';
import { ViewTypes } from './ViewType.js';
import { FormView } from './FormView.js';
import type { DataSetView } from './DataSetView.js';
import { resolveDataSetView } from './DataSetViewRegistry.js';
import type { TableView } from './TableView.js';
import type { TreeView } from './TreeView.js';
import type { CrudState, CrudMode } from '../types/state.js';
import type { ActionResolutionContext } from '../resolvers/ActionResolver.js';
import { ActionResolver } from '../resolvers/ActionResolver.js';
import type { CrudActionState, CrudActionStateAlias } from '../actions/crudActionState.js';
import { crudModeToActionState, normalizeCrudActionState } from '../actions/crudActionState.js';

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

  /** Returns the current CRUD state using Java-compatible READ/CREATE/UPDATE aliases. */
  getCrudActionState(): CrudActionState {
    return crudModeToActionState(this.getMode());
  }

  /** Returns the entity class name associated with this CRUD view, when known. */
  getEntityClassName(): string | null {
    return this.entityMetadata?.className ?? this.descriptor.beanClass ?? null;
  }

  /** Returns the data object that should be used for action execution in the given state. */
  getActionData(state: CrudActionStateAlias = this.getCrudActionState()): unknown {
    const normalizedState = normalizeCrudActionState(state) ?? this.getCrudActionState();
    if (normalizedState === 'CREATE' || normalizedState === 'UPDATE') {
      return this.formView.getValue();
    }

    return this.dataSetView.getSelected();
  }

  /** Builds an ActionExecutionRequest from the current CRUD context. */
  buildActionExecutionRequest(
    overrides: Partial<ActionExecutionRequest> = {},
    state: CrudActionStateAlias = this.getCrudActionState(),
  ): ActionExecutionRequest {
    const data = overrides.data ?? this.getActionData(state);
    const entityClassName = overrides.dataType ?? this.getEntityClassName() ?? undefined;
    const record = data && typeof data === 'object' ? (data as Record<string, unknown>) : null;
    const dataId = overrides.dataId ?? record?.id;
    const dataName = overrides.dataName ?? (typeof record?.name === 'string' ? record.name : undefined);

    return {
      ...(data !== undefined ? { data } : {}),
      ...(overrides.params !== undefined ? { params: overrides.params } : {}),
      ...(overrides.source ?? this.viewType.name ? { source: overrides.source ?? this.viewType.name } : {}),
      ...(entityClassName !== undefined ? { dataType: entityClassName } : {}),
      ...(dataId !== undefined ? { dataId } : {}),
      ...(dataName !== undefined ? { dataName } : {}),
    };
  }

  /** Resolve actions for the current CRUD state and entity class. */
  resolveActions(
    actions?: ActionMetadata[] | null,
    context: Omit<ActionResolutionContext, 'targetClass' | 'crudState'> = {},
  ): ActionMetadata[] {
    return ActionResolver.resolveActions(actions ?? this.entityMetadata?.actions ?? [], {
      ...context,
      targetClass: this.getEntityClassName(),
      crudState: this.getCrudActionState(),
    });
  }

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
