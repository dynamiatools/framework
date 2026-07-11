// TableView: handles tabular data, columns, pagination, sorting and search

import type { ViewDescriptor, EntityMetadata, CrudPageable } from '@dynamia-tools/sdk';
import { DataSetView } from './DataSetView.js';
import { ViewTypes } from './ViewType.js';
import type { ResolvedField } from '../types/field.js';
import type { TableState, SortDirection } from '../types/state.js';
import { FieldResolver } from '../resolvers/FieldResolver.js';

/**
 * DataSetView implementation for tabular data.
 * Handles column resolution, row data, pagination, sorting and search.
 *
 * Example:
 * <pre>{@code
 * const view = new TableView(descriptor, metadata);
 * await view.initialize();
 * await view.load();
 * }</pre>
 */
export class TableView extends DataSetView {
  protected override state: TableState;

  private _resolvedColumns: ResolvedField[] = [];
  private _readOnly = false;
  private _crudPath?: string;
  private _tableLoader?: (params: Record<string, unknown>) => Promise<{ rows: unknown[]; pagination: CrudPageable | null }>;

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(ViewTypes.Table, descriptor, entityMetadata);
    this.state = {
      loading: false,
      error: null,
      initialized: false,
      rows: [],
      pagination: null,
      sortField: null,
      sortDir: null,
      searchQuery: '',
      selectedItem: null,
    };
  }

  async initialize(): Promise<void> {
    this.state.loading = true;
    try {
      this._resolvedColumns = FieldResolver.resolveFields(this.descriptor, this.entityMetadata);
      this.state.initialized = true;
    } finally {
      this.state.loading = false;
    }
  }

  validate(): boolean { return true; }

  override getValue(): unknown[] { return [...this.state.rows]; }

  override setSource(source: unknown): void {
    if (Array.isArray(source)) {
      this.state.rows = source;
    }
  }

  override isReadOnly(): boolean { return this._readOnly; }
  override setReadOnly(readOnly: boolean): void { this._readOnly = readOnly; }

  // ── DataSetView contract ──────────────────────────────────────────────────

  override isTableView(): boolean { return true; }

  override getSelected(): unknown { return this.state.selectedItem; }

  override setSelected(item: unknown): void {
    this.state.selectedItem = item;
    this.emit('select', item);
  }

  override isEmpty(): boolean { return this.state.rows.length === 0; }

  /** Load data with optional query parameters */
  override async load(params: Record<string, unknown> = {}): Promise<void> {
    this.state.loading = true;
    this.state.error = null;
    try {
      if (this._tableLoader) {
        const result = await this._tableLoader({ ...params, ...this._buildQueryParams() });
        this.state.rows = result.rows;
        this.state.pagination = result.pagination;
        this.emit('load', this.state.rows);
      }
    } catch (e) {
      this.state.error = String(e);
      this.emit('error', e);
      throw e;
    } finally {
      this.state.loading = false;
    }
  }

  // ── Table-specific API ────────────────────────────────────────────────────

  /** Set the path for CRUD operations (used to build API calls) */
  setCrudPath(path: string): void { this._crudPath = path; }
  getCrudPath(): string | undefined { return this._crudPath; }

  /** Set a custom loader function for fetching rows */
  setLoader(loader: (params: Record<string, unknown>) => Promise<{ rows: unknown[]; pagination: CrudPageable | null }>): void {
    this._tableLoader = loader;
  }

  /** Go to next page */
  async nextPage(): Promise<void> {
    if (this.state.pagination && this.state.pagination.page < this.state.pagination.pagesNumber) {
      await this.load({ page: this.state.pagination.page + 1 });
    }
  }

  /** Go to previous page */
  async prevPage(): Promise<void> {
    if (this.state.pagination && this.state.pagination.page > 1) {
      await this.load({ page: this.state.pagination.page - 1 });
    }
  }

  /** Sort by a field */
  async sort(field: string): Promise<void> {
    if (this.state.sortField === field) {
      this.state.sortDir = this.state.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.state.sortField = field;
      this.state.sortDir = 'asc';
    }
    await this.load();
  }

  /** Search with a query string */
  async search(query: string): Promise<void> {
    this.state.searchQuery = query;
    await this.load({ page: 1 });
  }

  /** Select a row (alias for setSelected with table semantics) */
  selectRow(row: unknown): void { this.setSelected(row); }

  /** Get selected row (alias for getSelected) */
  getSelectedRow(): unknown { return this.getSelected(); }

  /** Get resolved column definitions */
  getResolvedColumns(): ResolvedField[] { return this._resolvedColumns; }

  /** Get current rows */
  getRows(): unknown[] { return this.state.rows; }

  /** Get current pagination state */
  getPagination(): CrudPageable | null { return this.state.pagination; }

  private _buildQueryParams(): Record<string, unknown> {
    const params: Record<string, unknown> = {};
    if (this.state.pagination) params['page'] = this.state.pagination.page;
    if (this.state.sortField) {
      params['sortField'] = this.state.sortField;
      params['sortDir'] = this.state.sortDir ?? 'asc';
    }
    if (this.state.searchQuery) params['q'] = this.state.searchQuery;
    return params;
  }
}
