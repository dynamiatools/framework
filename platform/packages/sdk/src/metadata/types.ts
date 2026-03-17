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

/**
 * Mirrors `tools.dynamia.app.ApplicationMetadata` (extends BasicMetadata).
 *
 * Java serialises all NON_NULL fields; optional fields below may be absent
 * from the JSON payload when the application has not configured them.
 */
export interface ApplicationMetadata extends BasicMetadata {
    /** Application display version */
    version?: string;
    /** Human-readable title (may differ from `name`) */
    title?: string;
    /** UI template / theme key */
    template?: string;
    /** Author or organisation name */
    author?: string;
    /** Public URL of the running application */
    url?: string;
    /** Path to the application logo asset */
    logo?: string;
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
    /** Ordering hint — Java `Double`, nullable */
    position?: number;
    /** Whether this node should appear in featured/shortcut areas — Java `Boolean`, nullable */
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

/**
 * Mirrors `tools.dynamia.app.EntityMetadata`.
 *
 * `descriptors` lists lightweight view references for this entity — note that
 * the actual `ViewDescriptor` content must be fetched separately via
 * `MetadataApi.getEntityViews()` or `MetadataApi.getEntityView()`.
 */
export interface EntityMetadata extends BasicMetadata {
    className: string;
    actions: ActionMetadata[];
    descriptors: ViewDescriptorMetadata[];
    actionsEndpoint: string;
    /**
     * REST endpoint that returns the full list of ViewDescriptor objects for
     * this entity (e.g. `/api/metadata/entities/MyClass/views`).
     */
    viewsEndpoint?: string;
}

// ── Actions ────────────────────────────────────────────────────────────────

export interface ApplicationMetadataActions {
    actions: ActionMetadata[];
}

export type ActionType = 'Action' | 'ClassAction' | 'CrudAction' | string;

/**
 * Mirrors `tools.dynamia.actions.ActionMetadata`.
 *
 * All nullable fields are annotated `@JsonInclude(NON_NULL)` in Java and may
 * therefore be absent from the JSON response.
 */
export interface ActionMetadata extends BasicMetadata {
    /** Logical server-side action type: Action, ClassAction or CrudAction */
    type?: ActionType;
    /** Simple Java class name of the action implementation */
    className?: string;
    /** Fully-qualified class name of the Java action implementation */
    actionClass?: string;
    /** Optional grouping label for the action */
    group?: string;
    /** Custom renderer key for the action button/widget */
    renderer?: string;
    /** Simple class names of entity types this action applies to */
    applicableClasses?: string[];
    /** Entity state names in which this action is available */
    applicableStates?: string[];
}

/**
 * Request body sent to `POST /api/actions/{actionClass}`.
 *
 * Mirrors `tools.dynamia.actions.ActionExecutionRequest`.
 * `data` is typed as `unknown` because the Java field is `Object` — it can
 * carry any JSON value (scalar, array, or object).
 */
export interface ActionExecutionRequest {
    /** The entity / payload on which the action should operate */
    data?: unknown;
    /** Arbitrary extra parameters forwarded to the action */
    params?: Record<string, unknown>;
    /** Identifies the UI component or view that triggered the action */
    source?: string;
    /** Simple class name of the entity being acted upon */
    dataType?: string;
    /** Primary key of the entity being acted upon */
    dataId?: unknown;
    /** Display name of the entity being acted upon */
    dataName?: string;
}

/**
 * Response body returned by action execution endpoints.
 *
 * Mirrors `tools.dynamia.actions.ActionExecutionResponse`.
 * Note: the field is `statusCode` (int) in Java — NOT `code`.
 */
export interface ActionExecutionResponse {
    /** Human-readable message produced by the action */
    message?: string;
    /** Logical status label, e.g. `"SUCCESS"` or `"ERROR"` */
    status?: string;
    /**
     * Numeric status code produced by the action.
     * Serialised as `"statusCode"` in JSON (Java field `private int statusCode`).
     */
    statusCode?: number;
    /** Payload returned by the action (any JSON value) */
    data?: unknown;
    /** Arbitrary response parameters */
    params?: Record<string, unknown>;
    /** Echo of the source field from the request */
    source?: string;
    /** Simple class name of the entity that was acted upon */
    dataType?: string;
    /** Primary key of the entity that was acted upon */
    dataId?: unknown;
    /** Display name of the entity that was acted upon */
    dataName?: string;
}

// ── View descriptors ───────────────────────────────────────────────────────

/**
 * Lightweight reference to a view descriptor associated with an entity.
 *
 * Mirrors the serialised form of `tools.dynamia.app.ViewDescriptorMetadata`.
 *
 * ⚠️  The `descriptor` field that exists in the Java class is annotated with
 * `@JsonIgnore` and is therefore **never** included in the JSON response.
 * To obtain the full `ViewDescriptor`, call `MetadataApi.getEntityViews()` or
 * `MetadataApi.getEntityView()`, which hit the dedicated `/views` endpoint and
 * return `ViewDescriptor[]` / `ViewDescriptor` directly.
 */
export interface ViewDescriptorMetadata {
    /** Unique identifier of this metadata entry */
    id?: string;
    /** View type name (e.g. `"form"`, `"table"`, `"tree"`) */
    view?: string;
    /** Target device class (e.g. `"desktop"`, `"mobile"`) */
    device?: string;
    /** Fully-qualified Java class name of the view bean */
    beanClass?: string;
    /** REST endpoint that returns the full ViewDescriptor for this view */
    endpoint?: string;
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

/**
 * Mirrors tools.dynamia.viewers.FieldGroup.
 *
 * The `fields` array carries field *names* serialized by the Java
 * `@JsonProperty("fields") getFieldsNames()` method.
 *
 * `params` may be absent from the JSON response when the group has no
 * parameters (`@JsonInclude(NON_DEFAULT)` — empty Map is omitted).
 */
export interface ViewFieldGroup {
    name: string;
    label?: string;
    description?: string;
    icon?: string;
    index?: number;
    collapse?: boolean;
    /** Arbitrary layout parameters — absent when empty (`@JsonInclude(NON_DEFAULT)`) */
    params?: Record<string, unknown>;
    /** Ordered list of field names belonging to this group */
    fields?: string[];
}

/** Mirrors tools.dynamia.viewers.ViewDescriptor */
export interface ViewDescriptor {
    id: string;
    /** Fully qualified class name of the target domain class */
    beanClass: string;
    /** View type name — matches the JSON "view" key produced by Java's @JsonProperty("view") */
    view: string;
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

/**
 * Mirrors tools.dynamia.viewers.Field.
 *
 * `params` may be absent from the JSON response when the field has no
 * parameters (`@JsonInclude(NON_DEFAULT)` — empty Map is omitted).
 */
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
    entity?: boolean;
    enum?: boolean;
    localizedLabel?: string;
    localizedDescription?: string;
    /** Arbitrary field parameters — absent when empty (`@JsonInclude(NON_DEFAULT)`) */
    params?: Record<string, unknown>;
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

/**
 * Resolve enum constants for enum fields
 * @param field
 */
export function resolveFieldEnumConstants(field: ViewField): string[] {
    if (!field.enum) return [];

    const raw = field.params?.['ENUM_CONSTANTS'];
    if (raw == null) return [];

    if (Array.isArray(raw)) {
        return raw.map(item => String(item));
    }

    if (typeof raw === 'string') {
        // Support comma-separated lists as well as single values
        return raw.includes(',')
            ? raw.split(',').map(s => s.trim()).filter(Boolean)
            : [raw];
    }

    return [];
}
