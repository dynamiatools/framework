// VueCrudView: Vue-reactive extension of CrudView

import { ref, computed } from 'vue';
import type { Ref, ComputedRef } from 'vue';
import { CrudView } from '@dynamia-tools/ui-core';
import type { CrudMode } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { VueFormView } from './VueFormView.js';
import { VueTableView } from './VueTableView.js';

/**
 * Vue-reactive extension of CrudView.
 * Owns Vue-reactive VueFormView and VueTableView instances.
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

  // Override with Vue-reactive subtypes. The initializers run after super(), so
  // this.descriptor and this.entityMetadata are already set by View's constructor.
  override readonly formView: VueFormView = new VueFormView(this.descriptor, this.entityMetadata);
  override readonly tableView: VueTableView = new VueTableView(this.descriptor, this.entityMetadata);

  /** Whether the form is currently shown */
  readonly showForm: ComputedRef<boolean> = computed(() =>
    this.mode.value === 'create' || this.mode.value === 'edit'
  );

  /** Whether the table is currently shown */
  readonly showTable: ComputedRef<boolean> = computed(() =>
    this.mode.value === 'list'
  );

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(descriptor, entityMetadata);
    // formView and tableView are replaced by the field initializers above
  }

  override async initialize(): Promise<void> {
    this.isLoading.value = true;
    try {
      await Promise.all([this.formView.initialize(), this.tableView.initialize()]);
      this.state.initialized = true;
    } catch (e) {
      this.errorMessage.value = String(e);
      throw e;
    } finally {
      this.isLoading.value = false;
    }
  }

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
      // super.save() already set this.state.mode = 'list'; mirror to reactive ref
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
