// VueTableView: Vue-reactive extension of TableView

import { ref, computed } from 'vue';
import type { Ref, ComputedRef } from 'vue';
import { TableView, FieldResolver } from '@dynamia-tools/ui-core';
import type { ResolvedField, TableState } from '@dynamia-tools/ui-core';
import type { CrudPageable } from '@dynamia-tools/sdk';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';

/**
 * Vue-reactive extension of TableView.
 * Exposes reactive refs for rows, columns and pagination.
 *
 * Example:
 * <pre>{@code
 * const table = new VueTableView(descriptor, metadata);
 * await table.initialize();
 * await table.load();
 * }</pre>
 */
export class VueTableView extends TableView {
  /** Reactive table rows */
  readonly rows: Ref<unknown[]> = ref([]);
  /** Reactive pagination info */
  readonly pagination: Ref<CrudPageable | null> = ref(null);
  /** Reactive loading state */
  readonly isLoading: Ref<boolean> = ref(false);
  /** Reactive selected row */
  readonly selectedRow: Ref<unknown> = ref(null);
  /** Reactive sort field */
  readonly sortField: Ref<string | null> = ref(null);
  /** Reactive sort direction */
  readonly sortDir: Ref<'asc' | 'desc' | null> = ref(null);
  /** Reactive search query */
  readonly searchQuery: Ref<string> = ref('');
  /** Reactive initialized flag */
  readonly isInitialized: Ref<boolean> = ref(false);

  private readonly _columnsRef: Ref<ResolvedField[]> = ref([]);

  /** Reactive computed columns */
  readonly columns: ComputedRef<ResolvedField[]> = computed(() => this._columnsRef.value);

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(descriptor, entityMetadata);
  }

  override async initialize(): Promise<void> {
    this._columnsRef.value = FieldResolver.resolveFields(this.descriptor, this.entityMetadata);
    this.isInitialized.value = true;
    this.state.initialized = true;
  }

  override async load(params: Record<string, unknown> = {}): Promise<void> {
    this.isLoading.value = true;
    try {
      await super.load(params);
      this.rows.value = [...super.getRows()];
      this.pagination.value = super.getPagination();
    } finally {
      this.isLoading.value = false;
    }
  }

  override setSource(source: unknown): void {
    super.setSource(source);
    if (Array.isArray(source)) {
      this.rows.value = source;
    }
  }

  override selectRow(row: unknown): void {
    super.selectRow(row);
    this.selectedRow.value = row;
  }

  override async sort(field: string): Promise<void> {
    await super.sort(field);
    const tableState = super.getState() as TableState;
    this.sortField.value = tableState.sortField ?? null;
    this.sortDir.value = tableState.sortDir ?? null;
  }

  override async search(query: string): Promise<void> {
    this.searchQuery.value = query;
    await super.search(query);
  }
}
