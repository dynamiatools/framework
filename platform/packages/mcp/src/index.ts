#!/usr/bin/env node

import path from 'node:path';
import { startServer } from './server.js';

interface CliArgs {
  transport: 'stdio' | 'http';
  port: number;
  rootDir: string;
}

function parseArgs(argv: string[]): CliArgs {
  const args = new Map<string, string>();

  for (let index = 0; index < argv.length; index += 1) {
    const token = argv[index];
    if (!token.startsWith('--')) {
      continue;
    }

    const key = token.slice(2);
    const next = argv[index + 1];
    if (next && !next.startsWith('--')) {
      args.set(key, next);
      index += 1;
    } else {
      args.set(key, 'true');
    }
  }

  const transportInput = args.get('transport') ?? process.env.DYNAMIA_MCP_TRANSPORT ?? 'stdio';
  const transport = transportInput === 'http' ? 'http' : 'stdio';

  const portInput = args.get('port') ?? process.env.DYNAMIA_MCP_PORT ?? '3900';
  const parsedPort = Number.parseInt(portInput, 10);
  const port = Number.isFinite(parsedPort) ? parsedPort : 3900;

  const rootInput = args.get('rootDir') ?? process.env.DYNAMIA_TOOLS_ROOT ?? process.cwd();

  return {
    transport,
    port,
    rootDir: path.resolve(rootInput),
  };
}

async function main(): Promise<void> {
  const cliArgs = parseArgs(process.argv.slice(2));
  await startServer(cliArgs);
}

main().catch((error) => {
  process.stderr.write(`Fatal error: ${String(error)}\n`);
  process.exitCode = 1;
});

