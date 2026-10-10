// ── Main client ─────────────────────────────────────────────────────────────
export {DynamiaClient} from './client.js';

// ── HTTP client (exported so extension SDK packages can type-check against it) ──
export {HttpClient} from './http.js';

// ── Error ────────────────────────────────────────────────────────────────────
export {DynamiaApiError} from './errors.js';

// ── Core types ────────────────────────────────────────────────────────────────
export type {DynamiaClientConfig} from './types.js';

// ── Metadata module ──────────────────────────────────────────────────────────
export {
    MetadataApi, ActionsApi, resolveViewFieldType, resolveFieldType, resolveFieldEnumConstants
} from './metadata/index.js';
export type {ExecuteActionOptions} from './metadata/index.js';
export type {
    // Application
    ApplicationMetadata,
    // Navigation
    NavigationTree,
    NavigationNode,
    // Metadata
    BasicMetadata,
    ApplicationMetadataEntities,
    EntityMetadata,
    EntityReference,
    ApplicationMetadataActions,
    ActionType,
    ActionMetadata,
    // Actions
    ActionExecutionRequest,
    ActionExecutionResponse,
    ActionFlowStepType,
    ActionRuntime,
    FlowFileRef,
    FlowDownload,
    ActionFlowStep,
    // Views
    ViewDescriptorMetadata,
    ActionReference,
    ViewLayout,
    ViewFieldGroup,
    ViewDescriptor,
    ViewField,
} from './metadata/index.js';

// ── CRUD module ───────────────────────────────────────────────────────────────
export {CrudResourceApi, CrudServiceApi} from './cruds/index.js';
export type {
    CrudListResult,
    CrudQueryParams,
    CrudRawResponse,
    CrudPageable,
} from './cruds/index.js';

// ── Schedule API (platform-core feature) ─────────────────────────────────────
export {ScheduleApi} from './schedule/index.js';
export {TransfersApi} from './transfers/index.js';
export {JobsApi} from './jobs/index.js';
export type {JobStatus, JobState} from './jobs/index.js';
export {ACTION_FLOW_STEP_TYPES, ACTION_FLOW_STEP_FIELDS, ACTION_RUNTIMES, UI_PORTS} from './generated/ui-contract.js';
export type {UIPortName} from './generated/ui-contract.js';
export type {UploadOptions} from './transfers/index.js';
