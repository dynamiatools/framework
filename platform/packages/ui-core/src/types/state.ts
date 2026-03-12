// State type definitions for all view types

import type { CrudPageable } from '@dynamia-tools/sdk';

/** Generic base state shared by all views */
export interface ViewState {
  loading: boolean;
  error: string | null;
  initialized: boolean;
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
export interface TableState extends ViewState {
  rows: unknown[];
  pagination: CrudPageable | null;
  sortField: string | null;
  sortDir: SortDirection;
  searchQuery: string;
  selectedRow: unknown | null;
}

/** CRUD interaction mode */
export type CrudMode = 'list' | 'create' | 'edit';

/** State specific to CrudView */
export interface CrudState extends ViewState {
  mode: CrudMode;
}

/** State specific to TreeView */
export interface TreeState extends ViewState {
  nodes: TreeNode[];
  expandedNodeIds: Set<string>;
  selectedNodeId: string | null;
}

/** A single node in a tree view */
export interface TreeNode {
  id: string;
  label: string;
  icon?: string;
  children?: TreeNode[];
  data?: unknown;
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
