# simple-file-server (SFS)

A standalone filesystem-native file server with a minimal S3-style API optimized for secure server-to-server file operations.

## Overview

SFS is designed as the backend for `RemoteSimpleEntityFileStorage` in EntityFiles and optimized for:

- ERP systems and multi-tenant applications
- Internal services and hybrid infrastructure
- Large file streaming with constant memory usage
- Low operational complexity (no database, no external dependencies)

## Quick Start

### Installation

```bash
npm install -g @dynamia-tools/simple-file-server
```

### Initialize and configure

```bash
# Create a bucket
sfs create bucket documents /mnt/storage/documents

# Create an identity
sfs create identity erp-prod my-secret-password

# Grant permissions
sfs grant erp-prod documents --read --write --delete --prefix /tenant-a/

# Start the server
sfs serve --host 0.0.0.0 --port 8080
```

## API

All requests require authentication via `X-SFS-Identity` + `X-SFS-Secret` headers, or HTTP Basic Auth.

### Download a file
```http
GET /:bucket/:key
X-SFS-Identity: erp-prod
X-SFS-Secret: my-secret-password
```

### Download a thumbnail
```http
GET /:bucket/:key?w=300&h=300&fit=cover&format=webp
```

### List a directory
```http
GET /:bucket/:path/
```

### List bucket contents (paginated)
```http
GET /:bucket?limit=100&cursor=...
```

### Upload a file
```http
PUT /:bucket/:key
Content-Type: application/octet-stream
[body: file stream]
```

### Delete a file
```http
DELETE /:bucket/:key
```

### Health check
```http
GET /health
```

## CLI Reference

```bash
# Server
sfs serve [--host <host>] [--port <port>] [--data-dir <dir>] [--log-level <level>]

# Buckets
sfs create bucket <name> <absolutePath>
sfs list buckets
sfs remove bucket <name>

# Identities
sfs create identity <identity> <secret>
sfs list identities
sfs remove identity <identity>

# Permissions
sfs grant <identity> <bucket> --read --write --delete --prefix <path>
sfs revoke <identity> <bucket>
```

## Data Directory Structure

SFS stores all runtime configuration in the working directory:

```
./sfs/
 ├── config/        # Server configuration
 ├── identities/    # Identity definitions + hashed secrets + grants
 ├── buckets/       # Bucket definitions
 ├── logs/          # Operational logs (JSONL format)
 │   ├── access.log.jsonl
 │   └── error.log.jsonl
 ├── runtime/       # Runtime metadata
 └── cache/         # (reserved)
```

Each bucket also contains an internal `.sfs/` directory:

```
/path/to/bucket/
 └── .sfs/
      ├── staging/   # Upload staging area
      ├── thumbs/    # Thumbnail cache
      └── runtime/   # Temporary artifacts
```

## Security

- **Private by default**: every request requires authentication
- **Argon2id password hashing**: secrets are never stored in plain text
- **Prefix-based authorization**: access restricted by bucket and path prefix
- **Path traversal protection**: canonical path validation on every request
- **No public access**: no anonymous or public bucket support

## Technology Stack

- **Node.js 22+** with TypeScript (strict mode)
- **Fastify** for high-throughput HTTP
- **Pino** for structured JSON logging
- **Sharp** for efficient thumbnail generation
- **Argon2** for password hashing
- **file-type** for MIME detection via magic bytes

## License

Apache-2.0 © Dynamia Soluciones IT SAS

