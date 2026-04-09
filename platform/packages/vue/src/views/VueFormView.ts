// VueFormView: Vue-reactive extension of FormView

import { ref, computed } from 'vue';
import type { Ref, ComputedRef } from 'vue';
import { FormView, FieldResolver, LayoutEngine } from '@dynamia-tools/ui-core';
import type { ResolvedField, ResolvedLayout } from '@dynamia-tools/ui-core';
import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';

/**
 * Vue-reactive extension of FormView.
 * Exposes reactive refs for values, errors, loading and layout.
 *
 * Example:
 * <pre>{@code
 * const form = new VueFormView(descriptor, metadata);
 * await form.initialize();
 * form.values.value['name'] = 'John';
 * }</pre>
 */
export class VueFormView extends FormView {
  /** Reactive form field values */
  readonly values: Ref<Record<string, unknown>> = ref({});
  /** Reactive field validation errors */
  readonly errors: Ref<Record<string, string>> = ref({});
  /** Reactive loading state */
  readonly isLoading: Ref<boolean> = ref(false);
  /** Reactive initialized flag */
  readonly isInitialized: Ref<boolean> = ref(false);
  /** Reactive dirty flag */
  readonly isDirty: Ref<boolean> = ref(false);

  private readonly _resolvedFieldsRef: Ref<ResolvedField[]> = ref([]);
  private readonly _layoutRef: Ref<ResolvedLayout | null> = ref(null);

  /** Reactive resolved fields list */
  readonly resolvedFields: ComputedRef<ResolvedField[]> = computed(() => this._resolvedFieldsRef.value);

  /** Reactive computed layout */
  readonly layout: ComputedRef<ResolvedLayout | null> = computed(() => this._layoutRef.value);

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(descriptor, entityMetadata);
  }

  override async initialize(): Promise<void> {
    this.isLoading.value = true;
    try {
      this._resolvedFieldsRef.value = FieldResolver.resolveFields(this.descriptor, this.entityMetadata);
      this._layoutRef.value = LayoutEngine.computeLayout(this.descriptor, this._resolvedFieldsRef.value);
      this.isInitialized.value = true;
      this.state.initialized = true;
    } finally {
      this.isLoading.value = false;
    }
  }

  override setValue(value: unknown): void {
    super.setValue(value);
    if (value && typeof value === 'object') {
      this.values.value = { ...(value as Record<string, unknown>) };
    }
  }

  override setFieldValue(field: string, value: unknown): void {
    super.setFieldValue(field, value);
    this.values.value[field] = value;
    this.isDirty.value = true;
  }

  override validate(): boolean {
    const result = super.validate();
    this.errors.value = { ...super.getErrors() };
    return result;
  }

  override reset(): void {
    super.reset();
    this.values.value = {};
    this.errors.value = {};
    this.isDirty.value = false;
  }
}
