// ViewRenderer and sub-renderer interface definitions

import type { ViewType } from '../view/ViewType.js';
import type { View } from '../view/View.js';
import type { FormView } from '../view/FormView.js';
import type { TableView } from '../view/TableView.js';
import type { CrudView } from '../view/CrudView.js';
import type { TreeView } from '../view/TreeView.js';
import type { ResolvedField } from '../types/field.js';

/**
 * Generic interface for rendering a View into an output type TOutput.
 * Framework adapters (Vue, React, etc.) implement this for each ViewType.
 *
 * @typeParam TView - The concrete View subclass this renderer handles
 * @typeParam TOutput - The rendered output type (e.g. Vue Component, React element)
 */
export interface ViewRenderer<TView extends View, TOutput> {
  /** The view type this renderer handles */
  readonly supportedViewType: ViewType;
  /**
   * Render the view into the framework-specific output.
   * @param view - The view to render
   * @returns Framework-specific rendered output
   */
  render(view: TView): TOutput;
}

/**
 * Renderer interface for FormView.
 * @typeParam TOutput - Framework-specific output type
 */
export interface FormRenderer<TOutput> extends ViewRenderer<FormView, TOutput> {}

/**
 * Renderer interface for TableView.
 * @typeParam TOutput - Framework-specific output type
 */
export interface TableRenderer<TOutput> extends ViewRenderer<TableView, TOutput> {}

/**
 * Renderer interface for CrudView.
 * @typeParam TOutput - Framework-specific output type
 */
export interface CrudRenderer<TOutput> extends ViewRenderer<CrudView, TOutput> {}

/**
 * Renderer interface for TreeView.
 * @typeParam TOutput - Framework-specific output type
 */
export interface TreeRenderer<TOutput> extends ViewRenderer<TreeView, TOutput> {}

/**
 * Renderer interface for individual fields.
 * @typeParam TOutput - Framework-specific output type
 */
export interface FieldRenderer<TOutput> {
  /** The field component identifier this renderer handles */
  readonly supportedComponent: string;
  /**
   * Render a field in the context of a FormView.
   * @param field - The resolved field to render
   * @param view - The parent FormView
   * @returns Framework-specific rendered output
   */
  render(field: ResolvedField, view: FormView): TOutput;
}
