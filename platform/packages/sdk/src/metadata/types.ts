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

/** Mirrors tools.dynamia.viewers.FieldGroup.
 *  The `fields` array carries field *names* serialized by the Java
 *  `@JsonProperty("fields") getFieldsNames()` method. */
export interface ViewFieldGroup {
  name: string;
  label?: string;
  description?: string;
  icon?: string;
  index?: number;
  collapse?: boolean;
  params: Record<string, unknown>;
  /** Ordered list of field names belonging to this group */
  fields?: string[];
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

// ── ViewField utilities ────────────────────────────────────────────────────

/**
 * Well-known Java fully-qualified class names → simple type token.
 * Extend this map to add more mappings.
 */
const FIELD_CLASS_TYPE_MAP: Record<string, string> = {
  // Text
  "java.lang.String": "text",
  "java.lang.Character": "text",
  // Integer numbers
  "java.lang.Integer": "number",
  "int": "number",
  "java.lang.Long": "number",
  "long": "number",
  "java.lang.Short": "number",
  "short": "number",
  "java.lang.Byte": "number",
  "byte": "number",
  "java.math.BigInteger": "number",
  // Decimal numbers
  "java.lang.Double": "decimal",
  "double": "decimal",
  "java.lang.Float": "decimal",
  "float": "decimal",
  "java.math.BigDecimal": "decimal",
  // Boolean
  "java.lang.Boolean": "boolean",
  "boolean": "boolean",
  // Date / time
  "java.time.LocalDate": "date",
  "java.sql.Date": "date",
  "java.time.LocalTime": "time",
  "java.time.LocalDateTime": "datetime",
  "java.time.ZonedDateTime": "datetime",
  "java.time.OffsetDateTime": "datetime",
  "java.time.Instant": "datetime",
  "java.util.Date": "datetime",
  "java.sql.Timestamp": "datetime",
};

/**
 * Converts a PascalCase or camelCase identifier to kebab-case.
 * e.g. `StockStatus` → `stock-status`, `myField` → `my-field`
 */
function toKebabCase(name: string): string {
  return name
    .replace(/([A-Z])/g, (letter, _match, offset) =>
      offset > 0 ? `-${letter.toLowerCase()}` : letter.toLowerCase()
    )
    .toLowerCase();
}

/**
 * Maps a `ViewField.fieldClass` (fully-qualified Java class name) to a
 * simple type token suitable for UI rendering.
 *
 * - Known primitive / standard-library types are mapped to canonical tokens
 *   (`"text"`, `"number"`, `"decimal"`, `"boolean"`, `"date"`, `"time"`, `"datetime"`).
 * - Any other class is reduced to its simple (unqualified) name and converted
 *   to kebab-case.
 *
 * @example
 * resolveFieldType("java.lang.String")             // → "text"
 * resolveFieldType("java.time.LocalDate")           // → "date"
 * resolveFieldType("mylibrary.enums.StockStatus")   // → "stock-status"
 * resolveFieldType(undefined)                       // → "text"
 */
export function resolveFieldType(fieldClass: string | undefined): string {
  if (!fieldClass) return "text";

  const known = FIELD_CLASS_TYPE_MAP[fieldClass];
  if (known) return known;

  // Fall back to the simple (unqualified) class name in kebab-case
  const simpleName = fieldClass.includes(".")
    ? fieldClass.substring(fieldClass.lastIndexOf(".") + 1)
    : fieldClass;

  return toKebabCase(simpleName);
}

/**
 * Convenience overload: resolves the type token directly from a `ViewField`.
 *
 * @example
 * resolveViewFieldType(field)  // delegates to resolveFieldType(field.fieldClass)
 */
export function resolveViewFieldType(field: ViewField): string {
  return resolveFieldType(field.fieldClass);
}

