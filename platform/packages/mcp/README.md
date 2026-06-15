# @dynamia-tools/mcp

MCP server package that exposes Dynamia Tools documentation as AI-callable tools.

## Features

- Reads docs from:
  - `website/src/content/docs`
  - `framework/docs`
  - `buckie/buckie-node-js/README.md`
  - `buckie/buckie-php/README.md`
- Supports two transports:
  - `stdio` for local MCP clients
  - `http` (SSE + message endpoint)
- Exposes four MCP tools:
  - `list_docs`
  - `get_doc`
  - `search_docs`
  - `get_section`

## Install

```bash
pnpm add -D @dynamia-tools/mcp
```

Or run from source in this workspace.

## Run

### Stdio transport (default)

```bash
pnpm --filter @dynamia-tools/mcp build
pnpm --filter @dynamia-tools/mcp exec dynamia-docs-mcp --rootDir /path/to/DynamiaTools
```

### HTTP transport (SSE)

```bash
pnpm --filter @dynamia-tools/mcp exec dynamia-docs-mcp --transport http --port 3900 --rootDir /path/to/DynamiaTools
```

Endpoints:

- `GET /sse`
- `POST /messages?sessionId=...`
- `GET /health`

## Configuration

CLI flags and env vars:

- `--transport` or `DYNAMIA_MCP_TRANSPORT` (`stdio` | `http`)
- `--port` or `DYNAMIA_MCP_PORT` (default `3900`)
- `--rootDir` or `DYNAMIA_TOOLS_ROOT`

`rootDir` must be the directory containing `website/`, `framework/`, and optionally `buckie/`.

## Development

```bash
pnpm --filter @dynamia-tools/mcp install
pnpm --filter @dynamia-tools/mcp test
pnpm --filter @dynamia-tools/mcp build
```

## License

Apache-2.0

