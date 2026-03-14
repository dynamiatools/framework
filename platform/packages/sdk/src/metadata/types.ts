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

/** Mirrors tools.dynamia.actions.ActionReference */
export interface ActionReference {
  id: string;
  label?: string;
  description?: string;
  icon?: string;
  width?: string;
  visible?: boolean;
  type?: string;
  attributes?: Record<string, unknown>;
}

/** Mirrors tools.dynamia.viewers.ViewLayout */
export interface ViewLayout {
  params: Record<string, unknown>;
}

/** Mirrors tools.dynamia.viewers.FieldGroup (fields list is @JsonIgnore on the Java side) */
export interface ViewFieldGroup {
  name: string;
  label?: string;
  description?: string;
  icon?: string;
  index?: number;
  collapse?: boolean;
  params: Record<string, unknown>;
}

/** Mirrors tools.dynamia.viewers.ViewDescriptor */
export interface ViewDescriptor {
  id: string;
  /** Fully qualified class name of the target domain class */
  beanClass: string;
  viewTypeName: string;
  fields: ViewField[];
  fieldGroups?: ViewFieldGroup[];
  layout?: ViewLayout;
  params: Record<string, unknown>;
  messages?: string;
  device?: string;
  autofields?: boolean;
  actions?: ActionReference[];
  /** ID of the parent descriptor this one extends */
  extends?: string;
  viewCustomizerClass?: string;
  customViewRenderer?: string;
}

/** Mirrors tools.dynamia.viewers.Field */
export interface ViewField {
  name: string;
  /** Fully qualified class name of the field type */
  fieldClass?: string;
  label?: string;
  description?: string;
  /** Component name used to render the field */
  component?: string;
  visible?: boolean;
  required?: boolean;
  optional?: boolean;
  index?: number;
  icon?: string;
  showIconOnly?: boolean;
  path?: string;
  variable?: string;
  temporal?: boolean;
  action?: ActionReference;
  params: Record<string, unknown>;
}
