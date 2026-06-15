import path from 'node:path';
import { DocumentationCatalog } from './catalog.js';
import type { DocSourceType } from './types.js';

interface RuntimeArgs {
  transport: 'stdio' | 'http';
  port: number;
  rootDir: string;
}

interface ToolArgs {
  source?: DocSourceType;
  locale?: string;
  limit?: number;
  offset?: number;
  id?: string;
  heading?: string;
  query?: string;
}

export async function startServer(args: RuntimeArgs): Promise<void> {
  const catalog = await DocumentationCatalog.fromWorkspace({ rootDir: args.rootDir });

  const mcpServerModule = await import('@modelcontextprotocol/sdk/server/index.js');
  const mcpTypesModule = await import('@modelcontextprotocol/sdk/types.js');

  const Server = (mcpServerModule as any).Server;
  const ListToolsRequestSchema = (mcpTypesModule as any).ListToolsRequestSchema;
  const CallToolRequestSchema = (mcpTypesModule as any).CallToolRequestSchema;

  const server = new Server(
    { name: '@dynamia-tools/mcp', version: '0.1.0' },
    { capabilities: { tools: {} } },
  );

  server.setRequestHandler(ListToolsRequestSchema, async () => {
    return {
      tools: [
        {
          name: 'list_docs',
          description: 'List available documentation pages by source and locale.',
          inputSchema: {
            type: 'object',
            properties: {
              source: { type: 'string', enum: ['website', 'framework', 'buckie'] },
              locale: { type: 'string', enum: ['en', 'es'] },
              limit: { type: 'number', minimum: 1, maximum: 200 },
              offset: { type: 'number', minimum: 0 },
            },
          },
        },
        {
          name: 'get_doc',
          description: 'Get one documentation page by id.',
          inputSchema: {
            type: 'object',
            properties: {
              id: { type: 'string' },
            },
            required: ['id'],
          },
        },
        {
          name: 'search_docs',
          description: 'Search docs by keyword with lightweight ranking.',
          inputSchema: {
            type: 'object',
            properties: {
              query: { type: 'string' },
              source: { type: 'string', enum: ['website', 'framework', 'buckie'] },
              locale: { type: 'string', enum: ['en', 'es'] },
              limit: { type: 'number', minimum: 1, maximum: 50 },
            },
            required: ['query'],
          },
        },
        {
          name: 'get_section',
          description: 'Extract a section from a page by heading name.',
          inputSchema: {
            type: 'object',
            properties: {
              id: { type: 'string' },
              heading: { type: 'string' },
            },
            required: ['id', 'heading'],
          },
        },
      ],
    };
  });

  server.setRequestHandler(CallToolRequestSchema, async (request: any) => {
    const name = request.params.name as string;
    const input: ToolArgs = (request.params.arguments ?? {}) as ToolArgs;

    if (name === 'list_docs') {
      const docs = catalog.list({
        source: input.source,
        locale: input.locale,
        limit: input.limit,
        offset: input.offset,
      });

      return jsonResponse({
        total: docs.length,
        docs: docs.map((doc) => ({
          id: doc.id,
          source: doc.source,
          locale: doc.locale,
          title: doc.title,
          description: doc.description,
          relativePath: doc.relativePath,
          updatedAt: doc.updatedAt,
        })),
      });
    }

    if (name === 'get_doc') {
      if (!input.id) {
        return errorResponse('Missing required argument: id');
      }

      const doc = catalog.get(input.id);
      if (!doc) {
        return errorResponse(`Document not found: ${input.id}`);
      }

      return jsonResponse({
        id: doc.id,
        source: doc.source,
        locale: doc.locale,
        title: doc.title,
        description: doc.description,
        relativePath: doc.relativePath,
        absolutePath: doc.absolutePath,
        updatedAt: doc.updatedAt,
        headings: doc.headings,
        content: doc.content,
      });
    }

    if (name === 'search_docs') {
      if (!input.query) {
        return errorResponse('Missing required argument: query');
      }

      const hits = catalog.search({
        query: input.query,
        source: input.source,
        locale: input.locale,
        limit: input.limit,
      });

      return jsonResponse({ query: input.query, total: hits.length, hits });
    }

    if (name === 'get_section') {
      if (!input.id || !input.heading) {
        return errorResponse('Missing required arguments: id and heading');
      }

      const section = catalog.getSection(input.id, input.heading);
      if (!section) {
        return errorResponse(`Section not found. id=${input.id}, heading=${input.heading}`);
      }

      return jsonResponse({
        id: input.id,
        heading: input.heading,
        content: section,
      });
    }

    return errorResponse(`Unknown tool: ${name}`);
  });

  if (args.transport === 'stdio') {
    const transportModule = await import('@modelcontextprotocol/sdk/server/stdio.js');
    const StdioServerTransport = (transportModule as any).StdioServerTransport;
    const transport = new StdioServerTransport();
    await server.connect(transport);
    return;
  }

  const transportModule = await import('@modelcontextprotocol/sdk/server/sse.js');
  const SSEServerTransport = (transportModule as any).SSEServerTransport;
  const http = await import('node:http');

  const sessions = new Map<string, any>();

  const httpServer = http.createServer(async (req, res) => {
    const url = new URL(req.url ?? '/', `http://${req.headers.host ?? `localhost:${args.port}`}`);

    if (req.method === 'GET' && url.pathname === '/sse') {
      const transport = new SSEServerTransport('/messages', res);
      sessions.set(transport.sessionId, transport);
      res.on('close', () => {
        sessions.delete(transport.sessionId);
      });
      await server.connect(transport);
      return;
    }

    if (req.method === 'POST' && url.pathname === '/messages') {
      const sessionId = url.searchParams.get('sessionId');
      if (!sessionId) {
        res.statusCode = 400;
        res.setHeader('content-type', 'application/json');
        res.end(JSON.stringify({ error: 'Missing sessionId query parameter' }));
        return;
      }

      const transport = sessions.get(sessionId);
      if (!transport) {
        res.statusCode = 404;
        res.setHeader('content-type', 'application/json');
        res.end(JSON.stringify({ error: 'Session not found' }));
        return;
      }

      await transport.handlePostMessage(req, res);
      return;
    }

    if (req.method === 'GET' && url.pathname === '/health') {
      res.statusCode = 200;
      res.setHeader('content-type', 'application/json');
      res.end(JSON.stringify({ ok: true }));
      return;
    }

    res.statusCode = 404;
    res.end('Not found');
  });

  await new Promise<void>((resolve, reject) => {
    httpServer.once('error', reject);
    httpServer.listen(args.port, () => resolve());
  });

  process.stderr.write(
    `Dynamia docs MCP over SSE listening on http://localhost:${args.port}/sse (root=${path.resolve(args.rootDir)})\n`,
  );
}

function jsonResponse(payload: unknown): { content: Array<{ type: 'text'; text: string }> } {
  return {
    content: [
      {
        type: 'text',
        text: JSON.stringify(payload, null, 2),
      },
    ],
  };
}

function errorResponse(message: string): { isError: true; content: Array<{ type: 'text'; text: string }> } {
  return {
    isError: true,
    content: [
      {
        type: 'text',
        text: message,
      },
    ],
  };
}

