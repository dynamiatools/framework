// useForm: composable for creating and managing a VueFormView

import { onMounted } from 'vue';
import type { ViewDescriptor } from '@dynamia-tools/sdk';
import type { EntityMetadata } from '@dynamia-tools/sdk';
import { VueFormView } from '../views/VueFormView.js';

/** Options for useForm composable */
export interface UseFormOptions {
  /** Pre-loaded view descriptor */
  descriptor: ViewDescriptor;
  /** Entity metadata */
  entityMetadata?: EntityMetadata | null;
  /** Initial form data */
  initialData?: Record<string, unknown>;
}

/**
 * Composable for creating and managing a VueFormView.
 * Provides direct access to reactive form state.
 *
 * Example:
 * <pre>{@code
 * const { view, values, errors, validate, submit } = useForm({ descriptor, initialData: book });
 * }</pre>
 *
 * @param options - UseFormOptions with descriptor and optional initial data
 * @returns Object with VueFormView and reactive state
 */
export function useForm(options: UseFormOptions) {
  const view = new VueFormView(options.descriptor, options.entityMetadata ?? null);

  onMounted(async () => {
    await view.initialize();
    if (options.initialData) view.setValue(options.initialData);
  });

  return {
    /** The VueFormView instance */
    view,
    /** Reactive field values */
    values: view.values,
    /** Reactive validation errors */
    errors: view.errors,
    /** Reactive loading state */
    loading: view.isLoading,
    /** Reactive dirty flag */
    isDirty: view.isDirty,
    /** Reactive resolved fields */
    fields: view.resolvedFields,
    /** Reactive computed layout */
    layout: view.layout,
    /** Validate the form */
    validate: () => view.validate(),
    /** Submit the form */
    submit: () => view.submit(),
    /** Reset the form */
    reset: () => view.reset(),
    /** Set a field value */
    setFieldValue: (field: string, value: unknown) => view.setFieldValue(field, value),
    /** Get a field value */
    getFieldValue: (field: string) => view.getFieldValue(field),
  };
}
