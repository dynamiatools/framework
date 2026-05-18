# simple-file-server (SFS)

`simple-file-server` (SFS) is a standalone filesystem-native file server with a minimal S3-style API designed for secure server-to-server file operations.

The system is intentionally simple:

- Private by default
- No anonymous access
- No public buckets
- No JWT
- No object metadata database
- No distributed storage features
- No S3 compatibility goals beyond basic object semantics

SFS is designed as the backend for `RemoteSimpleEntityFileStorage` in EntityFiles and optimized for:

- ERP systems
- Multi-tenant applications
- Internal services
- Hybrid infrastructure
- Large file streaming
- Low operational complexity

---

# Core Principles

1. Filesystem-native storage
2. All access requires authentication
3. Streaming-first architecture
4. No persistent object metadata
5. Predictable and portable deployment
6. Minimal operational complexity
7. Standalone runtime and CLI
8. JSON-only operational API

---

# Main Goals

Implement a standalone service that supports:

- Upload
- Download
- Listing
- Deletion
- Thumbnail generation
- Permission-based access control
- Persistent operational logging

using a simple JSON API and direct filesystem storage.

---

# Security Model

SFS is strictly private.

Every request requires authentication using:

- `identity`
- `secret`

Without valid credentials:
- no access
- no listing
- no downloads
- no uploads
- no metadata access

The server is intended exclusively for trusted backend/server-to-server communication.

---

# Authentication

Authentication uses:

- HTTP Basic Auth
or
- custom headers

Example:

```http
X-SFS-Identity: erp-prod
X-SFS-Secret: xxxxxxxxx
```

Secrets are never stored in plain text.

Requirements:

- Argon2id password hashing
- Timing-safe secret comparison
- Identity-based permission resolution

---

# Authorization Model

Each identity may have access to one or more buckets.

Permissions are granted per bucket and restricted by path prefixes.

Supported permissions:

- `read`
- `write`
- `delete`

Example grant:

```json
{
  "bucket": "documents",
  "prefixes": [
    "/tenant-a/",
    "/tenant-a/public/"
  ],
  "permissions": ["read", "write"]
}
```

Rules:

- Access outside allowed prefixes is denied
- Prefix validation is mandatory for every operation
- Authorization is evaluated after path normalization

---

# Bucket Model

A bucket is a logical alias mapped to an absolute filesystem path.

Example:

```bash
sfs create bucket documents /mnt/storage/documents
```

Internal example:

```text
documents -> /mnt/storage/documents
```

Buckets may point to any valid filesystem location.

Requirements:

- Absolute paths only
- Canonical path normalization
- Path traversal protection
- Writable validation during startup

---

# Local Configuration Persistence

All runtime configuration and metadata are stored in the server working directory.

Default structure:

```text
./sfs/
 ├── config/
 ├── identities/
 ├── buckets/
 ├── logs/
 ├── runtime/
 └── cache/
```

Requirements:

- Self-contained deployment
- Portable runtime
- No external database
- No external configuration dependency by default

Persisted data includes:

- identities
- grants
- bucket definitions
- runtime metadata
- operational logs

Object/file metadata is never persisted.

---

# Filesystem Layout

Each bucket internally contains a reserved `.sfs` directory.

Example:

```text
/mnt/storage/documents/
 └── .sfs/
      ├── staging/
      ├── thumbs/
      └── runtime/
```

Purpose:

- staging uploads
- thumbnail cache
- temporary runtime artifacts

Rules:

- `.sfs` is internal and inaccessible through the API
- users cannot upload or access `.sfs` paths

---

# API Design

## Object Download

```http
GET /:bucket/*key
```

Behavior:

- Streams file directly
- Supports large files
- Requires `read` permission

Responses:

- file stream
- JSON error on failure

---

## Folder Listing

```http
GET /:bucket/*key-folder/
```

Behavior:

- Lists directory contents
- Returns JSON response
- Requires `read` permission

If target:
- does not exist → `404`
- is not a directory → `404`

---

## Bucket Listing

```http
GET /:bucket
```

Returns:

- paginated object listing
- JSON response only

Supported query parameters:

```http
?limit=100
&cursor=...
```

Purpose:

- prevent excessive memory usage
- support large directories

---

## File Upload

```http
PUT /:bucket/*key
```

Requirements:

- streaming upload
- overwrite support
- mandatory staging
- requires `write` permission

Upload flow:

1. Stream upload into bucket-local staging directory
2. Validate successful write completion
3. fsync temporary file
4. Atomically move file into final destination when possible
5. Cleanup staging artifact

Rules:

- Never write directly into final destination
- Upload must not leave partial target files
- Upload failures must be logged
- Existing files may be overwritten by default

Optional future header:

```http
If-Not-Exists: true
```

---

## File Delete

```http
DELETE /:bucket/*key
```

Behavior:

- Deletes exact file only
- Non-recursive
- Requires `delete` permission

Rules:

- Directories cannot be recursively removed
- Missing targets return JSON error

---

# Path Safety

Path traversal protection is mandatory.

The server must:

- normalize paths
- resolve canonical paths
- validate bucket boundaries

Invalid examples:

```text
../../../etc/passwd
..%2F..%2F
```

After resolution:

- final path must remain inside bucket root
- otherwise request is denied

---

# Streaming Requirements

All file operations must support streaming.

Requirements:

- constant memory usage
- support for large files
- no full-file buffering

Applies to:

- uploads
- downloads
- thumbnail generation

---

# Thumbnail System

Thumbnail generation is lazy and cache-based.

Supported request:

```http
GET /bucket/image.jpg?w=300&h=300
```

Behavior:

1. Detect image file
2. Generate thumbnail on first request
3. Persist thumbnail into local cache
4. Reuse cached thumbnail on subsequent requests

Requirements:

- generation only on demand
- cached thumbnails stored in `.sfs/thumbs`
- thumbnail cache keys based on:
  - original file
  - width
  - height
  - resize mode
  - output format

---

# MIME Detection

MIME type detection must not rely exclusively on file extension.

Preferred detection:

- magic bytes
- content sniffing

Purpose:

- correct content type responses
- safer file handling

---

# Logging

SFS persists operational logs only.

No persistent object catalog exists.

Recommended format:

- JSON Lines (`.jsonl`)

Supported events:

- `upload_started`
- `upload_staged`
- `upload_committed`
- `upload_failed`
- `download`
- `list`
- `delete`
- `auth_failed`
- `permission_denied`

Example:

```json
{
  "ts": "2026-05-16T12:00:00Z",
  "event": "upload_committed",
  "identity": "erp-prod",
  "bucket": "documents",
  "key": "tenant-a/invoice.pdf",
  "size": 204812,
  "elapsedMs": 42
}
```

Recommended files:

```text
logs/access.log.jsonl
logs/error.log.jsonl
```

---

# JSON Response Format

## Success

```json
{
  "ok": true,
  "data": {}
}
```

## Error

```json
{
  "ok": false,
  "error": {
    "code": "SFS_ERROR_CODE",
    "message": "Human readable message",
    "details": {}
  }
}
```

---

# CLI (`sfs`)

The server includes a standalone CLI.

All CLI output must use JSON.

---

# Bucket Commands

Create bucket:

```bash
sfs create bucket <name> <absolutePath>
```

List buckets:

```bash
sfs list buckets
```

Remove bucket (optional policy):

```bash
sfs remove bucket <name>
```

---

# Identity Commands

Create identity:

```bash
sfs create identity <identity> <secret>
```

Grant permissions:

```bash
sfs grant <identity> <bucket> \
  --read \
  --write \
  --delete \
  --prefix <path>
```

List identities:

```bash
sfs list identities
```

Revoke permissions:

```bash
sfs revoke <identity> <bucket>
```

---

# Runtime Commands

Start server:

```bash
sfs serve --host 0.0.0.0 --port 8080
```

Optional future flags:

```bash
--data-dir
--config-dir
--log-level
```

---

# Startup Validation

During startup the server validates:

- bucket existence
- writable staging directories
- writable cache directories
- configuration integrity
- orphan staging cleanup

Old temporary staging files may be cleaned automatically.

---

# Recommended Node.js Technology Stack

SFS is designed to be implemented as an ultra-fast streaming-first backend optimized for filesystem IO and server-to-server communication.

## Runtime

- Node.js 22+
- TypeScript (strict mode)
- ESM modules

## HTTP Framework

Recommended framework:

- Fastify

Reasons:

- Very high throughput
- Low overhead
- Excellent streaming support
- Native schema integration
- Lightweight plugin system
- Efficient request lifecycle

## Validation

Recommended options:

- TypeBox + AJV (maximum performance)
or
- Zod (better developer experience)

## Logging

Recommended logger:

- Pino

Reasons:

- Extremely fast JSON logging
- Native Fastify integration
- Perfect for `.jsonl` operational logs

## Password Hashing

Recommended:

- Argon2id

Requirements:

- No plain text secret storage
- Timing-safe comparisons

## Thumbnail Processing

Recommended:

- Sharp

Reasons:

- Very fast image processing
- Low memory usage
- Based on libvips
- Efficient thumbnail generation

## MIME Detection

Recommended:

- file-type

Purpose:

- Detect MIME types using magic bytes
- Avoid extension-only detection

## CLI

Recommended:

- CAC
or
- Commander.js

## Development

Recommended tools:

- TSX
- Vitest

## Streaming Requirements

The implementation must use native streaming APIs:

- stream.pipeline
- fs.createReadStream
- fs.createWriteStream

The server must avoid:

- full-file buffering
- readFile/writeFile for large files
- heavy multipart parsers

## Upload Strategy

Uploads should use raw streaming requests.

Recommended request style:

```http
PUT /bucket/key
Content-Type: application/octet-stream
```

This avoids multipart overhead and improves performance.

## Recommended Internal Structure

```text
src/
 ├── app/
 ├── auth/
 ├── bucket/
 ├── cli/
 ├── config/
 ├── errors/
 ├── http/
 ├── logging/
 ├── storage/
 ├── thumbnail/
 ├── utils/
 └── types/
```

## Non-Recommended Frameworks

SFS intentionally avoids large abstraction-heavy backend frameworks such as:

- NestJS

Reason:

- unnecessary overhead
- excessive abstraction
- lower streaming control
- less predictable IO behavior

## Final Recommended Stack

```text
Node.js 24
TypeScript
Fastify
Pino
Sharp
TypeBox
AJV
Argon2
Vitest
TSX
```

---

# Proposed Internal Structure

```text
src/
 ├── auth/
 ├── cli/
 ├── config/
 ├── http/
 ├── logging/
 ├── storage/
 ├── thumbnail/
 └── runtime/
```

---

# Non-Goals

SFS intentionally does NOT implement:

- distributed storage
- clustering
- replication
- eventual consistency
- object versioning
- multipart upload
- public URLs
- anonymous access
- JWT authentication
- full S3 compatibility
- metadata indexing database

The goal is simplicity, predictability, and operational efficiency.

---

# Acceptance Criteria

1. Buckets map to arbitrary absolute filesystem paths
2. Identities authenticate using `identity + secret`
3. Permissions are bucket and prefix based
4. PUT operations use mandatory staging
5. Uploads and downloads support streaming
6. DELETE removes exact files only
7. Bucket and folder listing return paginated JSON
8. Thumbnail generation is lazy and cached
9. No persistent object metadata exists
10. Operational logs are persisted
11. Everything runs standalone through `sfs`
12. All runtime configuration persists locally in the working directory
13. All access requires authentication
14. Path traversal attacks are prevented

---

# Next Phase: EntityFiles Integration

Implement:

```text
RemoteSimpleEntityFileStorage
```

Responsibilities:

- upload
- download
- delete
- list
- thumbnail access

using SFS endpoints.

Additional goals:

- remote thumbnail strategy
- public URL abstraction layer
- transparent storage backend switching
- multi-storage support in EntityFiles
