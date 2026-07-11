// FormView: handles form field logic, layout, values and validation

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { View } from './View.js';
import { ViewTypes } from './ViewType.js';
import type { ResolvedField } from '../types/field.js';
import type { ResolvedLayout } from '../types/layout.js';
import type { FormState } from '../types/state.js';
import { FieldResolver } from '../resolvers/FieldResolver.js';
import { LayoutEngine } from '../resolvers/LayoutEngine.js';

/**
 * View implementation for rendering and managing entity forms.
 * Handles field resolution, layout computation, value management and validation.
 *
 * Example:
 * <pre>{@code
 * const view = new FormView(descriptor, metadata);
 * await view.initialize();
 * view.setFieldValue('name', 'John');
 * view.validate();
 * }</pre>
 */
export class FormView extends View {
  protected override state: FormState;

  private _resolvedFields: ResolvedField[] = [];
  private _layout: ResolvedLayout | null = null;
  private _readOnly = false;
  private _value: Record<string, unknown> = {};

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(ViewTypes.Form, descriptor, entityMetadata);
    this.state = {
      loading: false,
      error: null,
      initialized: false,
      values: {},
      errors: {},
      dirty: false,
      submitted: false,
    };
  }

  async initialize(): Promise<void> {
    this.state.loading = true;
    try {
      this._resolvedFields = FieldResolver.resolveFields(this.descriptor, this.entityMetadata);
      this._layout = LayoutEngine.computeLayout(this.descriptor, this._resolvedFields);
      this.state.initialized = true;
    } finally {
      this.state.loading = false;
    }
  }

  validate(): boolean {
    const errors: Record<string, string> = {};
    for (const field of this._resolvedFields) {
      if (field.resolvedRequired && field.resolvedVisible) {
        const value = this.state.values[field.name];
        if (value === undefined || value === null || value === '') {
          errors[field.name] = `${field.resolvedLabel} is required`;
        }
      }
    }
    this.state.errors = errors;
    return Object.keys(errors).length === 0;
  }

  override getValue(): Record<string, unknown> { return { ...this.state.values }; }

  override setValue(value: unknown): void {
    if (value && typeof value === 'object') {
      this.state.values = { ...(value as Record<string, unknown>) };
      this._value = this.state.values;
    }
  }

  override isReadOnly(): boolean { return this._readOnly; }
  override setReadOnly(readOnly: boolean): void { this._readOnly = readOnly; }

  /** Get the value of a specific field */
  getFieldValue(field: string): unknown { return this.state.values[field]; }

  /** Set the value of a specific field and emit change event */
  setFieldValue(field: string, value: unknown): void {
    this.state.values[field] = value;
    this.state.dirty = true;
    this.emit('change', { field, value });
  }

  /** Get all resolved fields */
  getResolvedFields(): ResolvedField[] { return this._resolvedFields; }

  /** Get the computed layout */
  getLayout(): ResolvedLayout | null { return this._layout; }

  /** Get validation errors map */
  getErrors(): Record<string, string> { return { ...this.state.errors }; }

  /** Get field error message */
  getFieldError(field: string): string | undefined { return this.state.errors[field]; }

  /** Submit the form */
  async submit(): Promise<void> {
    if (this.validate()) {
      this.state.submitted = true;
      this.emit('submit', this.state.values);
    } else {
      this.emit('error', this.state.errors);
    }
  }

  /** Reset the form to its initial state */
  reset(): void {
    this.state.values = {};
    this.state.errors = {};
    this.state.dirty = false;
    this.state.submitted = false;
    this.emit('reset');
  }
}
