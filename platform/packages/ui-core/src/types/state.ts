// State type definitions for all view types

import type { CrudPageable } from '@dynamia-tools/sdk';

/** Generic base state shared by all views */
export interface ViewState {
  loading: boolean;
  error: string | null;
  initialized: boolean;
}

/** Base state for DataSetView implementations (TableView, TreeView, etc.) */
export interface DataSetViewState extends ViewState {
  selectedItem: unknown | null;
}

/** State specific to FormView */
export interface FormState extends ViewState {
  values: Record<string, unknown>;
  errors: Record<string, string>;
  dirty: boolean;
  submitted: boolean;
}

/** Sort direction for TableView */
export type SortDirection = 'asc' | 'desc' | null;

/** State specific to TableView */
export interface TableState extends DataSetViewState {
  rows: unknown[];
  pagination: CrudPageable | null;
  sortField: string | null;
  sortDir: SortDirection;
  searchQuery: string;
}

/** CRUD interaction mode */
export type CrudMode = 'list' | 'create' | 'edit';

/** State specific to CrudView */
export interface CrudState extends ViewState {
  mode: CrudMode;
}

/**
 * A single node in a flat tree view.
 * Parent-child links are expressed via `parentId`; `children` is an optional
 * convenience for APIs that already return a nested structure.
 */
export interface TreeNode {
  id: string;
  label: string;
  /** ID of the parent node — undefined/null for root nodes */
  parentId?: string;
  icon?: string;
  /** Pre-nested children returned by the API (optional) */
  children?: TreeNode[];
  /** Original domain entity attached to this node */
  data?: unknown;
}

/** State specific to TreeView */
export interface TreeState extends DataSetViewState {
  nodes: TreeNode[];
  expandedNodeIds: Set<string>;
  /** Typed narrowing of DataSetViewState.selectedItem */
  selectedItem: TreeNode | null;
}

/** State specific to EntityPickerView */
export interface EntityPickerState extends ViewState {
  searchQuery: string;
  searchResults: unknown[];
  selectedEntity: unknown | null;
}

/** State specific to ConfigView */
export interface ConfigState extends ViewState {
  parameters: ConfigParameter[];
  values: Record<string, unknown>;
}

/** A single configuration parameter */
export interface ConfigParameter {
  name: string;
  label: string;
  description?: string;
  type: string;
  defaultValue?: unknown;
  required?: boolean;
}
