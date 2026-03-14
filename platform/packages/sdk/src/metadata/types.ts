// ─── Metadata types mirroring the Dynamia Platform Java model ───────────────

// ── Basic / shared ─────────────────────────────────────────────────────────

export interface BasicMetadata {
  id: string;
  name: string;
  endpoint?: string;
  description?: string;
  icon?: string;
}

// ── Application metadata ───────────────────────────────────────────────────

export interface ApplicationMetadata {
  name: string;
  version: string;
  description?: string;
  logo?: string;
  url?: string;
}

// ── Navigation ─────────────────────────────────────────────────────────────

export interface NavigationTree {
  navigation: NavigationNode[];
}

/**
 * A node in the navigation tree. The `type` field indicates the kind of element:
 * - `"Module"` — top-level module (children are groups or pages)
 * - `"PageGroup"` — group within a module (children are pages)
 * - `"Page"` — leaf page (has `internalPath` / `path`, no children)
 */
export interface NavigationNode {
  id: string;
  name: string;
  longName?: string;
  /** Simple class name of the navigation element: "Module", "PageGroup", "Page", etc. */
  type?: string;
  description?: string;
  icon?: string;
  /** Virtual path (e.g. /pages/store/books) — use for routing */
  internalPath?: string;
  /** Pretty/display path */
  path?: string;
  position?: number;
  featured?: boolean;
  children?: NavigationNode[];
  attributes?: Record<string, unknown>;
  /** Source file path for page nodes */
  file?: string;
}

// ── Entity metadata ────────────────────────────────────────────────────────

export interface ApplicationMetadataEntities {
  entities: EntityMetadata[];
}

export interface EntityMetadata extends BasicMetadata {
  className: string;
  actions: ActionMetadata[];
  descriptors: ViewDescriptorMetadata[];
  actionsEndpoint: string;
}

// ── Actions ────────────────────────────────────────────────────────────────

export interface ApplicationMetadataActions {
  actions: ActionMetadata[];
}

export interface ActionMetadata extends BasicMetadata {
  actionClass?: string;
  params?: Record<string, unknown>;
}

export interface ActionExecutionRequest {
  data?: Record<string, unknown>;
  params?: Record<string, unknown>;
}

export interface ActionExecutionResponse {
  message: string;
  status: string;
  code: number;
  data?: unknown;
}

// ── View descriptors ───────────────────────────────────────────────────────

export interface ViewDescriptorMetadata {
  view: string;
  descriptor: ViewDescriptor;
}

export interface ViewDescriptor {
  id: string;
  beanClass: string;
  viewTypeName: string;
  fields: ViewField[];
  params: Record<string, unknown>;
}

export interface ViewField {
  name: string;
  fieldClass?: string;
  label?: string;
  visible?: boolean;
  required?: boolean;
  params: Record<string, unknown>;
}
