# DynamiaTools SaaS — Tenant Mobility Module

[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.saas.migration)](https://search.maven.org/search?q=tools.dynamia.modules.saas.migration)
![Java Version Required](https://img.shields.io/badge/java-25-blue)

The **Tenant Mobility Module** is a sub-module of the SaaS extension that provides full lifecycle management for tenant data: export, import, clone, backup, restore, and cross-environment migration.

All operations run as **async background jobs** via virtual threads, so long-running processes (millions of rows) never block the application.

---

## Features

| Operation | Description |
|-----------|-------------|
| `EXPORT`  | Serialize all tenant data to a versioned JSON/GZIP file |
| `IMPORT`  | Restore tenant data from a previously exported file |
| `CLONE`   | Duplicate a tenant in the same system (different accountId) |
| `BACKUP`  | Alias for EXPORT tagged as backup |
| `RESTORE` | Alias for IMPORT that replaces existing data |
| `MIGRATE` | Cross-environment export + remote import (planned v2) |

---

## Installation

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.saas.migration</artifactId>
    <version>26.6.0</version>
</dependency>
```

Make sure your JPA entity scan includes `tools.dynamia.modules.saas.migration` (or let Spring Boot's auto-scan pick it up from the classpath).

---

## Quick Start

### 1. Mark entities to ignore (optional)

```java
// Suppress entire entity from export/import
@Entity
@AccountExportIgnore
public class LoginAuditLog extends SimpleEntitySaaS { ... }

// Suppress specific fields
@Entity
public class Customer extends SimpleEntitySaaS {

    @ExportIgnore
    private String cachedScore; // computed, do not export
}
```

### 2. Launch an async export job via REST

```http
POST /api/saas/migration/jobs/export/42
Content-Type: application/json

{
  "chunkSize": 500,
  "compressionEnabled": true,
  "identityStrategy": "KEEP_IDS"
}
```

Response:
```json
{
  "jobId": "abc-123",
  "jobType": "EXPORT",
  "status": "PENDING",
  "createdAt": "2026-06-14T10:00:00"
}
```

### 3. Poll job status

```http
GET /api/saas/migration/jobs/abc-123
```

### 4. Download result when COMPLETED

```http
GET /api/saas/migration/jobs/abc-123/download
```

### 5. Import a file

```http
POST /api/saas/migration/jobs/import
Content-Type: multipart/form-data

file=@tenant_backup.json.gz
targetAccountId=99
identityStrategy=REGENERATE_IDS
```

### 6. Clone a tenant

```http
POST /api/saas/migration/jobs/clone
Content-Type: application/json

{
  "sourceAccountId": 42,
  "targetAccountId": 99,
  "identityStrategy": "REGENERATE_IDS"
}
```

---

## REST API Reference

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/saas/migration/jobs/export/{accountId}` | Start export job |
| `POST` | `/api/saas/migration/jobs/import` | Start import job (multipart) |
| `POST` | `/api/saas/migration/jobs/clone` | Start clone job |
| `POST` | `/api/saas/migration/jobs/backup/{accountId}` | Start backup job |
| `POST` | `/api/saas/migration/jobs/restore/{accountId}` | Start restore job (multipart) |
| `GET`  | `/api/saas/migration/jobs` | List all jobs |
| `GET`  | `/api/saas/migration/jobs/{jobId}` | Get job status & progress |
| `POST` | `/api/saas/migration/jobs/{jobId}/cancel` | Cancel a running job |
| `GET`  | `/api/saas/migration/jobs/{jobId}/download` | Download result file |

---

## Configuration

```properties
# saas-migration
dynamia.saas.migration.chunk-size=500
dynamia.saas.migration.output-directory=${java.io.tmpdir}/saas-migration
dynamia.saas.migration.compression-enabled=false
dynamia.saas.migration.max-concurrent-jobs=5
dynamia.saas.migration.fail-on-entity-error=false
```

---

## Identity Strategies

| Strategy | Description |
|----------|-------------|
| `KEEP_IDS` | Preserve original database IDs. Suitable for cross-environment restore to an empty target DB. |
| `REGENERATE_IDS` | Assign new auto-generated IDs. Safe for cloning within the same DB. |
| `UUID7` | (Planned v3) Use UUIDv7 for all IDs. |

---

## Export Format

```json
{
  "version": "1",
  "exportedAt": "2026-06-14T10:05:00",
  "sourceAccountId": 42,
  "identityStrategy": "KEEP_IDS",
  "account": { "id": 42, "name": "Acme Corp", ... },
  "entities": {
    "com.example.Customer": [
      { "id": 1, "accountId": 42, "name": "John", "type_ref_id": 5 },
      ...
    ],
    "com.example.Order": [
      { "id": 10, "accountId": 42, "customer_ref_id": 1, ... }
    ]
  }
}
```

Relationship fields (`@ManyToOne`, `@OneToOne`) are serialized as `{fieldName}_ref_id` to decouple the export from JPA serialization complexity. Collections (`@OneToMany`, `@ManyToMany`) are reconstructed naturally when child entities reference their parents.

---

## Extension Points (SPI)

| Interface | Description |
|-----------|-------------|
| `IdentityMapper` | Custom ID mapping strategy |
| `MigrationProgressListener` | Hook for progress events |
| `CancellationToken` | Cooperative cancellation signal |

---

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed design decisions, component diagrams, and implementation roadmap.

---

## License

Apache License 2.0 — Copyright © 2026 Dynamia Soluciones IT S.A.S

