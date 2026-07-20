# Account Migration Module — Architecture

## 1. Overview

The Account Migration Module enables full lifecycle management of tenant (Account) data: export, import, clone, backup, and restore. It is designed for:

- **Millions of rows** — streaming, never loads all data into memory.
- **Database independence** — uses JPA/Hibernate metamodel exclusively.
- **Extensibility** — SPI interfaces for identity mapping, progress tracking, and cancellation.
- **Non-blocking** — every operation runs as an async background job (Java virtual threads).

---

## 2. Component Map

```
┌──────────────────────────────────────────────────────────────────────┐
│                    AccountMigrationController  (REST)                   │
│  POST /export  POST /import  POST /clone  GET /jobs/{jobId}  GET /download │
└─────────────────────────────┬────────────────────────────────────────┘
                              │ delegates to
┌─────────────────────────────▼────────────────────────────────────────┐
│                   AccountMigrationJobService                            │
│   createJob() · cancelJob() · getJob() · listJobs() · downloadResult()  │
│   Persists AccountMigrationJob entity in DB                            │
└──────┬───────────────────────────────────────────┬───────────────────┘
       │  launches via                              │ saves progress via
       │  SchedulerUtil.runWithResult(worker)       │  CrudService.update()
       ▼                                            │
┌─────────────────────────┐                        │
│  Workers (VirtualThread) │                       │
│  ┌──────────────────────┴─┐                      │
│  │ ExportWorker            │                     │
│  │ ImportWorker            │◄────────────────────┘
│  │ CloneWorker             │
│  └────────────┬───────────┘
│               │ calls
│  ┌────────────▼───────────┐
│  │ AccountMigrationService   │
│  │ (impl: coordinates      │
│  │  pipelines)             │
│  └────────────┬───────────┘
└───────────────┼───────────────────────────────────────────────────────
                │
    ┌───────────┴────────────┐
    │                        │
    ▼                        ▼
┌──────────────┐      ┌────────────────┐
│ ExportPipeline│      │ ImportPipeline │
│  (parallel   │      │  (streaming    │
│   ZIP write) │      │   ZIP v3)      │
└──────┬───────┘      └───────┬────────┘
       │                      │
       │ uses                 │ uses
    ┌──┴──────────────────────┴──┐
    │  AccountEntityDiscovery    │   discovers all @Entity + AccountAware
    │  EntityDependencyGraph     │   topological sort via JPA metamodel
    │  IdentityMapper SPI        │   KEEP_IDS / REGENERATE_IDS
    └────────────────────────────┘

EXPORT / BACKUP only, after the pipeline finishes successfully:

┌─────────────────────────┐        ┌─────────────────────────────────┐
│  local working ZIP file  │──────▶│  EntityFileService.createEntityFile│
│  (properties.output-dir) │  upload │  → EntityFileStorage (local/S3/  │
│  deleted right after     │        │     Buckie/…) — the durable copy │
└─────────────────────────┘        └─────────────────────────────────┘
```

---

## 3. Entity Discovery

### Algorithm

```
1. Get all managed entity types: EntityManagerFactory.getMetamodel().getEntities()
2. For each EntityType<T>:
   a. If AccountAware.class.isAssignableFrom(T.getJavaType()) → candidate
   b. If T.getJavaType().isAnnotationPresent(@AccountExportIgnore) → exclude
3. Always include Account.class (the tenant root)
4. Return final exportable set
```

### Dependency Graph (Topological Sort)

Used to determine import order: parents before children.

```
For each candidate entity E:
  For each SingularAttribute A in E's metamodel:
    If A.persistentAttributeType == MANY_TO_ONE or ONE_TO_ONE:
      If A.javaType is also in the candidate set:
        Add edge: A.javaType → E   (A.javaType must be imported before E)

Run Kahn's BFS algorithm to get topological order.
```

Example result:
```
Account → Customer → Order → OrderItem
              ↓
           Product
```

---

## 4. Export Pipeline (format v3)

### Output: ZIP Archive

Every export produces a single ZIP file containing one JSON entry per entity type, plus a manifest:

```
Account101_20260616T100500.zip
├── manifest.json              ← always first entry
├── Account101_Account.json    ← empty rows; Account data lives in manifest
├── Account101_Customer.json
├── Account101_Product.json
└── Account101_Order.json
```

ZIP entries use `DEFLATE` at `BEST_SPEED` level. Typical JSON compresses 5–10×.

> **Durability:** the ZIP built by this pipeline is written to a **local, transient working
> file** (`AccountMigrationProperties.outputDirectory`) — it is not the durable artifact.
> Only after the ZIP finishes writing does `AccountMigrationJobServiceImpl.finalizeJob()`
> upload it to `EntityFileService` and delete the local copy. See §7 and §9.1.

### Parallel Temp-Directory Strategy

Entity files are written in parallel to a temporary directory, then assembled into a ZIP in topological order:

```
1. Files.createTempDirectory("saas-export-{accountId}-")
2. Write manifest.json to temp dir (synchronous — entity list is known upfront)
3. For each entityClass (up to exportParallelism=4 concurrent virtual threads):
       EntityManager localEm = emf.createEntityManager()   ← one per thread, thread-safe
       write Account{id}_{SimpleName}.json to temp dir
       localEm.close()
4. ZipOutputStream → output stream (the local working ZIP file):
       addEntry(manifest.json)                              ← always first
       For each entityClass in topological order:
           addEntry(Account{id}_{SimpleName}.json)          ← Files.copy(), no heap allocation
       zipOut.finish()
5. deleteDirectory(tempDir)                                 ← always, in finally (entity JSON scratch files)
```

The column definition cache (`Map<Class<?>, List<ColumnDef>>`) is built once per entity type
and reused across all parallel tasks via a `ConcurrentHashMap`.

### Keyset Pagination

Entity rows are fetched in pages using keyset pagination (`id > lastId`) to avoid
`OFFSET`-based degradation on large tables:

```
lastId = 0
loop:
  SELECT ... WHERE accountId = ? AND id > lastId ORDER BY id LIMIT chunkSize
  → stream rows into current ZIP entry
  lastId = last row's id
until page.size() < chunkSize
```

### Serialization of a Single Entity (Columnar Format)

Columns are built once per entity type from `EntityType.getSingularAttributes()`:
```
id → always first column
For each SingularAttribute (excluding id):
  BASIC / EMBEDDED  → column name = fieldName, value written directly
  MANY_TO_ONE / ONE_TO_ONE → column name = fieldName + "_ref_id", value = referenced PK
  ONE_TO_MANY / MANY_TO_MANY / ELEMENT_COLLECTION → SKIP
```

Fields annotated with `@ExportIgnore` are skipped. Missing or inaccessible fields write `null`.
`Account` entity rows are intentionally empty — account data is in `manifest.json`.

---

## 5. Import Pipeline

Input must be a ZIP archive (format v3). Legacy JSON and GZIP formats are no longer supported.

### ZIP Import Flow (v3)

```
ZipInputStream (sequential — entries arrive in topological order)
    │
    ├── Entry: manifest.json
    │       Read version + sourceAccountId for logging
    │       (entity list not required — each entity file is self-describing)
    │
    └── For each *.json entry:
            JsonParser (via NoCloseInputStream wrapper)
            → read "entityClass" → Class.forName()
            → read "fields"      → ordered column names
            → read "rows"        → chunked into List<JsonNode>
                  persistChunk() [REQUIRES_NEW transaction per chunk]
                  → deserialize entity
                  → resolve _ref_id references via idMappings
                  → set accountId = targetAccountId
                  → persist + flush (REGENERATE_IDS) or set ID (KEEP_IDS)
                  → record originalId → newId in idMappings
            zipIn.closeEntry() — advances stream to next entry
```

`NoCloseInputStream` prevents `parser.close()` from closing the `ZipInputStream` between entries.
`ZipInputStream.read()` reports EOF at the end of each entry, so Jackson's parser stops
naturally without reading into the next entry.

### ID Resolution (Identity Mapper)

```
KEEP_IDS:
  newId = originalId
  ref resolution: use originalRefId as-is

REGENERATE_IDS (default for clone):
  newId = null → JPA auto-generates
  after persist: record {originalId → generatedId} in idMappings
  ref resolution: idMappings[refClass][originalRefId] → resolvedId
```

---

## 6. Clone Operation

Clone uses a temporary file (not an in-memory buffer) to safely handle tenants with
hundreds of megabytes of data:

```
Phase 1: ExportPipeline.export(source, tempFile) → Account{src}_timestamp.zip
Phase 2: ImportPipeline.importTenant(tempFile, importOptions)
Phase 3: Files.deleteIfExists(tempFile)   [in finally block]
```

---

## 7. Worker Lifecycle

```
POST /export/{accountId}
    │
    ▼
AccountMigrationJobService.createExportJob(accountId, options)
    │
    ├── 1. Persist AccountMigrationJob{status=PENDING}
    ├── 2. SchedulerUtil.runWithResult(new ExportWorker(jobId, accountId, options))
    │         └── Virtual Thread starts
    │               ├── Update job status → RUNNING
    │               ├── Call ExportPipeline.export(...)  → writes local working ZIP
    │               │     └── MigrationProgressListener updates job.progress periodically
    │               ├── finalizeJob():
    │               │     ├── On success: upload working ZIP → EntityFileService.createEntityFile()
    │               │     │     ├── upload OK   → job status → COMPLETED, resultFile = EntityFile
    │               │     │     └── upload FAILS → job status → FAILED (never reports success
    │               │     │                         without a persisted, downloadable result)
    │               │     ├── On pipeline failure: job status → FAILED, set errorMessage
    │               │     └── delete the local working ZIP — always, regardless of outcome
    └── 3. Return jobId to caller (non-blocking)

Cancellation:
    POST /jobs/{jobId}/cancel
        ├── Load job from DB
        ├── Get CancellationToken from in-memory registry
        ├── token.cancel()
        └── Worker's main loop checks token.isCancelled() between chunks → exits gracefully
```

---

## 7.1 Result Persistence (EntityFileStorage integration)

The ZIP produced by an EXPORT or BACKUP job is never kept as a "final" file on local or
container disk. It is built in a local, transient working file
(`AccountMigrationProperties.outputDirectory`, default `${java.io.tmpdir}/saas-migration`)
purely as scratch space while the pipeline runs — the same directory a container image or its
ephemeral volume could lose at any restart/redeploy.

```
ExportWorker writes ──▶ local working ZIP ──▶ finalizeJob() on completion
                                                    │
                                                    ▼
                                    EntityFileService.createEntityFile(
                                        fileInfo, /* target */ job, description)
                                                    │
                                                    ▼
                                   EntityFileStorage (LocalStorage / AWSS3Storage / Buckie / …)
                                                    │
                                                    ▼
                                  AccountMigrationJob.resultFile = EntityFile   (FK, eager)
                                  local working ZIP deleted
```

- The `EntityFile` is associated with the `AccountMigrationJob` itself
  (`targetEntity = AccountMigrationJob`, `targetEntityId = job.id`), not with the tenant Account —
  it is an operational artifact of the migration system, not tenant data.
- Which backend actually stores the bytes (local safe directory, S3, Buckie, …) is controlled by
  the host application's `EntityFileStorage` configuration (`DEFAULT_STORAGE_ID` app parameter) —
  the migration module has zero storage-specific code.
- `GET /jobs/{jobId}/download` resolves the result via
  `AccountMigrationJobService.downloadResult(jobUuid)` → `EntityFileService.download(entityFile)`
  → `StoredEntityFile.toResource()`, streaming from whatever backend is configured. The controller
  never touches a raw filesystem path.
- If the upload to `EntityFileStorage` fails, the job is marked **FAILED** (not COMPLETED) — a
  job never reports success without a durable, downloadable result.
- CLONE does not go through this path: its export phase writes to a JVM temp file
  (`Files.createTempFile`, OS temp dir) that is deleted in a `finally` block once the import phase
  finishes — it is a pure intermediate buffer, never exposed for download (see §6).

---

## 8. Export File Format (v3)

```
Account42_20260614T100500.zip
```

**manifest.json** (first ZIP entry):
```json
{
  "version": "3",
  "exportedAt": "2026-06-14T10:05:00",
  "sourceAccountId": 42,
  "identityStrategy": "KEEP_IDS",
  "account": {
    "id": 42,
    "name": "Acme Corp",
    "subdomain": "acme",
    "email": "admin@acme.com"
  },
  "entities": [
    { "file": "Account42_AccountParameter.json", "entityClass": "tools.dynamia.modules.saas.jpa.AccountParameter" },
    { "file": "Account42_Customer.json",         "entityClass": "com.example.Customer" },
    { "file": "Account42_Order.json",            "entityClass": "com.example.Order" }
  ]
}
```

**Account42_Customer.json** (ZIP entry per entity):
```json
{
  "entityClass": "com.example.Customer",
  "fields": ["id", "accountId", "name", "category_ref_id"],
  "rows": [
    [10, 42, "John", 3],
    [11, 42, "Cindy", null]
  ]
}
```

**Key conventions:**
- Each entity file is a standalone JSON document with `entityClass`, `fields`, and `rows`.
- `{fieldName}_ref_id` encodes a `@ManyToOne` / `@OneToOne` reference by its primary key.
- Entities appear in topological order (parents before children) both in the manifest and as ZIP entries.
- The `account` section in the manifest makes the archive self-describing.
- Format version `"3"` is the only supported format. Versions `"1"` and `"2"` (legacy JSON / GZIP) are no longer accepted by the importer.

---

## 9. Identity Mapping SPI

```java
public interface IdentityMapper {
    // Map an original ID to the ID to use when persisting
    // Return null → let JPA auto-generate
    Object mapId(Object originalId, Class<?> entityClass);

    // Resolve a reference ID from the exported file to the actual ID in the target DB
    Object resolveReferenceId(Object originalRefId, Class<?> refClass,
                               Map<String, Map<Object, Object>> idMappings);

    IdentityStrategy getStrategy();
}
```

Register a custom implementation as a Spring bean (`@Component` / `@Service`) to override the default behaviour for a given strategy. `ImportPipeline` auto-discovers all `IdentityMapper` beans and selects by `getStrategy()` before falling back to the built-in defaults.

### Built-in Implementations

| Class | Strategy | Use Case |
|-------|----------|---------- |
| `KeepIdsIdentityMapper` | `KEEP_IDS` | Cross-env restore to empty DB |
| `RegenerateIdsIdentityMapper` | `REGENERATE_IDS` | Clone within same DB |

> **Note:** `UUID7` is declared in `IdentityStrategy` but not yet implemented. Selecting it throws `MigrationException` (planned for v3).

---

## 10. Annotations

### `@AccountExportIgnore`

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AccountExportIgnore {}
```

Apply to `@Entity` classes that should never be exported (audit logs, metrics, caches).

### `@ExportIgnore`

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExportIgnore {}
```

Apply to entity fields that should be skipped during serialization (computed values, sensitive tokens, caches).

---

## 11. Database Schema

The module adds one table:

```sql
CREATE TABLE saas_migration_jobs (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  uuid           VARCHAR(50)   NOT NULL UNIQUE,
  account_id     BIGINT,
  target_account_id BIGINT,
  job_type       VARCHAR(30),
  status         VARCHAR(30),
  progress       INT           DEFAULT 0,
  progress_message VARCHAR(2000),
  created_at     DATETIME,
  started_at     DATETIME,
  finished_at    DATETIME,
  error_message  TEXT,
  result_file_id BIGINT REFERENCES mod_entity_files(id),
  options_json   VARCHAR(2000)
);
```

`result_file_id` is a nullable FK into `mod_entity_files` (owned by the `entity-files` module,
see [`entity-files/README.md`](../../../entity-files/README.md)) — it is only set once the
result ZIP has been durably persisted via `EntityFileService`. There is no raw filesystem path
column: a job with `status = COMPLETED` and `result_file_id IS NULL` should not occur (see §7.1).

---

## 12. Scalability Notes

| Concern | Mitigation |
|---------|------------|
| Large tables | Keyset pagination (`id > lastId`, configurable chunk size, default 500 rows/page) |
| Memory | Jackson streaming API; entity files written to temp dir, then streamed into ZIP via `Files.copy()` |
| Parallel export | Up to `exportParallelism` (default 4) entity types exported concurrently via virtual threads; each uses its own `EntityManager` |
| Disk I/O | 256 KB ZIP buffer, 64 KB per-entity file buffer; DEFLATE BEST_SPEED compression |
| Network / disk | ZIP always produced — typically 5–10× smaller than raw JSON |
| Result durability | Result ZIP persisted via `EntityFileService`/`EntityFileStorage` (local safe dir, S3, Buckie, …), not on container-local disk — survives restarts/redeploys and is never subject to image/volume size limits (see §7.1) |
| Long-running jobs | Virtual threads, cooperative cancellation via `CancellationToken` |
| DB load | Read-only keyset-paginated queries; imports batched per chunk in isolated transactions |
| Concurrent jobs | In-memory job registry + DB-backed state; configurable max concurrent |
| Large clone | Export → temp file → import (avoids OOM from in-memory buffers) |

---

## 13. Implementation Roadmap

| Phase | Scope |
|-------|-------|
| **v1 (legacy)** | EXPORT, IMPORT as single JSON. `KEEP_IDS` + `REGENERATE_IDS`. |
| **v2 (legacy)** | Columnar format (fields + rows arrays). Optional GZIP. |
| **v3 (current)** | ZIP multi-file: one JSON per entity. Always compressed. Keyset pagination. Parallel entity export (virtual threads). Clone via temp file. |
| **v3.1 (current)** | Result ZIP persisted via `EntityFileService`/`EntityFileStorage` (local safe dir, S3, Buckie, …) instead of raw container-local disk. Built as a transient working file, uploaded and locally deleted only after the pipeline succeeds. See §7.1. |
| **v4** | Cross-environment MIGRATE (HTTP push to remote endpoint). Resume after failure (checkpoint in DB). |
| **v5** | `UUID7` identity strategy. Partial export (subset of entities). Schema validation on import. |
| **v6** | Multi-region database migration. Event-driven progress via SSE/WebSocket. |
