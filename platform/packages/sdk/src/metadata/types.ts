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
  modules?: NavigationModule[];
}

// ── Navigation ─────────────────────────────────────────────────────────────

export interface NavigationTree {
  modules: NavigationModule[];
}

export interface NavigationModule {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  groups: NavigationGroup[];
}

export interface NavigationGroup {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  pages: NavigationPage[];
}

export interface NavigationPage {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  virtualPath: string;
  prettyVirtualPath: string;
  pageClass?: string;
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
