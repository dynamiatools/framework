// VueCrudView: Vue-reactive extension of CrudView

import { ref, computed } from 'vue';
import type { Ref, ComputedRef } from 'vue';
import { CrudView } from '@dynamia-tools/ui-core';
import type { CrudMode } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { VueFormView } from './VueFormView.js';
import { VueTableView } from './VueTableView.js';
import { VueTreeView } from './VueTreeView.js';
import { vueDataSetViewRegistry } from './VueDataSetViewRegistry.js';

/**
 * Vue-reactive extension of {@link CrudView}.
 * The concrete DataSetView is resolved from the descriptor param
 * `dataSetViewType` (default `'table'`) via {@link vueDataSetViewRegistry},
 * ensuring the dataset view is always a Vue-reactive instance.
 *
 * Use {@link tableView} / {@link treeView} to narrow `dataSetView` to its
 * concrete reactive type.
 *
 * Example:
 * <pre>{@code
 * const crud = new VueCrudView(descriptor, metadata);
 * await crud.initialize();
 * crud.startCreate();
 * }</pre>
 */
export class VueCrudView extends CrudView {
  /** Reactive CRUD mode */
  readonly mode: Ref<CrudMode> = ref('list');
  /** Reactive loading state */
  readonly isLoading: Ref<boolean> = ref(false);
  /** Reactive error message */
  readonly errorMessage: Ref<string | null> = ref(null);

  // Override with Vue-reactive subtypes.
  // Field initializers run after super(), so this.descriptor and
  // this.entityMetadata are already set by View's constructor.
  override readonly formView: VueFormView = new VueFormView(this.descriptor, this.entityMetadata);

  /** Vue-reactive DataSetView (VueTableView or VueTreeView depending on descriptor) */
  override readonly dataSetView: VueTableView | VueTreeView = (() => {
    const type = String(this.descriptor.params['dataSetViewType'] ?? 'table');
    return vueDataSetViewRegistry.resolve(type, this.descriptor, this.entityMetadata) as VueTableView | VueTreeView;
  })();

  // ── Narrowing getters ─────────────────────────────────────────────────────

  /** Returns `dataSetView` as {@link VueTableView}, or `null` if it is a tree */
  get tableView(): VueTableView | null {
    return this.dataSetView instanceof VueTableView ? this.dataSetView : null;
  }

  /** Returns `dataSetView` as {@link VueTreeView}, or `null` if it is a table */
  get treeView(): VueTreeView | null {
    return this.dataSetView instanceof VueTreeView ? this.dataSetView : null;
  }

  // ── Computed reactive state ───────────────────────────────────────────────

  /** Whether the form should be shown (create / edit mode) */
  readonly showForm: ComputedRef<boolean> = computed(() =>
    this.mode.value === 'create' || this.mode.value === 'edit',
  );

  /** Whether the dataset view should be shown (list mode) */
  readonly showDataSet: ComputedRef<boolean> = computed(() =>
    this.mode.value === 'list',
  );

  constructor(
    descriptor: ViewDescriptor,
    entityMetadata: EntityMetadata | null = null,
    formDescriptor?: ViewDescriptor,
    dataSetDescriptor?: ViewDescriptor,
  ) {
    super(descriptor, entityMetadata);
    // If a dedicated form descriptor is supplied (e.g. BookForm.yml, view: "form"),
    // replace the formView that the field initializer already created using the crud
    // descriptor. The crud descriptor has no fields / layout / fieldGroups.
    if (formDescriptor && formDescriptor !== descriptor) {
      (this as { formView: VueFormView }).formView = new VueFormView(formDescriptor, entityMetadata);
    }

    // Allow CRUD descriptor (actions/params) and dataset descriptor (table fields)
    // to be resolved independently.
    if (dataSetDescriptor) {
      const dataSetViewType = String(
        descriptor.params?.['dataSetViewType'] ?? dataSetDescriptor.params?.['dataSetViewType'] ?? 'table',
      );
      (this as { dataSetView: VueTableView | VueTreeView }).dataSetView =
        vueDataSetViewRegistry.resolve(dataSetViewType, dataSetDescriptor, entityMetadata) as VueTableView | VueTreeView;
    }
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  override async initialize(): Promise<void> {
    this.isLoading.value = true;
    try {
      await Promise.all([this.formView.initialize(), this.dataSetView.initialize()]);
      this.state.initialized = true;
    } catch (e) {
      this.errorMessage.value = String(e);
      throw e;
    } finally {
      this.isLoading.value = false;
    }
  }

  // ── Mode transitions ──────────────────────────────────────────────────────

  override startCreate(): void {
    super.startCreate();
    this.mode.value = 'create';
  }

  override startEdit(entity: unknown): void {
    super.startEdit(entity);
    this.mode.value = 'edit';
  }

  override cancelEdit(): void {
    super.cancelEdit();
    this.mode.value = 'list';
  }

  override async save(): Promise<void> {
    this.isLoading.value = true;
    try {
      await super.save();
      this.mode.value = 'list';
    } catch (e) {
      this.errorMessage.value = String(e);
      throw e;
    } finally {
      this.isLoading.value = false;
    }
  }

  override async delete(entity: unknown): Promise<void> {
    this.isLoading.value = true;
    try {
      await super.delete(entity);
    } catch (e) {
      this.errorMessage.value = String(e);
      throw e;
    } finally {
      this.isLoading.value = false;
    }
  }
}
