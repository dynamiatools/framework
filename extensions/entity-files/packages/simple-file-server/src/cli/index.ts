#!/usr/bin/env node

import { Command } from 'commander'
import { createRuntime, startServer } from '../runtime/index.js'
import { getDataDir } from '../config/config.service.js'
import type { Permission } from '../types/index.js'

const program = new Command()

program
  .name('sfs')
  .description('Simple File Server - filesystem-native file server with S3-style API')
  .version('26.5.0')

// ──────────────────────────────────────────────────────────
// sfs serve
// ──────────────────────────────────────────────────────────
program
  .command('serve')
  .description('Start the SFS server')
  .option('--host <host>', 'Host to listen on', process.env.SFS_HOST ?? '0.0.0.0')
  .option('--port <port>', 'Port to listen on', process.env.SFS_PORT ?? '8080')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .option('--log-level <level>', 'Log level (trace|debug|info|warn|error)', 'info')
  .action(async (opts) => {
    await startServer({
      host: opts.host,
      port: parseInt(opts.port, 10),
      dataDir: opts.dataDir,
      logLevel: opts.logLevel,
    })
  })

// ──────────────────────────────────────────────────────────
// sfs create
// ──────────────────────────────────────────────────────────
const create = program.command('create').description('Create resources')

create
  .command('bucket <name> <absolutePath>')
  .description('Create a new bucket mapped to an absolute filesystem path')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (name: string, absolutePath: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const bucket = await runtime.bucketService.create(name, absolutePath)
      console.log(JSON.stringify({ ok: true, data: bucket }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

create
  .command('identity <identity> <secret>')
  .description('Create a new identity with the given secret')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (identity: string, secret: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const id = await runtime.identityService.create(identity, secret)
      const safeId = { ...id, hashedSecret: '[REDACTED]' }
      console.log(JSON.stringify({ ok: true, data: safeId }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs list
// ──────────────────────────────────────────────────────────
const list = program.command('list').description('List resources')

list
  .command('buckets')
  .description('List all buckets')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const buckets = await runtime.bucketService.list()
      console.log(JSON.stringify({ ok: true, data: buckets }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

list
  .command('identities')
  .description('List all identities')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const ids = await runtime.identityService.list()
      const safeIds = ids.map(id => ({ ...id, hashedSecret: '[REDACTED]' }))
      console.log(JSON.stringify({ ok: true, data: safeIds }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs remove
// ──────────────────────────────────────────────────────────
const remove = program.command('remove').description('Remove resources')

remove
  .command('bucket <name>')
  .description('Remove a bucket definition (does not delete files)')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (name: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      await runtime.bucketService.delete(name)
      console.log(JSON.stringify({ ok: true, data: { removed: name } }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

remove
  .command('identity <identity>')
  .description('Remove an identity')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (identity: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      await runtime.identityService.delete(identity)
      console.log(JSON.stringify({ ok: true, data: { removed: identity } }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs grant
// ──────────────────────────────────────────────────────────
program
  .command('grant <identity> <bucket>')
  .description('Grant permissions to an identity on a bucket')
  .option('--read', 'Grant read permission')
  .option('--write', 'Grant write permission')
  .option('--delete', 'Grant delete permission')
  .option('--prefix <prefix>', 'Restrict to path prefix (can be used multiple times)', collect, [] as string[])
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (identity: string, bucket: string, opts) => {
    const permissions: Permission[] = []
    if (opts.read) permissions.push('read')
    if (opts.write) permissions.push('write')
    if (opts.delete) permissions.push('delete')

    if (permissions.length === 0) {
      console.error(JSON.stringify({ ok: false, error: { code: 'SFS_INVALID_ARGS', message: 'At least one permission (--read, --write, --delete) must be specified' } }))
      process.exit(1)
    }

    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const id = await runtime.identityService.grant(identity, {
        bucket,
        prefixes: opts.prefix as string[],
        permissions,
      })
      const safeId = { ...id, hashedSecret: '[REDACTED]' }
      console.log(JSON.stringify({ ok: true, data: safeId }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs revoke
// ──────────────────────────────────────────────────────────
program
  .command('revoke <identity> <bucket>')
  .description('Revoke all permissions from an identity on a bucket')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (identity: string, bucket: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const id = await runtime.identityService.revoke(identity, bucket)
      const safeId = { ...id, hashedSecret: '[REDACTED]' }
      console.log(JSON.stringify({ ok: true, data: safeId }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────────────────
function collect(value: string, previous: string[]): string[] {
  return previous.concat([value])
}

function printError(err: unknown): void {
  if (err && typeof err === 'object' && 'toJSON' in err && typeof (err as { toJSON: unknown }).toJSON === 'function') {
    console.error(JSON.stringify((err as { toJSON: () => unknown }).toJSON(), null, 2))
  } else {
    console.error(JSON.stringify({ ok: false, error: { code: 'SFS_ERROR', message: String(err) } }, null, 2))
  }
  process.exit(1)
}

program.parse(process.argv)

