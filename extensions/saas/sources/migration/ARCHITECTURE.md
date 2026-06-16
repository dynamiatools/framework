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
│   createJob() · cancelJob() · getJob() · listJobs()                  │
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
│  (streaming  │      │  (streaming    │
│   JSON write)│      │   JSON read)   │
└──────┬───────┘      └───────┬────────┘
       │                      │
       │ uses                 │ uses
    ┌──┴──────────────────────┴──┐
    │  AccountEntityDiscovery    │   discovers all @Entity + AccountAware
    │  EntityDependencyGraph     │   topological sort via JPA metamodel
    │  IdentityMapper SPI        │   KEEP_IDS / REGENERATE_IDS
    └────────────────────────────┘
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

## 4. Export Pipeline

### Streaming Strategy

```
OutputStream (raw or GZIPOutputStream)
    └── JsonGenerator (Jackson streaming)
            ├── Write header: {version, exportedAt, sourceAccountId, identityStrategy}
            ├── Write "account": AccountDTO object
            └── Write "entities": {
                    For each entityClass in topological order:
                        Write entityClass.getName(): [
                            LOOP (pagination by chunks):
                                count = CrudService.count(entityClass, {accountId})
                                for page 1..N:
                                    chunk = CrudService.find(entityClass, {accountId, paginator})
                                    for each record:
                                        write as JSON object (flat map, refs as _ref_id)
                        ]
                }
```

### Serialization of a Single Entity

```
For each SingularAttribute in EntityType:
  BASIC / EMBEDDED  → write field value directly
  MANY_TO_ONE / ONE_TO_ONE → write {fieldName}_ref_id: <referenced entity's id>
  ONE_TO_MANY / MANY_TO_MANY → SKIP (reconstructed during import via child entities)
```

Fields annotated with `@ExportIgnore` are skipped.

---

## 5. Import Pipeline

### Streaming Strategy

```
InputStream (auto-detected: raw or GZIPInputStream)
    └── JsonParser (Jackson streaming)
            ├── Read header → validate version, note sourceAccountId
            ├── Read "account" → AccountDTO (optionally create new Account)
            └── Read "entities" → {
                    For each entityClassName:
                        resolve class → Class.forName(entityClassName)
                        For each JSON record in array (chunked):
                            deserialize → entity instance
                            resolve _ref_id references via idMappings
                            set accountId = targetAccountId
                            persist entity
                            record originalId → newId in idMappings
                }
```

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

## 6. Worker Lifecycle

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
    │               ├── Call ExportPipeline.export(...)
    │               │     └── MigrationProgressListener updates job.progress periodically
    │               ├── On success: update job status → COMPLETED, set resultPath
    │               └── On failure: update job status → FAILED, set errorMessage
    └── 3. Return jobId to caller (non-blocking)

Cancellation:
    POST /jobs/{jobId}/cancel
        ├── Load job from DB
        ├── Get CancellationToken from in-memory registry
        ├── token.cancel()
        └── Worker's main loop checks token.isCancelled() between chunks → exits gracefully
```

---

## 7. Export File Format

```
saas_export_42_20260614T100500.json[.gz]
```

```json
{
  "version": "1",
  "exportedAt": "2026-06-14T10:05:00",
  "sourceAccountId": 42,
  "identityStrategy": "KEEP_IDS",
  "account": {
    "id": 42,
    "name": "Acme Corp",
    "subdomain": "acme",
    "email": "admin@acme.com",
    ...
  },
  "entities": {
    "tools.dynamia.modules.saas.jpa.AccountParameter": [
      { "id": 1, "accountId": 42, "name": "theme", "value": "dark" }
    ],
    "com.example.Customer": [
      { "id": 10, "accountId": 42, "name": "John", "category_ref_id": 3 }
    ],
    "com.example.Order": [
      { "id": 100, "accountId": 42, "customer_ref_id": 10, "total": 99.99 }
    ]
  }
}
```

**Key conventions:**
- `{fieldName}_ref_id` encodes a `@ManyToOne` / `@OneToOne` reference by its primary key.
- The `account` section makes the package self-describing.
- Entities appear in topological order (parents before children).

---

## 8. Identity Mapping SPI

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

## 9. Annotaions

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

## 10. Database Schema

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
  result_path    VARCHAR(1000),
  options_json   VARCHAR(2000)
);
```

---

## 11. Scalability Notes

| Concern | Mitigation |
|---------|------------|
| Large tables | Chunked pagination (configurable, default 500 rows/page) |
| Memory | Jackson streaming API: never load all rows |
| Network / disk | Optional GZIP compression |
| Long-running jobs | Virtual threads, cooperative cancellation via `CancellationToken` |
| DB load | Read-only paginated queries; imports batched per chunk |
| Concurrent jobs | In-memory job registry + DB-backed state; configurable max concurrent |

---

## 12. Implementation Roadmap

| Phase | Scope |
|-------|-------|
| **v1 (current)** | EXPORT, IMPORT, CLONE, BACKUP, RESTORE. `KEEP_IDS` + `REGENERATE_IDS`. REST API. Progress tracking. Cancellation. |
| **v2** | Cross-environment MIGRATE (HTTP push to remote endpoint). Resume after failure (checkpoint in DB). |
| **v3** | `UUID7` identity strategy. Partial export (subset of entities). Schema validation on import. Diff/merge strategy. |
| **v4** | Multi-region database migration. S3/GCS file storage backend. Event-driven progress via SSE/WebSocket. |

