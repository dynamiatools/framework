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

## curl Examples

All examples assume the server is running at `http://localhost:8080` with identity `erp-prod` and secret `my-secret-password`.

### Health check

```bash
curl http://localhost:8080/health
```

### Upload a file

```bash
curl -X PUT http://localhost:8080/documents/tenant-a/invoice.pdf \
  -H "X-SFS-Identity: erp-prod" \
  -H "X-SFS-Secret: my-secret-password" \
  -H "Content-Type: application/octet-stream" \
  --data-binary @/path/to/invoice.pdf
```

### Upload a file using HTTP Basic Auth

```bash
curl -X PUT http://localhost:8080/documents/tenant-a/report.xlsx \
  -u "erp-prod:my-secret-password" \
  -H "Content-Type: application/octet-stream" \
  --data-binary @/path/to/report.xlsx
```

### Download a file

```bash
curl http://localhost:8080/documents/tenant-a/invoice.pdf \
  -H "X-SFS-Identity: erp-prod" \
  -H "X-SFS-Secret: my-secret-password" \
  -o invoice.pdf
```

### Download a thumbnail (image resize on the fly)

```bash
# 300×300 WebP thumbnail with cover fit
curl "http://localhost:8080/documents/tenant-a/photo.jpg?w=300&h=300&fit=cover&format=webp" \
  -H "X-SFS-Identity: erp-prod" \
  -H "X-SFS-Secret: my-secret-password" \
  -o thumbnail.webp
```

### List a directory

```bash
curl http://localhost:8080/documents/tenant-a/ \
  -H "X-SFS-Identity: erp-prod" \
  -H "X-SFS-Secret: my-secret-password"
```

### List bucket contents (paginated)

```bash
# First page
curl "http://localhost:8080/documents?limit=50" \
  -H "X-SFS-Identity: erp-prod" \
  -H "X-SFS-Secret: my-secret-password"

# Next page using the cursor returned from the previous response
curl "http://localhost:8080/documents?limit=50&cursor=<cursor-value>" \
  -H "X-SFS-Identity: erp-prod" \
  -H "X-SFS-Secret: my-secret-password"
```

### Delete a file

```bash
curl -X DELETE http://localhost:8080/documents/tenant-a/invoice.pdf \
  -H "X-SFS-Identity: erp-prod" \
  -H "X-SFS-Secret: my-secret-password"
```

### Pretty-print JSON responses

Pipe responses through `jq` for readable output:

```bash
curl -s "http://localhost:8080/documents/tenant-a/" \
  -H "X-SFS-Identity: erp-prod" \
  -H "X-SFS-Secret: my-secret-password" | jq
```

## CLI Reference

### Tab Completion

SFS supports tab completion for Bash, Zsh and Fish.

```bash
# Bash — add to ~/.bashrc
eval "$(sfs completion bash)"

# Zsh — add to ~/.zshrc  (compinit must already be loaded)
eval "$(sfs completion zsh)"

# Fish — save to completions directory
sfs completion fish > ~/.config/fish/completions/sfs.fish
```

Completion is dynamic: bucket names and identity names are resolved at completion time by querying the local SFS runtime.

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

# Files
sfs list files <bucket> [path]          # List files in a bucket or directory
sfs upload <bucket> <key> <localFile>   # Upload a local file to a bucket
sfs copy <srcBucket> <srcKey> <dstBucket> <dstKey>  # Copy a file between buckets

# Provisioning (auto-generate identity + secret + grant in one step)
sfs provision <bucket> [--identity <name>] [--prefix <path>] [--read] [--write] [--delete]
```

## Data Directory Structure

SFS stores all runtime configuration under a `.sfs/` directory in the working directory:

```
.sfs/
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

